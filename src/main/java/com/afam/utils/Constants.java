package com.afam.utils;

/**
 * Costanti globali del sistema AFAM.
 * Tutti i valori numerici e testuali fissi vivono qui;
 * i parametri di connessione vengono caricati a runtime da config.properties.
 */
public final class Constants {

    // ── Costruttori ──────────────────
    private Constants() {}

    // ── File upload ───────────────────────────────────────────────────────────
    /** Dimensione massima consentita per un contenuto: 50 MB in byte. */
    public static final long MAX_FILE_SIZE_BYTES = 52_428_800L;

    // ── OTP ───────────────────────────────────────────────────────────────────
    /** Durata di validità di un OTP in minuti (1 ora). */
    public static final int OTP_DURATION_MINUTES = 60;
    /** Lunghezza del codice OTP (cifre). */
    public static final int OTP_LENGTH = 6;

    // ── Link di condivisione ──────────────────────────────────────────────────
    /** Prefisso URL per i link condivisi (personalizzabile in config). */
    public static final String LINK_BASE_URL = "http://localhost:8080/api/share/";

    // ── Sessione ──────────────────────────────────────────────────────────────
    public static final String SESSIONE_APERTA  = "aperta";
    public static final String SESSIONE_CHIUSA  = "chiusa";

    // ── Visibilità contenuto ──────────────────────────────────────────────────
    public static final String VIS_PRIVATO    = "privato";
    public static final String VIS_PUBBLICO   = "pubblico";
    public static final String VIS_CONDIVISO  = "condiviso";

    // ── Stato link ────────────────────────────────────────────────────────────
    public static final String LINK_ATTIVO   = "attivo";
    public static final String LINK_REVOCATO = "revocato";
    public static final String LINK_SCADUTO  = "scaduto";

    // ── Server ────────────────────────────────────────────────────────────────
    public static final String SERVER_BASE_URI = "http://localhost:8080/api/";
}
