package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → recuperaContenuto(idContenuto) → recuperaPosizione(idPortfolio, contenuto)
 *           → recuperaPosizioneAdiacente(idPortfolio, contenuto) → aggiornaPosizione(c1, c2)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class OrdinaSequenzaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityContenuto contenuto1;
    private EntityContenuto contenuto2;

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public void setContenuto1(EntityContenuto c) { this.contenuto1 = c; }
    public void setContenuto2(EntityContenuto c) { this.contenuto2 = c; }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    /** Carica il primo contenuto dal DB (o restituisce quello già impostato). */
    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        contenuto1 = db.recuperaContenuto(idContenuto);
        return contenuto1;
    }

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

    public EntityContenuto getContenuto1() { return contenuto1; }
    public EntityContenuto getContenuto2() { return contenuto2; }
}
