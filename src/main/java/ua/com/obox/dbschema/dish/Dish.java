package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.attachment.Attachment;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.EmptyIntegerDeserializer;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.dbschema.tools.ftp.AttachmentFTP;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "dish")
public class Dish {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String dishId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    private String name;
    private Double price;
    private String description;
    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    private Integer cooking_time;
    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    private Integer calories;
    private String weight;
    @Column(columnDefinition = "CHAR(16)")
    private String weight_unit;
    @JsonIgnore
    private String allergens;
    @Transient
    @JsonProperty("allergens")
    private List<String> listAllergens;
    @JsonIgnore
    private String tags;
    @Transient
    @JsonProperty("tags")
    private List<String> listTags;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String in_stock;
    private String associatedId;
    @Transient
    private String category_id;
    private String image;

    public void setListAllergens(List<String> listAllergens) {
        this.listAllergens = listAllergens;
        this.allergens = String.join("::", listAllergens);
    }

    public void setListTags(List<String> listTags) {
        this.listTags = listTags;
        this.tags = String.join("::", listTags);
    }

    @JsonIgnore
    public void setCategoryIdForDish(String category_id) {
        Category category = new Category();
        category.setCategoryId(category_id);
        this.category = category;
    }

    @PreRemove
    public void beforeRemove() {
        AttachmentRepository attachmentRepository = ApplicationContextProvider.getBean(AttachmentRepository.class);
        List<Attachment> attachments = attachmentRepository.findAllByReferenceId(this.dishId);
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

}
