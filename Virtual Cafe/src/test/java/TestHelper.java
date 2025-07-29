import java.lang.reflect.Field;

public class TestHelper {
    
    /**
     * Gets the value of a private static field using reflection.
     * @param clazz The class containing the field
     * @param fieldName The name of the field to get
     * @return The value of the field
     * @throws Exception if the field cannot be accessed
     */
    public static <T> T getStaticField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        T value = (T) field.get(null);
        return value;
    }
    
    /**
     * Sets the value of a private static field using reflection.
     * @param clazz The class containing the field
     * @param fieldName The name of the field to set
     * @param value The value to set
     * @throws Exception if the field cannot be accessed
     */
    public static void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
    
    /**
     * Gets the value of a private instance field using reflection.
     * @param instance The object instance containing the field
     * @param fieldName The name of the field to get
     * @return The value of the field
     * @throws Exception if the field cannot be accessed
     */
    public static <T> T getInstanceField(Object instance, String fieldName) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        T value = (T) field.get(instance);
        return value;
    }
    
    /**
     * Sets the value of a private instance field using reflection.
     * @param instance The object instance containing the field
     * @param fieldName The name of the field to set
     * @param value The value to set
     * @throws Exception if the field cannot be accessed
     */
    public static void setInstanceField(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
