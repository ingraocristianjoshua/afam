package com.afam.server.control.gestiscicadutaconnessione;

import com.afam.server.dao.DBMSBnd;

/**
 * Sequence: recuperaMessErrore → notificaErrore(messaggio)
 *
 * Formatta il messaggio di errore da trasmettere al boundary client tramite
 * la risposta REST. La "notifica" in questo contesto è il payload JSON di errore;
 * la visualizzazione finale spetta al boundary (MessErrBnd côté client).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class NotificaErrCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private String messaggioCorrente = "";

    /** Recupera il messaggio di errore più recente dal DB. */
    public String recuperaMessErrore() {
        messaggioCorrente = db.recuperaMessErrore();
        return messaggioCorrente != null ? messaggioCorrente : "";
    }

    /**
     * Prepara il messaggio da includere nella risposta REST.
     * Il boundary client mostrerà questo messaggio in MessErrBnd.
     */
    public String notificaErrore(String messaggio) {
        this.messaggioCorrente = messaggio;
        return "Errore di connessione: " + messaggio;
    }

    public String getMessaggioCorrente() { return messaggioCorrente; }
}
