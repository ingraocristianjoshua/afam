package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.MailServerBnd;

import java.util.UUID;

/**
 * ValidaEmailCtrl – valida l'indirizzo email tramite OTP.
 *
 * Eredita il Template Method da ValidaContattoCtrl.
 * Implementa i passi variabili: canale email, stato email_validata.
 *
 * Metodi del sequence diagram (tutti ereditati o implementati qui):
 *   getIdUtente(), recuperaEmail(idUtente), recuperaStatoEmail(email),
 *   verificaStatoEmail(statoEmail), generaOTP(otp), salvaCodiceOTP(otp, scadenza),
 *   inviaEmail(email, otp), verificaOTP(otp), recuperaOTP(), checkCurrentTime(),
 *   aggiornaStatoEmail(statoEmail)
 */
public class ValidaEmailCtrl extends ValidaContattoCtrl {

    // ── Campi ──────────────────
    private final MailServerBnd mail = MailServerBnd.getInstance();

    // ── Metodi esplicitamente nominati nel sequence diagram ───────────────────

    public String recuperaEmail(UUID idUtente) {
        return db.recuperaEmail(idUtente);
    }

    /** Recupera stato email. */
    public boolean recuperaStatoEmail(String email) {
        return db.recuperaStatoEmail(email);
    }

    /**
     * @param statoEmail true se l'email è già validata.
     * @return true se NON serve inviare l'OTP (già validata).
     */
    public boolean verificaStatoEmail(boolean statoEmail) {
        return statoEmail; // true = già validata → skip
    }

    /** Invia email. */
    public void inviaEmail(String email, String otp) {
        mail.inviaEmail(email, otp);
    }

    /** Aggiorna stato email. */
    public void aggiornaStatoEmail(boolean statoEmail) {
        db.aggiornaStatoEmail(statoEmail);
    }

    // ── Implementazione passi astratti del Template Method ────────────────────

    @Override
    protected String recuperaContatto(UUID idUtente) {
        return recuperaEmail(idUtente);
    }

    /** Recupera stato. */
    @Override
    protected boolean recuperaStato(String email) {
        return recuperaStatoEmail(email);
    }

    /** Verifica stato. */
    @Override
    protected boolean verificaStato(boolean stato) {
        return verificaStatoEmail(stato);
    }

    /** Invia. */
    @Override
    protected void invia(String email, String otp) {
        inviaEmail(email, otp);
    }

    /** Aggiorna stato. */
    @Override
    protected void aggiornaStato(boolean stato) {
        aggiornaStatoEmail(stato);
    }
}
