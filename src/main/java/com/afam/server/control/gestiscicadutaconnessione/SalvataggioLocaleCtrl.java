package com.afam.server.control.gestiscicadutaconnessione;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Raccoglie e conserva in memoria le operazioni in-flight durante una caduta
 * di connessione al DB.
 *
 * Sequence: raccogliDatiSessione → salvaLocale(data) → recuperaDatiLocali
 *
 * La coda è statica (condivisa nell'arco della vita del processo server)
 * perché deve sopravvivere alla reinstanziazione della control.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class SalvataggioLocaleCtrl {

    /** Coda thread-safe delle operazioni sospese (payload serializzabile come Map). */
    private static final List<Map<String, Object>> codaLocale = new CopyOnWriteArrayList<>();

    /**
     * Raccoglie i dati della sessione corrente (da passare poi a salvaLocale).
     * In questa implementazione restituisce uno snapshot della coda corrente.
     */
    public List<Map<String, Object>> raccogliDatiSessione() {
        return Collections.unmodifiableList(new ArrayList<>(codaLocale));
    }

    /** Aggiunge un'operazione alla coda locale. */
    public void salvaLocale(Map<String, Object> data) {
        if (data != null && !data.isEmpty()) {
            codaLocale.add(data);
        }
    }

    /** Restituisce tutte le operazioni locali salvate. */
    public List<Map<String, Object>> recuperaDatiLocali() {
        return Collections.unmodifiableList(new ArrayList<>(codaLocale));
    }

    /** Svuota la coda dopo un ripristino riuscito. */
    public void svuotaCoda() {
        codaLocale.clear();
    }

    public boolean isCodaVuota() { return codaLocale.isEmpty(); }
    public int     dimensioneCoda() { return codaLocale.size(); }
}
