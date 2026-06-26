package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.Map;
import java.util.UUID;

/**
 * ModificaInformazioniCtrl – aggiorna nome, cognome, email e telefono.
 *
 * Sequence: getIdUtente → recuperaEmail(idUtente) → verificaDati
 *           → isMailInUse(email) → checkValid → aggiornaDati(data)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ModificaInformazioniCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid        = true;
    private String  errorMessage = "";

    /** Legge l'id dell'utente corrente dal contesto DBMSBnd. */
    public UUID getIdUtente() {
        return db.getCurrentUserId();
    }

    public String recuperaEmail(UUID idUtente) {
        return db.recuperaEmail(idUtente);
    }

    /**
     * Valida i nuovi dati anagrafici.
     * data può contenere: nome, cognome, email, numeroTelefono (tutti opzionali).
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";
        String nome     = (String) data.get("nome");
        String cognome  = (String) data.get("cognome");
        String email    = (String) data.get("email");
        String telefono = (String) data.get("numeroTelefono");

        if (nome     != null && !nome.isBlank()     && !Validators.isNomeValido(nome))
            return fail("Nome non valido.");
        if (cognome  != null && !cognome.isBlank()  && !Validators.isNomeValido(cognome))
            return fail("Cognome non valido.");
        if (email    != null && !email.isBlank()    && !Validators.isEmailValida(email))
            return fail("Email non valida.");
        if (telefono != null && !telefono.isBlank() && !Validators.isTelefonoValido(telefono))
            return fail("Numero di telefono non valido.");
        return true;
    }

    /**
     * Controlla se la nuova email è già usata da un altro account.
     * Se occupata, imposta lo stato di errore per checkValid.
     * @return true se la mail è già in uso (caso d'errore).
     */
    public boolean isMailInUse(String email) {
        if (email == null || email.isBlank()) return false;
        if (db.isMailInUse(email)) return fail("Email già in uso da un altro account.");
        return false;
    }

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    public void aggiornaDati(Map<String, Object> data) {
        db.aggiornaDati(data);
    }

    public boolean isValid()         { return valid; }
    public String  getErrorMessage() { return errorMessage; }

    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
