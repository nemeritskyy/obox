package ua.com.obox.dbschema.language;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "selected_languages")
public class SelectedLanguages {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "id")
    private String id;
    @JsonProperty("restaurant_id")
    private String restaurantId;

    @Lob
    @JsonIgnore
    private String languagesList;
    @JsonIgnore
    private long createdAt;
    @JsonIgnore
    private long updatedAt;

    @Transient
    @JsonProperty("languages")
    String[] languagesArray;
}
