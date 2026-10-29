package com.oficina.oficinaapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviarToken(String destino, String nome, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(remetente);
        msg.setTo(destino);
        msg.setSubject("Seu código de verificação - Chuk's Car Services");
        msg.setText("Olá, " + nome + "!\n\n"
                + "Seu código de verificação é: " + token + "\n\n"
                + "Ele vale por 10 minutos. Digite-o na página de validação para liberar seu agendamento.\n\n"
                + "Se você não fez esse cadastro, ignore este e-mail.\n\n"
                + "Chuk's Car Services");
        mailSender.send(msg);
    }
}

