package com.afam.server.control.gestiscicadutaconnessione;

import com.afam.server.dao.DBMSBnd;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Control coordinatore della caduta di connessione al DB.
 *
 * Sequence principale (caduta):
 *   verificaStatoConnessione → [falso] → salvaDatiLocali(data)
 *   → interrompiSalvataggio → notificaErrore(messaggio)
 *
 * Sequence ripristino:
 *   verificaStatoConnessione → [vero] → eseguiRipristino
 *
 * Non contiene logica di business: orchestra le control specializzate.
 */
public class GestisciCadutaDiConnessioneCtrl {

    // ── Campi ──────────────────
    private static final Logger LOG = Logger.getLogger(
            GestisciCadutaDiConnessioneCtrl.class.getName());

    private final DBMSBnd               db          = DBMSBnd.getInstance();
    private final SalvataggioLocaleCtrl salvataggio = new SalvataggioLocaleCtrl();
    private final EseguiRollbackDatiCtrl rollbackCtrl = new EseguiRollbackDatiCtrl();
    private final NotificaErrCtrl        notificaCtrl = new NotificaErrCtrl();
    private final RipristinaDatiSessioneCtrl ripristinaCtrl = new RipristinaDatiSessioneCtrl();

    // ── Metodi ──────────────────
    /** @return true se la connessione al DB è attiva. */
    public boolean verificaStatoConnessione() {
        return db.verificaStatoConnessione();
    }

    /**
     * Salva l'operazione in sospeso nella coda locale.
     * Chiamato dal server quando rileva la caduta mentre un'operazione è in corso.
     */
    public void salvaDatiLocali(Map<String, Object> data) {
        salvataggio.salvaLocale(data);
        LOG.warning("Dati salvati localmente: " + data);
    }

    /**
     * Esegue il rollback della transazione corrente e notifica l'errore.
     * @return messaggio di errore formattato per il client.
     */
    public String gestisciCaduta() {
        rollbackCtrl.interrompiSalvataggio();
        rollbackCtrl.rollback(rollbackCtrl.recuperaOperazioniParziali());
        String errDB = notificaCtrl.recuperaMessErrore();
        return notificaCtrl.notificaErrore(errDB.isBlank() ? "Connessione al database persa." : errDB);
    }

    /**
     * Tenta il ripristino: verifica la connessione e invia le operazioni sospese.
     * @return numero di operazioni ripristinate, o -1 se ancora offline.
     */
    public int tentaRipristino() {
        return ripristinaCtrl.eseguiRipristino();
    }

    /** Has dati locali. */
    public boolean hasDatiLocali() { return !salvataggio.isCodaVuota(); }
    /** Dimensione coda. */
    public int     dimensioneCoda() { return salvataggio.dimensioneCoda(); }
}
