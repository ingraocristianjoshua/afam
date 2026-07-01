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
 */
public class VisualizzaPortfolioCondivisoCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;

    // ── Metodi ──────────────────
    public EntityPortfolio recuperaPortfolio(UUID idPortfolio) {
        portfolio = db.recuperaPortfolio(idPortfolio);
        return portfolio;
    }

    /** Recupera contenuti portfolio. */
    public List<EntityContenuto> recuperaContenutiPortfolio(UUID idPortfolio) {
        return db.recuperaContenutiPortfolio(idPortfolio);
    }

    /** Recupera visualizzazioni. */
    public int recuperaVisualizzazioni(UUID idPortfolio) {
        return db.recuperaVisualizzazioni(idPortfolio);
    }

    /** Incrementa il contatore delle visualizzazioni. */
    public void aggiornaNumero(int numeroVisualizzazioni, UUID idPortfolio) {
        db.aggiornaNumero(numeroVisualizzazioni, idPortfolio);
    }

    /** Restituisce portfolio. */
    public EntityPortfolio getPortfolio() { return portfolio; }
}
