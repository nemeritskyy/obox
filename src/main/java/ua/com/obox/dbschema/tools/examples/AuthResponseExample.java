package ua.com.obox.dbschema.tools.examples;

public class AuthResponseExample {
    public static final String CREATE_USER_EXAMPLE = "{ \"name\": \"Obox\", \"email\": \"user@obox.pp.ua\", \"password\": \"oboxboot2*D\", \"language\":\"en-US\" }";
    public static final String CREATE_USER_400 = "{ \"timestamp\": \"2024-01-22T15:29:13.732+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/auth/register\", \"fields\": { \"password\": \"Мінімум вісім символів, принаймні одна велика літера, одна мала літера, одна цифра та один спеціальний символ @$!%*?&\", \"name\": \"Це поле обов'язкове для заповнення\", \"language\": \"Поле language обовязкове у форматі uk-UA\", \"email\": \"Користувач з такою поштою вже існує, увійдіть або відновіть пароль\" } }";
}
