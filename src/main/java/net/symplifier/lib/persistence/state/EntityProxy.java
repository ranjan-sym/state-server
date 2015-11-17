package net.symplifier.lib.persistence.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ranjan on 11/15/15.
 */
public class EntityProxy<T extends State.Entity> {
  private final Set<EntityListProxy> owners = new HashSet<>();
  private final Set<State> states = new HashSet<>();

  private final EntityType type;
  private final Object[] values;
  private final Long id;

  private transient int position;
  private transient boolean changed;

  EntityProxy(State.Entity entity, EntityType type, int fieldsLength) {
    this.id = entity.getId();
    this.type = type;
    values = new Object[fieldsLength];
    position = 0;
  }

  Long getId() {
    return id;
  }

  void removeOwner(EntityListProxy owner) {
    assert(owners.contains(owner)):"An unwanted state, if the owner is being removed, it must have been added.";
    owners.remove(owner);
  }

  void updateProxy(State.Entity entity) {
    this.position = 0;
    this.changed = false;

    entity.updateProxy(this);

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
      assert(type.getValueClass(position).equals(value.getClass()))
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

  public <T extends State.Entity> void update(List<T> list) {
    // handle null which is also considered to be same as an empty list
    if (list == null || list.size() == 0) {
      if (values[position] != null && ((EntityListProxy)values[position]).size() > 0) {
        // A list is being removed all together, which means remove all the underlying elements
        ((EntityListProxy)values[position]).clear();
        changed = true;
      }
    } else {
      if (values[position] == null) {
        State.Entity sample = list.get(0);
        assert(sample != null):"Do not allow null values in the list";
        EntityType type = State.getEntityType(sample);
        values[position] = new EntityListProxy(type);
        changed = true;
      } else if (!((EntityListProxy)values[position]).equals(list)) {
        ((EntityListProxy)values[position]).clear();
        for(T o:list) {
          ((EntityListProxy)values[position]).add(o);
        }
        changed = true;
      }
    }

    position += 1;
  }

  public void update(EntityListProxy list) {
    assert (list != null);

    // This may be the first update
    if (values[position] == null) {
      values[position] = list;
      changed = true;
    } else {
      assert (values[position] == list);
    }

    position += 1;
  }

  public void update(State.Entity entity) {
    if (entity == null) {
      if (values[position] != null) {
        values[position] = null;
        changed = true;
      }
    } else {
      if (values[position] == null) {
        EntityType type = State.getEntityType(entity);
        values[position] = type.createProxy(entity);
        changed = true;
      } else {
        EntityProxy proxy = (EntityProxy) values[position];
        if (proxy.getId() != entity.getId()) {
          // values changed
          EntityType type = State.getEntityType(entity);
          values[position] = type.createProxy(entity);
          changed = true;
        }
      }
    }
  }
}
