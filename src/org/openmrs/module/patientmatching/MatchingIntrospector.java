/**
 *
 */
package org.openmrs.module.patientmatching;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to filter out non simple property of a class's properties.
 * This class will perform comparison to know whether a property is a simple
 * property or not.
 * 
 * Simple property are defined as:
 *   - primitive type and array of it
 *   - primitive wrapper type and array of it
 *   - String
 * 
 */
public class MatchingIntrospector {
    
    /**
     * Map of primitive wrapper class and the primitive type.
     */
    @SuppressWarnings("unchecked")
    private static final Map<Class, Class> primitiveWrapperTypeMap = new HashMap<Class, Class>();
    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
    }
    
    /**
     * Return whether the input is one of the primitive wrapper type. The
     * mapping between primitive wrapper and the primitive type are defined in
     * the <code>primitiveWrapperTypeMap</code>
     * 
     * @param clazz that will be tested
     * @return true if the input is one of the primitive wrapper
     */
    @SuppressWarnings("unchecked")
    private static final boolean isPrimitiveWrapper(Class clazz) {
        return primitiveWrapperTypeMap.containsKey(clazz);
    }
    
    /**
     * Return whether the input is an array of a primitive type in Java. 
     * 
     * @param clazz that will be tested
     * @return true if the input is an array of a primitive type
     */
    @SuppressWarnings("unchecked")
    private static final boolean isPrimitiveArray(Class clazz) {
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }
    
    /**
     * Return whether the input is an array of primitive type wrapper class in
     * Java. 
     * 
     * @param clazz that will be tested
     * @return true if the input is an array of primitive type wrapper
     */
    @SuppressWarnings("unchecked")
    private static final boolean isPrimitiveWrapperArray(Class clazz) {
        return (clazz.isArray() && isPrimitiveWrapper(clazz));
    }
    
    /**
     * Main method in this utility class to filter out non primitive property of
     * class. The property is a simple property if it's a:
     *   - primitive type or array of it
     *   - primitive type wrapper or array of it
     *   - String or array of it
     *   - Date
     *   
     * @param clazz of the property to be tested
     * @return true if the property is a simple property as mentioned above
     */
    @SuppressWarnings("unchecked")
    public static boolean isSimpleProperty(Class clazz) {
        return clazz.isPrimitive() || isPrimitiveArray(clazz) ||
        isPrimitiveWrapper(clazz) || isPrimitiveWrapperArray(clazz) ||
        clazz.equals(String.class) || clazz.equals(String[].class) ||
        clazz.equals(Class.class) || clazz.equals(Class[].class) ||
        clazz.equals(Date.class);
    }
}
