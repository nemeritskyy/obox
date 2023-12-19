package ua.com.obox.authserver.mail;

public interface EmailService {
    String sendEmailConfirmation(String sendTo, String confirmToken);
}
