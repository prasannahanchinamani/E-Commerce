package customDesign.E_commerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    public void sendEmail(String from,String to,String subject,String link){
        SimpleMailMessage mailSender=new SimpleMailMessage();
        mailSender.setFrom(from);
        mailSender.setTo(to);
        mailSender.setSubject(subject);
//        mailSender.setText(link);
        mailSender.setText(
                "Hi "  + ",\n\n"
                        + "Click the link below to reset your password:\n"
                        + link + "\n\n"
                        + "This link is valid for 10 minutes and can be used only once.\n"
                        + "If you did not request this, you can ignore this email.\n\n"
                        + "This is an automated no-reply message. Please do not reply.");
        this.mailSender.send(mailSender);

    }

}
