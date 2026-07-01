package com.afam.client.boundary.gestisciportfolio;

import com.afam.client.boundary.dialog.MessConfermaBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * GestionePortfolioBnd – schermata unica per gestire il portfolio.
 * Mostra raccolte e contenuti del portfolio selezionato con azioni inline
 * per ogni riga (come da mockup).
 */
public class GestionePortfolioBnd {

    // ── Campi ──────────────────
    @FXML private VBox      panelVuoto;
    @FXML private VBox      panelPortfolio;
    @FXML private VBox      boxPortfolios;
    @FXML private Label     labelPortfolioSel;
    @FXML private Label     labelVisualizzazioni;
    @FXML private VBox      boxRaccolte;
    @FXML private VBox      boxContenuti;

    private final RestClient rest = RestClient.getInstance();
    private Map<String, Object> portfolioCorrente;
    private final List<Map<String, Object>> portfolios = new java.util.ArrayList<>();

    // ── Metodi ──────────────────
    @FXML
    public void initialize() {
        new Thread(() -> caricaListaPortfolio(null), "carica-portfolio").start();
    }

    // ── Caricamento ───────────────────────────────────────────────────────────

    /**
     * Carica l'elenco dei portfolio, lo mostra come lista verticale e
     * seleziona quello indicato (o il primo), caricandone il dettaglio.
     */
    @SuppressWarnings("unchecked")
    private void caricaListaPortfolio(String idDaSelezionare) {
        try {
            Map<String, Object> resp = rest.get("portfolio");
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("portfolios");
            Platform.runLater(() -> {
                portfolios.clear();
                if (lista == null || lista.isEmpty()) {
                    boxPortfolios.getChildren().clear();
                    portfolioCorrente = null;
                    mostraVuoto();
                    return;
                }
                portfolios.addAll(lista);

                Map<String, Object> target = null;
                if (idDaSelezionare != null) {
                    target = lista.stream()
                            .filter(p -> idDaSelezionare.equals(p.get("idPortfolio")))
                            .findFirst().orElse(null);
                }
                if (target == null) target = lista.get(0);

                String selId = (String) target.get("idPortfolio");
                renderListaPortfolios(selId);

                final Map<String, Object> sel = target;
                new Thread(() -> apriPortfolio(sel), "apri-portfolio-init").start();
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> {
                MessErrBnd.create("Impossibile caricare i portfolio: " + e.getMessage());
                mostraVuoto();
            });
        }
    }

    /** Disegna la lista verticale dei portfolio, evidenziando quello selezionato. */
    private void renderListaPortfolios(String selId) {
        boxPortfolios.getChildren().clear();
        for (Map<String, Object> p : portfolios) {
            boxPortfolios.getChildren().add(creaRigaPortfolio(p, p.get("idPortfolio").equals(selId)));
        }
    }

    /** Crea riga portfolio. */
    private HBox creaRigaPortfolio(Map<String, Object> p, boolean selezionato) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (selezionato ? "#ede9fe" : "#f5f3ff") + ";"
                + " -fx-background-radius: 10; -fx-padding: 10 14;"
                + (selezionato ? " -fx-border-color: #6c3fc5; -fx-border-width: 2; -fx-border-radius: 10;" : ""));

        Label icona = new Label("🗂");
        icona.setStyle("-fx-font-size: 18px;");

        Label nome = new Label((String) p.getOrDefault("nome", ""));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (selezionato) {
            Label badge = new Label("● selezionato");
            badge.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c3fc5; -fx-font-weight: bold;");
            row.getChildren().addAll(icona, nome, spacer, badge);
        } else {
            Button btnApri = bottone("APRI", "btn-chip-green");
            btnApri.setOnAction(e -> onSelezionaPortfolio(p));
            row.getChildren().addAll(icona, nome, spacer, btnApri);
        }

        Button btnEl = bottone("ELIMINA", "btn-chip-red");
        btnEl.setOnAction(e -> onEliminaPortfolio(p));
        row.getChildren().add(btnEl);

