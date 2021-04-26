package vip.creatio.creflect.deprecated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectLib {

    // No default constructor
    private ReflectLib() {}

    static final Class<? extends ReflectLibInterface> implClass = ReflectLibVanillaImpl.class;

    static final ReflectLibInterface impl = ReflectLibInterface.getImpl(implClass);

    public static @NotNull ReflectMethod method(@NotNull Method mth) {
        Object method = impl.getMethod(mth);
        return new ReflectMethod(method);
    }

    public static @NotNull ReflectMethod method(@NotNull Class<?> clazz, @NotNull String name, Class<?>... paramTypes) {
        Object mth = impl.getMethod(clazz, name, paramTypes);
        return new ReflectMethod(mth);
    }

    public static @NotNull ReflectField field(@NotNull Field field) {
        Object f = impl.getField(field);
        return new ReflectField(f);
    }

    public static @NotNull ReflectField field(@NotNull Class<?> clazz, @NotNull String name) {
        Object field = impl.getField(clazz, name);
        return new ReflectField(field);
    }

    public static <T> @NotNull ReflectConstructor<T> constructor(@NotNull Constructor<T> constructor) {
        Object c = impl.getConstructor(constructor);
        return new ReflectConstructor<>(c);
    }

    public static <T> @NotNull ReflectConstructor<T> constructor(@NotNull Class<T> clazz, Class<?>... paramTypes) {
        Object c = impl.getConstructor(clazz, paramTypes);
        return new ReflectConstructor<>(c);
    }

    public static @NotNull Method[] getMethods(@NotNull Class<?> clazz, boolean publicOnly) {
        return impl.getMethods(clazz, publicOnly);
    }

    public static @NotNull Field[] getFields(@NotNull Class<?> clazz, boolean publicOnly) {
        return impl.getFields(clazz, publicOnly);
    }

    public static @NotNull Constructor<?>[] getConstructors(@NotNull Class<?> clazz, boolean publicOnly) {
        return impl.getConstructors(clazz, publicOnly);
    }


    public static Object invoke(@NotNull Class<?> clazz, @NotNull String name, @Nullable Object member, Object... params) {
        Class<?>[] paramsList = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramsList[i] = params[i] == null ? Void.class : params[i].getClass();
        }

        Outer:
        for (Method m : impl.getMethods(clazz, false)) {
            if (m.getParameterCount() == params.length && m.getName().equals(name)) {
                Class<?>[] expected = m.getParameterTypes();
                assert expected != null;
                for (int i = 0; i < params.length; i++) {
                    if (!paramsList[i].isAssignableFrom(ReflectLibInterface.toWrapper(expected[i])))
                        continue Outer;
                }
                m.setAccessible(true);
                return method(m).invoke(member, params);
            }
        }
        throw new NoSuchMethodError("No constructor found with this signature!");
    }

    /** For no-param method */
    public static Object invoke(@NotNull Class<?> clazz, @NotNull String name, @Nullable Object member) {
        return method(clazz, name).invoke(member);
    }

    /** For static no-param method */
    public static Object invoke(@NotNull Class<?> clazz, @NotNull String name) {
        return method(clazz, name).invoke(null);
    }

    public static Object get(@NotNull Class<?> clazz, @NotNull String name, @Nullable Object member) {
        return field(clazz, name).get(member);
    }

    /** For static field */
    public static Object get(@NotNull Class<?> clazz, @NotNull String name) {
        return field(clazz, name).get(null);
    }

    public static void set(@NotNull Class<?> clazz, @NotNull String name, @Nullable Object member, @Nullable Object value) {
        field(clazz, name).set(member, value);
    }


    @SuppressWarnings("unchecked")
    public static <T> @NotNull T newInstance(@NotNull Class<T> clazz, Object... params) {
        Class<?>[] paramsList = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramsList[i] = params[i] == null ? Void.class : params[i].getClass();
        }

        //Find constructor
        Outer:
        for (Constructor<?> c : impl.getConstructors(clazz, false)) {
            if (c.getParameterCount() == params.length) {
                Class<?>[] expected = c.getParameterTypes();
                assert expected != null;
                for (int i = 0; i < params.length; i++) {
                    if (!paramsList[i].isAssignableFrom(ReflectLibInterface.toWrapper(expected[i])))
                        continue Outer;
                }
                c.setAccessible(true);
                return (T) impl.newInstance(c, params);
            }
        }
        throw new NoSuchMethodError("No constructor found with this signature!");
    }

}
