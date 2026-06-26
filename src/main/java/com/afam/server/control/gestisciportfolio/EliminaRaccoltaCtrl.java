package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/** Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → eliminaRaccolta(idRaccolta)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EliminaRaccoltaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;

    public void setPortfolio(EntityPortfolio p) { this.portfolio = p; }
    public void setRaccolta(EntityRaccolta r)   { this.raccolta  = r; }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /**
     * Legge l'id della raccolta dalla entity nel contesto del portfolio.
     * Il parametro idPortfolio è il contesto di chiamata (spec naming).
     */
    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    public void eliminaRaccolta(UUID idRaccolta) {
        db.eliminaRaccolta(idRaccolta);
    }
}
