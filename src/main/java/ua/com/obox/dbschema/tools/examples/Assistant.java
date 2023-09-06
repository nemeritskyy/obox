package ua.com.obox.dbschema.tools.examples;

import ua.com.obox.dbschema.tools.Validator;

public class Assistant {
   public static void main(String[] args) {
        String s = "";
        System.out.println(Validator.removeExtraSpaces(s).replaceAll("\"", "\\\""));
    }
}