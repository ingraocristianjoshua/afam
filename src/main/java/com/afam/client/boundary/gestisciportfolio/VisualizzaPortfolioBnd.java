package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessConfermaBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * VisualizzaPortfolioBnd – mostra il portfolio con i suoi contenuti e raccolte.
 * Permette di aggiungere/rimuovere contenuti e gestire le raccolte.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaPortfolioBnd {

    @FXML private Label labelNomePortfolio;
    @FXML private ListView<Map<String, Object>> listViewContenuti;
    @FXML private ListView<Map<String, Object>> listViewRaccolte;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> portfolio;

    public void setPortfolio(Map<String, Object> p) {
        this.portfolio = p;
        labelNomePortfolio.setText((String) p.get("nome"));
        caricaDati();
    }

    @FXML
    public void initialize() {
        listViewContenuti.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.get("titolo"));
            }
        });
        listViewRaccolte.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (String) item.get("nome"));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void caricaDati() {
        if (portfolio == null) return;
        try {
            Map<String, Object> resp = rest.get("portfolio/" + portfolio.get("idPortfolio"));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
            listViewContenuti.setItems(FXCollections.observableArrayList(contenuti));
            List<Map<String, Object>> raccolte = (List<Map<String, Object>>) data.get("raccolte");
            if (raccolte != null) {
                listViewRaccolte.setItems(FXCollections.observableArrayList(raccolte));
            }
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Errore nel caricamento: " + e.getMessage());
        }
    }

    @FXML
    public void onCreaRaccolta() {
        apri("/fxml/gestisciportfolio/CampoNomeRaccolta.fxml", "Nome raccolta");
    }

    @FXML
    public void onApriRaccolta() {
        Map<String, Object> sel = listViewRaccolte.getSelectionModel().getSelectedItem();
        if (sel == null) { MessErrBnd.create("Seleziona una raccolta."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/fxml/gestisciportfolio/VisualizzaRaccolta.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Raccolta " + sel.get("nome"));
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            VisualizzaRaccoltaBnd ctrl = loader.getController();
            ctrl.setRaccolta(portfolio, sel);
            stage.showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la raccolta: " + e.getMessage());
        }
    }

    @FXML
    public void onRinominaRaccolta() {
        Map<String, Object> sel = listViewRaccolte.getSelectionModel().getSelectedItem();
        if (sel == null) { MessErrBnd.create("Seleziona una raccolta da rinominare."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/CampoNomeRaccolta.fxml"));
            Stage stage = new Stage();
            stage.setTitle("AFAM – Rinomina raccolta");
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            CampoNomeRaccoltaBnd ctrl = loader.getController();
            ctrl.setRaccoltaDaRinominare(portfolio, sel);
            stage.showAndWait();
            caricaDati();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    @FXML
    public void onEliminaRaccolta() {
        Map<String, Object> sel = listViewRaccolte.getSelectionModel().getSelectedItem();
        if (sel == null) { MessErrBnd.create("Seleziona una raccolta da eliminare."); return; }
        if (!MessConfermaBnd.create("Eliminare la raccolta \"" + sel.get("nome") + "\"?\nI contenuti non verranno cancellati dal portfolio.")) return;
        try {
            rest.delete("portfolio/" + portfolio.get("idPortfolio") + "/raccolte/" + sel.get("idRaccolta"));
            MessSuccessoBnd.create("Raccolta eliminata con successo.");
            caricaDati();
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Eliminazione fallita: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unchecked")
    public void onAggiungiContenuto() {
        try {
            Map<String, Object> resp = rest.get("contenuti");
            List<Map<String, Object>> tuttiContenuti = (List<Map<String, Object>>) resp.get("contenuti");
            
            if (tuttiContenuti == null || tuttiContenuti.isEmpty()) {
                MessErrBnd.create("Non hai ancora caricato nessun contenuto.\nVai nella sezione 'Gestione contenuti' per caricarne.");
                return;
            }
            
            List<Map<String, Object>> presenti = listViewContenuti.getItems();
            List<Map<String, Object>> disponibili = tuttiContenuti.stream()
                .filter(c -> presenti.stream().noneMatch(p -> p.get("idContenuto").equals(c.get("idContenuto"))))
                .toList();
                
            if (disponibili.isEmpty()) {
                MessErrBnd.create("Tutti i tuoi contenuti sono già presenti in questo portfolio.");
                return;
            }
            
            List<String> titoli = disponibili.stream().map(m -> (String) m.get("titolo")).toList();
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(
                titoli.get(0), titoli
            );
            dialog.setTitle("Aggiungi Contenuto");
            dialog.setHeaderText("Seleziona il contenuto da aggiungere al portfolio:");
            dialog.setContentText("Contenuto:");
            
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String titoloScelto = result.get();
                Map<String, Object> scelto = disponibili.stream()
                        .filter(m -> titoloScelto.equals(m.get("titolo")))
                        .findFirst().orElse(null);
                if (scelto != null) {
                    rest.post("portfolio/" + portfolio.get("idPortfolio") + "/contenuti", 
                        Map.of("idContenuto", scelto.get("idContenuto")));
                    caricaDati();
                }
            }
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Associazione fallita: " + e.getMessage());
        }
    }

    @FXML
    public void onRimuoviContenuto() {
        Map<String, Object> sel = listViewContenuti.getSelectionModel().getSelectedItem();
        if (sel == null) { MessErrBnd.create("Seleziona un contenuto."); return; }
        if (!MessConfermaBnd.create("Rimuovere il contenuto dal portfolio?")) return;
        try {
            rest.delete("portfolio/" + portfolio.get("idPortfolio")
                    + "/contenuti/" + sel.get("idContenuto"));
            caricaDati();
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Rimozione fallita: " + e.getMessage());
        }
    }

    @FXML
    public void onSposta() {
        List<Map<String, Object>> lista = listViewContenuti.getItems();
        Map<String, Object> sel = listViewContenuti.getSelectionModel().getSelectedItem();
        if (sel == null) { MessErrBnd.create("Seleziona un contenuto da spostare."); return; }
        int idx = lista.indexOf(sel);
        if (idx < 0 || idx >= lista.size() - 1) { MessErrBnd.create("Impossibile spostare questo contenuto."); return; }
        Map<String, Object> adiacente = lista.get(idx + 1);
        try {
            rest.post("portfolio/" + portfolio.get("idPortfolio") + "/ordina",
                    Map.of("idContenuto1", sel.get("idContenuto"),
                           "idContenuto2", adiacente.get("idContenuto")));
            caricaDati();
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Spostamento fallito: " + e.getMessage());
        }
    }

    private void apri(String fxml, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            CampoNomeRaccoltaBnd ctrl = loader.getController();
            ctrl.setPortfolio(portfolio);
            stage.showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }
}
