package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/** Sequence: getIdPortfolio → eliminaPortfolio(idPortfolio)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EliminaPortfolioCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityPortfolio portfolio;

    public void setPortfolio(EntityPortfolio p) { this.portfolio = p; }

    /** Legge l'id dalla entity caricata (getId... = lettura da memoria). */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    public void eliminaPortfolio(UUID idPortfolio) {
        db.eliminaPortfolio(idPortfolio);
    }
}
