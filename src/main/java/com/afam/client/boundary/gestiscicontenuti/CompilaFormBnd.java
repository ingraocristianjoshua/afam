package com.afam.client.boundary.gestiscicontenuti;

import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import com.afam.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;

/**
 * CompilaFormBnd – form per il caricamento di un nuovo contenuto.
 * L'utente seleziona un file locale; il client legge i metadati e li invia al server.
 * Il file fisico resta localmente (o è gestito da un sistema di storage esterno).
 * @author Cristian Joshua Ingrao (0780672)
 */
public class CompilaFormBnd {

    @FXML private TextField        fieldTitolo;
    @FXML private TextField        labelFile;
    @FXML private ComboBox<String> comboVisibilita;

    private final RestClient rest = RestClient.getInstance();
    private File fileScelto;

    @FXML
    public void initialize() {
        comboVisibilita.getItems().addAll(
                Constants.VIS_PRIVATO, Constants.VIS_PUBBLICO);
        comboVisibilita.setValue(Constants.VIS_PRIVATO);
    }

    @FXML
    public void onSfoglia() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona un file");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("File supportati",
                        "*.pdf", "*.mp3", "*.mp4", "*.jpg", "*.jpeg", "*.png",
                        "*.doc", "*.docx", "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*"));
        File f = fc.showOpenDialog(labelFile.getScene().getWindow());
        if (f != null) {
            fileScelto = f;
            labelFile.setText(f.getName() + " (" + (f.length() / 1024) + " KB)");
        }
    }

    @FXML
    public void onCarica() {
        if (fileScelto == null) { MessErrBnd.create("Seleziona un file."); return; }
        String titolo = fieldTitolo.getText().trim();
        if (titolo.isEmpty()) { MessErrBnd.create("Inserisci un titolo."); return; }

        String estensione = fileScelto.getName().contains(".")
                ? fileScelto.getName().substring(fileScelto.getName().lastIndexOf('.') + 1)
                : "bin";

        try {
            rest.post("contenuti", Map.of(
                    "titolo",           titolo,
                    "tipoFile",         estensione,
                    "dimensione",       fileScelto.length(),
                    "percorsoStorage",  fileScelto.getAbsolutePath(),
                    "visibilita",       comboVisibilita.getValue()
            ));
            MessSuccessoBnd.create("Contenuto \"" + titolo + "\" caricato.");
            chiudi();
        } catch (RestClient.RestException e) {
            MessErrBnd.create(e.getMessage());
        }
    }

    @FXML
    public void onAnnulla() { chiudi(); }

    private void chiudi() {
        Stage stage = (Stage) fieldTitolo.getScene().getWindow();
        stage.close();
    }
}
