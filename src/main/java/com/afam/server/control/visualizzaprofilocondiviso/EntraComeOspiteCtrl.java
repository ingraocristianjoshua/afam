package com.afam.server.control.visualizzaprofilocondiviso;

/**
 * Sequence: (entry point senza autenticazione) → (permette navigazione pubblica)
 *
 * Questa control verifica che l'accesso anonimo sia permesso per l'area pubblica.
 * Non richiede connessione al DB: il sistema AFAM consente sempre la navigazione
 * pubblica (ricerca studenti e visualizzazione portfolio pubblici) senza login.
 */
public class EntraComeOspiteCtrl {

    // ── Campi ──────────────────
    private static final boolean OSPITE_PERMESSO = true;

    // ── Metodi ──────────────────
    /**
     * Verifica se l'accesso come ospite è consentito.
     * Estendibile in futuro per configurare restrizioni all'accesso anonimo.
     */
    public boolean verificaAccessoOspite() {
        return OSPITE_PERMESSO;
    }

    /**
     * Restituisce l'identità "ospite" per il tracciamento delle sessioni anonime.
     * In questa implementazione non persiste alcun dato.
     */
    public String getIdOspite() {
        return "ospite-" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}
