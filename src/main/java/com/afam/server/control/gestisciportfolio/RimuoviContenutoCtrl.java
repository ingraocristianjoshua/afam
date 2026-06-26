package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdContenuto(idPortfolio) → rimuoviContenuto(idContenuto)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RimuoviContenutoCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

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

    public void rimuoviContenuto(UUID idContenuto) {
        db.rimuoviContenuto(idContenuto);
    }
}
