package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdContenuto(idPortfolio) → rimuoviContenuto(idContenuto)
 */
public class RimuoviContenutoCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityContenuto contenuto;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Imposta contenuto. */
    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Restituisce id contenuto. */
    public UUID getIdContenuto(UUID idPortfolio) {
        return contenuto != null ? contenuto.getIdContenuto(idPortfolio) : null;
    }

    /** Rimuovi contenuto. */
    public void rimuoviContenuto(UUID idContenuto) {
        db.rimuoviContenuto(idContenuto);
    }
}
