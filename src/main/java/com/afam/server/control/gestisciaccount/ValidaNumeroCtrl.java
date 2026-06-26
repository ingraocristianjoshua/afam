package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.MailServerBnd;

import java.util.UUID;

/**
 * ValidaNumeroCtrl – valida il numero di telefono tramite OTP via SMS.
 *
 * Eredita il Template Method da ValidaContattoCtrl.
 * Implementa i passi variabili: canale SMS, stato numero_validato.
 *
 * Metodi del sequence diagram:
 *   getIdUtente(), recuperaNumTelefono(idUtente), recuperaStatoNumero(numero),
 *   verificaStatoNumero(statoNumero), generaOTP(otp), salvaCodiceOTP(otp, scadenza),
 *   inviaNumero(numero, otp), verificaOTP(otp), recuperaOTP(), checkCurrentTime(),
 *   aggiornaStatoNumero(statoNumero)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ValidaNumeroCtrl extends ValidaContattoCtrl {

    private final MailServerBnd mail = MailServerBnd.getInstance();

    // ── Metodi esplicitamente nominati nel sequence diagram ───────────────────

    public String recuperaNumTelefono(UUID idUtente) {
        return db.recuperaNumTelefono(idUtente);
    }

    public boolean recuperaStatoNumero(String numero) {
        return db.recuperaStatoNumero(numero);
    }

    /**
     * @param statoNumero true se il numero è già validato.
     * @return true se NON serve inviare l'OTP (già validato).
     */
    public boolean verificaStatoNumero(boolean statoNumero) {
        return statoNumero;
    }

    public void inviaNumero(String numero, String otp) {
        mail.inviaNumero(numero, otp);
    }

    public void aggiornaStatoNumero(boolean statoNumero) {
        db.aggiornaStatoNumero(statoNumero);
    }

    // ── Implementazione passi astratti del Template Method ────────────────────

    @Override
    protected String recuperaContatto(UUID idUtente) {
        return recuperaNumTelefono(idUtente);
    }

    @Override
    protected boolean recuperaStato(String numero) {
        return recuperaStatoNumero(numero);
    }

    @Override
    protected boolean verificaStato(boolean stato) {
        return verificaStatoNumero(stato);
    }

    @Override
    protected void invia(String numero, String otp) {
        inviaNumero(numero, otp);
    }

    @Override
    protected void aggiornaStato(boolean stato) {
        aggiornaStatoNumero(stato);
    }
}
