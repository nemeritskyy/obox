package ua.com.obox.authserver.mail;

public interface EmailService {
    String sendSimpleMail(String sendTo, String confirmToken);
}
