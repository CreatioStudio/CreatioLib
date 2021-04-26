package vip.creatio.creflect.deprecated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectLibHandleImpl implements ReflectLibInterface {

    @Override
    public Object invoke(@NotNull Object mth, @Nullable Object member, Object... params) {
        try {
            // Method handle works bad on this.
            switch (((MethodHandle) mth).type().parameterCount()) {
                case 1:
                    return ((MethodHandle) mth).invoke(member);
                case 2:
                    return ((MethodHandle) mth).invoke(member, params[0]);
                case 3:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1]);
                case 4:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2]);
                case 5:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3]);
                case 6:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4]);
                case 7:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5]);
                case 8:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6]);
                case 9:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7]);
                case 10:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8]);
                case 11:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9]);
                case 12:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10]);
                case 13:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10], params[11]);
                case 14:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10], params[11], params[12]);
                case 15:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10], params[11], params[12],
                            params[13]);
                case 16:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10], params[11], params[12],
                            params[13], params[14]);
                case 17:
                    return ((MethodHandle) mth).invoke(member, params[0], params[1], params[2], params[3], params[4],
                            params[5], params[6], params[7], params[8], params[9], params[10], params[11], params[12],
                            params[13], params[14], params[15]);
                default:
                    throw new RuntimeException();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object get(@NotNull Object field, @Nullable Object member) {
        return ((VarHandle) field).get(member);
    }

    @Override
    public void set(@NotNull Object field, @Nullable Object member, @Nullable Object value) {
        ((VarHandle) field).set(member, value);
    }

    static final MethodHandle privateGetDeclaredConstructors;
    static final MethodHandle privateGetDeclaredMethods;
    static final MethodHandle privateGetDeclaredFields;

    static {
        try {
            Method mth;

            mth = Class.class.getDeclaredMethod("privateGetDeclaredConstructors", boolean.class);
            mth.setAccessible(true);
            privateGetDeclaredConstructors = MethodHandles.lookup().unreflect(mth);

            mth = Class.class.getDeclaredMethod("privateGetDeclaredMethods", boolean.class);
            mth.setAccessible(true);
            privateGetDeclaredMethods = MethodHandles.lookup().unreflect(mth);

            mth = Class.class.getDeclaredMethod("privateGetDeclaredFields", boolean.class);
            mth.setAccessible(true);
            privateGetDeclaredFields = MethodHandles.lookup().unreflect(mth);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Constructor<?>[] getConstructors(Class<?> clazz, boolean publicOnly) {
        try {
            Constructor<?>[] c = (Constructor<?>[]) privateGetDeclaredConstructors.invoke(clazz, publicOnly);
            return Arrays.copyOf(c, c.length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Field[] getFields(Class<?> clazz, boolean publicOnly) {
        try {
            Field[] c = (Field[]) privateGetDeclaredFields.invoke(clazz, publicOnly);
            return Arrays.copyOf(c, c.length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Method[] getMethods(Class<?> clazz, boolean publicOnly) {
        try {
            Method[] c = (Method[]) privateGetDeclaredMethods.invoke(clazz, publicOnly);
            return Arrays.copyOf(c, c.length);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @Override
    public @NotNull Object newInstance(@NotNull Object constructor, Object... params) {
        return invoke(constructor, params);
    }

    @Override
    public @NotNull Object getMethod(Method mth) {
        try {
            return lookup.unreflect(mth);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Object getMethod(Class<?> clazz, String name, Class<?>[] paramsType) {
        try {
            Method mth;
            mth = clazz.getDeclaredMethod(name, paramsType);
            mth.setAccessible(true);
            return getMethod(mth);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public @NotNull Object getField(Field field) {
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Object getField(Class<?> clazz, String name) {
        try {
            Field f;
            f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return getField(f);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    @Override
    public @NotNull Object getConstructor(Constructor<?> constructor) {
        try {
            return lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Object getConstructor(Class<?> clazz, Class<?>[] paramsType) {
        try {
            Constructor<?> c;
            c = clazz.getDeclaredConstructor(paramsType);
            c.setAccessible(true);
            return getConstructor(c);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public @NotNull Class<?> getMethodReturnType(Object mth) {
        return ((MethodHandle) mth).type().returnType();
    }

    @Override
    public @NotNull Class<?>[] getMethodParamTypes(Object mth) {
        return ((MethodHandle) mth).type().parameterArray();
    }

    @Override
    public int getMethodParamCount(Object mth) {
        return ((MethodHandle) mth).type().parameterCount();
    }

    @Override
    public @NotNull Class<?> getFieldType(Object field) {
        return ((VarHandle) field).varType();
    }

    @Override
    public @NotNull Class<?> getConstructorClass(Object c) {
        return getMethodReturnType(c);
    }

    @Override
    public @NotNull Class<?>[] getConstructorParamTypes(Object c) {
        return getMethodParamTypes(c);
    }

    @Override
    public int getConstructorParamCount(Object c) {
        return getMethodParamCount(c);
    }
}
