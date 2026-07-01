package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/** Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → eliminaRaccolta(idRaccolta)
 */
public class EliminaRaccoltaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) { this.portfolio = p; }
    /** Imposta raccolta. */
    public void setRaccolta(EntityRaccolta r)   { this.raccolta  = r; }

    /** Restituisce id portfolio. */
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

    /** Elimina raccolta. */
    public void eliminaRaccolta(UUID idRaccolta) {
        db.eliminaRaccolta(idRaccolta);
    }
}
