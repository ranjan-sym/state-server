package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * Created by ranjan on 11/15/15.
 */
public class State implements EntityListProxy.Owner {
  private static final HashMap<Class<? extends Entity>, EntityType> ENTITY_TYPES = new HashMap<>();

  static EntityType getEntityType(Entity entity) {
    Class<? extends Entity> entityClass = entity.getClass();
    EntityType type = ENTITY_TYPES.get(entityClass);
    if (type == null) {
      type = new EntityType(entity);
      ENTITY_TYPES.put(entityClass, type);
    }

    return type;
  }

  private final Map<EntityType, EntityListProxy> stateEntityList = new LinkedHashMap<>();

  public State() {

  }

  private EntityListProxy getEntityList(EntityType type) {
    EntityListProxy list = stateEntityList.get(type);
    if (list == null) {
      list = new EntityListProxy(type);
      stateEntityList.put(type, list);
    }
    return list;
  }

  public void add(Entity entity) {
    // Check if the entity type is already known or not, if not register
    // the entity type first
    EntityType type = getEntityType(entity);

    // Get the list for this state
    EntityListProxy list = getEntityList(type);

    list.add(entity);
  }

  @Override
  public void onChanged(EntityListProxy list) {

  }

  /**
   * The Entity maintained by the state that needs to be serialized over to
   * the client
   */
  public interface Entity {
    /**
     * An Entity must provide a unique id among the entity
     *
     * @return a unique numeric id
     */
    Long getId();

    /**
     * Register the structure of the entity. This method is invoked once in the
     * life time of the Entity
     *
     * @param type The EntityType that should be used for registration.
     */
    void register(EntityType type);

    /**
     * Update the Proxy with the data from the Entity. Use the
     * {@link EntityProxy#update} methods to update the proxy with the new data.
     * The proxy will check for changes in the data, if the change is found,
     * update the {@link State}
     *
     * @param proxy The {@link EntityProxy} instance that needs to be updated
     *              through this {@link Entity}
     */
    void updateProxy(EntityProxy proxy);

  }
}
