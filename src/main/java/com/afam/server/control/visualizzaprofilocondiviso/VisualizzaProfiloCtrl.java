package com.afam.server.control.visualizzaprofilocondiviso;

import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityUtente;
import com.afam.server.dao.DBMSBnd;

import java.util.List;
import java.util.UUID;

/**
 * Sequence: recuperaInfoProfilo(idUtente) → recuperaElencoPortfoli(idUtente)
 *
 * Espone solo le informazioni pubbliche dello studente (no hashPassword).
 * I portfolio restituiti sono quelli con visibilità pubblica o condivisa.
 */
public class VisualizzaProfiloCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private EntityUtente utente;

    // ── Metodi ──────────────────
    public EntityUtente recuperaInfoProfilo(UUID idUtente) {
        utente = db.recuperaInfoProfilo(idUtente);
        return utente;
    }

    /** Recupera elenco portfoli. */
    public List<EntityPortfolio> recuperaElencoPortfoli(UUID idUtente) {
        return db.recuperaElencoPortfoli(idUtente);
    }

    /** Restituisce utente. */
    public EntityUtente getUtente() { return utente; }
}
