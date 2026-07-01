package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.UUID;

/**
 * Entity che rappresenta un file/contenuto caricato da uno studente.
 *
 * I sequence diagram specificano tre varianti di getIdContenuto:
 *   – getIdContenuto()            chiamata senza contesto (es. AggiungiContenutoCtrl)
 *   – getIdContenuto(idPortfolio) chiamata nel contesto di un portfolio
 *                                  (es. RimuoviContenutoCtrl)
 *   – getIdContenuto(idRaccolta)  chiamata nel contesto di una raccolta
 *                                  (es. RimuoviDallaRaccoltaCtrl)
 *
 * In Java, idPortfolio e idRaccolta sono entrambi UUID, quindi
 * le varianti "con portfolio" e "con raccolta" collassano a un'unica
 * firma getIdContenuto(UUID contextId).
 * Il valore restituito è sempre this.idContenuto: il parametro
 * di contesto è presente perché i sequence diagram distinguono
 * il punto di chiamata, non perché influenzi il risultato.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class EntityContenuto {

    // ── Campi ──────────────────
    private UUID   idContenuto;
    private String titolo;
    private String tipoFile;
    private long   dimensione;      // in byte
    private String percorsoStorage;
    /** "privato" | "pubblico" | "condiviso" */
    private String visibilita;
    private UUID   idUtente;

    // ── Costruttori ──────────────────
    public EntityContenuto() {}

    public EntityContenuto(UUID idContenuto, String titolo, String tipoFile,
                           long dimensione, String percorsoStorage,
                           String visibilita, UUID idUtente) {
        this.idContenuto     = idContenuto;
        this.titolo          = titolo;
        this.tipoFile        = tipoFile;
        this.dimensione      = dimensione;
        this.percorsoStorage = percorsoStorage;
        this.visibilita      = visibilita;
        this.idUtente        = idUtente;
    }

    // ── getter richiesti dai sequence diagram ──────────────────────────────────

    /** Variante senza contesto. */
    public UUID getIdContenuto() { return idContenuto; }

    /**
     * Variante contestuale: accetta sia idPortfolio sia idRaccolta.
     * I sequence diagram le nominano diversamente, ma in Java condividono
     * la stessa firma (UUID → UUID); il parametro non altera il risultato.
     */
    public UUID getIdContenuto(UUID contextId) { return idContenuto; }

    // ── getter aggiuntivi per le control ──────────────────────────────────────

    public String getTitolo()          { return titolo; }
    /** Restituisce tipo file. */
    public String getTipoFile()        { return tipoFile; }
    /** Restituisce dimensione. */
    public long   getDimensione()      { return dimensione; }
    /** Restituisce percorso storage. */
    public String getPercorsoStorage() { return percorsoStorage; }
    /** Restituisce visibilita. */
    public String getVisibilita()      { return visibilita; }
    /** Restituisce id utente. */
    public UUID   getIdUtente()        { return idUtente; }
}
