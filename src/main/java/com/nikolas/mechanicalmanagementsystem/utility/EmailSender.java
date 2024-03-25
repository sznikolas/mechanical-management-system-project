package com.nikolas.mechanicalmanagementsystem.utility;

import com.nikolas.mechanicalmanagementsystem.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;


    private void emailMessage(String subject, String mailContent, JavaMailSender mailSender, User theUser) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);
        String senderName = "Mechanical Management System";
        messageHelper.setFrom(senderEmail, senderName);
        messageHelper.setTo(theUser.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }

    public void sendVerificationEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "New Account Registration";
        String mailContent = "<p> Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "Your new account has been created. Please click the link below to verify your new account.<br>" +
                "<a href=\"" +url+ "\">Verify your email to activate your new account</a><br>"+
                "<br>Thank you! <br>Mechanical Management System Team";
        emailMessage(subject, mailContent, mailSender, user);
    }

    public void sendNewVerificationEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Account Verification Request";
        String mailContent = "<p> Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "Here is the new user activation link. Please click the link below to verify your account.<br>" +
                "<a href=\"" +url+ "\">Verify your email to activate your account</a><br>"+
                "<br>Thank you! <br>Mechanical Management System Team";
        emailMessage(subject, mailContent, mailSender, user);
    }

    public void sendEmailVerificationWasSuccessful(User user) throws MessagingException, UnsupportedEncodingException {
        String subject = "Successful email verification";
        String mailContent = "<p> Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "<p>You successfully activated your account</p>"+
                "at: <strong>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")) + "</strong>" +
                "<br><p>Mechanical Management System Team</p>";
        emailMessage(subject, mailContent, mailSender, user);
    }

    public void sendChangePasswordRequestEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Change Password";
        String mailContent = "<p>Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "You recently requested to change your password. Please click the link below to change your password.<br>" +
                "<a href=\"" +url+ "\">Change password</a>"+
                "<br><p>Mechanical Management System Team</p>";
        emailMessage(subject, mailContent, mailSender, user);
    }

    public void sendForgotPasswordRequestEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Forgot Password";
        String mailContent = "<p> Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "You recently requested the forgot password request. Please click the link below to change your password.<br>" +
                "<a href=\"" +url+ "\">Reset password</a>"+
                "<br><p>Mechanical Management System Team</p>";
        emailMessage(subject, mailContent, mailSender, user);
    }

        public void sendPasswordSuccessfullyChangedEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String subject = "Password Successfully Changed";
        String mailContent = "<p> Hello "+ user.getFirstName()+" "+user.getLastName()+", </p>"+
                "<p>You successfully changed your password!</p>"+
                "at: <strong>" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")) + "</strong>" +
                "<br><p>Mechanical Management System Team</p>";
        emailMessage(subject, mailContent, mailSender, user);
    }

}

