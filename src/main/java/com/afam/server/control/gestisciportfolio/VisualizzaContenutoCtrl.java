package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdContenuto(idPortfolio) → recuperaContenuto(idContenuto)
 */
public class VisualizzaContenutoCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityContenuto contenuto;

    // ── Metodi ──────────────────
    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    /** Restituisce id contenuto. */
    public UUID getIdContenuto(UUID idPortfolio) {
        return contenuto != null ? contenuto.getIdContenuto(idPortfolio) : null;
    }

    /** Recupera contenuto. */
    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        contenuto = db.recuperaContenuto(idContenuto);
        return contenuto;
    }

    /** Restituisce contenuto. */
    public EntityContenuto getContenuto() { return contenuto; }
}
