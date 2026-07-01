package com.afam.server.control.autenticati;

import com.afam.entities.EntityUtente;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AuthCtrl – logica di autenticazione e registrazione.
 *
 * Copre due flussi distinti (come da sequence diagram):
 *   LOGIN:        verificaDati → checkValid → verificaCredenziali
 *   REGISTRAZIONE: compilaFormRegistrati → verificaDati → isMailInUse
 *                  → checkValid → generaIdUtente → creaAccount
 *
 * Istanza per-richiesta (stateful per la durata della singola chiamata REST).
 */
public class AuthCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // stato interno accumulato da verificaDati / isMailInUse
    private boolean valid        = true;
    private String  errorMessage = "";

    // entity popolata dopo login riuscito
    private EntityUtente entityUtente;
    private UUID         idUtente;

    // ── FLUSSO LOGIN ──────────────────────────────────────────────────────────

    /**
     * Valida i dati del form.
     * Flusso login: controlla email + password non vuota.
     * Flusso registrazione: controlla anche nome, cognome, robustezza password, telefono.
     * La distinzione avviene per presenza delle chiavi nome/cognome nella mappa.
     */
    public boolean verificaDati(Map<String, Object> data) {
        valid        = true;
        errorMessage = "";

        String email    = (String) data.get("email");
        String password = (String) data.get("password");
        String nome     = (String) data.get("nome");
        String cognome  = (String) data.get("cognome");

        if (!Validators.isEmailValida(email)) {
            return fail("Email non valida.");
        }

        // campi extra presenti solo in registrazione
        if (nome != null && !Validators.isNomeValido(nome)) {
            return fail("Nome non valido.");
        }
        if (cognome != null && !Validators.isNomeValido(cognome)) {
            return fail("Cognome non valido.");
        }
        if (nome != null && password != null && !Validators.isPasswordValida(password)) {
            return fail("Password troppo debole (min 8 car., 1 maiusc., 1 cifra, 1 simbolo).");
        }
        String tel = (String) data.get("numeroTelefono");
        if (tel != null && !tel.isBlank() && !Validators.isTelefonoValido(tel)) {
            return fail("Numero di telefono non valido.");
        }
        return true;
    }

    /**
     * Lancia {@link IllegalStateException} se verificaDati o isMailInUse hanno fallito.
     */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /**
     * Crea account con id fornito dalla control (flusso registrazione standard).
     * Esegue l'hashing bcrypt della password prima di persistere.
     */
    public void creaAccount(Map<String, Object> data, UUID idUtente) {
        Map<String, Object> prepared = new HashMap<>(data);
        String pwd = (String) prepared.remove("password");
        if (pwd != null && !pwd.isBlank()) {
            prepared.put("hashPassword", BCrypt.hashpw(pwd, BCrypt.gensalt()));
        }
        db.creaAccount(prepared, idUtente);
    }

    /**
     * Verifica le credenziali contro il DB.
     * Se valide, imposta currentUserId su DBMSBnd per le chiamate successive.
     * @return EntityUtente se le credenziali sono corrette, null altrimenti.
     */
    public EntityUtente verificaCredenziali(Map<String, Object> data) {
        entityUtente = db.verificaCredenziali(data);
        if (entityUtente != null) {
            idUtente = entityUtente.getIdUtente();
            db.setCurrentUser(idUtente);
        }
        return entityUtente;
    }

    // ── FLUSSO REGISTRAZIONE ─────────────────────────────────────────────────

    /** Normalizza i dati del form (già completi dalla boundary). */
    public Map<String, Object> compilaFormRegistrati(Map<String, Object> data) {
        return data;
    }

    /**
     * Controlla se l'email è già in uso.
     * Se sì, imposta lo stato di errore per checkValid.
     * @return true se la mail è già occupata (caso d'errore).
     */
    public boolean isMailInUse(String email) {
        if (db.isMailInUse(email)) {
            return fail("Questa email è già registrata.");
        }
        return false;
    }

    /** Genera e memorizza l'id del nuovo utente. */
    public UUID generaIdUtente() {
        idUtente = UUID.randomUUID();
        return idUtente;
    }

    // ── Getter per AuthApi ────────────────────────────────────────────────────

    public EntityUtente getEntityUtente()  { return entityUtente; }
    /** Restituisce id utente. */
    public UUID         getIdUtente()      { return idUtente; }
    /** Indica se valid. */
    public boolean      isValid()          { return valid; }
    /** Restituisce error message. */
    public String       getErrorMessage()  { return errorMessage; }

    // ── Helper privato ────────────────────────────────────────────────────────

    private boolean fail(String msg) {
        valid        = false;
        errorMessage = msg;
        return false;
    }
}
