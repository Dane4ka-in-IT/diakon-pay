package dev.diakon.diakonpay.service;

import dev.diakon.diakonpay.repository.TarantoolTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SMTPService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final TarantoolTokenRepository tokenRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${diakonpay.mail.reply}")
    private String replyTo;

    private static final long OTP_TTL_SECONDS = 600;

    @Async
    protected void sendMail(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setReplyTo(replyTo);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Не удалось отправить письмо на адрес " + to + ": " + e.getMessage());
        }
    }

    public void sendMessageAboutRegistry(String userName, String email) {
        sendMail(email, "Добро пожаловать в DiakonPay!", "welcome",
                Map.of("userName", userName));
    }

    public int sendMessageAboutChangedEmail(String userName, String oldEmail, String newEmail) {
        int code = new Random().nextInt(9000) + 1000;
        long expiresAt = Instant.now().getEpochSecond() + OTP_TTL_SECONDS;
        tokenRepository.saveOtp(newEmail, code, "email_change", expiresAt);
        sendMail(oldEmail, "Подтверждение смены Email — DiakonPay", "email-change",
                Map.of("userName", userName, "newEmail", newEmail, "code", code));
        return code;
    }

    public int sendMessageAboutRecoveryPass(String userName, String email) {
        int code = new Random().nextInt(9000) + 1000;
        long expiresAt = Instant.now().getEpochSecond() + OTP_TTL_SECONDS;
        tokenRepository.saveOtp(email, code, "password_recovery", expiresAt);
        sendMail(email, "Восстановление пароля — DiakonPay", "password-recovery",
                Map.of("userName", userName, "code", code));
        return code;
    }

    public void sendMonthlyReport(String userName, String email, String month,
                                  double totalSpent, int transactionsCount) {
        sendMail(email, "Ваш отчёт за " + month + " — DiakonPay", "monthly-report",
                Map.of(
                        "userName", userName,
                        "month", month,
                        "totalSpent", totalSpent,
                        "transactionsCount", transactionsCount
                ));
    }
}