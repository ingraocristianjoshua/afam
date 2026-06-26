package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getNomeRaccolta → verificaNomeRaccolta
 *           → isNomeInUso(nomeRaccolta) → checkValid → generaIdRaccolta
 *           → salvaRaccolta(idRaccolta, nomeRaccolta)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class CreaRaccoltaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private boolean valid = true;
    private String  errorMessage = "";

    private EntityPortfolio portfolio;
    private String          nomeRaccolta;
    private UUID            idRaccolta;

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public String getNomeRaccolta() { return nomeRaccolta; }

    public void setNomeRaccolta(String nome) { this.nomeRaccolta = nome; }

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

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    public UUID generaIdRaccolta() {
        idRaccolta = UUID.randomUUID();
        return idRaccolta;
    }

    public void salvaRaccolta(UUID idRaccolta, String nomeRaccolta) {
        db.salvaRaccolta(idRaccolta, nomeRaccolta);
    }

    public boolean isValid()         { return valid; }
    public String  getErrorMessage() { return errorMessage; }

    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
