package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity che rappresenta un portfolio di uno studente AFAM.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EntityPortfolio {

    // ── Campi ──────────────────
    private UUID           idPortfolio;
    private String         nome;
    private OffsetDateTime dataCreazione;
    private int            numeroVisualizzazioni;
    private UUID           idUtente;

    // ── Costruttori ──────────────────
    public EntityPortfolio() {}

    public EntityPortfolio(UUID idPortfolio, String nome,
                           OffsetDateTime dataCreazione,
                           int numeroVisualizzazioni, UUID idUtente) {
        this.idPortfolio           = idPortfolio;
        this.nome                  = nome;
        this.dataCreazione         = dataCreazione;
        this.numeroVisualizzazioni = numeroVisualizzazioni;
        this.idUtente              = idUtente;
    }

    // ── getter richiesto dai sequence diagram ─────────────────────────────────

    public UUID getIdPortfolio() { return idPortfolio; }

    // ── getter aggiuntivi ─────────────────────────────────────────────────────

    public String         getNome()                  { return nome; }
    /** Restituisce data creazione. */
    public OffsetDateTime getDataCreazione()          { return dataCreazione; }
    /** Restituisce numero visualizzazioni. */
    public int            getNumeroVisualizzazioni()  { return numeroVisualizzazioni; }
    /** Restituisce id utente. */
    public UUID           getIdUtente()               { return idUtente; }
}
