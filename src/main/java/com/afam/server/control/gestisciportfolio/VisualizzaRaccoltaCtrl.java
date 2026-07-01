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
 */
public class VisualizzaRaccoltaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Imposta raccolta. */
    public void setRaccolta(EntityRaccolta r) { this.raccolta = r; }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Restituisce id raccolta. */
    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    /** Recupera raccolta. */
    public EntityRaccolta recuperaRaccolta(UUID idRaccolta) {
        raccolta = db.recuperaRaccolta(idRaccolta);
        return raccolta;
    }

    /** Restituisce i contenuti della raccolta. */
    public List<EntityContenuto> recuperaContenutiRaccolta(UUID idRaccolta) {
        return db.recuperaContenutiRaccolta(idRaccolta);
    }

    /** Restituisce raccolta. */
    public EntityRaccolta getRaccolta() { return raccolta; }
}
