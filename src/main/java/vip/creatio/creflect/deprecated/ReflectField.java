package vip.creatio.creflect.deprecated;

public class ReflectField {

    private final Object field;

    ReflectField(Object field) {
        this.field = field;
    }

    public Object get(Object member) {
        return ReflectLib.impl.get(field, member);
    }

    public void set(Object member, Object value) {
        ReflectLib.impl.set(field, member, value);
    }

    public Class<?> getType() {
        return ReflectLib.impl.getFieldType(field);
    }
}
