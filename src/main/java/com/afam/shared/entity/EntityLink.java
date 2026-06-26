package com.afam.shared.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class EntityLink {
    private UUID idLink;
    private String urlToken;
    private LocalDateTime scadenza;
    private String stato;
    private int flagAperto;
    private UUID idUtente;

    // Getters and Setters omitted for brevity (stub)
}
