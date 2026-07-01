package com.afam.server.control.gestiscicondivisione;

import com.afam.entities.EntityLink;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;
import com.afam.server.dao.MailServerBnd;
import com.afam.utils.Constants;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdUtente → generaLink(idPortfolio, idUtente)
 *           → salvaNuovoLink(link) → [inviaLink(email, link)]
 */
public class GeneraLinkCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd      db   = DBMSBnd.getInstance();
    private final MailServerBnd mail = MailServerBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityLink      linkGenerato;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Legge l'id utente corrente (impostato dall'endpoint REST via setCurrentUser). */
    public UUID getIdUtente() {
        return db.getCurrentUserId();
    }

    /**
     * Genera un nuovo EntityLink. L'identificatore condivisibile del link è il
     * suo stesso id_link (UUID): non viene usato alcun token separato.
     * @param idPortfolio portfolio da condividere
     * @param idUtente    utente proprietario
     * @param visibilita  Constants.VIS_PRIVATO | VIS_PUBBLICO
     * @param scadenza    null per nessuna scadenza
     * @param flagAperto  true = accessibile senza autenticazione
     */
    public EntityLink generaLink(UUID idPortfolio, UUID idUtente,
                                 String visibilita, OffsetDateTime scadenza,
                                 boolean flagAperto) {
        UUID idLink = UUID.randomUUID();
        linkGenerato = new EntityLink(
                idLink, scadenza,
                Constants.LINK_ATTIVO, flagAperto, visibilita,
                idUtente, idPortfolio);
        return linkGenerato;
    }

    /** Salva nuovo link. */
    public void salvaNuovoLink(EntityLink link) {
        db.salvaNuovoLink(link);
    }

    /** Invia il link via email al destinatario. */
    public void inviaLink(String email, String link) {
        mail.inviaLink(email, link, "Uno studente AFAM");
    }

    /** Invia il link via email al destinatario con nome mittente. */
    public void inviaLink(String email, String link, String nomeMittente) {
        mail.inviaLink(email, link, nomeMittente);
    }

    /** Restituisce link generato. */
    public EntityLink getLinkGenerato() { return linkGenerato; }
}
