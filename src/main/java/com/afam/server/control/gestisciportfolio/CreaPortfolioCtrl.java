package com.afam.server.control.gestisciportfolio;

import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.Map;
import java.util.UUID;

/**
 * Sequence: getNomePortfolio → verificaNomePortfolio → isNomeInUso(data.nomePortfolio)
 *           → checkValid → generaIdPortfolio → salvaPortfolio(idPortfolio, nomePortfolio)
 */
public class CreaPortfolioCtrl {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    private boolean valid = true;
    private String  errorMessage = "";
    private String  nomePortfolio;
    private UUID    idPortfolio;

    // ── Metodi ──────────────────
    public String getNomePortfolio() { return nomePortfolio; }

    /** Imposta nome portfolio. */
    public void setNomePortfolio(String nome) { this.nomePortfolio = nome; }

    /** Verifica nome portfolio. */
    public boolean verificaNomePortfolio(String nomePortfolio) {
        if (!Validators.isNomeRaccoltaValido(nomePortfolio)) {
            return fail("Nome portfolio non valido (1–80 caratteri alfanumerici).");
        }
        return true;
    }

    /** Controlla se esiste già un portfolio con questo nome per l'utente corrente. */
    public boolean isNomeInUso(Map<String, Object> data) {
        if (db.isNomeInUso(data)) return fail("Hai già un portfolio con questo nome.");
        return false;
    }

    /** Check valid. */
    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /** Genera id portfolio. */
    public UUID generaIdPortfolio() {
        idPortfolio = UUID.randomUUID();
        return idPortfolio;
    }

    /** Salva portfolio. */
    public void salvaPortfolio(UUID idPortfolio, String nomePortfolio) {
        db.salvaPortfolio(idPortfolio, nomePortfolio);
    }

    /** Indica se valid. */
    public boolean isValid()         { return valid; }
    /** Restituisce error message. */
    public String  getErrorMessage() { return errorMessage; }

    /** Fail. */
    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
