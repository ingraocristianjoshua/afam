package com.afam.shared.entity;

import java.util.UUID;

/**
 * EntityContenuto – DTO del contenuto lato client: file caricato dallo studente
 * (titolo, tipo, dimensione, percorso di storage, visibilità).
 */
public class EntityContenuto {

    // ── Campi ──────────────────
    private UUID idContenuto;
    private String titolo;
    private String tipoFile;
    private long dimensione;
    private String percorsoStorage;
    private String visibilita;
    private UUID idUtente;

    // Getters and Setters omitted for brevity (stub)
}
