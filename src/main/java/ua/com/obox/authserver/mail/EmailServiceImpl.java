package ua.com.obox.authserver.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;


@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String sender;
    @Value("${application.url}")
    String siteUrl;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Autowired
    private HttpServletRequest request;

    public String getSiteUrl() {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String siteUrl = scheme + "://" + serverName + ":" + serverPort + contextPath;
        return siteUrl;
    }

    public String sendSimpleMail(String sendTo, String confirmToken) {
        try {

            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(msg, true);
            messageHelper.setTo(sendTo);
            messageHelper.setFrom(sender);
            messageHelper.setSubject("Please confirm your email");
            messageHelper.setText("" +
                    "<html><body>" +
                    "<a href=\"" + siteUrl + "/api/v1/auth/confirm/" + confirmToken + "\">Confirm your email</a>" +
                    "</body></html>", true);
            javaMailSender.send(msg);
            return "Mail Sent Successfully...";
        } catch (Exception e) {
            return "Error while Sending Mail";
        }
    }
}