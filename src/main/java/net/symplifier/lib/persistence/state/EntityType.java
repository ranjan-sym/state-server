package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * Created by ranjan on 11/15/15.
 */
public class EntityType {

  private static final Map<Long, EntityProxy> ALL_PROXIES = new HashMap<>();

  private final Class<? extends State.Entity> entityClass;
  private final Map<String, Object> types = new LinkedHashMap<>();
  private final String[] fieldNames;
  private final Object[] fieldTypes;


  public EntityType(State.Entity entity) {
    this.entityClass = (Class<T>)entity.getClass();

    entity.register(this);

    fieldNames = new String[types.size()];
    fieldTypes = new String[types.size()];

    int i=0;
    for(Map.Entry<String, Object> entry:types.entrySet()) {
      fieldNames[i] = entry.getKey();
      fieldTypes[i] = entry.getValue();

      i += 1;
    }

  }

  String getValueName(int index) {
    return fieldNames[index];
  }

  Object getValueClass(int index) {
    return fieldTypes[index];
  }

  Class<? extends State.Entity> getEntityClass() {
    return entityClass;
  }

  public EntityProxy createProxy(State.Entity entity) {
    assert(entity.getClass() == entityClass);

    EntityProxy proxy = ALL_PROXIES.get(entity.getId());
    if (proxy == null) {
      proxy = new EntityProxy(entity, this, fieldNames.length);
      ALL_PROXIES.put(entity.getId(), proxy);
    }

    return proxy;
  }

  public void registerLong(String name) {
    register(name, Long.class);
  }

  public void registerString(String name) {
    register(name, String.class);
  }

  public void registerInteger(String name) {
    register(name, Integer.class);
  }

  public void registerReference(String name, Class<? extends State.Entity> entityType) {
    register(name, entityType);
  }

  public void registerList(String name, Class<? extends State.Entity> entityType) {
    register(name, new EntityListProxy(entityType));
  }

  private void register(String name, Object type) {
    // Make sure the type is not registered more than once
    types.put(name, type);
  }

}
