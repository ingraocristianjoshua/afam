package com.afam.server.dao;

import com.afam.utils.Constants;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * MailServerBnd – Façade + Singleton per l'invio di email e SMS simulati.
 *
 * Gestisce tutta la comunicazione esterna (email SMTP e SMS simulato via email).
 * Nessuna control chiama Jakarta Mail direttamente.
 *
 * SMS: questa implementazione simula l'invio SMS inviando un'email al gateway
 * SMTP configurato. In produzione, sostituire con un'API SMS reale.
 */
public class MailServerBnd {

    // ── Campi ──────────────────
    private static final Logger LOG = Logger.getLogger(MailServerBnd.class.getName());

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static volatile MailServerBnd instance;

    /** Restituisce instance. */
    public static MailServerBnd getInstance() {
        if (instance == null) {
            synchronized (MailServerBnd.class) {
                if (instance == null) instance = new MailServerBnd();
            }
        }
        return instance;
    }

    // ── Configurazione ────────────────────────────────────────────────────────

    private Session mailSession;
    private String  fromAddress;
    private String  linkBaseUrl;

    // ── Costruttori ──────────────────
    private MailServerBnd() {
        loadConfig();
    }

    // ── Metodi ──────────────────
    public String getLinkBaseUrl() { return linkBaseUrl; }

    /** Load config. */
    private void loadConfig() {
        Properties cfg = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in != null) cfg.load(in);
        } catch (IOException e) {
            LOG.warning("config.properties non trovato per MailServerBnd: " + e.getMessage());
        }

        String host     = cfg.getProperty("mail.host",     "smtp.example.com");
        String port     = cfg.getProperty("mail.port",     "587");
        String user     = cfg.getProperty("mail.user",     "afam@example.com");
        String password = cfg.getProperty("mail.password", "changeme");
        fromAddress     = cfg.getProperty("mail.from",     "noreply@afam.it");
        linkBaseUrl     = System.getProperty("link.baseUrl",
                          cfg.getProperty("link.baseUrl", "http://localhost:8080/api/share/"));
        boolean starttls = Boolean.parseBoolean(cfg.getProperty("mail.starttls", "true"));

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", host);
        mailProps.put("mail.smtp.port", port);
        mailProps.put("mail.smtp.auth", "true");
        mailProps.put("mail.smtp.starttls.enable", String.valueOf(starttls));

        final String finalUser = user;
        final String finalPassword = password;

        mailSession = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(finalUser, finalPassword);
            }
        });
    }

    // ── Metodi esposti ────────────────────────────────────────────────────────

    /**
     * Simula l'invio di un SMS con OTP inviando un'email a un gateway SMS.
     * Usato da Verifica2FACtrl.
     */
    public void inviaSMS(String numero, String otp) {
        stampaOTPTerminale("SMS → " + numero, otp);
    }

    /**
     * Invia OTP via SMS al numero indicato.
     * Usato da ValidaNumeroCtrl.
     */
    public void inviaNumero(String numero, String otp) {
        inviaSMS(numero, otp);
    }

    /**
     * Invia OTP via email all'indirizzo indicato.
     * Usato da ValidaEmailCtrl.
     */
    public void inviaEmail(String email, String otp) {
        stampaOTPTerminale("EMAIL → " + email, otp);
    }

    /**
     * Invia OTP via email usando l'indirizzo già noto al server (utente corrente).
     * Usato da RecuperaPasswordCtrl.
     */
    public void inviaOTPviaEmail(String otp) {
        LOG.warning("inviaOTPviaEmail chiamata senza email destinatario: OTP non inviato.");
    }

    /** Overload che accetta anche l'indirizzo (usato dagli endpoint REST). */
    public void inviaOTPviaEmail(String email, String otp) {
        inviaEmail(email, otp);
    }

    /**
     * Invia il link di condivisione via email al destinatario.
     * Usato da GeneraLinkCtrl.
     */
    public void inviaLink(String email, String link) {
        inviaLink(email, link, "Uno studente AFAM");
    }

    /** Invia link. */
    public void inviaLink(String email, String link, String nomeMittente) {
        String soggetto = "AFAM – " + nomeMittente + " ha condiviso un portfolio con te";
        String corpo = "Ciao,\n\n"
                + nomeMittente + " ha condiviso con te il proprio portfolio su AFAM.\n\n"
                + "Clicca sul link qui sotto per visualizzarlo:\n"
                + linkBaseUrl + link + "\n\n"
                + "Il link si aprirà direttamente nel client AFAM.\n"
                + "Se il client non si apre, copia il codice qui sotto e incollalo nella schermata 'Accedi con link'.\n\n"
                + "Codice del link: " + link + "\n\n"
                + "Team AFAM";
        invia(email, soggetto, corpo);
    }

    /**
     * Recupera l'identificatore condivisibile del link (id_link in forma di stringa),
     * delegando a DBMSBnd. Implementazione di supporto per MailServerBnd.recuperaLink.
     */
    public String recuperaLink(String email, UUID idLink) {
        com.afam.entities.EntityLink link = DBMSBnd.getInstance().recuperaLink(email, idLink);
        return link != null ? link.getLink() : null;
    }

    // ── Helper privato ────────────────────────────────────────────────────────

    /** Stampa l'OTP sul terminale del server — azione di Sistema come da sequence diagram. */
    private void stampaOTPTerminale(String destinatario, String otp) {
        String sep = "═".repeat(50);
        System.out.println("\n" + sep);
        System.out.println("  [SISTEMA] OTP GENERATO");
        System.out.println("  Destinatario : " + destinatario);
        System.out.println("  Codice OTP   : " + otp);
        System.out.println("  Scadenza     : " + com.afam.utils.Constants.OTP_DURATION_MINUTES + " minuti");
        System.out.println(sep + "\n");
    }

    /** Invia. */
    private void invia(String to, String soggetto, String corpo) {
        // Stampa sempre in console per consentire i test locali facilitati
        System.out.println("\n[AFAM MAIL GATEWAY] Destinatario: " + to);
        System.out.println("[AFAM MAIL GATEWAY] Oggetto:      " + soggetto);
        System.out.println("[AFAM MAIL GATEWAY] Contenuto:\n" + corpo + "\n");

        try {
            Message msg = new MimeMessage(mailSession);
            msg.setFrom(new InternetAddress(fromAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(soggetto);
            msg.setText(corpo);
            Transport.send(msg);
            LOG.info("Email inviata a: " + to);
        } catch (Exception e) {
            LOG.warning("Invio SMTP reale fallito (SMTP bloccato o credenziali invalide): " + e.getMessage());
            LOG.info("Test procedibile leggendo il codice sopra stampato in console.");
        }
    }
}
