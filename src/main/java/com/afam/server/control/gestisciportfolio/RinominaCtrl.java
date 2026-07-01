package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → recuperaRaccolta(idPortfolio)
 *           → getNomeRaccolta → verificaNomeRaccolta → verificaNome(nomeRaccolta)
 *           → checkValid → aggiornaNomeRaccolta(raccolta, nomeRaccolta)
 *
 * Distinzione tra i due metodi di verifica:
 *   verificaNomeRaccolta(nome)  – valida il formato del nome (regex)
 *   verificaNome(nome)          – controlla unicità nel portfolio (DB)
 *   Sono metodi separati per rispettare la spec (non uniformati).
 */
public class RinominaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid = true;
    private String  errorMessage = "";

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;
    private String          nuovoNome;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Imposta nuovo nome. */
    public void setNuovoNome(String nome) { this.nuovoNome = nome; }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Restituisce id raccolta. */
    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    /** Carica la raccolta dal DB e la memorizza per le operazioni successive. */
    public EntityRaccolta recuperaRaccolta(UUID idPortfolio) {
        raccolta = db.recuperaRaccolta(
                raccolta != null ? raccolta.getIdRaccolta() : idPortfolio);
        return raccolta;
    }

    /** Restituisce nome raccolta. */
    public String getNomeRaccolta() { return nuovoNome; }

    /** Verifica il formato del nome (regex — non accede al DB). */
    public boolean verificaNomeRaccolta(String nomeRaccolta) {
        if (!Validators.isNomeRaccoltaValido(nomeRaccolta)) {
            return fail("Nome raccolta non valido.");
        }
        return true;
    }

    /**
     * Verifica l'unicità del nome nel portfolio corrente (accede al DB).
     * Separato da verificaNomeRaccolta come da spec.
     */
    public boolean verificaNome(String nomeRaccolta) {
        if (db.isNomeInUso(nomeRaccolta)) {
            return fail("Esiste già una raccolta con questo nome nel portfolio.");
        }
        return true;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /** Aggiorna nome raccolta. */
    public void aggiornaNomeRaccolta(EntityRaccolta raccolta, String nomeRaccolta) {
        db.aggiornaNomeRaccolta(raccolta, nomeRaccolta);
    }

    /** Restituisce raccolta. */
    public EntityRaccolta getRaccolta()  { return raccolta; }
    /** Indica se valid. */
    public boolean        isValid()      { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage()     { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
