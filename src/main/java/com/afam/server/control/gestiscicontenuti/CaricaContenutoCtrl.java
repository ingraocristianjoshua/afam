package com.afam.server.control.gestiscicontenuti;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;
import com.afam.utils.Validators;

import java.util.UUID;

/**
 * Sequence: verificaDimensione(dimensione) → isFileInUso(contenuto)
 *           → checkValid → generaIdContenuto → caricaContenuto(contenuto)
 * @author Cristian Joshua Ingrao (0780672)
 */
public class CaricaContenutoCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private boolean valid = true;
    private String  errorMessage = "";

    private String titolo;
    private String tipoFile;
    private long   dimensione;
    private String percorsoStorage;

    public void setTitolo(String titolo)               { this.titolo          = titolo; }
    public void setTipoFile(String tipoFile)           { this.tipoFile        = tipoFile; }
    public void setDimensione(long dimensione)         { this.dimensione      = dimensione; }
    public void setPercorsoStorage(String percorso)    { this.percorsoStorage = percorso; }

    /** Verifica che il file non superi la dimensione massima consentita (50 MB). */
    public boolean verificaDimensione(long dimensione) {
        if (!Validators.isDimensioneValida(dimensione)) {
            return fail("File troppo grande. Dimensione massima: "
                    + (Constants.MAX_FILE_SIZE_BYTES / 1_048_576) + " MB.");
        }
        return true;
    }

    /** Verifica che il percorso di storage non sia già in uso (contenuto duplicato). */
    public boolean isFileInUso(EntityContenuto contenuto) {
        if (db.isFileInUso(contenuto)) {
            return fail("Un contenuto con lo stesso file è già presente.");
        }
        return false;
    }

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    public UUID generaIdContenuto() {
        return UUID.randomUUID();
    }

    /**
     * Persiste il contenuto nel DB tramite DBMSBnd.
     * L'entity deve già avere tutti i campi impostati (incluso idContenuto).
     */
    public void caricaContenuto(EntityContenuto contenuto) {
        db.caricaContenuto(contenuto);
    }

    public boolean isValid()         { return valid; }
    public String  getErrorMessage() { return errorMessage; }

    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
