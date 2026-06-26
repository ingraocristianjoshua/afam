package com.afam.server.control.gestiscicadutaconnessione;

import com.afam.server.dao.DBMSBnd;

import java.util.List;

/**
 * Sequence: recuperaOperazioniParziali → interrompiSalvataggio → rollback(operazioniParziali)
 *
 * Esegue il rollback delle operazioni parziali causate dalla caduta di connessione.
 * interrompiSalvataggio agisce sulla transazione JDBC attiva (se presente).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class EseguiRollbackDatiCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    /** Recupera la lista delle operazioni non completate (dal DB o da stato interno). */
    public List<String> recuperaOperazioniParziali() {
        return db.recuperaOperazioniParziali();
    }

    /** Annulla immediatamente qualsiasi transazione aperta sulla connessione JDBC. */
    public void interrompiSalvataggio() {
        db.interrompiSalvataggio();
    }

    /** Annulla le operazioni parziali indicate. */
    public void rollback(List<String> operazioniParziali) {
        db.rollback(operazioniParziali);
    }
}
