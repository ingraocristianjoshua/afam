package com.afam.server.control.gestiscicadutaconnessione;

import com.afam.server.dao.DBMSBnd;

import java.util.List;
import java.util.Map;

/**
 * Sequence: verificaStatoConnessione → recuperaDatiLocali → inviaDatiAttuali(data)
 *           → svuotaCoda (se invio riuscito)
 *
 * Tentativo di ripristino della sessione: quando la connessione torna disponibile,
 * invia al DB le operazioni accumulate localmente da SalvataggioLocaleCtrl.
 */
public class RipristinaDatiSessioneCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd              db           = DBMSBnd.getInstance();
    private final SalvataggioLocaleCtrl salvataggio = new SalvataggioLocaleCtrl();

    // ── Metodi ──────────────────
    /** @return true se la connessione al DB è attiva. */
    public boolean verificaStatoConnessione() {
        return db.verificaStatoConnessione();
    }

    public List<Map<String, Object>> recuperaDatiLocali() {
        return salvataggio.recuperaDatiLocali();
    }

    /**
     * Invia le operazioni sospese al DB.
     * @return true se l'invio è riuscito.
     */
    public boolean inviaDatiAttuali(Map<String, Object> data) {
        try {
            db.inviaDatiAttuali(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sequenza completa di ripristino: verifica connessione → invia dati → svuota coda.
     * @return numero di operazioni ripristinate, o -1 se la connessione è ancora assente.
     */
    public int eseguiRipristino() {
        if (!verificaStatoConnessione()) return -1;
        List<Map<String, Object>> dati = recuperaDatiLocali();
        int ripristinate = 0;
        for (Map<String, Object> op : dati) {
            if (inviaDatiAttuali(op)) ripristinate++;
        }
        if (ripristinate == dati.size()) {
            salvataggio.svuotaCoda();
        }
        return ripristinate;
    }
}
