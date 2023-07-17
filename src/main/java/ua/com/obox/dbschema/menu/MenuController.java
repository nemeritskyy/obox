package ua.com.obox.dbschema.menu;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.obox.dbschema.menuitem.MenuItemResponse;
import ua.com.obox.dbschema.menuitem.MenuItemService;

import java.util.List;

@Hidden
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuItemService service;

    @GetMapping("/items/{tenantName}")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByTenantName(@PathVariable String tenantName) {
        return ResponseEntity.ok(service.getAllItem(tenantName));
    }
}
