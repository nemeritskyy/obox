package ua.com.obox.dbschema.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class StringArrayDeserializer extends JsonDeserializer<String[]> {
    @Override
    public String[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode jsonNode = p.getCodec().readTree(p);

        if (jsonNode.isArray()) {
            if (jsonNode.size() > 0) {
                String[] result = new String[jsonNode.size()];
                int i = 0;
                for (JsonNode element : jsonNode) {
                    result[i++] = element.asText();
                }
                return result;
            } else {
                return new String[]{""};
            }
        } else if (jsonNode.isNull()) {
            return null;
        } else {
            return new String[]{jsonNode.asText()};
        }
    }
}