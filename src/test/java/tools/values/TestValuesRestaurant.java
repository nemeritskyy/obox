package tools.values;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.stream.Stream;

public class TestValuesRestaurant {
    private static Stream<String> getInvalidAddress() {
        return Stream.of(
                RandomStringUtils.random(256, true, true)
        );
    }

    private static Stream<String> getValidAddress() {
        return Stream.of(
                RandomStringUtils.random(255, true, true)
        );
    }
}