        // L'intera riga è cliccabile per selezionare il portfolio
        row.setOnMouseClicked(e -> { if (!selezionato) onSelezionaPortfolio(p); });
        return row;
    }

    /** Gestisce l'azione «Seleziona Portfolio». */
    private void onSelezionaPortfolio(Map<String, Object> p) {
        renderListaPortfolios((String) p.get("idPortfolio"));
        new Thread(() -> apriPortfolio(p), "apri-portfolio-sel").start();
    }

    /** Apri portfolio. */
    @SuppressWarnings("unchecked")
    private void apriPortfolio(Map<String, Object> portfolio) {
        try {
            String id = (String) portfolio.get("idPortfolio");
            Map<String, Object> resp = rest.get("portfolio/" + id);
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            Map<String, Object> pf       = (Map<String, Object>) data.get("portfolio");
            List<Map<String, Object>> raccolte  = (List<Map<String, Object>>) data.get("raccolte");
            List<Map<String, Object>> contenuti = (List<Map<String, Object>>) data.get("contenuti");
            Platform.runLater(() -> {
                portfolioCorrente = pf;
                if (labelPortfolioSel != null)
                    labelPortfolioSel.setText("Portfolio selezionato: " + pf.getOrDefault("nome", ""));
                if (labelVisualizzazioni != null) {
                    int vis = ((Number) pf.getOrDefault("numeroVisualizzazioni", 0)).intValue();
                    labelVisualizzazioni.setText("👁 " + vis + (vis == 1 ? " visualizzazione" : " visualizzazioni"));
                }
                popolaRaccolte(raccolte);
                popolaContenuti(contenuti);
                mostraPortfolio();
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> MessErrBnd.create("Errore nel caricamento del portfolio: " + e.getMessage()));
        }
    }

    /** Aggiorna dati. */
    private void aggiornaDati() {
        if (portfolioCorrente == null) return;
        new Thread(() -> apriPortfolio(portfolioCorrente), "aggiorna-portfolio").start();
    }

    // ── Popolamento righe Raccolte ────────────────────────────────────────────

    private void popolaRaccolte(List<Map<String, Object>> raccolte) {
        boxRaccolte.getChildren().clear();
        if (raccolte == null || raccolte.isEmpty()) {
            Label vuoto = new Label("Nessuna raccolta creata.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
            boxRaccolte.getChildren().add(vuoto);
            return;
        }
        for (Map<String, Object> r : raccolte) {
            boxRaccolte.getChildren().add(creaRigaRaccolta(r));
        }
    }

    /** Crea riga raccolta. */
    private HBox creaRigaRaccolta(Map<String, Object> r) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 8; -fx-padding: 8 12;");

        Label icona = new Label("📁");
        icona.setStyle("-fx-font-size: 18px;");

        Label nome = new Label((String) r.get("nome"));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVis = bottone("VISUALIZZA RACCOLTA", "btn-chip-green");
        btnVis.setOnAction(e -> onVisualizzaRaccolta(r));

        Button btnRin = bottone("RINOMINA RACCOLTA", "btn-chip-orange");
        btnRin.setOnAction(e -> onRinominaRaccolta(r));

        Button btnEl = bottone("ELIMINA RACCOLTA", "btn-chip-red");
        btnEl.setOnAction(e -> onEliminaRaccolta(r));

        row.getChildren().addAll(icona, nome, spacer, btnVis, btnRin, btnEl);
        return row;
    }

    // ── Popolamento righe Contenuti ───────────────────────────────────────────

    private void popolaContenuti(List<Map<String, Object>> contenuti) {
        boxContenuti.getChildren().clear();
        if (contenuti == null || contenuti.isEmpty()) {
            Label vuoto = new Label("Nessun contenuto nel portfolio.");
            vuoto.setStyle("-fx-text-fill: #9879e0; -fx-font-size: 13px;");
            boxContenuti.getChildren().add(vuoto);
            return;
        }
        for (int i = 0; i < contenuti.size(); i++) {
            boxContenuti.getChildren().add(creaRigaContenuto(contenuti, i));
        }
    }

    /** Crea riga contenuto. */
    private HBox creaRigaContenuto(List<Map<String, Object>> lista, int i) {
        Map<String, Object> c = lista.get(i);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f5f3ff; -fx-background-radius: 8; -fx-padding: 8 12;");

        String tipo = (String) c.getOrDefault("tipoFile", "");
        Label icona = new Label(iconaPerTipo(tipo));
        icona.setStyle("-fx-font-size: 18px;");

        Label nome = new Label((String) c.getOrDefault("titolo", ""));
        nome.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3d1a78;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSu   = bottone("↑", "btn-chip-purple");
        Button btnGiu  = bottone("↓", "btn-chip-purple");
        btnSu.setTooltip(new javafx.scene.control.Tooltip("Sposta su"));
        btnGiu.setTooltip(new javafx.scene.control.Tooltip("Sposta giù"));
        Button btnAgg  = bottone("AGGIUNGI ALLA RACCOLTA", "btn-chip-orange");
        Button btnRim  = bottone("RIMUOVI CONTENUTO",      "btn-chip-red");

        final int idx = i;
        btnSu.setOnAction(e  -> onSpostaSu(lista, idx));
        btnGiu.setOnAction(e -> onSpostaGiu(lista, idx));
        btnAgg.setOnAction(e -> onAggiungiAllaRaccolta(c));
        btnRim.setOnAction(e -> onRimuoviContenuto(c));

        row.getChildren().addAll(icona, nome, spacer, btnSu, btnGiu, btnAgg, btnRim);
        return row;
    }

    /** Icona per tipo. */
    private String iconaPerTipo(String tipo) {
        if (tipo == null) return "📄";
        return switch (tipo.toLowerCase()) {
            case "video", "mp4", "avi", "mov" -> "📹";
            case "audio", "mp3", "wav", "flac" -> "🎵";
            case "pdf", "doc", "docx" -> "📄";
            default -> "📄";
        };
    }

    /** Bottone. */
    private Button bottone(String testo, String classeColore) {
        Button b = new Button(testo);
        b.getStyleClass().addAll("btn-chip", classeColore);
        return b;
    }

    // ── Azioni Portfolio ──────────────────────────────────────────────────────

    @FXML
    public void onCreaPortfolio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/CampoNomePortfolio.fxml"));
            Stage stage = nuovoStage("Crea portfolio", loader.load());
            CampoNomePortfolioBnd ctrl = loader.getController();
            stage.showAndWait();
            // ricarica e seleziona il portfolio appena creato (se creato)
            String nuovoId = ctrl.getIdCreato();
            new Thread(() -> caricaListaPortfolio(nuovoId), "ricarica-dopo-crea").start();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    /** Gestisce l'azione «Elimina Portfolio». */
    private void onEliminaPortfolio(Map<String, Object> portfolio) {
        if (portfolio == null) return;
        String nome = (String) portfolio.get("nome");
        if (!MessConfermaBnd.create("Eliminare il portfolio \"" + nome + "\"?\nTutti i contenuti verranno rimossi dal portfolio.")) return;
        Object idP = portfolio.get("idPortfolio");
        new Thread(() -> {
            try {
                rest.delete("portfolio/" + idP);
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Portfolio eliminato con successo.");
                    portfolioCorrente = null;
                    new Thread(() -> caricaListaPortfolio(null), "ricarica-dopo-elimina").start();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Eliminazione fallita: " + e.getMessage()));
            }
        }, "elimina-portfolio").start();
    }

    /** Gestisce l'azione «Annulla». */
    @FXML
    public void onAnnulla() {
        vai("/fxml/homepage/HomePage.fxml", "Home");
    }

    // ── Azioni Raccolte ───────────────────────────────────────────────────────

    @FXML
    public void onCreaRaccolta() {
        if (portfolioCorrente == null) { MessErrBnd.create("Seleziona prima un portfolio."); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/CampoNomeRaccolta.fxml"));
            Stage stage = nuovoStage("Crea raccolta", loader.load());
            CampoNomeRaccoltaBnd ctrl = loader.getController();
            ctrl.setPortfolio(portfolioCorrente);
            stage.showAndWait();
            aggiornaDati();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    /** Gestisce l'azione «Visualizza Raccolta». */
    private void onVisualizzaRaccolta(Map<String, Object> raccolta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/VisualizzaRaccolta.fxml"));
            Stage stage = nuovoStage("Raccolta – " + raccolta.get("nome"), loader.load());
            VisualizzaRaccoltaBnd ctrl = loader.getController();
            ctrl.setRaccolta(portfolioCorrente, raccolta);
            stage.showAndWait();
            aggiornaDati();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la raccolta: " + e.getMessage());
        }
    }

    /** Gestisce l'azione «Rinomina Raccolta». */
    private void onRinominaRaccolta(Map<String, Object> raccolta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/CampoNomeRaccolta.fxml"));
            Stage stage = nuovoStage("Rinomina raccolta", loader.load());
            CampoNomeRaccoltaBnd ctrl = loader.getController();
            ctrl.setRaccoltaDaRinominare(portfolioCorrente, raccolta);
            stage.showAndWait();
            aggiornaDati();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    /** Gestisce l'azione «Elimina Raccolta». */
    private void onEliminaRaccolta(Map<String, Object> raccolta) {
        String nome = (String) raccolta.get("nome");
        if (!MessConfermaBnd.create("Eliminare la raccolta \"" + nome + "\"?\nI contenuti non verranno cancellati dal portfolio.")) return;
        String url = "portfolio/" + portfolioCorrente.get("idPortfolio") + "/raccolte/" + raccolta.get("idRaccolta");
        new Thread(() -> {
            try {
                rest.delete(url);
                Platform.runLater(() -> {
                    MessSuccessoBnd.create("Raccolta eliminata con successo.");
                    aggiornaDati();
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Eliminazione fallita: " + e.getMessage()));
            }
        }, "elimina-raccolta").start();
    }

    // ── Azioni Contenuti ──────────────────────────────────────────────────────

    @FXML
    @SuppressWarnings("unchecked")
    public void onAggiungiContenuto() {
        if (portfolioCorrente == null) return;
        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.get("contenuti");
                List<Map<String, Object>> tutti;
                Object raw = resp.get("contenuti");
                if (raw == null) {
                    Map<String, Object> data = (Map<String, Object>) resp.get("data");
                    tutti = data != null ? (List<Map<String, Object>>) data.get("contenuti") : null;
                } else {
                    tutti = (List<Map<String, Object>>) raw;
                }
                final List<Map<String, Object>> rawTutti = tutti;
                Platform.runLater(() -> {
                    if (rawTutti == null || rawTutti.isEmpty()) {
                        MessErrBnd.create("Non hai ancora caricato nessun contenuto.\nVai nella sezione 'Gestione contenuti' per caricarne.");
                        return;
                    }
                    // Solo contenuti NON privati: i privati non sono aggiungibili al portfolio
                    List<Map<String, Object>> visibili = rawTutti.stream()
                            .filter(m -> !"privato".equalsIgnoreCase(
                                    String.valueOf(m.getOrDefault("visibilita", "pubblico"))))
                            .toList();
                    if (visibili.isEmpty()) {
                        MessErrBnd.create("Tutti i tuoi contenuti sono privati.\nImposta un contenuto come pubblico in 'Gestisci Contenuti' per poterlo aggiungere al portfolio.");
                        return;
                    }
                    // Boundary di selezione (come da sequence diagram: InsiemeContenutiBnd)
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/InsiemeContenuti.fxml"));
                        Stage stage = nuovoStage("Aggiungi contenuto", loader.load());
                        InsiemeContenutiBnd ctrl = loader.getController();
                        ctrl.setContenuti(visibili, scelto -> {
                            String url = "portfolio/" + portfolioCorrente.get("idPortfolio") + "/contenuti";
                            Map<String, Object> body = Map.of("idContenuto", scelto.get("idContenuto"));
                            new Thread(() -> {
                                try {
                                    rest.post(url, body);
                                    Platform.runLater(this::aggiornaDati);
                                } catch (RestClient.RestException e) {
                                    Platform.runLater(() -> MessErrBnd.create("Aggiunta fallita: " + e.getMessage()));
                                }
                            }, "aggiungi-contenuto").start();
                        });
                        stage.showAndWait();
                    } catch (Exception e) {
                        MessErrBnd.create("Impossibile aprire: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Impossibile caricare i contenuti: " + e.getMessage()));
            }
        }, "carica-contenuti-portfolio").start();
    }

    /** Gestisce l'azione «Aggiungi Alla Raccolta». */
    private void onAggiungiAllaRaccolta(Map<String, Object> contenuto) {
        new Thread(() -> {
            try {
                Map<String, Object> resp = rest.get("portfolio/" + portfolioCorrente.get("idPortfolio"));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> raccolte = (List<Map<String, Object>>) data.get("raccolte");
                Platform.runLater(() -> {
                    if (raccolte == null || raccolte.isEmpty()) {
                        MessErrBnd.create("Non hai ancora creato nessuna raccolta."); return;
                    }
                    // Boundary di selezione (come da sequence diagram: SelezioneRaccoltaBnd)
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gestisciportfolio/SelezioneRaccolta.fxml"));
                        Stage stage = nuovoStage("Aggiungi alla raccolta", loader.load());
                        SelezioneRaccoltaBnd ctrl = loader.getController();
                        ctrl.setRaccolte((String) contenuto.getOrDefault("titolo", ""), raccolte, r -> {
                            String url = "portfolio/" + portfolioCorrente.get("idPortfolio") +
                                         "/raccolte/" + r.get("idRaccolta") + "/contenuti";
                            Map<String, Object> body = Map.of("idContenuto", contenuto.get("idContenuto"));
                            new Thread(() -> {
                                try {
                                    rest.post(url, body);
                                    Platform.runLater(() -> MessSuccessoBnd.create("Contenuto aggiunto alla raccolta con successo."));
                                } catch (RestClient.RestException e) {
                                    Platform.runLater(() -> MessErrBnd.create("Aggiunta alla raccolta fallita: " + e.getMessage()));
                                }
                            }, "aggiungi-a-raccolta").start();
                        });
                        stage.showAndWait();
                    } catch (Exception e) {
                        MessErrBnd.create("Impossibile aprire: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Errore: " + e.getMessage()));
            }
        }, "carica-raccolte").start();
    }

    /** Gestisce l'azione «Rimuovi Contenuto». */
    private void onRimuoviContenuto(Map<String, Object> contenuto) {
        if (!MessConfermaBnd.create("Rimuovere \"" + contenuto.get("titolo") + "\" dal portfolio?")) return;
        String url = "portfolio/" + portfolioCorrente.get("idPortfolio") + "/contenuti/" + contenuto.get("idContenuto");
        new Thread(() -> {
            try {
                rest.delete(url);
                Platform.runLater(this::aggiornaDati);
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Rimozione fallita: " + e.getMessage()));
            }
        }, "rimuovi-contenuto").start();
    }

    /** Gestisce l'azione «Sposta Su». */
    private void onSpostaSu(List<Map<String, Object>> lista, int idx) {
        if (idx <= 0) {
            MessErrBnd.create("Il contenuto è già in cima: non può essere spostato più su.");
            return;
        }
        scambiaPosizione(lista.get(idx), lista.get(idx - 1));
    }

    /** Gestisce l'azione «Sposta Giu». */
    private void onSpostaGiu(List<Map<String, Object>> lista, int idx) {
        if (idx >= lista.size() - 1) {
            MessErrBnd.create("Il contenuto è già in fondo: non può essere spostato più giù.");
            return;
        }
        scambiaPosizione(lista.get(idx), lista.get(idx + 1));
    }

    /** Scambia posizione. */
    private void scambiaPosizione(Map<String, Object> c1, Map<String, Object> c2) {
        String url = "portfolio/" + portfolioCorrente.get("idPortfolio") + "/ordina";
        Map<String, Object> body = Map.of(
                "idContenuto1", c1.get("idContenuto"),
                "idContenuto2", c2.get("idContenuto"));
        new Thread(() -> {
            try {
                rest.post(url, body);
                Platform.runLater(this::aggiornaDati);
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Spostamento fallito: " + e.getMessage()));
            }
        }, "sposta-contenuto").start();
    }

    // ── Navigazione ───────────────────────────────────────────────────────────

    @FXML
    public void chiudi() {
        javafx.scene.Node anchor = panelPortfolio.getScene() != null ? panelPortfolio : panelVuoto;
        ((Stage) anchor.getScene().getWindow()).close();
    }

    /** Mostra vuoto. */
    private void mostraVuoto() {
        panelVuoto.setVisible(true);
        panelVuoto.setManaged(true);
        panelPortfolio.setVisible(false);
        panelPortfolio.setManaged(false);
    }

    /** Mostra portfolio. */
    private void mostraPortfolio() {
        panelVuoto.setVisible(false);
        panelVuoto.setManaged(false);
        panelPortfolio.setVisible(true);
        panelPortfolio.setManaged(true);
    }

    /** Apri. */
    private void apri(String fxml, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            nuovoStage(titolo, loader.load()).showAndWait();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire: " + e.getMessage());
        }
    }

    /** Vai. */
    private void vai(String fxml, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = nuovoStage(titolo, loader.load());
            stage.show();
            chiudi();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile navigare: " + e.getMessage());
        }
    }

    /** Nuovo stage. */
    private Stage nuovoStage(String titolo, javafx.scene.Parent root) {
        Stage stage = new Stage();
        stage.setTitle("AFAM – " + titolo);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        stage.setScene(scene);
        return stage;
    }
}
