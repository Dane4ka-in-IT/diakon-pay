package dev.diakon.diakonpay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SMTPService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${diakonpay.mail.reply}")
    private String replyTo;

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
        String text = "Здравствуйте, " + userName + "!\n\n" +
                "Мы рады приветствовать вас в приложении DiakonPay.\n" +
                "Ваш аккаунт успешно зарегистрирован и готов к работе.\n\n" +
                "Следите за обновлениями на нашем GitHub:\n\n" +
                "https://github.com/Dane4ka-in-IT/diakon-pay\n\n" +
                "\t\t\t\t\t\t\tC уважением, команда diakon-team!";
        sendMail(email, subject, text);
    }

    public int sendMessageAboutChangedEmail(String userName, String oldEmail, String newEmail) {
        int randomNumber = new Random().nextInt(9000) + 1000;
        String subject = "Cмена почты в DiakonPay!";
        String text = "Здравствуйте, " + userName + "!\n\n" +
                "Вы подали запрос на смену почты. Если это были не вы - срочно смените пароль." +
                "Ожидаемая новая почта: " + newEmail + "." +
                "Для подтверждения введите код: " + randomNumber + ".\n\n" +
                "\t\t\t\t\t\t\tC уважением, команда diakon-team!";
        sendMail(oldEmail, subject, text);
        return randomNumber;
    }

    public int sendMessageAboutRecoveryPass(String userName, String email) {
        int randomNumber = new Random().nextInt(9000) + 1000;
        String subject = "Смена пароля в DiakonPay!";
        String text = "Здравствуйте, " + userName + "!\n\n" +
                "Вы подали запрос на смену пароля. Если это были не вы - проигнорируйте это сообщение." +
                "Ваш временный код: " + randomNumber + ".\n\n"  +
                "\t\t\t\t\t\t\tC уважением, команда diakon-team!";
        sendMail(email, subject, text);
        return randomNumber;
    }

    public void sendMonthlyReport(String userName, String email, String month,
                                  double totalSpent, int transactionsCount) {
        String subject = "Ваш отчет в DiakonPay за " + month;

        String text = String.format(
                "Здравствуйте, %s!\n\n" +
                        "Ваш краткий отчет за %s готов:\n" +
                        "— Всего потрачено: %.2f руб.\n" +
                        "— Количество операций: %d\n\n" +
                        "Полную детализацию вы можете увидеть в приложении.\n\n" +
                        "\t\t\t\t\t\t\tС уважением, команда diakon-team!",
                userName, month, totalSpent, transactionsCount
        );

        sendMail(email, subject, text);
    }
}
