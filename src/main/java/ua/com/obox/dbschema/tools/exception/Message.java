package ua.com.obox.dbschema.tools.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Message {
    NOT_FOUND(" not found"),
    TENANT_NOT_FOUND(" tenant not found"),
    RESTAURANT_NOT_FOUND(" restaurant not found"),
    MENU_NOT_FOUND(" Menu not found"),
    GET_BY_ID(" Get by id"),
    CREATE(" created"),
    UPDATE(" updated"),
    FIND_COUNT(" find COUNT="),
    REQUIRED(" Name field is required"),
    LIMIT_200(" Name field must contain from 1 to 200 characters"),
    ERROR(" Error"),
    DELETE(" deleted");

    private final String message;
}
