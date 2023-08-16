package ua.com.obox.dbschema.tools.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Message {
    NOT_FOUND(" not found"),
    NOT_EMPTY(" cannot be empty"),
    TENANT_NOT_FOUND(" tenant not found"),
    RESTAURANT_NOT_FOUND(" restaurant not found"),
    MENU_NOT_FOUND(" Menu not found"),
    CATEGORY_NOT_FOUND(" Category not found"),
    GET_BY_ID(" Get by id"),
    CREATE(" created"),
    UPDATE(" updated"),
    FIND_COUNT("find COUNT="),
    REQUIRED("Field name is required"),
    LIMIT_200("Name field must contain from 1 to 200 characters"),
    LIMIT_255(" field must contain from 1 to 255 characters"),
    BOOLEAN(" must be false or true"),
    CHECK_PRICE(" the price should be in the range of 0 to 100000"),
    LIMIT(" must be in the range of 0 to "),
    HIDDEN(" is hidden"),
    BAD_UUID(" uuid is incorrect"),
    BAD_STATE("The state is incorrect"),
    ERROR(" Error"),
    DELETE(" deleted"),
    LANGUAGE("Bad language code must contain from 2 to 3 characters"),
    FORBIDDEN(" forbidden");

    private final String message;
}
