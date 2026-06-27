package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessConfermaBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * VisualizzaRaccoltaBnd – mostra i contenuti di una raccolta con azioni inline.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class VisualizzaRaccoltaBnd {

    @FXML private TextField fieldNomeRaccolta;
    @FXML private VBox      boxContenuti;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> portfolio;
    private Map<String, Object> raccolta;

    public void setRaccolta(Map<String, Object> portfolio, Map<String, Object> raccolta) {
        this.portfolio = portfolio;
        this.raccolta  = raccolta;
        fieldNomeRaccolta.setText("Titolo raccolta: " + raccolta.get("nome"));
        caricaDati();
    }

    @SuppressWarnings("unchecked")
    private void caricaDati() {
        try {
            Map<String, Object> resp = rest.get(
                    "portfolio/" + portfolio.get("idPortfolio") +
                    "/raccolte/" + raccolta.get("idRaccolta"));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
            popolaContenuti(contenuti);
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Errore nel caricamento: " + e.getMessage());
        }
    }

    private void popolaContenuti(List<Map<String, Object>> contenuti) {
        boxContenuti.getChildren().removeIf(n -> n instanceof HBox || (n instanceof Label l && !"Contenuti".equals(l.getText())));

        if (contenuti == null || contenuti.isEmpty()) {
            Label vuoto = new Label("Nessun contenuto in questa raccolta.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
            boxContenuti.getChildren().add(vuoto);
            return;
        }
        for (int i = 0; i < contenuti.size(); i++) {
            boxContenuti.getChildren().add(creaRiga(contenuti, i));
        }
    }

    private HBox creaRiga(List<Map<String, Object>> lista, int i) {
        Map<String, Object> c = lista.get(i);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #ede9fe; -fx-background-radius: 8; -fx-padding: 8 12;");

        Label icona = new Label(iconaPerTipo((String) c.getOrDefault("tipoFile", "")));
        icona.setStyle("-fx-font-size: 18px;");

        VBox info = new VBox(2);
        Label titolo = new Label((String) c.getOrDefault("titolo", ""));
        titolo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");
        Label sottotitolo = new Label(c.getOrDefault("tipoFile", "") + "");
        sottotitolo.setStyle("-fx-font-size: 11px; -fx-text-fill: #9879e0;");
        info.getChildren().addAll(titolo, sottotitolo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSu  = bottone("↑", "btn-chip-purple");
        Button btnGiu = bottone("↓", "btn-chip-purple");
        btnSu.setTooltip(new javafx.scene.control.Tooltip("Sposta su"));
        btnGiu.setTooltip(new javafx.scene.control.Tooltip("Sposta giù"));
        Button btnRim = bottone("RIMUOVI DALLA RACCOLTA", "btn-chip-red");

        final int idx = i;
        btnSu.setOnAction(e  -> sposta(lista, idx, -1));
        btnGiu.setOnAction(e -> sposta(lista, idx,  1));
        btnRim.setOnAction(e -> rimuoviDallaRaccolta(c));

        row.getChildren().addAll(icona, info, spacer, btnSu, btnGiu, btnRim);
        return row;
    }

    private String iconaPerTipo(String tipo) {
        if (tipo == null) return "📄";
        return switch (tipo.toLowerCase()) {
            case "video", "mp4", "avi", "mov" -> "📹";
            case "audio", "mp3", "wav", "flac" -> "🎵";
            default -> "📄";
        };
    }

    private Button bottone(String testo, String classeColore) {
        Button b = new Button(testo);
        b.getStyleClass().addAll("btn-chip", classeColore);
        return b;
    }

    // ── Azioni ────────────────────────────────────────────────────────────────

    @FXML
    public void onRinomina() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/CampoNomeRaccolta.fxml"));
            Stage stage = nuovoStage("Rinomina raccolta", loader.load());
            CampoNomeRaccoltaBnd ctrl = loader.getController();
            ctrl.setRaccoltaDaRinominare(portfolio, raccolta);
            stage.showAndWait();
            caricaDati();
            fieldNomeRaccolta.setText("Titolo raccolta: " + raccolta.get("nome"));
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unchecked")
    public void onAggiungiAllaRaccolta() {
        try {
            Map<String, Object> resp = rest.get("portfolio/" + portfolio.get("idPortfolio"));
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> tuttiContenuti = (List<Map<String, Object>>) data.get("contenuti");

            if (tuttiContenuti == null || tuttiContenuti.isEmpty()) {
                MessErrBnd.create("Il portfolio non contiene contenuti.\nAggiungi prima contenuti al portfolio."); return;
            }
            List<String> titoli = tuttiContenuti.stream().map(m -> (String) m.get("titolo")).toList();
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(titoli.get(0), titoli);
            dialog.setTitle("Aggiungi alla raccolta");
            dialog.setHeaderText("Seleziona il contenuto da aggiungere a questa raccolta:");
            dialog.setContentText("Contenuto:");
            dialog.showAndWait().ifPresent(t -> {
                tuttiContenuti.stream().filter(m -> t.equals(m.get("titolo"))).findFirst().ifPresent(c -> {
                    try {
                        rest.post("portfolio/" + portfolio.get("idPortfolio") +
                                  "/raccolte/" + raccolta.get("idRaccolta") + "/contenuti",
                                  Map.of("idContenuto", c.get("idContenuto")));
                        MessSuccessoBnd.create("Contenuto aggiunto alla raccolta con successo.");
                        caricaDati();
                    } catch (RestClient.RestException e) {
                        MessErrBnd.create("Aggiunta fallita: " + e.getMessage());
                    }
                });
            });
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Errore: " + e.getMessage());
        }
    }

    private void rimuoviDallaRaccolta(Map<String, Object> contenuto) {
        if (!MessConfermaBnd.create("Rimuovere \"" + contenuto.get("titolo") + "\" dalla raccolta?")) return;
        try {
            rest.post("portfolio/" + portfolio.get("idPortfolio") +
                      "/raccolte/" + raccolta.get("idRaccolta") + "/contenuti",
                      Map.of("idContenuto", contenuto.get("idContenuto")));
            caricaDati();
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Rimozione fallita: " + e.getMessage());
        }
    }

    private void sposta(List<Map<String, Object>> lista, int idx, int delta) {
        int target = idx + delta;
        if (target < 0) {
            MessErrBnd.create("Il contenuto è già in cima: non può essere spostato più su.");
            return;
        }
        if (target >= lista.size()) {
            MessErrBnd.create("Il contenuto è già in fondo: non può essere spostato più giù.");
            return;
        }
        Map<String, Object> c1 = lista.get(idx);
        Map<String, Object> c2 = lista.get(target);
        try {
            rest.post("portfolio/" + portfolio.get("idPortfolio") + "/ordina",
                    Map.of("idContenuto1", c1.get("idContenuto"),
                           "idContenuto2", c2.get("idContenuto")));
            caricaDati();
        } catch (RestClient.RestException e) {
            MessErrBnd.create("Spostamento fallito: " + e.getMessage());
        }
    }

    @FXML
    public void chiudi() {
        Stage stage = (Stage) fieldNomeRaccolta.getScene().getWindow();
        stage.close();
    }

    private Stage nuovoStage(String titolo, javafx.scene.Parent root) {
        Stage stage = new Stage();
        stage.setTitle("AFAM – " + titolo);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }
}
