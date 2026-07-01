package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → getIdContenuto(idRaccolta)
 *           → aggiornaStatoRaccolta(idRaccolta, idContenuto)
 *
 * Firma identica ad AggiungiAllaRaccoltaCtrl per la stessa operazione toggle del DB.
 */
public class RimuoviDallaRaccoltaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;
    private EntityContenuto contenuto;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Imposta raccolta. */
    public void setRaccolta(EntityRaccolta r)   { this.raccolta  = r; }
    /** Imposta contenuto. */
    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Restituisce id raccolta. */
    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    /** Restituisce id contenuto. */
    public UUID getIdContenuto(UUID idRaccolta) {
        return contenuto != null ? contenuto.getIdContenuto(idRaccolta) : null;
    }

    /** Toggle: se la coppia esiste → DELETE; se non esiste → INSERT (come da spec). */
    public void aggiornaStatoRaccolta(UUID idRaccolta, UUID idContenuto) {
        db.aggiornaStatoRaccolta(idRaccolta, idContenuto);
    }
}
