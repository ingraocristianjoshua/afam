package com.afam.shared.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EntityLink – DTO del link di condivisione lato client: id del link (anche
 * identificatore condivisibile), stato, visibilità, scadenza e flag di accesso aperto.
 */
public class EntityLink {

    // ── Campi ──────────────────
    private UUID idLink;
    private LocalDateTime scadenza;
    private String stato;
    private int flagAperto;
    private UUID idUtente;

    // Getters and Setters omitted for brevity (stub)
}
