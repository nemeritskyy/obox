package ua.com.obox.dbschema.tools.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Message {
    NOT_FOUND(" not found"),
    FIND_COUNT(" find COUNT=");

    private final String message;
}
