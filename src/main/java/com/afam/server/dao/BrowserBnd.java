package com.afam.server.dao;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Logger;

/**
 * BrowserBnd – Façade verso il browser di sistema.
 * Usato da AccediTramiteLinkCtrl per aprire il portfolio condiviso
 * nel browser predefinito del sistema operativo.
 */
public class BrowserBnd {

    // ── Campi ──────────────────
    private static final Logger LOG = Logger.getLogger(BrowserBnd.class.getName());

    private static volatile BrowserBnd instance;

    // ── Metodi ──────────────────
    public static BrowserBnd getInstance() {
        if (instance == null) {
            synchronized (BrowserBnd.class) {
                if (instance == null) instance = new BrowserBnd();
            }
        }
        return instance;
    }

    // ── Costruttori ──────────────────
    private BrowserBnd() {}

    /**
     * Apre il browser di sistema per visualizzare il portfolio condiviso.
     * Chiamato da AccediTramiteLinkCtrl dopo aver validato il link.
     *
     * @param url URL completo da aprire (es. http://localhost:8080/api/condiviso/{token}).
     *            Se null o vuoto, apre la home page del sistema.
     */
    public void richiediBrowser(String url) {
        String target = (url != null && !url.isBlank()) ? url : "http://localhost:8080/api/";
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(target));
                LOG.info("Browser aperto su: " + target);
            } else {
                // Fallback via Runtime per sistemi headless
                Runtime.getRuntime().exec(new String[]{"xdg-open", target});
                LOG.info("Browser aperto via xdg-open su: " + target);
            }
        } catch (Exception e) {
            LOG.warning("Impossibile aprire il browser: " + e.getMessage());
        }
    }

    /**
     * Overload senza parametri: apre il browser sulla home page AFAM.
     * Firma richiesta dai sequence diagram di AccediTramiteLinkCtrl.
     */
    public void richiediBrowser() {
        richiediBrowser(null);
    }
}
