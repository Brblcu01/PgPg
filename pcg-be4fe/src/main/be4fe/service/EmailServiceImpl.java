package be4fe.service;

import common.entity.CeBooking;
import common.entity.CfSystemConfig;
import common.entity.CfUser;
import common.repository.CeBookingRepository;
import common.repository.CfSystemConfigRepository;
import common.repository.CfUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final CfSystemConfigRepository systemConfigRepository;
    private final CeBookingRepository bookingRepository;
    private final CfUserRepository userRepository;

    @Override
    public void inviaNotificaPrenotazioneAnnullata(Long idPrenotazione) {
        try {
            CeBooking prenotazione = bookingRepository.findById(idPrenotazione)
                    .orElse(null);

            if (prenotazione == null) {
                return;
            }

            CfUser utente = userRepository.findById(prenotazione.getIdUtenteFk())
                    .orElse(null);

            if (utente == null || utente.getEmail() == null) {
                return;
            }

            JavaMailSenderImpl mailSender = creaMailSender();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(config("MAIL_FROM"));
            message.setTo(utente.getEmail());
            message.setSubject("Prenotazione annullata");
            message.setText(creaTestoPrenotazioneAnnullata(utente, prenotazione));

            mailSender.send(message);

        } catch (Exception e) {
            log.warn("Errore invio email annullamento prenotazione {}", idPrenotazione, e);
        }
    }

    private JavaMailSenderImpl creaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(config("MAIL_HOST"));
        mailSender.setPort(Integer.parseInt(config("MAIL_PORT")));
        mailSender.setUsername(config("MAIL_USERNAME"));
        mailSender.setPassword(config("MAIL_PASSWORD"));

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", config("MAIL_SMTP_AUTH"));
        properties.put("mail.smtp.starttls.enable", config("MAIL_STARTTLS_ENABLE"));

        return mailSender;
    }

    private String config(String key) {
        return systemConfigRepository.findByCfgKey(key)
                .map(CfSystemConfig::getCfgValue)
                .orElseThrow(() -> new IllegalStateException("Configurazione mancante: " + key));
    }

    private String creaTestoPrenotazioneAnnullata(CfUser utente, CeBooking prenotazione) {
        String nome = utente.getFirstName() != null ? utente.getFirstName() : utente.getUsername();

        return """
                Ciao %s,

                la tua prenotazione del giorno %s e stata annullata da HR.

                Grazie.
                """.formatted(
                nome,
                prenotazione.getDataPrenotazione()
        );
    }
}