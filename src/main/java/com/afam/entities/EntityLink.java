package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity che rappresenta un link di condivisione generato da uno studente.
 *
 * Il campo idPortfolio NON è una colonna diretta della tabella link:
 * viene caricato da DBMSBnd con una JOIN su link_portfolio quando si
 * recupera il link. Questo permette a getIdPortfolio(idLink) di
 * restituire l'id del portfolio associato leggendo solo dalla memoria,
 * rispettando la convenzione getId... = lettura da entity, non da DB.
 *
 * Getter dal sequence diagram:
 *   – getIdLink()              → uuid del link
 *   – getLink()                → l'id del link in forma di stringa (identificatore condivisibile)
 *   – getIdPortfolio(idLink)   → id del portfolio collegato (caricato via JOIN)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EntityLink {

    // ── Campi ──────────────────
    private UUID           idLink;
    private OffsetDateTime scadenza;     // null = nessuna scadenza
    /** "attivo" | "revocato" | "scaduto" */
    private String         stato;
    private boolean        flagAperto;
    /** "privato" | "pubblico" */
    private String         visibilita;
    private UUID           idUtente;

    /**
     * Id del portfolio collegato, popolato da DBMSBnd via JOIN su link_portfolio.
     * Può essere null se il link non è ancora associato a un portfolio
     * o se il link non è stato caricato con la JOIN.
     */
    private UUID idPortfolio;

    // ── Costruttori ──────────────────
    public EntityLink() {}

    public EntityLink(UUID idLink, OffsetDateTime scadenza,
                      String stato, boolean flagAperto, String visibilita,
                      UUID idUtente, UUID idPortfolio) {
        this.idLink      = idLink;
        this.scadenza    = scadenza;
        this.stato       = stato;
        this.flagAperto  = flagAperto;
        this.visibilita  = visibilita;
        this.idUtente    = idUtente;
        this.idPortfolio = idPortfolio;
    }

    // ── getter richiesti dai sequence diagram ──────────────────────────────────

    public UUID getIdLink() { return idLink; }

    /** Restituisce l'identificatore condivisibile del link: l'id_link in forma di stringa. */
    public String getLink() { return idLink != null ? idLink.toString() : null; }

    /**
     * Restituisce l'id del portfolio associato al link (letto dalla memoria,
     * non dal DB). Il parametro idLink serve da verifica contestuale nei
     * sequence diagram; non altera il risultato.
     */
    public UUID getIdPortfolio(UUID idLink) { return idPortfolio; }

    // ── getter aggiuntivi ─────────────────────────────────────────────────────

    public OffsetDateTime getScadenza()   { return scadenza; }
    /** Restituisce stato. */
    public String         getStato()      { return stato; }
    /** Indica se flag aperto. */
    public boolean        isFlagAperto()  { return flagAperto; }
    /** Restituisce visibilita. */
    public String         getVisibilita() { return visibilita; }
    /** Restituisce id utente. */
    public UUID           getIdUtente()   { return idUtente; }
    /** Restituisce id portfolio. */
    public UUID           getIdPortfolio(){ return idPortfolio; }
}
