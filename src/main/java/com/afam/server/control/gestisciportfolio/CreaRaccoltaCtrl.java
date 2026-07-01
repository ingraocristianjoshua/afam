package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getNomeRaccolta → verificaNomeRaccolta
 *           → isNomeInUso(nomeRaccolta) → checkValid → generaIdRaccolta
 *           → salvaRaccolta(idRaccolta, nomeRaccolta)
 */
public class CreaRaccoltaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid = true;
    private String  errorMessage = "";

    private EntityPortfolio portfolio;
    private String          nomeRaccolta;
    private UUID            idRaccolta;

    // ── Metodi ──────────────────
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Imposta portfolio. */
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Restituisce nome raccolta. */
    public String getNomeRaccolta() { return nomeRaccolta; }

    /** Imposta nome raccolta. */
    public void setNomeRaccolta(String nome) { this.nomeRaccolta = nome; }

    /** Verifica nome raccolta. */
    public boolean verificaNomeRaccolta(String nomeRaccolta) {
        if (!Validators.isNomeRaccoltaValido(nomeRaccolta)) {
            return fail("Nome raccolta non valido (1–80 caratteri).");
        }
        return true;
    }

    /** Controlla se esiste già una raccolta con questo nome nel portfolio corrente. */
    public boolean isNomeInUso(String nomeRaccolta) {
        if (db.isNomeInUso(nomeRaccolta)) return fail("Esiste già una raccolta con questo nome.");
        return false;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /** Genera id raccolta. */
    public UUID generaIdRaccolta() {
        idRaccolta = UUID.randomUUID();
        return idRaccolta;
    }

    /** Salva raccolta. */
    public void salvaRaccolta(UUID idRaccolta, String nomeRaccolta) {
        db.salvaRaccolta(idRaccolta, nomeRaccolta);
    }

    /** Indica se valid. */
    public boolean isValid()         { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage() { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
