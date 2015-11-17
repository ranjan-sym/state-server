package net.symplifier.lib.persistence.state;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 11/16/15.
 */
public class EntityListProxy {

  public interface Owner {
    void onChanged(EntityListProxy list);
  }

  private final List<EntityProxy> entities = new ArrayList<>();
  private final Class<? extends State.Entity> entityClass;
  private final EntityType type;

  public EntityListProxy(EntityType type) {
    this.type = type;
    entityClass = type.getEntityClass();
  }

  public int size() {
    return entities.size();
  }

  public void clear() {
    // The proxies need to know that a owner has been removed
    Iterator<EntityProxy> iterator = entities.iterator();
    while(iterator.hasNext()) {
      iterator.next().removeOwner(this);
      iterator.remove();
    }
  }

  public void add(State.Entity entity) {
    assert(entity.getClass() == entityClass);

    entities.add(type.createProxy(entity));
  }

  void onRemoved() {
    // The list is being removed, which means we have one less owner to worry
    // about on all of its child proxies
    for(EntityProxy proxy:entities) {
      proxy.removeOwner(this);
    }

  }

  @Override
  public boolean equals(Object list) {
    if (list instanceof List) {
      List sourceList = (List) list;
      if (this.entities.size() != sourceList.size()) {
        return false;
      }

      // Check if each and every entity within the list are the same
      for(int i=0; i<entities.size(); ++i) {
        EntityProxy src = entities.get(i);
        Object target = sourceList.get(i);

        if (src == null) {
          if (target != null) {
            // The target must be of the required type
            assert(target.getClass() == entityClass);
            // null and not null are different
            return false;
          }
        } else {
          if (target == null) {
            // not null and null are different
            return false;
          } else {
            assert(target.getClass() == entityClass);
            // Check if the entities are same or not by their ID
            if (!src.getId().equals( ((State.Entity)target).getId())) {
              return false;
            }
          }
        }
      }

      // looks like we didn't find any difference
      return true;

    } else {
      return false;
    }
  }
}
