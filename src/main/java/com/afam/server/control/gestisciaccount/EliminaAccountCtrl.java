package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;

import java.util.Map;
import java.util.UUID;

/**
 * EliminaAccountCtrl – eliminazione definitiva dell'account.
 *
 * Sequence: getIdUtente → verificaDati → verificaPassword(password)
 *           → checkValid → eliminaUtente(idUtente)
 *
 * La verifica della password prima dell'eliminazione è un controllo
 * di sicurezza obbligatorio: impedisce eliminazioni accidentali o
 * da sessioni non autorizzate.
 */
public class EliminaAccountCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid        = true;
    private String  errorMessage = "";

    // ── Metodi ──────────────────
    public UUID getIdUtente() {
        return db.getCurrentUserId();
    }

    /**
     * Valida che la richiesta contenga la password di conferma.
     * data deve contenere: password.
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";
        String pwd = (String) data.get("password");
        if (pwd == null || pwd.isBlank()) {
            return fail("Inserisci la password per confermare l'eliminazione.");
        }
        return true;
    }

    /**
     * Verifica la password tramite bcrypt prima di procedere con l'eliminazione.
     */
    public boolean verificaPassword(String password) {
        if (!db.verificaPassword(password)) {
            return fail("Password non corretta.");
        }
        return true;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /**
     * Elimina l'utente e tutti i dati associati (CASCADE nel DB).
     * Dopo questa chiamata il client deve effettuare il logout.
     */
    public void eliminaUtente(UUID idUtente) {
        db.eliminaUtente(idUtente);
    }

    /** Indica se valid. */
    public boolean isValid()         { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage() { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
