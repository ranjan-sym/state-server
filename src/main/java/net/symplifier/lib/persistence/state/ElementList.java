package net.symplifier.lib.persistence.state;

import java.util.*;

/**
 * Created by ranjan on 11/17/15.
 */
public class ElementList<T extends State.Element> implements List<T> {
  private final List<T> elements = new ArrayList<>();

  private ElementListProxy linkedProxy;

  void linkProxy(ElementListProxy proxy) {
    this.linkedProxy = proxy;
  }

  ElementListProxy getLinkedProxy() {
    return linkedProxy;
  }

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
  public boolean containsAll(Collection<?> c) {
    return elements.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) throws UnsupportedOperationException{
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) throws UnsupportedOperationException {
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

  @Override
  public ListIterator<T> listIterator() {
    return elements.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return elements.listIterator(index);
  }

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

  @Override
  public Iterator<T> iterator() {
    return elements.iterator();
  }

  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return elements.toArray(a);
  }

}
