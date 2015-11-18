package net.symplifier.lib.persistence.state;

import java.util.Set;

/**
 * A {@link ElementProxy} owner whose list is maintained by the Proxy specifically
 * to determine the {@link State}s to which a Proxy belongs to.
 *
 * <p>This interface has been used by {@link State} since it is the starting
 * pointing which owns the list of all the seed
 * {@link net.symplifier.lib.persistence.state.State.Element}s, {@link ElementListProxy}
 * which maintains the list of {@link net.symplifier.lib.persistence.state.State.Element}
 * and {@link ElementProxy} which can contain reference to other
 * {@link net.symplifier.lib.persistence.state.State.Element}</p>
 *
 * Created by ranjan on 11/17/15.
 */
interface ElementProxyOwner {

  /**
   * Check and find if the Owner is actually a {@link State}.
   *
   * @return {@code null} if the owner is not a state otherwise the state instance
   */
  default State isState() {
    return null;
  }

  /**
   * Retrieve the Set of Owners of this owner for recursively moving upwards
   * in the ownership hierarchy. The {@link State} should return {@code null}.
   *
   * @return The set of owners of this owner. All the owners must maintain this.
   */
  Set<ElementProxyOwner> getOwners();

  /**
   * Find out all the states that this owner can point to using a recursive
   * search through its own owners
   *
   * @param container The set that needs to be updated with the states that have
   *                  been found during the recursive processes.
   */
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