package com.afam.client.boundary.gestiscicondivisione;

import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * FormScadenzaBnd – form per impostare o rimuovere la scadenza di un link.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class FormScadenzaBnd {

    @FXML private Label      labelToken;
    @FXML private Label      labelErrore;
    @FXML private DatePicker datePicker;
    @FXML private Button     btnSalva;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> link;
    private Runnable onSuccesso;

    public void setLink(Map<String, Object> l, Runnable onSuccesso) {
        this.link       = l;
        this.onSuccesso = onSuccesso;
        String token = (String) l.getOrDefault("urlToken", "");
        labelToken.setText("Token: " + (token.length() > 20 ? token.substring(0, 20) + "…" : token));
        Object scad = l.get("scadenza");
        if (scad instanceof String s && !s.isBlank()) {
            try { datePicker.setValue(OffsetDateTime.parse(s).toLocalDate()); }
            catch (Exception ignored) {}
        }
    }

    public void setLink(Map<String, Object> l) { setLink(l, null); }

    @FXML
    public void onSalva() {
        if (link == null) return;
        LocalDate data = datePicker.getValue();
        if (data != null && !data.isAfter(LocalDate.now())) {
            mostraErrore("La data deve essere almeno domani.");
            return;
        }
        nascondErrore();
        btnSalva.setDisable(true);
        btnSalva.setText("Salvataggio…");

        String scadenzaStr = data != null
                ? data.atStartOfDay().atOffset(ZoneOffset.UTC).toString()
                : null;

        new Thread(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("scadenza", scadenzaStr);
                rest.patch("condivisione/links/" + link.get("idLink") + "/scadenza", body);
                Platform.runLater(() -> {
                    if (onSuccesso != null) onSuccesso.run();
                    chiudi();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> {
                    btnSalva.setDisable(false);
                    btnSalva.setText("SALVA");
                    mostraErrore("Errore: " + e.getMessage());
                });
            }
        }, "imposta-scadenza").start();
    }

    @FXML
    public void onAnnulla() { chiudi(); }

    private void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    private void nascondErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    private void chiudi() {
        ((Stage) btnSalva.getScene().getWindow()).close();
    }
}
