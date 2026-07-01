package com.afam.shared.entity;

import java.util.UUID;

/**
 * EntityUtente – DTO dell'utente lato client (oggetto entity dell'analisi):
 * dati anagrafici, credenziali, stato validazioni, 2FA e sessione.
 */
public class EntityUtente {

    // ── Campi ──────────────────
    private UUID idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private String numeroTelefono;
    private int emailValidata;
    private int numeroValidato;
    private int stato2fa;
    private String statoSessione;

    // ── Metodi ──────────────────
    public UUID getIdUtente() { return idUtente; }
    /** Imposta id utente. */
    public void setIdUtente(UUID idUtente) { this.idUtente = idUtente; }

    /** Restituisce nome. */
    public String getNome() { return nome; }
    /** Imposta nome. */
    public void setNome(String nome) { this.nome = nome; }

    /** Restituisce cognome. */
    public String getCognome() { return cognome; }
    /** Imposta cognome. */
    public void setCognome(String cognome) { this.cognome = cognome; }

    /** Restituisce email. */
    public String getEmail() { return email; }
    /** Imposta email. */
    public void setEmail(String email) { this.email = email; }

    /** Restituisce password. */
    public String getPassword() { return password; }
    /** Imposta password. */
    public void setPassword(String password) { this.password = password; }

    /** Restituisce numero telefono. */
    public String getNumeroTelefono() { return numeroTelefono; }
    /** Imposta numero telefono. */
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    /** Restituisce email validata. */
    public int getEmailValidata() { return emailValidata; }
    /** Imposta email validata. */
    public void setEmailValidata(int emailValidata) { this.emailValidata = emailValidata; }

    /** Restituisce numero validato. */
    public int getNumeroValidato() { return numeroValidato; }
    /** Imposta numero validato. */
    public void setNumeroValidato(int numeroValidato) { this.numeroValidato = numeroValidato; }

    /** Restituisce stato2fa. */
    public int getStato2fa() { return stato2fa; }
    /** Imposta stato2fa. */
    public void setStato2fa(int stato2fa) { this.stato2fa = stato2fa; }

    /** Restituisce stato sessione. */
    public String getStatoSessione() { return statoSessione; }
    /** Imposta stato sessione. */
    public void setStatoSessione(String statoSessione) { this.statoSessione = statoSessione; }
}
