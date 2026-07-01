package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.Map;

/**
 * Gestione2FACtrl – attiva o disattiva l'autenticazione a due fattori.
 *
 * Sequence: recuperaStato2FA → verificaDati(data) → verificaStato2FA(email, numero)
 *           → checkValid → aggiornaStato2FA(stato2FA)
 *
 * Il metodo verificaStato2FA assicura che l'utente abbia sia un'email
 * sia un numero di telefono associati prima di abilitare il 2FA,
 * garantendo che il canale SMS sia disponibile.
 */
public class Gestione2FACtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid        = true;
    private String  errorMessage = "";

    // ── Metodi ──────────────────
    /** @return true se il 2FA è attualmente attivo per l'utente corrente. */
    public boolean recuperaStato2FA() {
        return db.recuperaStato2FA();
    }

    /**
     * Valida i dati richiesti per l'abilitazione: numero di telefono.
     * data deve contenere: numero (telefono da associare, opzionale se disabilitazione).
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";
        String numero = (String) data.get("numero");
        // la validazione del numero è richiesta solo in fase di abilitazione
        Boolean abilita = (Boolean) data.get("abilita");
        if (Boolean.TRUE.equals(abilita) && (numero == null || numero.isBlank())) {
            return fail("Inserisci un numero di telefono per abilitare il 2FA.");
        }
        if (numero != null && !numero.isBlank() && !Validators.isTelefonoValido(numero)) {
            return fail("Numero di telefono non valido.");
        }
        return true;
    }

    /**
     * Verifica che email e numero siano entrambi associati all'account.
     * Se il numero non è registrato sul DB, il 2FA non può essere abilitato.
     */
    public boolean verificaStato2FA(String email, String numero) {
        if (!db.verificaStato2FA(email, numero)) {
            return fail("Email o numero di telefono non corrispondono all'account.");
        }
        return true;
    }

    /**
     * Verifica che, in fase di abilitazione, l'utente abbia SIA l'email SIA il
     * numero di telefono già validati: condizione necessaria affinché il codice
     * OTP possa essere recapitato in modo affidabile. In fase di disabilitazione
     * non è richiesta alcuna validazione.
     */
    public boolean verificaContattiValidati(boolean abilita) {
        if (abilita && !db.verificaContattiValidati()) {
            return fail("Per abilitare il 2FA devi prima validare sia l'email sia il numero di telefono.");
        }
        return true;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /** Aggiorna stato2 fa. */
    public void aggiornaStato2FA(boolean stato2FA) {
        db.aggiornaStato2FA(stato2FA);
    }

    /** Indica se valid. */
    public boolean isValid()         { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage() { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
