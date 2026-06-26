package com.afam.shared.entity;

import java.util.UUID;

public class EntityUtente {
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

    public UUID getIdUtente() { return idUtente; }
    public void setIdUtente(UUID idUtente) { this.idUtente = idUtente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNumeroTelefono() { return numeroTelefono; }
    public void setNumeroTelefono(String numeroTelefono) { this.numeroTelefono = numeroTelefono; }

    public int getEmailValidata() { return emailValidata; }
    public void setEmailValidata(int emailValidata) { this.emailValidata = emailValidata; }

    public int getNumeroValidato() { return numeroValidato; }
    public void setNumeroValidato(int numeroValidato) { this.numeroValidato = numeroValidato; }

    public int getStato2fa() { return stato2fa; }
    public void setStato2fa(int stato2fa) { this.stato2fa = stato2fa; }

    public String getStatoSessione() { return statoSessione; }
    public void setStatoSessione(String statoSessione) { this.statoSessione = statoSessione; }
}
