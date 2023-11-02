package ua.com.obox.dbschema.tools.examples;

public class TranslationResponseExample {
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"translation_id\": \"f45a7da8-0c5f-43ba-8901-13a96a490dc6\", \"reference_id\": \"0ea492a8-4ea4-40b8-a43a-28aaba9b4364\", \"reference_type\": \"restaurant\", \"content\": { \"uk-UA\": { \"name\": \"Ресторан корисної їжі\", \"address\": \"вул. Богдана Хмельницького 77\" }, \"en-US\": { \"name\": \"Healthy food restaurant\", \"address\": null } } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-11-02T01:24:55.258+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Translation for entity with id 99cfe13c-ed54-4d7f9825-0bc21d1460f3 not found\", \"path\": \"/translations/99cfe13c-ed54-4d7f9825-0bc21d1460f3\" }";
}
