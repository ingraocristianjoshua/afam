package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.server.dao.DBMSBnd;

import java.util.List;

/**
 * Sequence: recuperaLinks → (restituisce lista link dell'utente corrente)
 */
public class VisualizzaLinksCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Metodi ──────────────────
    /** Restituisce tutti i link di condivisione dell'utente corrente. */
    public List<EntityLink> recuperaLinks() {
        return db.recuperaLinks();
    }
}
