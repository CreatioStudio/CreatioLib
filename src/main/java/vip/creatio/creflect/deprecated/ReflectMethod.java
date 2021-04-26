package vip.creatio.creflect.deprecated;

public class ReflectMethod {

    private final Object mth;

    ReflectMethod(Object mth) {
        this.mth = mth;
    }

    public Object invoke(Object member, Object... args) {
        return ReflectLib.impl.invoke(mth, member, args);
    }

    public Class<?> getReturnType() {
        return ReflectLib.impl.getMethodReturnType(mth);
    }

    public Class<?>[] getParamTypes() {
        return ReflectLib.impl.getMethodParamTypes(mth);
    }

    public int getParamCount() {
        return ReflectLib.impl.getMethodParamCount(mth);
    }
}
