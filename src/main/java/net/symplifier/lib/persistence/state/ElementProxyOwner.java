package net.symplifier.lib.persistence.state;

import net.symplifier.lib.persistence.state.State;

import java.util.Set;

/**
 * Created by ranjan on 11/17/15.
 */
public interface ElementProxyOwner {
  default State isState() {
    return null;
  }

  Set<ElementProxyOwner> getOwners();

  default void updateStates(Set<State> container) {
    for(ElementProxyOwner owner:getOwners()) {
      State state = owner.isState();
      if (state != null) {
        container.add(state);
      } else {
        owner.updateStates(container);
      }
    }
  }


}