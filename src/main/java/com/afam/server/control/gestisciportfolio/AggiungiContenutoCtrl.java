package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdContenuto(idPortfolio) → recuperaContenuto(idContenuto)
 *           → checkValid → aggiungiContenuto(idContenuto)
 */
public class AggiungiContenutoCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid = true;
    private String  errorMessage = "";

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

    /** Verifica che il contenuto esista nel catalogo utente prima di aggiungerlo. */
    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        EntityContenuto c = db.recuperaContenuto(idContenuto);
        if (c == null) { valid = false; errorMessage = "Contenuto non trovato."; }
        else this.contenuto = c;
        return c;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /** Aggiungi contenuto. */
    public void aggiungiContenuto(UUID idContenuto) {
        db.aggiungiContenuto(idContenuto);
    }

    /** Indica se valid. */
    public boolean isValid()         { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage() { return errorMessage; }
}
