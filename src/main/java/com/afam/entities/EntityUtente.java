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
public class EntityUtente {

    // ── Campi ──────────────────
    private UUID   idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String hashPassword;
    private String numeroTelefono;
    private String dataNascita;
    private boolean emailValidata;
    private boolean numeroValidato;
    private boolean stato2FA;
    /** "aperta" | "chiusa" */
    private String statoSessione;

    // ── Costruttori ──────────────────
    /** Costruttore no-arg richiesto da Jackson per la deserializzazione JSON. */
    public EntityUtente() {}

    public EntityUtente(UUID idUtente, String nome, String cognome,
                        String email, String hashPassword,
                        String numeroTelefono, String dataNascita,
                        boolean emailValidata, boolean numeroValidato,
                        boolean stato2FA, String statoSessione) {
        this.idUtente        = idUtente;
        this.nome            = nome;
        this.cognome         = cognome;
        this.email           = email;
        this.hashPassword    = hashPassword;
        this.numeroTelefono  = numeroTelefono;
        this.dataNascita     = dataNascita;
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
    /** Restituisce cognome. */
    public String  getCognome()       { return cognome; }
    /** Restituisce email. */
    public String  getEmail()         { return email; }
    /** Restituisce hash password. */
    public String  getHashPassword()  { return hashPassword; }
    /** Restituisce data nascita. */
    public String  getDataNascita()   { return dataNascita; }
    /** Indica se email validata. */
    public boolean isEmailValidata()  { return emailValidata; }
    /** Indica se numero validato. */
    public boolean isNumeroValidato() { return numeroValidato; }
    /** Indica se stato2 fa. */
    public boolean isStato2FA()       { return stato2FA; }
    /** Restituisce stato sessione. */
    public String  getStatoSessione() { return statoSessione; }
}
