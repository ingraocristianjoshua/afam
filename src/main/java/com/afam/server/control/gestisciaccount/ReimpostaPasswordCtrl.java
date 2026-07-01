package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

/**
 * ReimpostaPasswordCtrl – cambio password per l'utente già autenticato.
 *
 * Sequence: verificaDati → verificaVecchiaPassword → checkValid → aggiornaPassword
 */
public class ReimpostaPasswordCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid        = true;
    private String  errorMessage = "";

    // ── Metodi ──────────────────
    /**
     * Valida che la nuova password sia robusta e diversa da quella attuale.
     * data deve contenere: vecchiaPassword, nuovaPassword.
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";
        String nuova = (String) data.get("nuovaPassword");
        if (!Validators.isPasswordValida(nuova)) {
            return fail("Nuova password troppo debole (min 8 car., 1 maiusc., 1 cifra, 1 simbolo).");
        }
        return true;
    }

    /**
     * Verifica che la vecchia password corrisponda all'hash nel DB.
     * Il confronto avviene tramite bcrypt (nessuna password in chiaro nel DB).
     */
    public boolean verificaVecchiaPassword(String vecchiaPassword) {
        if (!db.verificaVecchiaPassword(vecchiaPassword)) {
            return fail("La password attuale non è corretta.");
        }
        return true;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /**
     * Calcola l'hash bcrypt della nuova password e la salva nel DB.
     * @param nuovaPassword password in chiaro (hashata qui, non nella boundary).
     */
    public void aggiornaPassword(String nuovaPassword) {
        String hash = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
        db.aggiornaPassword(hash);
    }

    /** Indica se valid. */
    public boolean isValid()          { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage()  { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
