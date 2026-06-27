package com.afam.client.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * AppIcon – genera l'icona dell'applicazione: il logo viene ridotto con un
 * margine trasparente e con gli angoli arrotondati (stile icona macOS), così
 * da non apparire fuori scala né squadrato nel dock/taskbar.
 * @author Cristian Joshua Ingrao (0780672)
 */
public final class AppIcon {

    private static final String LOGO = "/images/logo.png";
    /** Frazione del riquadro occupata dal logo (il resto è margine trasparente). */
    private static final double SCALA = 0.82;
    /** Raggio degli angoli, in frazione del lato del logo (squircle ~22%). */
    private static final double RAGGIO = 0.225;

    private AppIcon() {}

    /** Icona JavaFX (barra del titolo finestra) ridotta e con angoli arrotondati. */
    public static Image fxIcon() {
        try (InputStream in = AppIcon.class.getResourceAsStream(LOGO)) {
            if (in == null) return null;
            Image logo = new Image(in);
            double lato = 256, inner = lato * SCALA;

            ImageView iv = new ImageView(logo);
            iv.setFitWidth(inner);
            iv.setFitHeight(inner);
            iv.setPreserveRatio(false);
            Rectangle clip = new Rectangle(inner, inner);
            clip.setArcWidth(inner * RAGGIO * 2);
            clip.setArcHeight(inner * RAGGIO * 2);
            iv.setClip(clip);

            StackPane holder = new StackPane(iv);
            holder.setStyle("-fx-background-color: transparent;");
            holder.resize(lato, lato);
            holder.layout();

            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            return holder.snapshot(sp, null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Imposta l'icona del dock macOS (ridotta, con angoli arrotondati). Best-effort. */
    public static void applyDockIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) return;
            Taskbar taskbar = Taskbar.getTaskbar();
            if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return;
            try (InputStream in = AppIcon.class.getResourceAsStream(LOGO)) {
                if (in == null) return;
                BufferedImage logo = ImageIO.read(in);
                int lato = 1024, inner = (int) Math.round(lato * SCALA), pos = (lato - inner) / 2;
                double arc = inner * RAGGIO;
                BufferedImage out = new BufferedImage(lato, lato, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = out.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setClip(new RoundRectangle2D.Double(pos, pos, inner, inner, arc, arc));
                g.drawImage(logo, pos, pos, inner, inner, null);
                g.dispose();
                taskbar.setIconImage(out);
            }
        } catch (Exception ignored) {
            // dock non disponibile (OS non supportato): ignora, resta l'icona finestra
        }
    }
}
