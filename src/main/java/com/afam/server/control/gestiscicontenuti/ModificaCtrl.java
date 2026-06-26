package com.afam.server.control.gestiscicontenuti;

import com.afam.entities.EntityContenuto;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Validators;

import java.util.Map;
import java.util.UUID;

/**
 * Sequence: getIdContenuto → recuperaContenuto(idContenuto)
 *           → verificaNome(titolo) → isNomeInUso(data.nome) → checkValid
 *           → aggiornaDati(contenuto)
 *
 * verificaNome = verifica formato (regex)
 * isNomeInUso  = verifica unicità per l'utente corrente (DB)
 * Mantenuti distinti come da spec (non unificati).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class ModificaCtrl {

    private final DBMSBnd db = DBMSBnd.getInstance();
    private boolean valid = true;
    private String  errorMessage = "";

    private EntityContenuto contenuto;
    private String          nuovoTitolo;

    public void setContenuto(EntityContenuto c)  { this.contenuto    = c; }
    public void setNuovoTitolo(String titolo)     { this.nuovoTitolo = titolo; }

    public UUID getIdContenuto() {
        return contenuto != null ? contenuto.getIdContenuto() : null;
    }

    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        contenuto = db.recuperaContenuto(idContenuto);
        return contenuto;
    }

    /**
     * Verifica il formato del titolo già impostato via setNuovoTitolo (regex — non accede al DB).
     * Accetta anche il titolo come parametro per compatibilità con la sequenza della spec.
     */
    public boolean verificaNome(String titolo) {
        this.nuovoTitolo = titolo;
        if (titolo == null || titolo.isBlank() || !Validators.isNomeRaccoltaValido(nuovoTitolo)) {
            return fail("Titolo non valido (1–80 caratteri alfanumerici).");
        }
        return true;
    }

    /**
     * Verifica l'unicità del titolo per l'utente corrente (accede al DB).
     * data deve contenere la chiave "nome" con il titolo da verificare.
     */
    public boolean isNomeInUso(Map<String, Object> data) {
        if (db.isNomeInUso(data)) {
            return fail("Hai già un contenuto con questo titolo.");
        }
        return false;
    }

    public boolean checkValid() {
        if (!valid) throw new IllegalStateException(errorMessage);
        return true;
    }

    /**
     * Aggiorna i metadati del contenuto nel DB.
     * Il contenuto passato deve già avere tutti i campi aggiornati.
     */
    public void aggiornaDati(EntityContenuto contenuto) {
        db.aggiornaDati(contenuto);
    }

    public EntityContenuto getContenuto() { return contenuto; }
    public boolean isValid()              { return valid; }
    public String  getErrorMessage()      { return errorMessage; }

    private boolean fail(String msg) { valid = false; errorMessage = msg; return false; }
}
