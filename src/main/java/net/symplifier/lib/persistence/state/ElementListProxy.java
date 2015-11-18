package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * Created by ranjan on 11/16/15.
 */
public class ElementListProxy implements ElementType.ElementProxyOwner {

  private final List<ElementProxy> children = new ArrayList<>();
  private final Class<? extends State.Element> entityClass;
  private final ElementType type;
  private final Set<ElementType.ElementProxyOwner> owner;

  public ElementListProxy(ElementType.ElementProxyOwner owner, ElementType type) {
    this.owner = Collections.singleton(owner);

    this.type = type;
    entityClass = type.getElementClass();
  }

  public Iterable<ElementProxy> getElements() {
    return children;
  }

  public int size() {
    return children.size();
  }

  public ElementType getType() {
    return type;
  }

  public void add(State.Element element) {
    assert(element.getClass() == entityClass);
    children.add(type.createProxy(this, element));
  }

  public ElementProxy set(int index, State.Element element) {
    assert(element.getClass() == entityClass);
    ElementProxy previous = children.get(index);
    if (!previous.getId().equals(element.getId())) {
      // only need to process if the element was actually different
      previous.removeOwner(this);
      ElementProxy proxy = type.createProxy(this, element);
      children.set(index, proxy);
      return previous;
    }

    return previous;
  }

  public void add(int index, State.Element element) {
    assert(element.getClass() == entityClass);
    ElementProxy proxy = type.createProxy(this, element);
    children.add(index, proxy);
  }

  public ElementProxy remove(int index) {
    ElementProxy proxy = children.remove(index);
    proxy.removeOwner(this);
    return proxy;
  }

  public void remove(State.Element element) {
    ElementProxy proxy = type.findProxy(element);
    assert(proxy != null):"Trying to remove an element which was never registered";
    assert(children.contains(proxy)):"Trying to remove an element from a list " +
            "without adding. We are doing reference counting, where this is " +
            "quite sensitive";

    children.remove(proxy);
    proxy.removeOwner(this);
  }

  void clear() {
    Iterator<ElementProxy> it = children.iterator();
    while(it.hasNext()) {
      ElementProxy proxy = it.next();
      proxy.removeOwner(this);
      it.remove();
    }
  }

  /**
   * The update optimization logic works on the assumption that in most of the
   * cases the elements are randomly removed, but the addition are normally done
   * at the end of the list.
   *
   * @param elements The list that needs to be retained
   * @return true if the children was changed due to update
   */
  boolean update(Collection<? extends State.Element> elements) {
    // Find out the difference between the two list, removing and adding to
    // the target (children) to maintain the list to look like the source(elements)
    boolean changed = false;

    Iterator<ElementProxy> it = children.iterator();
    Iterator<? extends State.Element> source = elements.iterator();

    State.Element srcElement = source.hasNext() ? source.next() : null;
    while(it.hasNext()) {
      ElementProxy proxy = it.next();

      // We might either outrun the source or we might have a difference
      if (srcElement == null || !proxy.getId().equals(srcElement.getId())) {
        changed = true;
        proxy.removeOwner(this);
        it.remove();
      } else {
        // matching elements, we move to the next element
        srcElement = source.hasNext() ? source.next() : null;
      }
    }

    while(srcElement != null) {
      changed = true;
      children.add(type.createProxy(this, srcElement));
      srcElement = source.hasNext() ? source.next() : null;
    }

    return changed;
  }
//
//  @Override
//  public boolean equals(Object list) {
//    if (list instanceof List) {
//      List sourceList = (List) list;
//      if (this.children.size() != sourceList.size()) {
//        return false;
//      }
//
//      // Check if each and every entity within the list are the same
//      for(int i=0; i<children.size(); ++i) {
//        ElementProxy src = children.get(i);
//        Object target = sourceList.get(i);
//
//        if (src == null) {
//          if (target != null) {
//            // The target must be of the required type
//            assert(target.getClass() == entityClass);
//            // null and not null are different
//            return false;
//          }
//        } else {
//          if (target == null) {
//            // not null and null are different
//            return false;
//          } else {
//            assert(target.getClass() == entityClass);
//            // Check if the entities are same or not by their ID
//            if (!src.getId().equals( ((State.Element)target).getId())) {
//              return false;
//            }
//          }
//        }
//      }
//
//      // looks like we didn't find any difference
//      return true;
//
//    } else {
//      return false;
//    }
//  }

  @Override
  public Set<ElementType.ElementProxyOwner> getOwners() {
    return owner;
  }
}
