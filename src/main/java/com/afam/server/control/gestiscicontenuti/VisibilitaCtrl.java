package com.afam.server.control.gestiscicontenuti;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;

import java.util.UUID;

/**
 * Sequence: getIdContenuto → recuperaVisibilita(idContenuto)
 *           → aggiornaLivelloVisibilita(idContenuto, visibilita)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisibilitaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityContenuto contenuto;

    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    public UUID getIdContenuto() {
        return contenuto != null ? contenuto.getIdContenuto() : null;
    }

    /** Restituisce la visibilità attuale del contenuto dal DB. */
    public String recuperaVisibilita(UUID idContenuto) {
        return db.recuperaVisibilita(idContenuto);
    }

    /**
     * Aggiorna il livello di visibilità del contenuto.
     * @param visibilita uno tra Constants.VIS_PRIVATO, VIS_PUBBLICO, VIS_CONDIVISO.
     */
    public void aggiornaLivelloVisibilita(UUID idContenuto, String visibilita) {
        db.aggiornaLivelloVisibilita(idContenuto, visibilita);
    }

    /** Cicla al livello successivo: privato → pubblico → privato. */
    public String alternaVisibilita(String attuale) {
        return switch (attuale) {
            case Constants.VIS_PRIVATO -> Constants.VIS_PUBBLICO;
            default                    -> Constants.VIS_PRIVATO;
        };
    }
}
