package net.symplifier.lib.persistence.state.test;

import net.symplifier.lib.persistence.state.EntityProxy;
import net.symplifier.lib.persistence.state.EntityType;
import net.symplifier.lib.persistence.state.State;

/**
 * Created by ranjan on 11/15/15.
 */
public class Parameter implements State.Entity {
  private long id;
  private String name;
  private Unit unit;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void register(EntityType type) {
    type.registerLong("id");
    type.registerString("name");
    type.registerReference("unit", Unit.class);
  }

  public void updateProxy(EntityProxy proxy) {
    proxy.update(id);
    proxy.update(name);
    proxy.update(unit);
  }
}
