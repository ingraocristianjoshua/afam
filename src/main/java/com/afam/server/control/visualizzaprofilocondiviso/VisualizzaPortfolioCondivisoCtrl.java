package com.afam.server.control.visualizzaprofilocondiviso;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.List;
import java.util.UUID;

/**
 * Sequence: recuperaPortfolio(idPortfolio) → recuperaContenutiPortfolio(idPortfolio)
 *           → recuperaVisualizzazioni(idPortfolio) → aggiornaNumero(n+1, idPortfolio)
 *
 * Versione "condivisa" del visualizzatore: incrementa sempre il contatore
 * di visualizzazioni, anche per accessi anonimi o tramite link.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaPortfolioCondivisoCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityPortfolio portfolio;

    public EntityPortfolio recuperaPortfolio(UUID idPortfolio) {
        portfolio = db.recuperaPortfolio(idPortfolio);
        return portfolio;
    }

    public List<EntityContenuto> recuperaContenutiPortfolio(UUID idPortfolio) {
        return db.recuperaContenutiPortfolio(idPortfolio);
    }

    public int recuperaVisualizzazioni(UUID idPortfolio) {
        return db.recuperaVisualizzazioni(idPortfolio);
    }

    /** Incrementa il contatore delle visualizzazioni. */
    public void aggiornaNumero(int numeroVisualizzazioni, UUID idPortfolio) {
        db.aggiornaNumero(numeroVisualizzazioni, idPortfolio);
    }

    public EntityPortfolio getPortfolio() { return portfolio; }
}
