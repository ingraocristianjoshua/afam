package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.List;
import java.util.UUID;

/**
 * Sequence: getIdPortfolio → recuperaPortfolio(idPortfolio) → recuperaContenutiPortfolio(idPortfolio)
 *           → aggiornaNumero(n+1, idPortfolio)
 *
 * aggiornaNumero incrementa il contatore di visualizzazioni.
 */
public class VisualizzaPortfolioCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Recupera portfolio. */
    public EntityPortfolio recuperaPortfolio(UUID idPortfolio) {
        portfolio = db.recuperaPortfolio(idPortfolio);
        return portfolio;
    }

    /** Restituisce i contenuti del portfolio ordinati per posizione. */
    public List<EntityContenuto> recuperaContenutiPortfolio(UUID idPortfolio) {
        return db.recuperaContenutiPortfolio(idPortfolio);
    }

    /** Incrementa il contatore di visualizzazioni del portfolio. */
    public void aggiornaNumero(int numeroVisualizzazioni, UUID idPortfolio) {
        db.aggiornaNumero(numeroVisualizzazioni, idPortfolio);
    }

    /** Recupera visualizzazioni. */
    public int recuperaVisualizzazioni(UUID idPortfolio) {
        return db.recuperaVisualizzazioni(idPortfolio);
    }

    /** Restituisce portfolio. */
    public EntityPortfolio getPortfolio() { return portfolio; }
}
