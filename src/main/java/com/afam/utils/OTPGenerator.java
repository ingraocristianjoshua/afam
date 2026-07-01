package com.afam.utils;

import java.security.SecureRandom;

/** Genera codici OTP numerici crittograficamente sicuri.
 */
public final class OTPGenerator {

    // ── Campi ──────────────────
    private static final SecureRandom RNG = new SecureRandom();

    // ── Costruttori ──────────────────
    private OTPGenerator() {}

    // ── Metodi ──────────────────
    /**
     * Restituisce un codice OTP numerico di {@link Constants#OTP_LENGTH} cifre,
     * con zeri iniziali dove necessario.
     */
    public static String genera() {
        int bound = (int) Math.pow(10, Constants.OTP_LENGTH);
        int code  = RNG.nextInt(bound);
        return String.format("%0" + Constants.OTP_LENGTH + "d", code);
    }
}
