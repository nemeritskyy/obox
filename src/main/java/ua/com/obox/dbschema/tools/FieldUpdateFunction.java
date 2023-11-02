package ua.com.obox.dbschema.tools;

@FunctionalInterface
public interface FieldUpdateFunction<T> {
    String updateField(T value);
}
