package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdContenuto(idPortfolio) → recuperaContenuto(idContenuto)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaContenutoCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityContenuto contenuto;

    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    public UUID getIdContenuto(UUID idPortfolio) {
        return contenuto != null ? contenuto.getIdContenuto(idPortfolio) : null;
    }

    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        contenuto = db.recuperaContenuto(idContenuto);
        return contenuto;
    }

    public EntityContenuto getContenuto() { return contenuto; }
}
