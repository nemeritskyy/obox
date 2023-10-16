package ua.com.obox.dbschema.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class EmptyDoubleDeserializer extends JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null)
            return null;
        if (value.isEmpty()){
            return (double) 0;
        } else {
            return Double.valueOf(value);
        }
    }
}
