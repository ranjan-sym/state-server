package net.symplifier.lib.state;

import java.util.*;

/**
 * A state provides a mechanism for maintianing state of a system at the server
 * side where the actual operations are performed and synchronize with the
 * clients that are listening to these states. A very good use case would be
 * synchronizing states via AJAX (using server sent events) with the server.
 *
 * <h3>Usage</h3>
 * <p>
 *   Create a state and keep it as long as it is needed on the
 *   server. Use {@link #createList(String, Class)} to create lists that need
 *   to be maintained for different
 *   {@link State.Element} types.
 * </p>
 * <p>
 *   Use {@link #getAll()} to get initial data from the state for sending
 *   entire state over to the client.
 * </p>
 * <p>
 *   Use {@link #addEventListener(EventListener)} to add listeners for listening
 *   to all the {@link State.Element}s that
 *   are linked to this state. The linkage is determined automatically based
 *   on the elements that have been added to the seed list.
 * </p>
 * <p>
 *   Use helper methods {@link #add(String, Element)} and
 *   {@link #remove(String, Element)} to operate on the Seed list or use
 *   {@link #getList(String)} to retrieve the list by its name assigned
 *   during the creation.
 * </p>
 *
 * Created by ranjan on 11/15/15.
 */
public class State implements ElementProxyOwner {
  /* All the ElementTypes that have been registered on the system */
  private static final HashMap<Class<? extends Element>, ElementType> ELEMENT_TYPES = new HashMap<>();

  /**
   * Get the ElementType for the given Java Class. This method tries to find
   * out the {@link ElementType} for the given class, if its not found then
   * a type is created, stored and returned.
   *
   * <b>ThreadSafe</b>
   * @param elementClass The Java Class
   * @return {@link ElementType}
   */
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

  /* The seed elements of this State */
  private final Map<String, ElementList<? extends State.Element>> stateEntityList = new LinkedHashMap<>();
  /* The Event Listeners for this State */
  private final Set<EventListener> eventListeners = new LinkedHashSet<>();

  /**
   * Mechanism to create seed list on the state. Each list on the state is
   * identified by a name. All the dependent elements are added on this list
   *
   * @param name The name of the list
   * @param elementClass The type of the element that this list is supposed to hold
   * @param <T> The specific Element Type
   * @return The newly created list {@link ElementList}
   */
  public <T extends Element> ElementList<T> createList(String name, Class<T> elementClass) {
    // The name should not be reused
    assert(!stateEntityList.containsKey(name));

    // The new list
    ElementList<T> list = new ElementList<>();
    // A proxy for the list is needed, for handling the automated event mechanism
    ElementListProxy proxy = new ElementListProxy(this, getElementType(elementClass));
    list.linkProxy(new ElementListProxy(this, getElementType(elementClass)));
    stateEntityList.put(name, list);

    return list;
  }


  /**
   * Get the entire set of the ElementProxy that this State is linked to.
   *
   * @return A map of {@link Set} of {@link ElementProxy} by {@link ElementType}
   */
  public Map<ElementType, Set<ElementProxy>> getAll() {
    // Create the map
    Map<ElementType, Set<ElementProxy>>res = new HashMap<>();

    // Go through the seed elements in the main lists of the stations
    for(ElementList<? extends State.Element> list:stateEntityList.values()) {
      ElementListProxy proxyList = list.getLinkedProxy();
      ElementType type = proxyList.getType();

      Set<ElementProxy> proxySet = res.get(type);
      // Create a set of Proxy if one is not already there
      if (proxySet == null) {
        proxySet = new HashSet<>();
        res.put(type, proxySet);
      }

      // Go through each and every element and recursively get all the dependent
      // elements. Make sure to check if the proxy has already been found, in which
      // case the recursion must not be done to avoid circular reference and
      // infinite searching
      for(ElementProxy proxy:proxyList.getElements()) {
        if (!proxySet.contains(proxy)) {
          proxySet.add(proxy);
          proxy.getAll(res);
        }
      }
    }
    return res;
  }

  /**
   * The EventListener for listening on any change being made to one of the
   * dependent Element
   */
  public interface EventListener {
    void onChangedEvent(ElementProxy proxy);
  }

  /**
   * Add a EventListener to this state
   *
   * @param listener The listener that is informed of changes
   */
  public void addEventListener(EventListener listener) {
    eventListeners.add(listener);
  }

  /**
   * Remove an existing EventListener
   * @param listener The listener that needs to be removed
   */
  public void removeEventListener(EventListener listener) {
    eventListeners.remove(listener);
  }

  /**
   * The event firing mechanism
   * @param proxy The element that has changed
   */
  void fireEventListeners(ElementProxy proxy) {
    for(EventListener e:eventListeners) {
      e.onChangedEvent(proxy);
    }
  }

  /**
   * Retrieve the name of all the seed list that have been created on this
   * State on the same order as they were created.
   *
   * @return An {@link Iterable} list of names of seed list
   */
  public Iterable<String> getLists() {
    return stateEntityList.keySet();
  }

  /**
   * Retrieve the seed list by the given name
   *
   * @param name The name of the list to be retrieved
   * @return {@link ElementList} or {@code null}
   */
  public ElementList<? extends State.Element> getList(String name) {
    return stateEntityList.get(name);
  }

  /**
   * Adds an element as seed element to the state. This starts a chain reaction
   * and adds all the elements that are dependent on this element to this state.
   * Once an element is added to a state, the state will be informed as soon as
   * any changes are made on the element triggered via
   * {@link Element#sync()}
   * @param name The name of the list on which to add the element
   * @param element The element to be added
   */
  public void add(String name, State.Element element) {
    ElementList list = stateEntityList.get(name);
    assert(list != null);
    list.add(element);
  }

  /**
   * Remove an element from the list of seed elements on this state
   * @param name The name fo the list from which to remove the element
   * @param element The element to be removed
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
