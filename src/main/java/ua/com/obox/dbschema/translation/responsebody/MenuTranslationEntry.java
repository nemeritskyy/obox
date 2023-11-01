package ua.com.obox.dbschema.translation.responsebody;

import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.assistant.ExistName;

@Data
@NoArgsConstructor
public class MenuTranslationEntry extends ExistName {
    public MenuTranslationEntry(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}