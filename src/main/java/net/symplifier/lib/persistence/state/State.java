package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * Created by ranjan on 11/15/15.
 */
public class State implements ElementProxyOwner {
  /* All the ElementTypes that have been registered on the system */
  private static final HashMap<Class<? extends Element>, ElementType> ELEMENT_TYPES = new HashMap<>();


  static ElementType getElementType(Class<? extends Element> elementClass) {
    ElementType type;
    synchronized (ELEMENT_TYPES) {
      type = ELEMENT_TYPES.get(elementClass);
      if (type == null) {
        type = new ElementType(elementClass);
        ELEMENT_TYPES.put(elementClass, type);
      }
    }

    return type;
  }

  public Map<ElementType, Set<ElementProxy>> getAll() {
    Map<ElementType, Set<ElementProxy>>res = new HashMap<>();
    for(ElementList<? extends State.Element> list:stateEntityList.values()) {
      ElementListProxy proxyList = list.getLinkedProxy();
      ElementType type = proxyList.getType();

      Set<ElementProxy> proxySet = res.get(type);
      if (proxySet == null) {
        proxySet = new HashSet<>();
        res.put(type, proxySet);
      }

      for(ElementProxy proxy:proxyList.getElements()) {
        if (!proxySet.contains(proxy)) {
          proxySet.add(proxy);
          proxy.getAll(res);
        }
      }
    }
    return res;
  }

  public interface EventListener {
    void onChangedEvent(ElementProxy proxy);
  }

  /* The seed elements of this State */
  private final Map<String, ElementList<? extends State.Element>> stateEntityList = new LinkedHashMap<>();
  private final Set<EventListener> eventListeners = new LinkedHashSet<>();

  public State() {

  }

  public <T extends Element> ElementList<T> createList(String name, Class<T> elementClass) {
    assert(!stateEntityList.containsKey(name));

    ElementList<T> list = new ElementList<>();
    ElementListProxy proxy = new ElementListProxy(this, getElementType(elementClass));
    list.linkProxy(new ElementListProxy(this, getElementType(elementClass)));
    stateEntityList.put(name, list);

    return list;
  }

  public void addEventListener(EventListener listener) {
    eventListeners.add(listener);
  }

  public void removeEventListener(EventListener listener) {
    eventListeners.remove(listener);
  }

  void fireEventListeners(ElementProxy proxy) {
    for(EventListener e:eventListeners) {
      e.onChangedEvent(proxy);
    }
  }

  public Iterable<String> getLists() {
    return stateEntityList.keySet();
  }

  public ElementList<? extends State.Element> getList(String name) {
    return stateEntityList.get(name);
  }
//
//  private ElementListProxy getEntityList(ElementType type) {
//    ElementListProxy list;
//    synchronized (stateEntityList) {
//      list = stateEntityList.get(type);
//      if (list == null) {
//        list = new ElementListProxy(this, type);
//        stateEntityList.put(type, list);
//      }
//    }
//    return list;
//  }

  /**
   * Adds an element as seed element to the state. This starts a chain reaction
   * and adds all teh elements that are dependent on this element to this state.
   * Once an element is added to a state, the state will be informed as soon as
   * any changes are made on the element triggered via
   * {@link State.Element#sync}
   * @param element
   */
  public void add(String name, State.Element element) {
    ElementList list = stateEntityList.get(name);
    assert(list != null);
    list.add(element);
//    // Check if the entity type is already known or not, if not register
//    // the entity type first
//    ElementType type = getElementType(element.getClass());
//
//    // Get the list for this state
//    ElementListProxy list = getEntityList(type);
//
//    list.add(element);
  }

  /**
   * Remove an element from the list of seed elements on this state
   * @param name
   * @param element
   */
  public void remove(String name, Element element) {
    ElementList list = stateEntityList.get(name);
    assert(list != null);
    list.remove(element);
  }

  @Override
  public State isState() {
    return this;
  }

  @Override
  public Set<ElementProxyOwner> getOwners() {
    // A state cannot be owned
    return null;
  }

  /**
   * The Element maintained by the state that needs to be serialized over to
   * the client.
   *
   * <p>
   * All Element implementation are required to provide a static method named
   * register(ElementType type) which should register the name of the
   * different fields that are being provided by this Element using one of the
   * register methods available in the {@link ElementType}
   * </p>
   */
  public interface Element {
    /**
     * An Entity must provide a unique id among the entity
     *
     * @return a unique numeric id
     */
    Long getId();

    /**
     * Update the Proxy with the data from the Entity. Use the
     * {@link ElementProxy#update} methods to update the proxy with the new data.
     * The proxy will check for changes in the data, if the change is found,
     * update the {@link State}
     *
     * @param proxy The {@link ElementProxy} instance that needs to be updated
     *              through this {@link Element}
     */
    void updateProxy(ElementProxy proxy);

    /**
     * Check for changes made on this Element and sync it through all the states
     *
     */
    default void sync() {
      // First find out the proxy for this element
      ElementType type = State.getElementType(getClass());
      ElementProxy proxy = type.findProxy(this);

      // Try to update the proxy with the values from this element
      // and sync all the states if there was any change
      if (proxy.updateProxy(this)) {
        // Inform all the states for the proxy that the proxy has changed
        proxy.syncStates();
      }
    }


  }

}
