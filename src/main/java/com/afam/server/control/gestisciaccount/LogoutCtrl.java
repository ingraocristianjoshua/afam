package com.afam.server.control.gestisciaccount;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;

/**
 * LogoutCtrl – invalida la sessione corrente.
 *
 * Sequence: recuperaStatoSessione → aggiornaStatoSessione('chiusa')
 *
 * Nota: recuperaStatoSessione è listato senza parentesi nel sequence diagram
 * (accesso a proprietà); in Java è un metodo che interroga il DB.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class LogoutCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();

    /**
     * Legge lo stato della sessione corrente dal DB.
     * @return "aperta" | "chiusa"
     */
    public String recuperaStatoSessione() {
        return db.recuperaStatoSessione();
    }

    /**
     * Imposta lo stato sessione a 'chiusa'.
     * @param stato valore da impostare (normalmente Constants.SESSIONE_CHIUSA).
     */
    public void aggiornaStatoSessione(String stato) {
        db.aggiornaStatoSessione(stato);
    }

    /** Convenience: esegue il logout completo in un solo passo. */
    public void eseguiLogout() {
        if (Constants.SESSIONE_APERTA.equals(recuperaStatoSessione())) {
            aggiornaStatoSessione(Constants.SESSIONE_CHIUSA);
        }
    }
}
