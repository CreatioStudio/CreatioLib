package vip.creatio.creflect.deprecated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectLibVanillaImpl implements ReflectLibInterface {

    @Override
    public @Nullable Object invoke(@NotNull Object mth, @Nullable Object member, Object... params) {
        try {
            ((Method) mth).setAccessible(true);
            return ((Method) mth).invoke(member, params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    @Override
    public Object get(@NotNull Object field, @Nullable Object member) {
        try {
            ((Field) field).setAccessible(true);
            return ((Field) field).get(member);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(@NotNull Object field, @Nullable Object member, @Nullable Object value) {
        try {
            ((Field) field).setAccessible(true);
            ((Field) field).set(member, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Constructor<?>[] getConstructors(Class<?> clazz, boolean publicOnly) {
        return publicOnly ? clazz.getConstructors() : clazz.getDeclaredConstructors();
    }

    @Override
    public @NotNull Field[] getFields(Class<?> clazz, boolean publicOnly) {
        return publicOnly ? clazz.getFields() : clazz.getDeclaredFields();
    }

    @Override
    public @NotNull Method[] getMethods(Class<?> clazz, boolean publicOnly) {
        return publicOnly ? clazz.getMethods() : clazz.getDeclaredMethods();
    }

    @Override
    public @NotNull Object newInstance(@NotNull Object constructor, Object... params) {
        try {
            ((Constructor<?>) constructor).setAccessible(true);
            return ((Constructor<?>) constructor).newInstance(params);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    @Override
    public @NotNull Object getMethod(Method mth) {
        return mth;
    }

    @Override
    public @NotNull Object getMethod(Class<?> clazz, String name, Class<?>[] paramsType) {
        try {
            Method mth = clazz.getDeclaredMethod(name, paramsType);
            mth.setAccessible(true);
            return mth;
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public @NotNull Object getField(Field field) {
        return field;
    }

    @Override
    public @NotNull Object getField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    @Override
    public @NotNull Object getConstructor(Constructor<?> constructor) {
        return constructor;
    }

    @Override
    public @NotNull Object getConstructor(Class<?> clazz, Class<?>[] paramsType) {
        try {
            Constructor<?> c = clazz.getDeclaredConstructor(paramsType);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public @NotNull Class<?> getMethodReturnType(Object mth) {
        return ((Method) mth).getReturnType();
    }

    @Override
    public @NotNull Class<?>[] getMethodParamTypes(Object mth) {
        return ((Method) mth).getParameterTypes();
    }

    @Override
    public int getMethodParamCount(Object mth) {
        return ((Method) mth).getParameterCount();
    }

    @Override
    public @NotNull Class<?> getFieldType(Object field) {
        return ((Field) field).getType();
    }

    @Override
    public @NotNull Class<?> getConstructorClass(Object c) {
        return ((Constructor<?>) c).getDeclaringClass();
    }

    @Override
    public @NotNull Class<?>[] getConstructorParamTypes(Object c) {
        return ((Constructor<?>) c).getParameterTypes();
    }

    @Override
    public int getConstructorParamCount(Object c) {
        return ((Constructor<?>) c).getParameterCount();
    }
}
