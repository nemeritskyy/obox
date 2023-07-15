package ua.com.obox.dbschema.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.com.obox.dbschema.menu.MenuItemResponse;
import ua.com.obox.dbschema.menu.MenuItemService;

import java.util.List;

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
