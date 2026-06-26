package com.afam.server.control.visualizzaprofilocondiviso;

import com.afam.entities.EntityUtente;
import com.afam.server.dao.DBMSBnd;

import java.util.List;

/**
 * Sequence: recuperaElencoStudenti(nomeUtente) → (restituisce lista studenti)
 * Overload senza parametro restituisce tutti gli studenti (browsing pubblico).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class RicercaStudenteCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    /** Cerca studenti il cui nome o cognome contiene la stringa fornita. */
    public List<EntityUtente> recuperaElencoStudenti(String nomeUtente) {
        if (nomeUtente == null || nomeUtente.isBlank()) {
            return recuperaElencoStudenti();
        }
        return db.recuperaElencoStudenti(nomeUtente);
    }

    /** Restituisce tutti gli studenti (lista completa). */
    public List<EntityUtente> recuperaElencoStudenti() {
        return db.recuperaElencoStudenti();
    }
}
