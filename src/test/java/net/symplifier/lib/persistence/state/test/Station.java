package net.symplifier.lib.persistence.state.test;

import net.symplifier.lib.persistence.state.ElementList;
import net.symplifier.lib.persistence.state.ElementProxy;
import net.symplifier.lib.persistence.state.ElementType;
import net.symplifier.lib.persistence.state.State;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ranjan on 11/15/15.
 */
public class Station implements State.Element {
  private long id;
  private String name;
  private List<Parameter> parameters = new ArrayList<>();

  public static void register(ElementType type) {
    type.registerLong("id");
    type.registerString("name");
    type.registerList("parameters", Parameter.class);
  }

  public Station(long id, String name) {
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
    proxy.update(parameters);
  }


  public String getName() {
    return name;
  }

  public void addParameter(Parameter parameter) {
    parameters.add(parameter);
  }

  public void removeParameter(Parameter parameter) {
    parameters.remove(parameter);
  }

}
