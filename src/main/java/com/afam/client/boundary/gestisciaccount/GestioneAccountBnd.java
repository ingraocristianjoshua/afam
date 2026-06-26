package com.afam.client.boundary.gestisciaccount;

import com.afam.client.boundary.dialog.MessConfermaBnd;
import com.afam.client.boundary.dialog.MessErrBnd;
import com.afam.client.boundary.dialog.MessSuccessoBnd;
import com.afam.client.rest.RestClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * GestioneAccountBnd – schermata principale dell'area personale.
 * Layout BorderPane con sidebar di navigazione.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class GestioneAccountBnd {

    @FXML private BorderPane root;

    // Campi profilo (read-only)
    @FXML private TextField campoNomeRO;
    @FXML private TextField campoCognomeRO;
    @FXML private TextField campoDataNascitaRO;
    @FXML private TextField campoTelefonoRO;
    @FXML private TextField campoEmailRO;

    // Header card
    @FXML private Label labelNomeCompleto;
    @FXML private Label labelEmailHeader;

    // Stato validazioni
    @FXML private Label labelStatoEmail;
    @FXML private Label labelStatoNumero;
    @FXML private Label labelStato2FA;

    private final RestClient rest = RestClient.getInstance();

    @FXML
    public void initialize() {
        new Thread(this::caricaProfilo, "carica-profilo").start();
    }

    private void caricaProfilo() {
        try {
            Map<String, Object> p = rest.get("account/profilo");
            String nome     = (String) p.getOrDefault("nome", "");
            String cognome  = (String) p.getOrDefault("cognome", "");
            String email    = (String) p.getOrDefault("email", "");
            String telefono = (String) p.getOrDefault("numeroTelefono", "");
            boolean emailVal  = Boolean.TRUE.equals(p.get("emailValidata"));
            boolean numeroVal = Boolean.TRUE.equals(p.get("numeroValidato"));
            boolean fa2       = Boolean.TRUE.equals(p.get("flag2fa"));

            Platform.runLater(() -> {
                campoNomeRO.setText(nome);
                campoCognomeRO.setText(cognome);
                campoDataNascitaRO.setText("");
                campoEmailRO.setText(email);
                campoTelefonoRO.setText(telefono != null ? telefono : "");

                labelNomeCompleto.setText(nome + " " + cognome);
                labelEmailHeader.setText(email);

                labelStatoEmail.setText(emailVal ? "✓ Validata" : "✗ Non validata");
                labelStatoEmail.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                        + (emailVal ? "#27ae60;" : "#e74c3c;"));

                labelStatoNumero.setText(numeroVal ? "✓ Validato" : "✗ Non validato");
                labelStatoNumero.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                        + (numeroVal ? "#27ae60;" : "#e74c3c;"));

                labelStato2FA.setText(fa2 ? "✓ Attiva" : "✗ Non attiva");
                labelStato2FA.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                        + (fa2 ? "#27ae60;" : "#e74c3c;"));
            });
        } catch (RestClient.RestException e) {
            Platform.runLater(() -> labelNomeCompleto.setText("Errore caricamento"));
        }
    }

    // ── Azioni account ────────────────────────────────────────────────────────

    @FXML
    public void onCaricaCV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona il tuo CV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;
        try {
            Path dest = Path.of(System.getProperty("user.home"), ".afam", "cv_" + System.currentTimeMillis() + ".pdf");
            Files.createDirectories(dest.getParent());
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            MessSuccessoBnd.create("CV caricato con successo:\n" + file.getName());
        } catch (Exception e) {
            MessErrBnd.create("Impossibile caricare il CV: " + e.getMessage());
        }
    }

    @FXML public void onModificaInformazioni() { apri("/fxml/gestisciaccount/FormModifica.fxml",           "Modifica dati"); }
    @FXML public void onReimpostaPassword()    { apri("/fxml/gestisciaccount/FormReimpostaPassword.fxml",  "Imposta password"); }
    @FXML public void onGestione2FA()          { apri("/fxml/gestisciaccount/FormSelettore2FA.fxml",        "Selettore 2FA"); }
    @FXML public void onValidaEmail()          { apri("/fxml/gestisciaccount/ValidaEmail.fxml",             "Valida email"); }
    @FXML public void onValidaNumero()         { apri("/fxml/gestisciaccount/ValidaNumero.fxml",            "Valida numero"); }

    @FXML
    public void onEliminaAccount() {
        if (MessConfermaBnd.create("Sei sicuro di voler eliminare il tuo account? L'operazione è irreversibile.")) {
            apri("/fxml/gestisciaccount/FormEliminaAccount.fxml", "Elimina account");
        }
    }

    // ── Navigazione sezioni ───────────────────────────────────────────────────

    @FXML public void onVaiPortfolio()    { vai("/fxml/gestisciportfolio/GestionePortfolio.fxml",    "Gestione Portfolio"); }
    @FXML public void onVaiContenuti()    { vai("/fxml/gestiscicontenuti/GestioneContenuti.fxml",    "Gestione Contenuti"); }
    @FXML public void onVaiCondivisione() { vai("/fxml/gestiscicondivisione/GestioneCondivisione.fxml", "Gestione Condivisione"); }

    @FXML
    public void onVisualizzaProfilo() {
        new Thread(() -> {
            try {
                Map<String, Object> profilo = rest.get("account/profilo");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/visualizzaprofilocondiviso/VisualizzaProfilo.fxml"));
                        Stage stage = new Stage();
                        stage.setTitle("AFAM – Il tuo profilo pubblico");
                        stage.setScene(new Scene(loader.load()));
                        stage.getScene().getStylesheets().add(
                                getClass().getResource("/css/application.css").toExternalForm());
                        com.afam.client.boundary.visualizzaprofilocondiviso.VisualizzaProfiloBnd ctrl =
                                loader.getController();
                        ctrl.setStudente(profilo);
                        stage.show();
                    } catch (Exception e) {
                        MessErrBnd.create("Errore apertura profilo: " + e.getMessage());
                    }
                });
            } catch (RestClient.RestException e) {
                Platform.runLater(() -> MessErrBnd.create("Impossibile caricare il profilo: " + e.getMessage()));
            }
        }, "carica-profilo-pubblico").start();
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    public void onLogout() {
        try { rest.post("account/logout", Map.of()); } catch (RestClient.RestException ignored) {}
        rest.logout();
        chiudi();
        apri("/fxml/autenticati/AuthPage.fxml", "AFAM");
    }

    public void chiudi() {
        ((Stage) root.getScene().getWindow()).close();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Apre una nuova finestra senza chiudere quella corrente. */
    private void apri(String path, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            MessErrBnd.create("Impossibile aprire la vista: " + e.getMessage());
        }
    }

    /** Chiude la finestra corrente e apre una nuova sezione principale. */
    private void vai(String path, String titolo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Stage stage = new Stage();
            stage.setTitle("AFAM – " + titolo);
            stage.setScene(new Scene(loader.load()));
            stage.getScene().getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
            stage.show();
            chiudi();
        } catch (Exception e) {
            MessErrBnd.create("Impossibile aprire la vista: " + e.getMessage());
        }
    }
}
