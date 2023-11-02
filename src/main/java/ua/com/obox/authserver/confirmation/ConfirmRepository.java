package ua.com.obox.authserver.confirmation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmRepository extends JpaRepository<Confirm, Integer> {
    Optional<Confirm> findByConfirmationKey(String confirmationKey);
}
