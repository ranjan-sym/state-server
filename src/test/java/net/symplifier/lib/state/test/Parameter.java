package net.symplifier.lib.state.test;

import net.symplifier.lib.state.ElementProxy;
import net.symplifier.lib.state.ElementType;
import net.symplifier.lib.state.State;

/**
 * Created by ranjan on 11/15/15.
 */
public class Parameter implements State.Element {
  private long id;
  private String name;
  private Unit unit;

  public static void register(ElementType type) {
    type.registerLong("id");
    type.registerString("name");
    type.registerReference("unit", Unit.class);
  }

  public Parameter(long id, String name, Unit unit) {
    this.id = id;
    this.name = name;
    this.unit = unit;
  }

  @Override
  public Long getId() {
    return id;
  }

  public void updateProxy(ElementProxy proxy) {
    proxy.update(id);
    proxy.update(name);
    proxy.update(unit);
  }

  public String getName() {
    return name;
  }

  public Unit getUnit() {
    return unit;
  }
}
