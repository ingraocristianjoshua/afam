package com.afam.server.control.visualizzaprofilocondiviso;

import com.afam.entities.EntityLink;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: isLinkValido(idLink) → getIdPortfolio(idLink) → recuperaPortfolio(idPortfolio)
 *
 * La verifica della validità del link è sincrona (confronto con now() nel DB);
 * non esiste uno scheduler che revoca automaticamente i link scaduti.
 * Il link viene verificato al momento dell'accesso.
 */
public class AccediTramiteLinkCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityLink    link;
    private EntityPortfolio portfolio;

    // ── Metodi ──────────────────
    public void setLink(EntityLink l) { this.link = l; }

    /**
     * Verifica che il link sia attivo e non scaduto.
     * Implementato come controllo sincrono nel DB (WHERE stato='attivo' AND scadenza > NOW()).
     */
    public boolean isLinkValido(UUID idLink) {
        return db.isLinkValido(idLink);
    }

    /**
     * Recupera l'id del portfolio collegato al link (dalla tabella link_portfolio).
     * Rispetta la convenzione getId... (lettura da entity o da DB con JOIN).
     */
    public UUID getIdPortfolio(UUID idLink) {
        if (link != null) {
            UUID idPortfolio = link.getIdPortfolio(idLink);
            if (idPortfolio != null) return idPortfolio;
        }
        return db.getIdPortfolio(idLink);
    }

    /** Recupera portfolio. */
    public EntityPortfolio recuperaPortfolio(UUID idPortfolio) {
        portfolio = db.recuperaPortfolio(idPortfolio);
        return portfolio;
    }

    /** Restituisce portfolio. */
    public EntityPortfolio getPortfolio() { return portfolio; }
}
