package com.afam.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.UUID;

/**
 * Entity che rappresenta uno studente/utente AFAM.
 * Espone solo getter; la modifica dei dati avviene esclusivamente
 * tramite DBMSBnd (mai tramite setter su questa classe).
 * @JsonAutoDetect permette a Jackson di serializzare/deserializzare
 * i campi privati senza richiedere setter.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
/**
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EntityUtente {

    private UUID   idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String hashPassword;
    private String numeroTelefono;
    private boolean emailValidata;
    private boolean numeroValidato;
    private boolean stato2FA;
    /** "aperta" | "chiusa" */
    private String statoSessione;

    /** Costruttore no-arg richiesto da Jackson per la deserializzazione JSON. */
    public EntityUtente() {}

    public EntityUtente(UUID idUtente, String nome, String cognome,
                        String email, String hashPassword,
                        String numeroTelefono, boolean emailValidata,
                        boolean numeroValidato, boolean stato2FA,
                        String statoSessione) {
        this.idUtente        = idUtente;
        this.nome            = nome;
        this.cognome         = cognome;
        this.email           = email;
        this.hashPassword    = hashPassword;
        this.numeroTelefono  = numeroTelefono;
        this.emailValidata   = emailValidata;
        this.numeroValidato  = numeroValidato;
        this.stato2FA        = stato2FA;
        this.statoSessione   = statoSessione;
    }

    // ── getter richiesti dai sequence diagram ──────────────────────────────────

    /** Usato da tutte le control che devono passare l'id all'utente a DBMSBnd. */
    public UUID getIdUtente() { return idUtente; }

    /** Usato da Verifica2FACtrl e ValidaNumeroCtrl per recuperare il numero in memoria. */
    public String getNumTelefono() { return numeroTelefono; }

    // ── getter aggiuntivi per le control ──────────────────────────────────────

    public String  getNome()          { return nome; }
    public String  getCognome()       { return cognome; }
    public String  getEmail()         { return email; }
    public String  getHashPassword()  { return hashPassword; }
    public boolean isEmailValidata()  { return emailValidata; }
    public boolean isNumeroValidato() { return numeroValidato; }
    public boolean isStato2FA()       { return stato2FA; }
    public String  getStatoSessione() { return statoSessione; }
}
