package ua.com.obox.dbschema.associateddata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class RestaurantAssociatedData {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String associatedId;

    private String restaurantId;
    private String languageCode;
    private String allergens;
    private String tags;

    public RestaurantAssociatedData(String languageCode, String restaurantId) {
        this.languageCode = languageCode;
        this.allergens = setDefaultAllergens(languageCode);
        this.tags = setDefaultTags(languageCode);
        this.restaurantId = restaurantId;

    }

    @JsonIgnore
    public String setDefaultAllergens(String languageCode) {
        List<String> allergens = switch (languageCode) {
            case "ua" -> List.of("Крупи, що містять глютен", "Ракоподібні", "Яйця", "Риба", "Арахіс", "Соя", "Молоко",
                    "Горіхи", "Селера", "Гірчиця", "Кунжут", "Двоокис сірки та сульфіти", "Люпин", "Молюски", "Мед");
            case "en" ->
                    List.of("Cereals containing gluten", "Crustaceans", "Eggs", "Fish", "Peanuts", "Soybeans", "Milk",
                            "Nuts", "Celery", "Mustard", "Sesame seeds", "Sulphur dioxide and sulphites", "Lupin", "Molluscs", "Honey");
            case "es" ->
                    List.of("Cereales que contienen gluten", "Crustáceos", "Huevos", "Pescado", "Cacahuetes", "Soja",
                            "Leche", "Frutos secos", "Apio", "Mostaza", "Semillas de sésamo", "Dióxido de azufre y sulfitos", "Altramuces", "Moluscos", "Miel");
            case "fr" -> List.of("Céréales contenant du gluten", "Crustacés", "Œufs", "Poisson", "Arachides", "Soja",
                    "Lait", "Fruits à coque", "Céleri", "Moutarde", "Graines de sésame", "Dioxyde de soufre et sulfites", "Lupin", "Mollusques", "Miel");
            case "de" ->
                    List.of("Glutenhaltige Getreide", "Krebstiere", "Eier", "Fisch", "Erdnüsse", "Sojabohnen", "Milch",
                            "Schalenfrüchte", "Sellerie", "Senf", "Sesamsamen", "Schwefeldioxid und Sulphite", "Lupinen", "Mollusken", "Honig");
            case "it" ->
                    List.of("Cereali contenenti glutine", "Crostacei", "Uova", "Pesce", "Arachidi", "Soia", "Latte",
                            "Frutta a guscio", "Sedano", "Senape", "Semi di sesamo", "Anidride solforosa e solfiti", "Lupini", "Molluschi", "Miele");
            case "pt" -> List.of("Cereais contendo glúten", "Crustáceos", "Ovos", "Peixe", "Amendoins", "Soja", "Leite",
                    "Frutos de casca rija", "Aipo", "Mostarda", "Sementes de sésamo", "Dióxido de enxofre e sulfitos", "Lupinos", "Moluscos", "Mel");
            case "ru" -> List.of("Крупы, содержащие глютен", "Ракообразные", "Яйца", "Рыба", "Арахис", "Соя", "Молоко",
                    "Орехи", "Сельдерей", "Горчица", "Кунжут", "Двуокись серы и сульфиты", "Люпин", "Моллюски", "Мед");
            case "zh", "cn" ->
                    List.of("含麸质的谷类", "甲壳类动物", "鸡蛋", "鱼", "花生", "大豆", "牛奶", "坚果", "芹菜", "芥末", "芝麻籽", "二氧化硫和亚硫酸盐", "蜀葵", "软体动物", "蜜蜂蜜");
            case "ja" ->
                    List.of("グルテン含有穀物", "甲殻類", "卵", "魚", "ピーナッツ", "大豆", "乳", "ナッツ", "セロリ", "マスタード", "ゴマの種", "二酸化硫黄と亜硫酸塩", "ルピン", "貝類", "蜂蜜");
            default -> Collections.emptyList();
        };
        return allergens.stream().sorted()
                .collect(Collectors.joining("::"));
    }


    @JsonIgnore
    public String setDefaultTags(String languageCode) {
        List<String> tags = switch (languageCode) {
            case "ua" ->
                    List.of("Вегетаріанський", "Не містить глютену", "Гостре", "Середньо-гострий", "Рекомендовано", "Веганське");
            case "en" -> List.of("Vegetarian", "Gluten-free", "Spicy", "Medium-spicy", "Recommended", "Vegan");
            case "es" -> List.of("Vegetariano", "Sin gluten", "Picante", "Medio picante", "Recomendado", "Vegano");
            case "fr" -> List.of("Végétarien", "Sans gluten", "Épicé", "Moyennement épicé", "Recommandé", "Végétalien");
            case "de" -> List.of("Vegetarisch", "Glutenfrei", "Scharf", "Mittel-scharf", "Empfohlen", "Vegan");
            case "it" -> List.of("Vegetariano", "Senza glutine", "Piccante", "Medio piccante", "Consigliato", "Vegano");
            case "pt" -> List.of("Vegetariano", "Sem glúten", "Picante", "Meio picante", "Recomendado", "Vegano");
            case "ru" ->
                    List.of("Вегетарианский", "Без глютена", "Острый", "Средне-острый", "Рекомендуемое", "Веганское");
            case "zh", "cn" -> List.of("素食", "无麸质", "辣", "中辣", "推荐", "纯素");
            case "ja" -> List.of("ベジタリアン", "グルテンフリー", "辛い", "中辛い", "おすすめ", "ビーガン");
            default -> Collections.emptyList();
        };
        return tags.stream().sorted()
                .collect(Collectors.joining("::"));
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getAllergens() {
        return allergens;
    }
}
