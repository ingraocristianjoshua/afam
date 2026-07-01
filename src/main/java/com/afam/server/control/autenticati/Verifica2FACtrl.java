package com.afam.server.control.autenticati;

import com.afam.entities.EntityUtente;
import com.afam.server.dao.DBMSBnd;
import com.afam.server.dao.MailServerBnd;
import com.afam.utils.Constants;
import com.afam.utils.OTPGenerator;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Verifica2FACtrl – orchestrazione del flusso di verifica a due fattori.
 *
 * Sequence diagram:
 *   generaOTP → salvaCodiceOTP → getNumTelefono → inviaSMS
 *   → loop { verificaOTP } → (successo)
 *
 * Nota sulle firme:
 *   generaOTP(otp) nei diagrammi indica che il risultato è catturato
 *   nella variabile otp; in Java è un metodo che RESTITUISCE il codice.
 */
public class Verifica2FACtrl {

    // ── Campi ──────────────────
    private final DBMSBnd       db   = DBMSBnd.getInstance();
    private final MailServerBnd mail = MailServerBnd.getInstance();

    // ── Metodi ──────────────────
    /**
     * Genera un codice OTP.
     * Nel sequence diagram la notazione è generaOTP(otp) dove otp
     * è il valore di ritorno catturato dalla control.
     */
    public String generaOTP() {
        return OTPGenerator.genera();
    }

    /** Salva l'OTP nel DB con scadenza di default (Constants.OTP_DURATION_MINUTES). */
    public void salvaCodiceOTP(String otp) {
        OffsetDateTime scadenza = OffsetDateTime.now()
                .plusMinutes(Constants.OTP_DURATION_MINUTES);
        salvaCodiceOTP(otp, scadenza);
    }

    /** Salva l'OTP nel DB con scadenza esplicita. */
    public void salvaCodiceOTP(String otp, OffsetDateTime scadenza) {
        db.salvaCodiceOTP(otp, scadenza);
    }

    /**
     * Legge il numero di telefono dall'entity (dati già in memoria,
     * nessuna query aggiuntiva al DB).
     */
    public String getNumTelefono(EntityUtente utente) {
        return utente.getNumTelefono();
    }

    /** Overload zero-arg: legge dal DB tramite DBMSBnd (utente corrente). */
    public String getNumTelefono() {
        return db.recuperaNumTelefono(null); // id_utente preso da currentUserId in DBMSBnd
    }

    /** Invia sms. */
    public void inviaSMS(String numero, String otp) {
        mail.inviaSMS(numero, otp);
    }

    /**
     * Verifica l'OTP ricevuto dal form.
     * @param data mappa con chiave "otp".
     */
    public boolean verificaOTP(Map<String, Object> data) {
        return verificaOTP((String) data.get("otp"));
    }

    /**
     * Confronta il codice inserito con quello salvato nel DB.
     * La scadenza non è controllata qui: viene verificata in recuperaOTP.
     */
    public boolean verificaOTP(String otp) {
        String stored = db.recuperaOTP();
        return stored != null && stored.equals(otp);
    }

    /**
     * Recupera l'OTP dal DB e verifica sia il codice sia la scadenza.
     * Restituisce true solo se il codice è corretto E non è scaduto.
     */
    public boolean recuperaOTP(String otp, OffsetDateTime scadenza) {
        String stored = db.recuperaOTP();
        if (stored == null || !stored.equals(otp)) return false;
        return OffsetDateTime.now().isBefore(scadenza);
    }
}
