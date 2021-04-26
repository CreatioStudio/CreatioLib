package vip.creatio.creflect.deprecated;

public class ReflectConstructor<T> {

    private final Object constructor;

    ReflectConstructor(Object constructor) {
        this.constructor = constructor;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(Object... args) {
        return (T) ReflectLib.impl.newInstance(constructor, args);
    }

    public Class<?> getDeclaringClass() {
        return ReflectLib.impl.getConstructorClass(constructor);
    }

    public Class<?>[] getParamTypes() {
        return ReflectLib.impl.getConstructorParamTypes(constructor);
    }

    public int getParamCount() {
        return ReflectLib.impl.getConstructorParamCount(constructor);
    }
}
