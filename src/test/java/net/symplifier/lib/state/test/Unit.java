package net.symplifier.lib.state.test;

import net.symplifier.lib.state.ElementProxy;
import net.symplifier.lib.state.ElementType;
import net.symplifier.lib.state.State;

/**
 * Created by ranjan on 11/16/15.
 */
public class Unit implements State.Element {
  private long id;
  private String name;

  public static void register(ElementType type) {
    type.registerLong("id");
    type.registerString("name");
  }

  public Unit(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void updateProxy(ElementProxy proxy) {
    proxy.update(id);
    proxy.update(name);
  }

  public String getName() {
    return name;
  }
}
