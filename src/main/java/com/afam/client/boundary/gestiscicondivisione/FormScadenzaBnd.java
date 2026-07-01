package com.afam.client.boundary.gestiscicondivisione;

import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * FormScadenzaBnd – form per impostare o rimuovere la scadenza di un link.
 */
public class FormScadenzaBnd {

    // ── Campi ──────────────────
    @FXML private Label      labelToken;
    @FXML private Label      labelErrore;
    @FXML private DatePicker datePicker;
    @FXML private Button     btnSalva;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> link;
    private Runnable onSuccesso;

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        datePicker.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d)   { return d != null ? FMT.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                try { return (s != null && !s.isBlank()) ? LocalDate.parse(s, FMT) : null; }
                catch (Exception e) { return null; }
            }
        });
        datePicker.setPromptText("gg/mm/aaaa");
    }

    /** Imposta link. */
    public void setLink(Map<String, Object> l, Runnable onSuccesso) {
        this.link       = l;
        this.onSuccesso = onSuccesso;
        String idLink = String.valueOf(l.getOrDefault("idLink", ""));
        labelToken.setText("Link: " + (idLink.length() > 20 ? idLink.substring(0, 20) + "…" : idLink));
        Object scad = l.get("scadenza");
        if (scad instanceof String s && !s.isBlank()) {
            try { datePicker.setValue(OffsetDateTime.parse(s).toLocalDate()); }
            catch (Exception ignored) {}
        }
    }

    /** Imposta link. */
    public void setLink(Map<String, Object> l) { setLink(l, null); }

    /** Gestisce l'azione «Salva». */
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

    /** Gestisce l'azione «Annulla». */
    @FXML
    public void onAnnulla() { chiudi(); }

    /** Mostra il messaggio di errore indicato. */
    private void mostraErrore(String msg) {
        labelErrore.setText(msg);
        labelErrore.setVisible(true);
        labelErrore.setManaged(true);
    }

    /** Nasconde il messaggio di errore. */
    private void nascondErrore() {
        labelErrore.setVisible(false);
        labelErrore.setManaged(false);
    }

    /** Chiude la finestra corrente. */
    private void chiudi() {
        ((Stage) btnSalva.getScene().getWindow()).close();
    }
}
