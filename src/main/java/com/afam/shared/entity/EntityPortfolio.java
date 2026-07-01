package com.afam.shared.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EntityPortfolio – DTO del portfolio lato client: nome, data di creazione,
 * numero di visualizzazioni e riferimento all'utente proprietario.
 */
public class EntityPortfolio {

    // ── Campi ──────────────────
    private UUID idPortfolio;
    private String nome;
    private UUID idUtente;
    private LocalDateTime dataCreazione;
    private int numeroVisualizzazioni;

    // Getters and Setters omitted for brevity (stub)
}
