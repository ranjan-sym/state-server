package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * The ElementList provides a list that is being used in {@link State} for
 * maintaining list of {@link net.symplifier.lib.persistence.state.State.Element}s
 * or could be used within the {@link net.symplifier.lib.persistence.state.State.Element}
 * implementation for keeping a list of dependent {@link net.symplifier.lib.persistence.state.State.Element}s.
 *
 * Created by ranjan on 11/17/15.
 */
public class ElementList<T extends State.Element> implements List<T> {

  /* The underlying list of elements */
  private final List<T> elements = new ArrayList<>();

  /**
   * The Proxy List that this list is linked to. The Proxy is responsible for
   * doing all the state work
   */
  private ElementListProxy linkedProxy;

  /**
   * Link the Proxy with this list, this method is declared on a Package level
   * and invoked from {@link State} while creating the lists within the States
   * and from {@link ElementProxy} when this list is being updated to the
   * Proxy.
   *
   * @param proxy The ElementListProxy that this list is being linked to
   */
  void linkProxy(ElementListProxy proxy) {
    this.linkedProxy = proxy;
  }

  /**
   * Retrieve the Proxy that this list is linked to. This method is invoked
   * by {@link ElementProxy} during assertion and by {@link State} while
   * generating the oracle.
   *
   * @return {@link ElementListProxy}
   */
  ElementListProxy getLinkedProxy() {
    return linkedProxy;
  }

  /**
   * The type of the Elements that this list stores. Make sure that the List
   * is already linked before this method is invoked.
   *
   * @return {@link ElementType}
   */
  public ElementType getType() {
    return linkedProxy.getType();
  }

  @Override
  public boolean add(T element) {
    if (linkedProxy != null) {
      linkedProxy.add(element);
    }
    return elements.add(element);
  }

  @Override
  public boolean remove(Object o) {
    assert(o instanceof State.Element);
    if (linkedProxy != null) {
      linkedProxy.remove((State.Element)o);
    }

    return elements.remove(o);
  }

  @Override
  public boolean containsAll(@SuppressWarnings("NullableProblems") Collection<?> c) {
    return elements.containsAll(c);
  }

  @Override
  public boolean addAll(@SuppressWarnings("NullableProblems") Collection<? extends T> c) throws UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, @SuppressWarnings("NullableProblems") Collection<? extends T> c) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@SuppressWarnings("NullableProblems") Collection<?> c) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(@SuppressWarnings("NullableProblems") Collection<?> c) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    if (linkedProxy != null) {
      linkedProxy.clear();
    }

    elements.clear();
  }

  @Override
  public T get(int index) {
    return elements.get(index);
  }

  @Override
  public T set(int index, T element) {
    if (linkedProxy != null) {
      linkedProxy.set(index, element);
    }

    return elements.set(index, element);
  }

  @Override
  public void add(int index, T element) {
    if (linkedProxy != null) {
      linkedProxy.add(index, element);
    }

    elements.add(index, element);
  }

  @Override
  public T remove(int index) {
    if (linkedProxy != null) {
      linkedProxy.remove(index);
    }

    return elements.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return elements.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return elements.lastIndexOf(o);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public ListIterator<T> listIterator() {
    return elements.listIterator();
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public ListIterator<T> listIterator(int index) {
    return elements.listIterator(index);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return elements.subList(fromIndex, toIndex);
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return elements.contains(o);
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Iterator<T> iterator() {
    return elements.iterator();
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public <T1> T1[] toArray(T1[] a) {
    //noinspection SuspiciousToArrayCall
    return elements.toArray(a);
  }

}
