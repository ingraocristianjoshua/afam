package com.afam.client.boundary.gestiscicontenuti;

import com.afam.client.boundary.dialog.MessErrBnd;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * AnteprimaContenutoBnd – anteprima in-app per immagini, PDF, audio e video.
 * Documenti Office aprono l'app nativa tramite Desktop.open().
 * @author Cristian Joshua Ingrao (0780672)
 */
public class AnteprimaContenutoBnd {

    @FXML private StackPane areaAnteprima;
    @FXML private Label     labelTitolo;

    private static final Set<String> IMMAGINI  = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final Set<String> AUDIO     = Set.of("mp3", "wav", "ogg", "flac", "aac");
    private static final Set<String> VIDEO     = Set.of("mp4", "avi", "mov", "mkv", "webm");
    private static final Set<String> DOCUMENTI = Set.of("doc", "docx", "ppt", "pptx", "xls", "xlsx", "odt");

    private MediaPlayer mediaPlayer;

    public void setContenuto(Map<String, Object> c) {
        String titolo   = (String) c.getOrDefault("titolo", "Contenuto");
        String percorso = (String) c.get("percorsoStorage");
        String tipo     = ((String) c.getOrDefault("tipoFile", "")).toLowerCase();

        labelTitolo.setText(titolo);

        if (percorso == null || percorso.isBlank()) {
            mostraMessaggio("Percorso file non disponibile.");
            return;
        }

        File file = new File(percorso);
        if (!file.exists()) {
            mostraMessaggio("File non trovato:\n" + percorso);
            return;
        }

        String estensione = estensione(percorso);
        if (estensione.isEmpty()) estensione = tipo;

        try {
            if (IMMAGINI.contains(estensione)) {
                mostraImmagine(file);
            } else if ("pdf".equals(estensione)) {
                mostraPdf(file);
            } else if (AUDIO.contains(estensione)) {
                mostraAudio(file);
            } else if (VIDEO.contains(estensione)) {
                mostraVideo(file);
            } else if (DOCUMENTI.contains(estensione)) {
                apriNativa(file);
            } else {
                apriNativa(file);
            }
        } catch (Exception e) {
            mostraMessaggio("Impossibile aprire il file:\n" + e.getMessage());
        }
    }

    private void mostraImmagine(File file) {
        Image img = new Image(file.toURI().toString(), true);
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(700);
        iv.setFitHeight(500);
        areaAnteprima.getChildren().setAll(iv);
    }

    private void mostraPdf(File file) {
        WebView wv = new WebView();
        wv.getEngine().load(file.toURI().toString());
        wv.setPrefSize(750, 550);
        areaAnteprima.getChildren().setAll(wv);
    }

    private void mostraAudio(File file) {
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        javafx.scene.control.Button btnPlay = new javafx.scene.control.Button("▶  Riproduci");
        btnPlay.getStyleClass().add("button");

        javafx.scene.control.Button btnStop = new javafx.scene.control.Button("⏹  Ferma");
        btnStop.getStyleClass().add("button-secondary");

        Label icona = new Label("🎵");
        icona.setStyle("-fx-font-size: 64px;");

        Label stato = new Label("In attesa…");
        stato.setStyle("-fx-text-fill: #6c3fc5; -fx-font-size: 13px;");

        mediaPlayer.statusProperty().addListener((obs, o, n) ->
            javafx.application.Platform.runLater(() -> stato.setText(
                switch (n) {
                    case PLAYING -> "▶ In riproduzione";
                    case PAUSED  -> "⏸ In pausa";
                    case STOPPED -> "⏹ Fermato";
                    default -> n.toString();
                }
            ))
        );

        btnPlay.setOnAction(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                btnPlay.setText("▶  Riproduci");
            } else {
                mediaPlayer.play();
                btnPlay.setText("⏸  Pausa");
            }
        });
        btnStop.setOnAction(e -> { mediaPlayer.stop(); btnPlay.setText("▶  Riproduci"); });

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(16, icona, stato, btnPlay, btnStop);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        areaAnteprima.getChildren().setAll(box);

        Stage stage = (Stage) areaAnteprima.getScene().getWindow();
        stage.setOnCloseRequest(e -> mediaPlayer.dispose());
    }

    private void mostraVideo(File file) {
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        MediaView mv = new MediaView(mediaPlayer);
        mv.setFitWidth(750);
        mv.setFitHeight(480);
        mv.setPreserveRatio(true);

        javafx.scene.control.Button btnPlay = new javafx.scene.control.Button("▶  Riproduci");
        btnPlay.getStyleClass().add("button");

        btnPlay.setOnAction(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                btnPlay.setText("▶  Riproduci");
            } else {
                mediaPlayer.play();
                btnPlay.setText("⏸  Pausa");
            }
        });

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10, mv, btnPlay);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        areaAnteprima.getChildren().setAll(box);

        Stage stage = (Stage) areaAnteprima.getScene().getWindow();
        stage.setOnCloseRequest(e -> mediaPlayer.dispose());
    }

    private void apriNativa(File file) {
        mostraMessaggio("Apertura con l'applicazione predefinita…\n" + file.getName());
        new Thread(() -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                    MessErrBnd.create("Impossibile aprire il file: " + e.getMessage()));
            }
        }, "apri-nativa").start();
    }

    private void mostraMessaggio(String testo) {
        Label l = new Label(testo);
        l.setStyle("-fx-text-fill: #6c3fc5; -fx-font-size: 14px; -fx-text-alignment: center;");
        l.setWrapText(true);
        areaAnteprima.getChildren().setAll(l);
    }

    private String estensione(String percorso) {
        int idx = percorso.lastIndexOf('.');
        return idx >= 0 ? percorso.substring(idx + 1).toLowerCase() : "";
    }

    @FXML
    public void onChiudi() {
        if (mediaPlayer != null) mediaPlayer.dispose();
        ((Stage) areaAnteprima.getScene().getWindow()).close();
    }
}
