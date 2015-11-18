package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * The ElementProxy keeps track of the changes in the Element
 *
 * Created by ranjan on 11/15/15.
 */
public class ElementProxy implements ElementProxyOwner {

  // All the owners that own this Proxy. We keep a reference count, since the
  // same elementProxy may be owned by the same owner more than once. For example
  // the list might contain the same Element twice or the same Element may be
  // referenced in different context within a single Element (For Example
  // Manager and Supervisor  could be the same instance of Employee)
  private final Map<ElementProxyOwner, Integer> owners = new HashMap<>();

  private final ElementType type;
  private final Object[] values;
  private final Long id;

  private transient int position;
  private transient boolean changed;

  ElementProxy(State.Element element, ElementType type, int fieldsLength) {
    this.id = element.getId();
    this.type = type;
    values = new Object[fieldsLength];
    position = 0;
  }


  public Object getValue(int idx) {
    return values[idx];
  }

  void getAll(Map<ElementType, Set<ElementProxy>> container) {
    for(int i=0; i<values.length; ++i) {
      ValueType type = this.type.getValueType(i);
      if (type instanceof ElementType) {
        ElementProxy proxy = (ElementProxy)values[i];
        if (proxy != null) {
          Set<ElementProxy> proxySet = container.get(type);
          if (proxySet == null) {
            proxySet = new HashSet<>();
            container.put((ElementType)type, proxySet);
          }

          if (!proxySet.contains(proxy)) {
            proxySet.add(proxy);
            proxy.getAll(container);
          }
        }
      } else if (type instanceof ElementType.ListType) {
        ElementListProxy list = (ElementListProxy)values[i];
        if (list != null && list.size() > 0) {
          ElementType listType = list.getType();
          Set<ElementProxy> proxySet = container.get(listType);
          if (proxySet == null) {
            proxySet = new HashSet<>();
            container.put(listType, proxySet);
          }

          for(ElementProxy proxy: list.getElements()) {
            if (!proxySet.contains(proxy)) {
              proxySet.add(proxy);
              proxy.getAll(container);
            }
          }
        }
      }
    }
  }

  void syncStates() {
    Set<State> states = new LinkedHashSet<>();
    this.updateStates(states);

    for(State state: states) {
      state.fireEventListeners(this);
    }
  }

  void addOwner(ElementProxyOwner owner) {
    synchronized(owners) {
      if (owners.containsKey(owner)) {
        owners.put(owner, owners.get(owner) + 1);
      } else {
        owners.put(owner, 1);
      }
    }
  }

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

  public Long getId() {
    return id;
  }

  void updateProxy(State.Element element) {
    this.position = 0;
    this.changed = false;

    element.updateProxy(this);

    if (this.changed) {
      // Let all its owners know that the proxy has changed

    }
  }

  public void update(long value) {
    updatePrimitive(value);
  }

  public void update(int value) {
    updatePrimitive(value);
  }

  public void update(String value) {
    updatePrimitive(value);
  }

  public void update(byte value) {
    updatePrimitive(value);
  }

  public void update(short value) {
    updatePrimitive(value);
  }

  public void update(float value) {
    updatePrimitive(value);
  }

  public void update(double value) {
    updatePrimitive(value);
  }

  public void update(boolean value) {
    updatePrimitive(value);
  }

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
