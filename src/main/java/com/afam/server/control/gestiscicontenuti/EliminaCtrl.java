package com.afam.server.control.gestiscicontenuti;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdContenuto → eliminaContenuto(idContenuto)
 */
public class EliminaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityContenuto contenuto;

    // ── Metodi ──────────────────
    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    /** Legge l'id dal contenuto caricato in memoria (getId... = lettura da entity). */
    public UUID getIdContenuto() {
        return contenuto != null ? contenuto.getIdContenuto() : null;
    }

    /** Elimina contenuto. */
    public void eliminaContenuto(UUID idContenuto) {
        db.eliminaContenuto(idContenuto);
    }
}
