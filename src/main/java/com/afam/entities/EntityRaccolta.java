package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.UUID;

/**
 * Entity che rappresenta una raccolta tematica all'interno di un portfolio.
 *
 * Il sequence diagram specifica getIdRaccolta(idPortfolio): il parametro
 * idPortfolio fornisce contesto alla chiamata (identifica quale portfolio
 * si sta gestendo) ma non altera il valore restituito, che è sempre
 * this.idRaccolta.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
/**
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EntityRaccolta {

    private UUID   idRaccolta;
    private String nome;
    private int    ordine;
    private UUID   idPortfolio;

    public EntityRaccolta() {}

    public EntityRaccolta(UUID idRaccolta, String nome, int ordine, UUID idPortfolio) {
        this.idRaccolta  = idRaccolta;
        this.nome        = nome;
        this.ordine      = ordine;
        this.idPortfolio = idPortfolio;
    }

    // ── getter richiesto dai sequence diagram ─────────────────────────────────

    /**
     * Restituisce l'id della raccolta nel contesto di un portfolio.
     * Il parametro idPortfolio è presente per rispecchiare la firma
     * dei sequence diagram; non modifica il risultato.
     */
    public UUID getIdRaccolta(UUID idPortfolio) { return idRaccolta; }

    // ── getter aggiuntivi ─────────────────────────────────────────────────────

    /** Getter diretto, usato internamente dalle control senza contesto portfolio. */
    public UUID   getIdRaccolta()  { return idRaccolta; }
    public String getNome()        { return nome; }
    public int    getOrdine()      { return ordine; }
    public UUID   getIdPortfolio() { return idPortfolio; }
}
