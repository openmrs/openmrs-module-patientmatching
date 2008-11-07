/**
 * Auto generated file comment
 */
package org.openmrs.module.patientmatching;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MatchingIntrospector {
    
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
    
    @SuppressWarnings("unchecked")
    public static final boolean isPrimitiveWrapper(Class clazz) {
        return primitiveWrapperTypeMap.containsKey(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static final boolean isPrimitiveArray(Class clazz) {
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }
    
    @SuppressWarnings("unchecked")
    public static final boolean isPrimitiveWrapperArray(Class clazz) {
        return (clazz.isArray() && isPrimitiveWrapper(clazz));
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isSimpleProperty(Class clazz) {
        return clazz.isPrimitive() || isPrimitiveArray(clazz) ||
        isPrimitiveWrapper(clazz) || isPrimitiveWrapperArray(clazz) ||
        clazz.equals(String.class) || clazz.equals(String[].class) ||
        clazz.equals(Class.class) || clazz.equals(Class[].class) ||
        clazz.equals(Date.class);
    }
}
