package ua.com.obox.dbschema.tools.response;

import java.util.HashMap;

public class ResponseErrorMap<V> extends HashMap<String, V> { // if value is null skip put
    @Override
    public V put(String key, V value) {
        if (value == null) {
            return null;
        }
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return super.get(((String) key).toLowerCase());
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return super.containsKey(((String) key).toLowerCase());
        }
        return false;
    }
}
