package ua.com.obox.dbschema.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.confirmation.MenuItemRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepository;

    public List<MenuItemResponse> getAllItem(String tenantName) {
        List<MenuItemResponse> responseList = new ArrayList<>();
        List<MenuItem> itemList = itemRepository.findAllByCategoryMenuRestaurantTenantName(tenantName);

        for (MenuItem item : itemList) {
            MenuItemResponse response = MenuItemResponse.builder()
                    .itemId(item.getItemId())
                    .description(item.getDescription())
                    .name(item.getName())
                    .price(item.getPrice())
                    .categoryId(item.getCategory().getCategoryId())
                    .build();
            responseList.add(response);
        }

        return responseList;
    }
}
