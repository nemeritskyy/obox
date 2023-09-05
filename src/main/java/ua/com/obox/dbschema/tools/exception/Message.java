package ua.com.obox.dbschema.tools.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Message {
    NOT_FOUND(" not found"),
    NOT_EMPTY("cannot be empty"),
    CREATE("created"),
    UPDATE("updated"),
    FIND_COUNT("find COUNT="),
    REQUIRED("Field name is required"),
    LIMIT_200("Name field must contain from 1 to 200 characters"),
    LIMIT_255("field must contain from 1 to 255 characters"),
    LIMIT("must be in the range of 1 to"),
    BAD_STATE("The state is incorrect"),
    ERROR("ERROR"),
    DELETE("deleted"),
    LANGUAGE("Bad language code must contain from 2 to 3 characters"),
    FORBIDDEN(" forbidden"),
    BAD_IMAGE_TYPE("Bad type support only jpg, png, heic, svg"),
    MENU_EXISTS("Menu with this name already exists"),
    CATEGORY_EXISTS("Category with this name already exists"),
    DISH_EXISTS("Dish with this name in this category already exists"),
    PRICE_NOT_ZERO("must greater be than 0");

    private final String message;
}
