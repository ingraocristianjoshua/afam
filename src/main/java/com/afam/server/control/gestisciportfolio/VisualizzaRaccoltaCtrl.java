package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;

import java.util.List;
import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → recuperaRaccolta(idRaccolta)
 *           → recuperaContenutiRaccolta(idRaccolta)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaRaccoltaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public void setRaccolta(EntityRaccolta r) { this.raccolta = r; }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    public EntityRaccolta recuperaRaccolta(UUID idRaccolta) {
        raccolta = db.recuperaRaccolta(idRaccolta);
        return raccolta;
    }

    /** Restituisce i contenuti della raccolta. */
    public List<EntityContenuto> recuperaContenutiRaccolta(UUID idRaccolta) {
        return db.recuperaContenutiRaccolta(idRaccolta);
    }

    public EntityRaccolta getRaccolta() { return raccolta; }
}
