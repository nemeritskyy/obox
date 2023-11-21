package ua.com.obox.dbschema.translation.responsebody;

import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.assistant.OnlyName;

@Data
@NoArgsConstructor
public class DishTranslationEntry extends OnlyName {
    String description;

    public DishTranslationEntry(String name, String description) {
        this.description = description;
        super.setName(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
