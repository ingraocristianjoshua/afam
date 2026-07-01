package com.afam.shared.entity;

import java.util.UUID;

/**
 * EntityRaccolta – DTO della raccolta lato client: raggruppa contenuti
 * all'interno di un portfolio (nome, ordine, riferimento al portfolio).
 */
public class EntityRaccolta {

    // ── Campi ──────────────────
    private UUID idRaccolta;
    private String nome;
    private int ordine;
    private UUID idPortfolio;

    // Getters and Setters omitted for brevity (stub)
}
