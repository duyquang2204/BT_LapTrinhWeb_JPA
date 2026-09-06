package vn.iotstar.services.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailService {

    public void sendText(
            String recipient,
            String subject,
            String content) throws MessagingException {

        String username = requiredEnvironment("MAIL_USERNAME");
        String appPassword = requiredEnvironment("MAIL_APP_PASSWORD")
                .replace(" ", "");

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException(
                    "Email người nhận không được để trống.");
        }

        InternetAddress to =
                new InternetAddress(recipient.trim(), true);
        to.validate();

        Properties properties = new Properties();

        properties.setProperty(
                "mail.smtp.host", "smtp.gmail.com");
        properties.setProperty(
                "mail.smtp.port", "587");
        properties.setProperty(
                "mail.smtp.auth", "true");

        properties.setProperty(
                "mail.smtp.starttls.enable", "true");
        properties.setProperty(
                "mail.smtp.starttls.required", "true");
        properties.setProperty(
                "mail.smtp.ssl.checkserveridentity", "true");

        properties.setProperty(
                "mail.smtp.connectiontimeout", "10000");
        properties.setProperty(
                "mail.smtp.timeout", "10000");
        properties.setProperty(
                "mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                username, appPassword);
                    }
                });

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(username));
        message.setRecipient(Message.RecipientType.TO, to);

        message.setSubject(
                subject, StandardCharsets.UTF_8.name());

        message.setText(
                content, StandardCharsets.UTF_8.name());

        message.setSentDate(new Date());

        Transport.send(message);
    }

    private String requiredEnvironment(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình biến môi trường " + name);
        }

        return value.trim();
    }
}