package com.afam.server.control.autenticati;

import com.afam.server.dao.DBMSBnd;
import com.afam.server.dao.MailServerBnd;
import com.afam.utils.Constants;
import com.afam.utils.OTPGenerator;
import com.afam.utils.Validators;
import org.mindrot.jbcrypt.BCrypt;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * RecuperaPasswordCtrl – flusso di recupero password via OTP email.
 *
 * Sequence:
 *   verificaDati(email) → verificaEmailAssociata → checkValid
 *   → generaOTP → inviaOTPviaEmail
 *   → loop { recuperaOTP → verificaDati(nuovaPwd, ora) } → aggiornaPassword
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RecuperaPasswordCtrl {

    private final DBMSBnd       db   = DBMSBnd.getInstance();
    private final MailServerBnd mail = MailServerBnd.getInstance();

    private boolean        valid        = true;
    private String         errorMessage = "";
    private OffsetDateTime otpScadenza;

    // ── FASE 1: verifica email ────────────────────────────────────────────────

    /**
     * Prima chiamata verificaDati: controlla solo il formato dell'email.
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";
        String email = (String) data.get("email");
        if (!Validators.isEmailValida(email)) {
            return fail("Email non valida.");
        }
        return true;
    }

    public boolean verificaEmailAssociata(String email) {
        if (!db.verificaEmailAssociata(email)) {
            return fail("Nessun account associato a questa email.");
        }
        return true;
    }

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    // ── FASE 2: genera e invia OTP ────────────────────────────────────────────

    /**
     * Genera il codice OTP e calcola la scadenza.
     * Nel sequence la notazione generaOTP(otp, scadenza) indica che il metodo
     * produce entrambi i valori; in Java il codice è il valore di ritorno,
     * la scadenza è memorizzata internamente.
     */
    public String generaOTP() {
        otpScadenza = OffsetDateTime.now().plusMinutes(Constants.OTP_DURATION_MINUTES);
        return OTPGenerator.genera();
    }

    /** Invia l'OTP via email (delega a MailServerBnd). */
    public void inviaOTPviaEmail(String email, String otp) {
        mail.inviaOTPviaEmail(email, otp);
    }

    // ── FASE 3: verifica OTP e reimposta password ─────────────────────────────

    /**
     * Recupera l'OTP dal DB e ne verifica correttezza e validità temporale.
     * @param otp      codice inserito dall'utente.
     * @param scadenza timestamp di scadenza.
     * @return true se il codice è corretto e non è ancora scaduto.
     */
    public boolean recuperaOTP(String otp, OffsetDateTime scadenza) {
        String stored = db.recuperaOTP();
        if (stored == null || !stored.equals(otp)) return false;
        return OffsetDateTime.now().isBefore(scadenza);
    }

    /**
     * Seconda chiamata verificaDati: valida la nuova password e controlla
     * che l'OTP non sia scaduto comparando con l'ora passata.
     */
    public boolean verificaDati(Map<String, Object> data, OffsetDateTime ora) {
        valid        = true;
        errorMessage = "";
        String nuovaPassword = (String) data.get("nuovaPassword");
        if (!Validators.isPasswordValida(nuovaPassword)) {
            return fail("Password troppo debole (min 8 car., 1 maiusc., 1 cifra, 1 simbolo).");
        }
        if (ora != null && OffsetDateTime.now().isAfter(ora)) {
            return fail("Il codice OTP è scaduto. Richiedine uno nuovo.");
        }
        return true;
    }

    /**
     * Aggiorna la password nel DB dopo aver calcolato l'hash bcrypt.
     * @param nuovaPassword password in chiaro ricevuta dal client.
     */
    public void aggiornaPassword(String nuovaPassword) {
        String hash = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
        db.aggiornaPassword(hash);
    }

    // ── Getter per AuthApi ────────────────────────────────────────────────────

    public OffsetDateTime getOtpScadenza() { return otpScadenza; }
    public boolean        isValid()         { return valid; }
    public String         getErrorMessage() { return errorMessage; }

    private boolean fail(String msg) {
        valid = false; errorMessage = msg; return false;
    }
}
