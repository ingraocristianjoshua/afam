package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/** Sequence: getIdPortfolio → eliminaPortfolio(idPortfolio)
 */
public class EliminaPortfolioCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) { this.portfolio = p; }

    /** Legge l'id dalla entity caricata (getId... = lettura da memoria). */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Elimina portfolio. */
    public void eliminaPortfolio(UUID idPortfolio) {
        db.eliminaPortfolio(idPortfolio);
    }
}
