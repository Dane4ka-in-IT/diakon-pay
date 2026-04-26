package dev.diakon.diakonpay.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SMTPService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${diakonpay.mail.reply}")
    private String replyTo;

    @Async
    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setReplyTo(replyTo);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendMessageAboutRegistry(String userName, String email) {
        String subject = "Добро пожаловать в DiakonPay!";
        String text = String.format("""
                Здравствуйте, %s!

                Мы рады приветствовать вас в приложении DiakonPay.
                Ваш аккаунт успешно зарегистрирован и готов к работе.

                Следите за обновлениями на нашем GitHub:
                https://github.com/Dane4ka-in-IT/diakon-pay

                \t\t\t\t\t\t\tC уважением, команда diakon-team!""",
                userName);

        sendMail(email, subject, text);
    }

    public int sendMessageAboutChangedEmail(String userName, String oldEmail, String newEmail) {
        int randomNumber = new Random().nextInt(9000) + 1000;
        String subject = "Cмена почты в DiakonPay!";
        String text = String.format("""
                Здравствуйте, %s!

                Вы подали запрос на смену почты. Если это были не вы - срочно смените пароль.
                Ожидаемая новая почта: %s.
                Для подтверждения введите код: %d.

                \t\t\t\t\t\t\tC уважением, команда diakon-team!""",
                userName, newEmail, randomNumber);

        sendMail(oldEmail, subject, text);
        return randomNumber;
    }

    public int sendMessageAboutRecoveryPass(String userName, String email) {
        int randomNumber = new Random().nextInt(9000) + 1000;
        String subject = "Смена пароля в DiakonPay!";
        String text = String.format("""
                Здравствуйте, %s!

                Вы подали запрос на смену пароля. Если это были не вы - проигнорируйте это сообщение.
                Ваш временный код: %d.

                \t\t\t\t\t\t\tC уважением, команда diakon-team!""",
                userName, randomNumber);

        sendMail(email, subject, text);
        return randomNumber;
    }

    public void sendMonthlyReport(String userName, String email, String month,
                                  double totalSpent, int transactionsCount) {
        String subject = "Ваш отчет в DiakonPay за " + month;
        String text = String.format("""
                Здравствуйте, %s!
                
                Ваш краткий отчет за %s готов:
                - Всего потрачено: %.2f руб.
                - Количество операций: %d
                
                Полную детализацию вы можете увидеть в приложении.
                
                \t\t\t\t\t\t\tС уважением, команда diakon-team!""",
                userName, month, totalSpent, transactionsCount
        );

        sendMail(email, subject, text);
    }
}