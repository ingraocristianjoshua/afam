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
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaPortfolioCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

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

    public int recuperaVisualizzazioni(UUID idPortfolio) {
        return db.recuperaVisualizzazioni(idPortfolio);
    }

    public EntityPortfolio getPortfolio() { return portfolio; }
}
