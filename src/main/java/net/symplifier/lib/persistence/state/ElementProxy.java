package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * The ElementProxy keeps track of the changes in the Element. Any element that
 * needs to be relayed over by the State will have a Proxy created.
 *
 * <p>
 * The {@link State} gets notified on the changes made to a relevant Element
 * through the Proxy
 * </p>
 * <p>
 *   Use the {@link ElementProxy#update} methods from within the
 *   {@link net.symplifier.lib.persistence.state.State.Element#updateProxy(ElementProxy)}
 *   method of the Element implementation. The update methods has to be called
 *   in the same sequence as in the register method.
 * </p>
 * Created by ranjan on 11/15/15.
 */
public class ElementProxy implements ElementProxyOwner {

  // All the owners that own this Proxy. We keep a reference count, since the
  // same elementProxy may be owned by the same owner more than once. For example
  // the list might contain the same Element twice or the same Element may be
  // referenced in different context within a single Element (For Example
  // Manager and Supervisor  could be the same instance of Employee)
  private final Map<ElementProxyOwner, Integer> owners = new HashMap<>();

  /* The type of the Element */
  private final ElementType type;
  /* The values of the Element as updated in the proxy */
  private final Object[] values;
  /* The unique id of the element */
  private final Long id;

  /**
   * Transient variables used for data updates via update methods. These two
   * variables - position & changed must be used carefully
   */
  private transient int position;
  private transient boolean changed;


  /**
   * Create a proxy for the given element. The Proxy is created only from
   * {@link ElementType#createProxy(ElementProxyOwner, State.Element)}.
   *
   * @param element The element whose proxy is to be created
   * @param type The type of the element
   * @param fieldsLength The number fo the fields that this element type holds
   */
  ElementProxy(State.Element element, ElementType type, int fieldsLength) {
    this.id = element.getId();
    this.type = type;
    values = new Object[fieldsLength];
    position = 0;
  }


  /**
   * Retrieve the value of the element field as updated on this proxy at the
   * given index position
   * @param idx The index position of the field whose value is to be retrieved
   * @return The value
   */
  public Object getValue(int idx) {
    return values[idx];
  }

  /**
   * Populate the given container with the Proxy of all the Elements that can
   * be reached through this element recursively
   * .
   * @param container The container which needs is populated without repetition
   */
  void getAll(Map<ElementType, Set<ElementProxy>> container) {
    // Go through all the values searching for reference to another element
    // or list of elements
    for(int i=0; i<values.length; ++i) {
      ValueType type = this.type.getValueType(i);
      if (type instanceof ElementType) {
        // If its an element, get its proxy
        ElementProxy proxy = (ElementProxy)values[i];
        // only if the referenced value is not null
        if (proxy != null) {
          // get the element set for the type
          Set<ElementProxy> proxySet = container.get(type);
          // if this is the first time we are adding an element, we also need
          // to create the set and populate it in the container
          if (proxySet == null) {
            proxySet = new HashSet<>();
            container.put((ElementType)type, proxySet);
          }

          // To avoid circular referencing, we check if this has been already
          // added, and add it again and recurse only if this is new
          if (!proxySet.contains(proxy)) {
            proxySet.add(proxy);
            proxy.getAll(container);
          }
        }
      } else if (type instanceof ElementType.ListType) {
        ElementListProxy list = (ElementListProxy)values[i];
        // In case of list, we got through the process, only if there are any
        // elements in the list
        if (list != null && list.size() > 0) {
          ElementType listType = list.getType();
          Set<ElementProxy> proxySet = container.get(listType);
          // if this is the first time we are adding an element, we also need
          // to create the set and populate it in the container
          if (proxySet == null) {
            proxySet = new HashSet<>();
            container.put(listType, proxySet);
          }

          // Go through all elements one by one
          for(ElementProxy proxy: list.getElements()) {
            // Only add and recurse if not already in the list, to avoid circular reference
            if (!proxySet.contains(proxy)) {
              proxySet.add(proxy);
              proxy.getAll(container);
            }
          }
        }
      }
    }
  }

  // The method used for letting the states know that this element has changed
  void syncStates() {
    Set<State> states = new LinkedHashSet<>();
    this.updateStates(states);

    for(State state: states) {
      state.fireEventListeners(this);
    }
  }

  /**
   * Adds an owner to a proxy. A owner is added as soon as this element is
   * added to a list or is referenced from another Element.
   *
   * <p>
   *   The same owner can own the same element more than once in different
   *   context, so we keep a reference counting mechanism and do the
   *   disowning in the same way
   * </p>
   *
   * @param owner The owner who wants to own this element
   */
  void addOwner(ElementProxyOwner owner) {
    synchronized(owners) {
      if (owners.containsKey(owner)) {
        owners.put(owner, owners.get(owner) + 1);
      } else {
        owners.put(owner, 1);
      }
    }
  }

  /**
   * Removes an owner from the proxy. The owner must be removed as soon as
   * it is removed from the underlying list or referencing object
   *
   * @param owner The owner who wants to disown this element
   */
  void removeOwner(ElementProxyOwner owner) {
    synchronized (owners) {
      assert(owners.containsKey(owner)): "Invalid State, Trying to remove owner " +
              "from a non member element. Reference counting is very sensitive, " +
              "we don't want any unbalanced add/remove";

      int count = owners.get(owner) - 1;
      if (count == 0) {
        owners.remove(owner);
      } else {
        owners.put(owner, count);
      }
    }
  }

  /**
   * Retrieve the id of the element
   *
   * @return The id of the element
   */
  public Long getId() {
    return id;
  }



  boolean updateProxy(State.Element element) {
    // Reset the dynamic variables
    this.position = 0;
    this.changed = false;

    // Each element has its own implementation on how the proxy needs to
    // be updated
    element.updateProxy(this);

    return changed;
  }

  // The list of update methods for updating the values

  /**
   * Update the field value with a primitive long
   *
   * @param value The long value
   */
  public void update(long value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive integer
   *
   * @param value The integer value
   */
  public void update(int value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive String
   *
   * @param value The string value
   */
  public void update(String value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive byte
   * @param value The byte value
   */
  public void update(byte value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive short
   *
   * @param value The short value
   */
  public void update(short value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive float
   *
   * @param value The float value
   */
  public void update(float value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive double
   *
   * @param value The double value
   */
  public void update(double value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive boolean
   * @param value The boolean value
   */
  public void update(boolean value) {
    updatePrimitive(value);
  }

  /**
   * Update the field value with a primitive date
   * @param value The data value
   */
  public void update(Date value) {
    updatePrimitive(value);
  }

  // helper method used by all the primitive updates
  private <T> void updatePrimitive(T value) {
    // first handle null
    if (value == null) {
      if (values[position] != null) {
        // The existing value was not null, set that to null
        values[position] = null;
        changed = true;
      }
    } else {
      // Make sure the value is valid type
      assert(type.getValueType(position).isValid(value))
              : "The value provided for " + type.getValueName(position)
              + " doesn't match the registered type for " + type + ". You"
              + " provided " + value.toString() + " of type " + value.getClass();

      // only update the proxy if the value has actually changed
      if (!value.equals(values[position])) {
        values[position] = value;
        changed = true;
      }
    }

    // shift the position to the next value
    position += 1;
  }

  /**
   * Update the field value with an Element list. The proxy doesn't make any
   * change in the given list and uses a List Proxy to store the list values
   *
   * @param list The list of the elements that provides the list as the Element value
   * @param <T> The type of Element
   */
  public <T extends State.Element> void update(List<T> list) {
    // handle null which is also considered to be same as an empty list
    if (list == null || list.size() == 0) {
      if (values[position] != null && ((ElementListProxy)values[position]).size() > 0) {
        // A list is being removed all together, which means remove all the underlying elements
        ((ElementListProxy)values[position]).clear();
        changed = true;
      }
    } else {
      if (values[position] == null) {
        ElementType.ListType type = (ElementType.ListType) this.type.getValueType(position);
        ElementListProxy listProxy = new ElementListProxy(this, type.getElementType());
        values[position] = listProxy;
        if (listProxy.update(list)) {
          changed = true;
        }
      } else {
        ElementListProxy listProxy = (ElementListProxy)values[position];
        if (listProxy.update(list)) {
          changed = true;
        }
      }
    }

    position += 1;
  }

  /**
   * Update the field value with an Element list. The Element list is supposed
   * to work directly on an underlying structure of the List Proxy. This method
   * would link the given list with a Proxy if its not already linked.
   *
   * The ElementList should be able to provide the event whenever the list changes
   * and will not depend on proxy for syncing the changes directly
   *
   * @param list The list of elements
   */
  public void update(ElementList list) {
    assert (list != null);

    // This may be the first update
    if (values[position] == null) {
      Object valueType = type.getValueType(position);
      assert(valueType == type.getListType());
      ElementListProxy listProxy = new ElementListProxy(this,
              ((ElementType.ListType)valueType).getElementType());
      list.linkProxy(listProxy);
      values[position] = listProxy;
      changed = true;
    } else {
      assert (values[position] == list.getLinkedProxy());
    }

    position += 1;
  }

  /**
   * Update the field value with an Element. An element can contain a reference
   * to another element which is updated on the proxy with this method
   *
   * @param element The referenced element
   */
  public void update(State.Element element) {
    if (element == null) {
      if (values[position] != null) {
        // Ownership needs to be removed
        ((ElementProxy)values[position]).removeOwner(this);
        values[position] = null;
        changed = true;
      }
    } else {
      if (values[position] == null) {
        ElementType type = (ElementType)this.type.getValueType(position);
        ElementProxy proxy = type.createProxy(this, element);
        values[position] = proxy;
        changed = true;
      } else {
        ElementProxy proxy = (ElementProxy) values[position];
        if (!proxy.getId().equals(element.getId())) {
          // values changed
          proxy.removeOwner(this);
          ElementType type = (ElementType)this.type.getValueType(position);
          proxy = type.createProxy(this, element);
          changed = true;
        }
      }
    }
  }

  @Override
  public Set<ElementProxyOwner> getOwners() {
    return owners.keySet();
  }

}
