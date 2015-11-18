package net.symplifier.lib.persistence.state;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by ranjan on 11/15/15.
 */
public class ElementType implements ValueType {


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

  class ListType implements ValueType {
    public ElementType getElementType() {
      return ElementType.this;
    }

    @Override
    public String getName() {
      return ElementType.this.getName();
    }

    @Override
    public boolean isValid(Object value) {
      return value instanceof ElementListProxy
              && ((ElementListProxy) value).getType() == getElementType();
    }

    @Override
    public boolean isList() {
      return true;
    }
  }

  /* All the proxies that have been created and active on the system */
  private final Map<Long, ElementProxy> ALL_PROXIES = new HashMap<>();

  private String name;
  private final Class<? extends State.Element> elementClass;
  private final Map<String, ValueType> types = new LinkedHashMap<>();
  private final String[] fieldNames;
  private final ValueType[] fieldTypes;

  public Map<String, ValueType> getStructure() {
    return types;
  }


  // A marker type for list representation
  private final ListType listType = new ListType();

  public ElementType(Class<? extends State.Element> elementClass) {
    this.elementClass = elementClass;

    // there must be a static method on the ElementClass that should register
    // the element, use Reflection to do that register
    doRegister(elementClass);
    if (name == null) {
      name = elementClass.getSimpleName();
    }


    fieldNames = new String[types.size()];
    fieldTypes = new ValueType[types.size()];

    int i=0;
    for(Map.Entry<String, ValueType> entry:types.entrySet()) {
      fieldNames[i] = entry.getKey();
      fieldTypes[i] = entry.getValue();

      i += 1;
    }
  }

  @Override
  public boolean isElement() {
    return true;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isValid(Object value) {
    return value.getClass() == this.getElementClass();
  }

  public void setName(String name) {
    assert(this.name == null):"The name can be set only once";
    this.name = name;
  }

  private void doRegister(Class<? extends State.Element> elementClass) {
    Method method = null;
    try {
      method = elementClass.getMethod("register", ElementType.class);
    } catch(NoSuchMethodException e) {
      e.printStackTrace();
    }

    assert(method != null):"The Element classes that need to work with the State " +
            "must provide a static method register(ElementType) for Element " +
            "registration";

    assert(Modifier.isStatic(method.getModifiers())):"The register(ElementType) " +
            "method defined on the " + elementClass + " must be static";
    assert(Modifier.isPublic(method.getModifiers())):"The register(ElementType) " +
            "method defined on the " + elementClass + " must be public";

    try {
      method.invoke(null, this);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }


  }

  public ListType getListType() {
    return listType;
  }

  String getValueName(int index) {
    return fieldNames[index];
  }

  ValueType getValueType(int index) {
    return fieldTypes[index];
  }

  Class<? extends State.Element> getElementClass() {
    return elementClass;
  }

  ElementProxy findProxy(State.Element element) {
    return ALL_PROXIES.get(element.getId());
  }

  public ElementProxy createProxy(ElementProxyOwner owner, State.Element element) {
    assert(element.getClass() == elementClass);

    ElementProxy proxy = ALL_PROXIES.get(element.getId());
    if (proxy == null) {
      proxy = new ElementProxy(element, this, fieldNames.length);
      // When the proxy is created for the first time, update it
      element.updateProxy(proxy);
      ALL_PROXIES.put(element.getId(), proxy);
    }

    proxy.addOwner(owner);

    return proxy;
  }

  public void registerByte(String name) {
    register(name, ValueType.BYTE);
  }

  public void registerShort(String name) {
    register(name, ValueType.SHORT);
  }

  public void registerInteger(String name) {
    register(name, ValueType.INTEGER);
  }

  public void registerLong(String name) {
    register(name, ValueType.LONG);
  }



  public void registerString(String name) {
    register(name, ValueType.STRING);
  }

  public void registerBoolean(String name) {
    register(name, ValueType.BOOLEAN);
  }

  public void registerFloat(String name) {
    register(name, ValueType.FLOAT);
  }

  public void registerDouble(String name) {
    register(name, ValueType.DOUBLE);
  }

  public void registerDate(String name) {
    register(name, ValueType.DATE);
  }

  public void registerReference(String name, Class<? extends State.Element> elementType) {
    register(name, State.getElementType(elementType));
  }

  public void registerList(String name, Class<? extends State.Element> entityType) {
    ElementType type = State.getElementType(entityType);
    register(name, type.getListType());
  }

  private void register(String name, ValueType type) {
    // Make sure the type is not registered more than once
    types.put(name, type);
  }

}
