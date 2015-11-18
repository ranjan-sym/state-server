package net.symplifier.lib.persistence.state.test;

import net.symplifier.lib.persistence.state.ElementList;
import net.symplifier.lib.persistence.state.ElementProxy;
import net.symplifier.lib.persistence.state.State;
import net.symplifier.lib.persistence.state.utils.StateJsoniser;
import org.junit.Test;

/**
 * Created by ranjan on 11/16/15.
 */
public class TestCase implements State.EventListener {
  private final Unit Celcius = new Unit(1, "Celcius");
  private final Unit Percentage = new Unit(2, "Percentage");

  private final Parameter Temperature = new Parameter(1, "Temperature", Celcius);
  private final Parameter Humidity = new Parameter(2, "Humidity", Celcius);

  @Test
  public void start() {

    State state = new State();
    state.addEventListener(this);
    ElementList<Station> stations = state.createList("stations", Station.class);

    Station station = new Station(1, "Kathmandu");
    station.addParameter(Temperature);
    stations.add(station);

    station = new Station(2, "Pokhara");
    station.addParameter(Humidity);
    stations.add(station);
    stations.remove(station);


    state.addEventListener(this);

    StateJsoniser jsoniser = new StateJsoniser(state);
    System.out.println(jsoniser.toJSON().toString(2));
  }

  @Override
  public void onChangedEvent(ElementProxy proxy) {
    System.out.println("EVENT:CHANGED:" + proxy.toString());
  }
}
