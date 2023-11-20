package ua.com.obox.dbschema.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import ua.com.obox.dbschema.allergen.Allergen;
import ua.com.obox.dbschema.allergen.AllergenRepository;
import ua.com.obox.dbschema.mark.Mark;
import ua.com.obox.dbschema.mark.MarkRepository;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.assistant.OnlyName;

import java.time.Instant;
import java.util.*;

public class BasicAllergensAndMarks {

    public static void addBasicMarks(String referenceId, TranslationRepository translationRepository, MarkRepository markRepository) throws JsonProcessingException {
        List<Map<String, OnlyName>> marks = new ArrayList<>();

        List<String> labelsUA = List.of("Вегетаріанський", "Не містить глютену", "Гостре", "Середньо-гострий", "Рекомендовано", "Веганське");
        List<String> labelsUS = List.of("Vegetarian", "Gluten-free", "Spicy", "Medium-spicy", "Recommended", "Vegan");

        for (int i = 0; i < labelsUS.size(); i++) {
            Map<String, OnlyName> labelMap = new HashMap<>();
            labelMap.put("uk-UA", new OnlyName(labelsUA.get(i)));
            labelMap.put("en-US", new OnlyName(labelsUS.get(i)));
            marks.add(labelMap);
        }

        for (Map<String, OnlyName> record : marks) {
            Mark mark = new Mark();
            mark.setReferenceId(referenceId);
            mark.setReferenceType("mark");
            mark.setCreatedAt(Instant.now().getEpochSecond());
            mark.setUpdatedAt(Instant.now().getEpochSecond());
            markRepository.save(mark);

            {
                CreateTranslation<OnlyName> createTranslation = new CreateTranslation<>(translationRepository);
                Translation translation = createTranslation
                        .createBasic(referenceId, mark.getReferenceType(), record);
                mark.setTranslationId(translation.getTranslationId());
                markRepository.save(mark);
            }
        }
    }

    public static void addBasicAllergens(String referenceId, TranslationRepository translationRepository, AllergenRepository allergenRepository) throws JsonProcessingException {
        List<Map<String, OnlyName>> marks = new ArrayList<>();

        List<String> labelsUA = List.of("Крупи, що містять глютен", "Ракоподібні", "Яйця", "Риба", "Арахіс", "Соя", "Молоко", "Горіхи", "Селера", "Гірчиця", "Кунжут", "Двоокис сірки та сульфіти", "Люпин", "Молюски", "Мед");
        List<String> labelsUS = List.of("Cereals containing gluten", "Crustaceans", "Eggs", "Fish", "Peanuts", "Soybeans", "Milk","Nuts", "Celery", "Mustard", "Sesame seeds", "Sulphur dioxide and sulphites", "Lupin", "Molluscs", "Honey");

        for (int i = 0; i < labelsUS.size(); i++) {
            Map<String, OnlyName> labelMap = new HashMap<>();
            labelMap.put("uk-UA", new OnlyName(labelsUA.get(i)));
            labelMap.put("en-US", new OnlyName(labelsUS.get(i)));
            marks.add(labelMap);
        }

        for (Map<String, OnlyName> record : marks) {
            Allergen allergen = new Allergen();
            allergen.setReferenceId(referenceId);
            allergen.setReferenceType("allergen");
            allergen.setCreatedAt(Instant.now().getEpochSecond());
            allergen.setUpdatedAt(Instant.now().getEpochSecond());
            allergenRepository.save(allergen);

            {
                CreateTranslation<OnlyName> createTranslation = new CreateTranslation<>(translationRepository);
                Translation translation = createTranslation
                        .createBasic(referenceId, allergen.getReferenceType(), record);
                allergen.setTranslationId(translation.getTranslationId());
                allergenRepository.save(allergen);
            }
        }
    }
}
