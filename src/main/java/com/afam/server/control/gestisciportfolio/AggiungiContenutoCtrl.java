package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdContenuto(idPortfolio) → recuperaContenuto(idContenuto)
 *           → checkValid → aggiungiContenuto(idContenuto)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AggiungiContenutoCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private boolean valid = true;
    private String  errorMessage = "";

    private EntityPortfolio portfolio;
    private EntityContenuto contenuto;

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

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

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    public void aggiungiContenuto(UUID idContenuto) {
        db.aggiungiContenuto(idContenuto);
    }

    public boolean isValid()         { return valid; }
    public String  getErrorMessage() { return errorMessage; }
}
