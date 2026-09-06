package vn.iotstar.configs;

import jakarta.mail.MessagingException;

import vn.iotstar.services.impl.MailService;

public class TestMail {

    public static void main(String[] args) {
        String recipient = System.getenv("MAIL_TEST_TO");

        if (recipient == null || recipient.isBlank()) {
            System.err.println(
                    "Chưa cấu hình MAIL_TEST_TO.");
            return;
        }

        MailService mailService = new MailService();

        try {
            mailService.sendText(
                    recipient,
                    "Kiểm tra gửi email từ project JPA",
                    "Đây là email kiểm tra cấu hình Gmail.\n"
                            + "Email này chưa phải mã OTP.\n"
                            + "Nếu nhận được thư, phần gửi email "
                            + "đã hoạt động.");

            System.out.println(
                    "Máy chủ SMTP đã chấp nhận gửi thư. "
                            + "Hãy kiểm tra hộp thư người nhận.");
        } catch (MessagingException | RuntimeException e) {
            System.err.println("Gửi email thất bại.");
            e.printStackTrace();
        }
    }
}