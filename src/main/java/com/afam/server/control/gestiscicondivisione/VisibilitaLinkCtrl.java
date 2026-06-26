package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;

import java.util.UUID;

/**
 * Sequence: getIdLink → recuperaVisibilitaLink(idLink) → aggiornaVisibilitaLink(visibilita, idLink)
 *
 * Nota: la firma aggiornaVisibilitaLink(visibilita, idLink) ha l'ordine dei parametri
 * della spec (visibilita prima, idLink secondo) — stesso dell'overload in DBMSBnd.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisibilitaLinkCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityLink link;

    public void setLink(EntityLink l) { this.link = l; }

    public UUID getIdLink() {
        return link != null ? link.getIdLink() : null;
    }

    public String recuperaVisibilitaLink(UUID idLink) {
        return db.recuperaVisibilitaLink(idLink);
    }

    /**
     * Aggiorna la visibilità del link.
     * @param visibilita uno tra Constants.VIS_PRIVATO, VIS_PUBBLICO.
     */
    public void aggiornaVisibilitaLink(String visibilita, UUID idLink) {
        db.aggiornaVisibilitaLink(visibilita, idLink);
    }

    /** Alterna tra privato e pubblico. */
    public String alternaVisibilita(String attuale) {
        return Constants.VIS_PRIVATO.equals(attuale)
                ? Constants.VIS_PUBBLICO
                : Constants.VIS_PRIVATO;
    }
}
