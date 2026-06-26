package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdLink → aggiornaStatoLink(idLink)
 * La revoca imposta lo stato del link a "revocato" (valore in Constants.LINK_REVOCATO).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RevocaLinkCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityLink link;

    public void setLink(EntityLink l) { this.link = l; }

    /** Legge l'id del link dalla entity in memoria. */
    public UUID getIdLink() {
        return link != null ? link.getIdLink() : null;
    }

    public void aggiornaStatoLink(UUID idLink) {
        db.aggiornaStatoLink(idLink);
    }
}
