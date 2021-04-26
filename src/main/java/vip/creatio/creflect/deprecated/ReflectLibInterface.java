package vip.creatio.creflect.deprecated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface ReflectLibInterface {

    /** Get reflection lib instance, access to all methods in this interface */
    @SuppressWarnings("unchecked")
    static ReflectLibInterface getImpl(Class<?> implementation) {
        try {
            Constructor<ReflectLibInterface> c =
                    (Constructor<ReflectLibInterface>)
                    implementation.getDeclaredConstructor();
            return c.newInstance();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    Object invoke(@NotNull Object mth, @Nullable Object member, Object... params);

    Object get(@NotNull Object field, @Nullable Object member);
    
    void set(@NotNull Object field, @Nullable Object member, @Nullable Object value);

    @NotNull Object newInstance(@NotNull Object constructor, Object... params);


    
    @NotNull Constructor<?>[] getConstructors(Class<?> clazz, boolean publicOnly);

    @NotNull Field[] getFields(Class<?> clazz, boolean publicOnly);

    @NotNull Method[] getMethods(Class<?> clazz, boolean publicOnly);




    @NotNull Object getMethod(Method mth);

    @NotNull Object getMethod(Class<?> clazz, String name, Class<?>[] paramsType);

    @NotNull Object getField(Field field);

    @NotNull Object getField(Class<?> clazz, String name);

    @NotNull Object getConstructor(Constructor<?> constructor);

    @NotNull Object getConstructor(Class<?> clazz, Class<?>[] paramsType);


    @NotNull Class<?> getMethodReturnType(Object mth);

    @NotNull Class<?>[] getMethodParamTypes(Object mth);

    int getMethodParamCount(Object mth);

    @NotNull Class<?> getFieldType(Object field);

    @NotNull Class<?> getConstructorClass(Object c);

    @NotNull Class<?>[] getConstructorParamTypes(Object c);

    int getConstructorParamCount(Object c);

    static Class<?> toWrapper(Class<?> primitive) {
        switch (primitive.getName()) {
            case "int":
                return Integer.class;
            case "short":
                return Short.class;
            case "byte":
                return Byte.class;
            case "char":
                return Character.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "boolean":
                return Boolean.class;
            default:
                return primitive;
        }
    }

    static Class<?> toPrimitive(Class<?> wrapper) {
        switch (wrapper.getSimpleName()) {
            case "Integer":
                return int.class;
            case "Short":
                return short.class;
            case "Byte":
                return byte.class;
            case "Character":
                return char.class;
            case "Long":
                return long.class;
            case "Float":
                return float.class;
            case "Double":
                return double.class;
            case "Boolean":
                return boolean.class;
            default:
                return wrapper;
        }
    }
    
}
