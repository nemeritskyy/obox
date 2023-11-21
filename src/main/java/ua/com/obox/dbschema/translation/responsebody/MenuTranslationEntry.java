package ua.com.obox.dbschema.translation.responsebody;

import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.assistant.OnlyName;

@Data
@NoArgsConstructor
public class MenuTranslationEntry extends OnlyName {
    public MenuTranslationEntry(String name) {
        super(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}