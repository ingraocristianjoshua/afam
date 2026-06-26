package com.afam.client.boundary.gestiscicadutaconnessione;

import com.afam.client.boundary.dialog.MessAnnBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.rest.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * GestisciCadutaBnd – boundary client-side per la gestione della caduta di connessione.
 *
 * Wrapper da usare attorno alle chiamate REST nelle boundary:
 *
 *   GestisciCadutaBnd.getInstance().esegui(() -> rest.post("..."), datiSospesi)
 *
 * Flusso quando isConnectionError():
 *   1. Salva datiSospesi nella coda locale
 *   2. Notifica l'utente con MessAnnBnd
 *   3. Avvia polling periodico verso GET /api/sessione/stato
 *   4. Quando la connessione torna → invia POST /api/sessione/ripristina
 *   5. Notifica il ripristino
 *
 * Flusso normale (nessun errore): trasparente, restituisce il risultato.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class GestisciCadutaBnd {

    private static final Logger LOG = Logger.getLogger(GestisciCadutaBnd.class.getName());

    private static final GestisciCadutaBnd INSTANCE = new GestisciCadutaBnd();
    public  static GestisciCadutaBnd getInstance() { return INSTANCE; }

    private final RestClient rest = RestClient.getInstance();

    /** Coda locale di operazioni sospese (dati da ripristinare). */
    private final List<Map<String, Object>> codaLocale = new ArrayList<>();

    private ScheduledFuture<?> pollingTask;
    private boolean             inRipristino = false;

    private GestisciCadutaBnd() {}

    // ── API pubblica ──────────────────────────────────────────────────────────

    /**
     * Esegue una chiamata REST; in caso di caduta di connessione, gestisce
     * il salvataggio locale e avvia il tentativo di ripristino.
     *
     * @param chiamata    la REST call da eseguire (lambda)
     * @param datiSospesi payload da salvare se la chiamata fallisce per connessione
     * @return risultato della chiamata, o null se offline
     */
    public Map<String, Object> esegui(Supplier<Map<String, Object>> chiamata,
                                       Map<String, Object> datiSospesi) {
        try {
            return chiamata.get();
        } catch (RestClient.RestException e) {
            if (e.isConnectionError()) {
                gestisciCaduta(datiSospesi);
                return null;
            }
            throw e;
        }
    }

    /** Tenta manualmente il ripristino (es. da bottone "Riprova"). */
    public void tentaRipristinoManuale() {
        tentaRipristino();
    }

    public boolean isOffline()      { return inRipristino; }
    public int     dimensioneCoda() { return codaLocale.size(); }

    // ── Logica interna ────────────────────────────────────────────────────────

    private void gestisciCaduta(Map<String, Object> dati) {
        if (dati != null) {
            codaLocale.add(dati);
            // Informa il server di salvare localmente (se raggiungibile in futuro)
        }
        LOG.warning("Caduta di connessione rilevata. Dati in coda: " + codaLocale.size());
        inRipristino = true;

        // Notifica utente (su JavaFX thread)
        javafx.application.Platform.runLater(() ->
                MessAnnBnd.create("Connessione al server persa. "
                        + "Le operazioni verranno ripristinate automaticamente quando disponibile."));

        avviaPolling();
    }

    private void avviaPolling() {
        if (pollingTask != null && !pollingTask.isDone()) return;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "afam-reconnect-poll");
            t.setDaemon(true);
            return t;
        });
        pollingTask = scheduler.scheduleWithFixedDelay(this::tentaRipristino, 5, 10, TimeUnit.SECONDS);
    }

    private void tentaRipristino() {
        try {
            Map<String, Object> stato = rest.get("sessione/stato");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) stato.get("data");
            if (Boolean.TRUE.equals(data.get("connesso"))) {
                // Invia le operazioni in coda
                for (Map<String, Object> op : new ArrayList<>(codaLocale)) {
                    try {
                        rest.post("sessione/salva-locale", op);
                    } catch (Exception ignored) {}
                }
                // Richiede ripristino al server
                rest.post("sessione/ripristina", Map.of());
                codaLocale.clear();
                inRipristino = false;
                if (pollingTask != null) pollingTask.cancel(false);
                LOG.info("Connessione ripristinata.");
                javafx.application.Platform.runLater(() ->
                        MessAnnBnd.create("Connessione ripristinata. Operazioni sincronizzate."));
            }
        } catch (RestClient.RestException e) {
            // Ancora offline — il polling continua silenziosamente
            LOG.fine("Server ancora irraggiungibile: " + e.getMessage());
        } catch (Exception e) {
            LOG.warning("Errore durante il tentativo di ripristino: " + e.getMessage());
        }
    }
}
