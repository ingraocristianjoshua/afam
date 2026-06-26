package com.afam.utils;

import java.util.regex.Pattern;

/** Validazioni riusabili condivise tra tutte le control.
 * @author Cristian Joshua Ingrao (0780672)
 */
public final class Validators {

    private static final Pattern EMAIL_RE =
        Pattern.compile("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$");

    // Minimo 8 caratteri, almeno una maiuscola, una cifra, un simbolo
    private static final Pattern PASSWORD_RE =
        Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$");

    // Stringa non vuota, max 120 caratteri, solo lettere/spazi/trattini/apostrofi
    private static final Pattern NOME_RE =
        Pattern.compile("^[\\p{L} '\\-]{1,120}$");

    // Nome raccolta/portfolio: alfanumerico + spazi, 1–80 caratteri
    private static final Pattern NOME_RACCOLTA_RE =
        Pattern.compile("^[\\p{L}\\d '\\-_]{1,80}$");

    // Telefono: formato E.164 opzionale (+ e cifre) o solo cifre, 7–15 char
    private static final Pattern TELEFONO_RE =
        Pattern.compile("^\\+?[0-9]{7,15}$");

    private Validators() {}

    public static boolean isEmailValida(String email) {
        return email != null && EMAIL_RE.matcher(email.trim()).matches();
    }

    public static boolean isPasswordValida(String password) {
        return password != null && PASSWORD_RE.matcher(password).matches();
    }

    public static boolean isNomeValido(String nome) {
        return nome != null && NOME_RE.matcher(nome.trim()).matches();
    }

    public static boolean isNomeRaccoltaValido(String nome) {
        return nome != null && NOME_RACCOLTA_RE.matcher(nome.trim()).matches();
    }

    public static boolean isTelefonoValido(String numero) {
        return numero != null && TELEFONO_RE.matcher(numero.trim()).matches();
    }

    /** Controlla che il file non superi la dimensione massima consentita. */
    public static boolean isDimensioneValida(long dimensioneBytes) {
        return dimensioneBytes > 0 && dimensioneBytes <= Constants.MAX_FILE_SIZE_BYTES;
    }
}
