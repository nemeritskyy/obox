package ua.com.obox.authserver.confirmation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Confirm {
    @Id
    @GeneratedValue
    public Integer id;
    private String email;
    private String confirmationKey;
}
