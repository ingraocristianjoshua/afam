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
 *   – getLink()                → url_token (il token condivisibile)
 *   – getIdPortfolio(idLink)   → id del portfolio collegato (caricato via JOIN)
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
/**
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EntityLink {

    private UUID           idLink;
    private String         urlToken;
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

    public EntityLink() {}

    public EntityLink(UUID idLink, String urlToken, OffsetDateTime scadenza,
                      String stato, boolean flagAperto, String visibilita,
                      UUID idUtente, UUID idPortfolio) {
        this.idLink      = idLink;
        this.urlToken    = urlToken;
        this.scadenza    = scadenza;
        this.stato       = stato;
        this.flagAperto  = flagAperto;
        this.visibilita  = visibilita;
        this.idUtente    = idUtente;
        this.idPortfolio = idPortfolio;
    }

    // ── getter richiesti dai sequence diagram ──────────────────────────────────

    public UUID getIdLink() { return idLink; }

    /** Restituisce il token del link (campo url_token), usato come URL condivisibile. */
    public String getLink() { return urlToken; }

    /**
     * Restituisce l'id del portfolio associato al link (letto dalla memoria,
     * non dal DB). Il parametro idLink serve da verifica contestuale nei
     * sequence diagram; non altera il risultato.
     */
    public UUID getIdPortfolio(UUID idLink) { return idPortfolio; }

    // ── getter aggiuntivi ─────────────────────────────────────────────────────

    public OffsetDateTime getScadenza()   { return scadenza; }
    public String         getStato()      { return stato; }
    public boolean        isFlagAperto()  { return flagAperto; }
    public String         getVisibilita() { return visibilita; }
    public UUID           getIdUtente()   { return idUtente; }
    public UUID           getIdPortfolio(){ return idPortfolio; }
}
