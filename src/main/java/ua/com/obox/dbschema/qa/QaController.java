package ua.com.obox.dbschema.qa;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
public class QaController {
    private final QaService qaService;
    @DeleteMapping("user/email={userEmail}")
    public ResponseEntity<Void> deleteUserByEmail(@PathVariable String userEmail) {
        qaService.deleteUserByEmail(userEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("user/unblock={userIp}")
    public ResponseEntity<Void> unblockByUserIp(@PathVariable String userIp) {
        qaService.unblockByUserIp(userIp);
        return ResponseEntity.noContent().build();
    }
}
