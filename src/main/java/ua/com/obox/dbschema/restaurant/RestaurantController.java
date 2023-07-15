package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService service;
    @GetMapping("/{tenantName}")
    public ResponseEntity<RestaurantResponse> getRestaurantInfo(@PathVariable String tenantName) {
        return ResponseEntity.ok(service.getRestaurantInfo(tenantName));
    }
}
