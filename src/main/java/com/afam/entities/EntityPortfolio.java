package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity che rappresenta un portfolio di uno studente AFAM.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
/**
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EntityPortfolio {

    private UUID           idPortfolio;
    private String         nome;
    private OffsetDateTime dataCreazione;
    private int            numeroVisualizzazioni;
    private UUID           idUtente;

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
    public OffsetDateTime getDataCreazione()          { return dataCreazione; }
    public int            getNumeroVisualizzazioni()  { return numeroVisualizzazioni; }
    public UUID           getIdUtente()               { return idUtente; }
}
