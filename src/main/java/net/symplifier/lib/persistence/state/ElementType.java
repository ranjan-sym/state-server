package net.symplifier.lib.persistence.state;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * The ElementType stores the structural layout of the
 * {@link net.symplifier.lib.persistence.state.State.Element} that needs to
 * be included in the {@link State}.
 *
 * Created by ranjan on 11/15/15.
 */
public class ElementType implements ValueType {

  /**
   * The ListType provides a place holder for recognizing list type from other
   * type of values
   */
  class ListType implements ValueType {
    /**
     * Retrieve the ElementType for the Elements that are stored in the given
     * List Type
     *
     * @return {@link ElementType}
     */
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
  // TODO It might be needed to use a WeakReferenced collection
  private final Map<Long, ElementProxy> ALL_PROXIES = new HashMap<>();

  /**
   * The name for this ElementType, by default it is same as the name of Class
   * but could be changed with setName method during the registration
   */
  private String name;
  /* The java class for this element type */
  private final Class<? extends State.Element> elementClass;
  /* The underlying structure of the element in pairs, order is maintained */
  private final Map<String, ValueType> types = new LinkedHashMap<>();
  /* For efficient extraction of names and types from the structure the generic
     arrays are used*/
  private final String[] fieldNames;
  private final ValueType[] fieldTypes;

  // A marker type for list representation
  private final ListType listType = new ListType();

  /**
   * Get the underlying structure of Element for sharing with other applications
   *
   * @return A name and ValueType pair of all the fields available on the
   *         Element in the order they are registered by Element implementation
   *         specific register methods
   */
  public Map<String, ValueType> getStructure() {
    return types;
  }

  /**
   * Create an ElementType for the given Element class. The type creation is done
   * during the Element registration in the State (global method)
   *
   * @param elementClass The Java Class of the Element
   */
  ElementType(Class<? extends State.Element> elementClass) {
    this.elementClass = elementClass;

    // there must be a static method on the Element implementation that should register
    // the element, uses Reflection to search for the method and do the registration
    doRegister(elementClass);
    if (name == null) {
      name = elementClass.getSimpleName();
    }

    // Update the more efficient arrays for faster access.
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

  /**
   * Override the name of the Element. By default the Simple name of the class
   * is used. The name should be unique within a State for error free synchronization
   *
   * @param name The name to be used for the Element
   */
  public void setName(String name) {
    assert(this.name == null):"The name can be set only once";
    this.name = name;
  }

  /**
   * The registration worker method, which uses reflection to search for
   * the register static method within the Element implementation and
   * executes it.
   *
   * @param elementClass The java class of Element which needs to be registered
   */
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

  /**
   * Get the ListType for this ElementType
   *
   * @return {@link net.symplifier.lib.persistence.state.ElementType.ListType}
   */
  public ListType getListType() {
    return listType;
  }

  /**
   * Helper method for retrieving the name of the field at the given index.
   * Called from {@link ElementProxy}. Only used for debugging information
   *
   * @param index The ordered position of the field
   * @return String name of the field
   */
  String getValueName(int index) {
    return fieldNames[index];
  }

  /**
   * Helper method for retrieving the type of the field at the given index.
   * Called from {@link ElementProxy}. Used for validation and data transfer.
   *
   * @param index The ordered position of the field
   * @return {@link ValueType} of the field
   */
  ValueType getValueType(int index) {
    return fieldTypes[index];
  }

  /**
   * Get the Element java class for this type
   * @return Java Class for this type
   */
  Class<? extends State.Element> getElementClass() {
    return elementClass;
  }

  /**
   * The ElementType keeps track of all the element proxies that have been created
   * for this ElementType mapped by its ID. This method finds such stored proxies
   * for the given Element
   *
   * @param element The element whose proxy is to be searched
   * @return The {@link ElementProxy} of the element or {@code null} if not found
   */
  ElementProxy findProxy(State.Element element) {
    return ALL_PROXIES.get(element.getId());
  }

  /**
   * Creates a Proxy of the given Element. If a proxy already exists the existing
   * proxy is returned. the Owership is added each time this method is called.
   * DO NOT call {@link ElementProxy#addOwner(ElementProxyOwner)} after this
   * method is invoked.
   *
   * @param owner The owner which is trying to own this element
   * @param element The element of which the proxy needs to be created
   * @return {@link ElementProxy} of the element
   */
  ElementProxy createProxy(ElementProxyOwner owner, State.Element element) {
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

  // Registration methods for defining the structure of the Element

  /**
   * Register a field of type {@link Byte}
   *
   * @param name The name of the field
   */
  public void registerByte(String name) {
    register(name, ValueType.BYTE);
  }

  /**
   * Register a field of type {@link Short}
   *
   * @param name The name of the field
   */
  public void registerShort(String name) {
    register(name, ValueType.SHORT);
  }

  /**
   * Register a field of type {@link Integer}
   * @param name The name of the field
   */
  public void registerInteger(String name) {
    register(name, ValueType.INTEGER);
  }

  /**
   * Register a field of type {@link Long}
   *
   * @param name The name of the field
   */
  public void registerLong(String name) {
    register(name, ValueType.LONG);
  }

  /**
   * Register a field of type {@link String}
   *
   * @param name The name of the field
   */
  public void registerString(String name) {
    register(name, ValueType.STRING);
  }

  /**
   * Register a field of type {@link Boolean}
   * @param name The name of the field
   */
  public void registerBoolean(String name) {
    register(name, ValueType.BOOLEAN);
  }

  /**
   * Register a field of type {@link Float}
   * @param name The name of the field
   */
  public void registerFloat(String name) {
    register(name, ValueType.FLOAT);
  }

  /**
   * Register a field of type {@link Double}
   * @param name The name of the field
   */
  public void registerDouble(String name) {
    register(name, ValueType.DOUBLE);
  }

  /**
   * Register a field of type {@link Date}
   * @param name The name of the field
   */
  public void registerDate(String name) {
    register(name, ValueType.DATE);
  }

  /**
   * Register a field of type reference to another {@link net.symplifier.lib.persistence.state.State.Element}
   *
   * @param name The name of the field
   * @param elementType The java class for the type of the Element to be referenced
   */
  public void registerReference(String name, Class<? extends State.Element> elementType) {
    register(name, State.getElementType(elementType));
  }

  /**
   * Register a field of type list of {@link net.symplifier.lib.persistence.state.State.Element}s
   *
   * @param name The name of the field
   * @param entityType The java class for the type of the Element that the list
   *                   is made up of
   */
  public void registerList(String name, Class<? extends State.Element> entityType) {
    ElementType type = State.getElementType(entityType);
    register(name, type.getListType());
  }

  /* The actual registration type */
  private void register(String name, ValueType type) {
    //TODO Make sure the type is not registered more than once
    types.put(name, type);
  }

}
