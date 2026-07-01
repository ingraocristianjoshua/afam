package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;
import com.afam.utils.OTPGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ValidaContattoCtrl – classe astratta che definisce il Template Method
 * per la validazione di un contatto tramite OTP.
 *
 * Scheletro fisso (passi concreti):
 *   generaOTP → salvaCodiceOTP → invia → loop{verificaOTP / checkCurrentTime} → aggiornaStato
 *
 * Passi variabili (hook astratti):
 *   recuperaContatto(idUtente)   – email o numero di telefono
 *   recuperaStato(contatto)      – stato corrente di validazione
 *   verificaStato(stato)         – true se già validato
 *   invia(contatto, otp)         – canale: email vs SMS
 *   aggiornaStato(stato)         – aggiorna email_validata o numero_validato
 *
 * Entrambe le sottoclassi (ValidaEmailCtrl, ValidaNumeroCtrl) ereditano
 * i passi concreti e sovrascrivono solo i passi variabili.
 */
public abstract class ValidaContattoCtrl {

    // ── Campi ──────────────────
    protected final DBMSBnd db = DBMSBnd.getInstance();

    // ── Passi CONCRETI (identici in entrambe le sottoclassi) ─────────────────

    public UUID getIdUtente() {
        return db.getCurrentUserId();
    }

    /**
     * Genera un codice OTP.
     * La notazione dei sequence diagram generaOTP(otp) indica che il codice
     * è il valore di ritorno catturato nella variabile otp.
     */
    public String generaOTP() {
        return OTPGenerator.genera();
    }

    /** Salva codice otp. */
    public void salvaCodiceOTP(String otp, OffsetDateTime scadenza) {
        db.salvaCodiceOTP(otp, scadenza);
    }

    /**
     * Confronta il codice inserito dall'utente con quello salvato nel DB.
     */
    public boolean verificaOTP(String otp) {
        String stored = db.recuperaOTP();
        return stored != null && stored.equals(otp);
    }

    /**
     * Legge il codice OTP dal DB senza confrontarlo.
     * Usato dalla control per ottenere il codice in scenari di reinvio.
     */
    public String recuperaOTP() {
        return db.recuperaOTP();
    }

    /**
     * Controlla che l'OTP non sia scaduto confrontando la scadenza
     * salvata nel DB con il momento attuale.
     * @return true se l'OTP è ancora valido.
     */
    public boolean checkCurrentTime() {
        OffsetDateTime scad = db.recuperaScadenzaOTP();
        return scad != null && OffsetDateTime.now().isBefore(scad);
    }

    // ── Template Method ───────────────────────────────────────────────────────

    /**
     * Esegue il flusso completo: genera → salva → invia.
     * La verifica avviene in una chiamata separata (chiamata via API).
     * @param idUtente id dell'utente corrente.
     * @return la scadenza dell'OTP generato, da restituire al client.
     */
    public final OffsetDateTime avviaValidazione(UUID idUtente) {
        db.setCurrentUser(idUtente);

        String  contatto = recuperaContatto(idUtente);
        boolean stato    = recuperaStato(contatto);

        if (verificaStato(stato)) {
            // già validato: nessun OTP necessario
            return null;
        }

        String         otp      = generaOTP();
        OffsetDateTime scadenza = OffsetDateTime.now()
                .plusMinutes(Constants.OTP_DURATION_MINUTES);
        salvaCodiceOTP(otp, scadenza);
        invia(contatto, otp);
        return scadenza;
    }

    /**
     * Completa la validazione: verifica OTP e aggiorna lo stato nel DB.
     * @param otp     codice inserito dall'utente.
     * @param stato   nuovo stato da impostare (true = validato).
     * @return true se la verifica ha avuto successo.
     */
    public final boolean completaValidazione(String otp, boolean stato) {
        if (!verificaOTP(otp)) return false;
        if (!checkCurrentTime()) return false;
        aggiornaStato(stato);
        return true;
    }

    // ── Passi ASTRATTI (variabili nelle sottoclassi) ─────────────────────────

    /** Recupera il contatto (email o telefono) dell'utente dal DB. */
    protected abstract String recuperaContatto(UUID idUtente);

    /** Recupera lo stato di validazione corrente del contatto. */
    protected abstract boolean recuperaStato(String contatto);

    /**
     * @return true se il contatto è già validato (nessun OTP da inviare).
     */
    protected abstract boolean verificaStato(boolean stato);

    /** Invia l'OTP al contatto (canale: email o SMS). */
    protected abstract void invia(String contatto, String otp);

    /** Aggiorna lo stato di validazione nel DB. */
    protected abstract void aggiornaStato(boolean stato);
}
