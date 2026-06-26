package com.afam.server.control.gestisciportfolio;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.server.dao.DBMSBnd;

import java.util.UUID;

/**
 * Sequence: getIdPortfolio → getIdRaccolta(idPortfolio) → getIdContenuto(idRaccolta)
 *           → aggiornaStatoRaccolta(idRaccolta, idContenuto)
 *
 * aggiornaStatoRaccolta ha la stessa firma di RimuoviDallaRaccoltaCtrl (toggle nel DB).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AggiungiAllaRaccoltaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityPortfolio portfolio;
    private EntityRaccolta  raccolta;
    private EntityContenuto contenuto;

    public void setPortfolio(EntityPortfolio p) {
        this.portfolio = p;
        db.setCurrentPortfolio(p.getIdPortfolio());
    }

    public void setRaccolta(EntityRaccolta r)   { this.raccolta  = r; }
    public void setContenuto(EntityContenuto c) { this.contenuto = c; }

    public UUID getIdPortfolio() {
        return portfolio != null ? portfolio.getIdPortfolio() : null;
    }

    public UUID getIdRaccolta(UUID idPortfolio) {
        return raccolta != null ? raccolta.getIdRaccolta(idPortfolio) : null;
    }

    public UUID getIdContenuto(UUID idRaccolta) {
        return contenuto != null ? contenuto.getIdContenuto(idRaccolta) : null;
    }

    /** Toggle: se la coppia non esiste → INSERT; se esiste → DELETE (spec usa stessa firma). */
    public void aggiornaStatoRaccolta(UUID idRaccolta, UUID idContenuto) {
        db.aggiornaStatoRaccolta(idRaccolta, idContenuto);
    }
}
