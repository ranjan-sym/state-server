package net.symplifier.lib.persistence.state.test;

import net.symplifier.lib.persistence.state.EntityListProxy;
import net.symplifier.lib.persistence.state.EntityProxy;
import net.symplifier.lib.persistence.state.EntityType;
import net.symplifier.lib.persistence.state.State;

/**
 * Created by ranjan on 11/15/15.
 */
public class Station implements State.Entity {
  private long id;
  private String name;
  private EntityListProxy<Parameter> parameters;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void register(EntityType type) {
    type.registerLong("id");
    type.registerString("name");
    type.registerList("parameters", Parameter.class);
  }

  @Override
  public void updateProxy(EntityProxy proxy) {
    proxy.update(id);
    proxy.update(name);
    proxy.update(parameters);
  }

}
