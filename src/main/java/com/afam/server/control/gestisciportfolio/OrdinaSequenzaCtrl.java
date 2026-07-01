package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → recuperaContenuto(idContenuto) → recuperaPosizione(idPortfolio, contenuto)
 *           → recuperaPosizioneAdiacente(idPortfolio, contenuto) → aggiornaPosizione(c1, c2)
 */
public class OrdinaSequenzaCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityContenuto contenuto1;
    private EntityContenuto contenuto2;

    // ── Metodi ──────────────────
    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    /** Imposta contenuto1. */
    public void setContenuto1(EntityContenuto c) { this.contenuto1 = c; }
    /** Imposta contenuto2. */
    public void setContenuto2(EntityContenuto c) { this.contenuto2 = c; }

    /** Restituisce id portfolio. */
    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Carica il primo contenuto dal DB (o restituisce quello già impostato). */
    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        contenuto1 = db.recuperaContenuto(idContenuto);
        return contenuto1;
    }

    /** Recupera posizione. */
    public int recuperaPosizione(UUID idPortfolio, EntityContenuto contenuto) {
        return db.recuperaPosizione(idPortfolio, contenuto);
    }

    /** Restituisce la posizione del contenuto che segue (posizione+1). */
    public int recuperaPosizioneAdiacente(UUID idPortfolio, EntityContenuto contenuto) {
        return db.recuperaPosizioneAdiacente(idPortfolio, contenuto);
    }

    /** Scambia le posizioni dei due contenuti nella sequenza del portfolio. */
    public void aggiornaPosizione(EntityContenuto c1, EntityContenuto c2) {
        db.aggiornaPosizione(c1, c2);
    }

    /** Restituisce contenuto1. */
    public EntityContenuto getContenuto1() { return contenuto1; }
    /** Restituisce contenuto2. */
    public EntityContenuto getContenuto2() { return contenuto2; }
}
