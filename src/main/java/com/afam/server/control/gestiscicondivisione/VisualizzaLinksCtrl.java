package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.server.dao.DBMSBnd;

import java.util.List;

/**
 * Sequence: recuperaLinks → (restituisce lista link dell'utente corrente)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaLinksCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    /** Restituisce tutti i link di condivisione dell'utente corrente. */
    public List<EntityLink> recuperaLinks() {
        return db.recuperaLinks();
    }
}
