package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.server.dao.DBMSBnd;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Sequence: getIdLink → recuperaScadenza(idLink) → aggiornaScadenza(scadenza, idLink)
 *
 * Nota: la firma aggiornaScadenza(scadenza, idLink) ha idLink come secondo parametro
 * perché la spec richiede la stessa firma disambiguata del DBMSBnd
 * (necessaria per distinguerla da altri metodi senza parametro).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ImpostaScadenzaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private EntityLink link;

    public void setLink(EntityLink l) { this.link = l; }

    public UUID getIdLink() {
        return link != null ? link.getIdLink() : null;
    }

    public OffsetDateTime recuperaScadenza(UUID idLink) {
        return db.recuperaScadenza(idLink);
    }

    /** Aggiorna la scadenza del link. Passa null per rimuovere la scadenza. */
    public void aggiornaScadenza(OffsetDateTime scadenza, UUID idLink) {
        db.aggiornaScadenza(scadenza, idLink);
    }
}
