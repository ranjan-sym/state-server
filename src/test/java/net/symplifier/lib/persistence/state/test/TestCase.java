package net.symplifier.lib.persistence.state.test;

import net.symplifier.lib.persistence.state.State;
import org.junit.Test;

/**
 * Created by ranjan on 11/16/15.
 */
public class TestCase {
  @Test
  public void start() {
    State state = new State();
    Station station = new Station();

    state.add(station);




  }
}
