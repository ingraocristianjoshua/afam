package com.afam.server.control.gestiscicontenuti;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdContenuto → eliminaContenuto(idContenuto)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EliminaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityContenuto contenuto;

    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    /** Legge l'id dal contenuto caricato in memoria (getId... = lettura da entity). */
    public UUID getIdContenuto() {
        return contenuto != null ? contenuto.getIdContenuto() : null;
    }

    public void eliminaContenuto(UUID idContenuto) {
        db.eliminaContenuto(idContenuto);
    }
}
