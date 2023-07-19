package ua.com.obox.dbschema.tools.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Message {
    NOT_FOUND(" Not found"),
    TENANT_NOT_FOUND(" Tenant not found"),
    RESTAURANT_NOT_FOUND(" Restaurant not found"),
    MENU_NOT_FOUND(" Menu not found"),
    GET_BY_ID(" Get by id"),
    CREATE(" Create"),
    UPDATE(" Update"),
    FIND_COUNT(" Find COUNT="),
    REQUIRED(" Name field is required"),
    LIMIT_200(" Name field must contain from 1 to 200 characters"),
    ERROR(" Error"),
    DELETE(" Delete");

    private final String message;
}
