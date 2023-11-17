package ua.com.obox.dbschema.tools;

import ua.com.obox.dbschema.allergen.Allergen;
import ua.com.obox.dbschema.allergen.AllergenRepository;
import ua.com.obox.dbschema.attachment.Attachment;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.dbschema.tools.ftp.AttachmentFTP;
import ua.com.obox.dbschema.translation.TranslationRepository;

import java.util.List;

public class PreRemoveAssistant {
    public static void removeByEntityId(String entityId) {
        EntityOrderRepository entityOrderRepository = ApplicationContextProvider.getBean(EntityOrderRepository.class);
        List<EntityOrder> list = entityOrderRepository.findByReferenceId(entityId);
        entityOrderRepository.deleteAll(list);
        EntityOrder existSorted = entityOrderRepository.findBySortedListContaining(entityId).orElse(null);
        if (existSorted != null) {
            String[] elements = existSorted.getSortedList().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(entityId)) {
                    result.append(element).append(",");
                }
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
                existSorted.setSortedList(result.toString());
                entityOrderRepository.save(existSorted);
            } else {
                entityOrderRepository.delete(existSorted);
            }
        }
        TranslationRepository translationRepository = ApplicationContextProvider.getBean(TranslationRepository.class);
        translationRepository.findAllByReferenceId(entityId).ifPresent(translationRepository::delete);
    }

    public static void removeByEntityId(String entityId, String referenceType) {
        if (referenceType.equals("restaurant")) {
            AllergenRepository allergenRepository = ApplicationContextProvider.getBean(AllergenRepository.class);
            List<Allergen> allergens = allergenRepository.findAllByReferenceId(entityId);
            allergenRepository.deleteAll(allergens);
        }
        removeByEntityId(entityId);
    }

    public static void removeAttachmentByEntityId(String entityId) {
        AttachmentRepository attachmentRepository = ApplicationContextProvider.getBean(AttachmentRepository.class);
        List<Attachment> attachments = attachmentRepository.findAllByReferenceId(entityId);
        if (!attachments.isEmpty()) {
            try {
                for (Attachment attachment : attachments) {
                    AttachmentFTP.deleteAttachment(attachment.getAttachmentUrl());
                    attachmentRepository.delete(attachment);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void removeAllergenFromDish(String allergenId) {
        DishRepository dishRepository = ApplicationContextProvider.getBean(DishRepository.class);
        Dish dish = dishRepository.findByAllergensContaining(allergenId).orElse(null);
        if (dish != null) {
            String[] elements = dish.getAllergens().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(allergenId)) {
                    result.append(element).append(",");
                }
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
                dish.setAllergens(result.toString());
            } else {
                dish.setAllergens(null);
            }
            dishRepository.save(dish);
        }
    }

    public static void removeMarkFromDish(String markId) {
        DishRepository dishRepository = ApplicationContextProvider.getBean(DishRepository.class);
        Dish dish = dishRepository.findByMarksContaining(markId).orElse(null);
        if (dish != null) {
            String[] elements = dish.getMarks().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(markId)) {
                    result.append(element).append(",");
                }
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
                dish.setMarks(result.toString());
            } else {
                dish.setMarks(null);
            }
            dishRepository.save(dish);
        }
    }
}
