package com.afam.client.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * AppIcon – genera l'icona dell'applicazione con un margine trasparente attorno
 * al logo, così da apparire più piccola nel dock/taskbar (il logo originale
 * riempie l'intero riquadro fino ai bordi).
 * @author Cristian Joshua Ingrao (0780672)
 */
public final class AppIcon {

    private static final String LOGO = "/images/logo.png";
    /** Frazione del riquadro occupata dal logo (il resto è margine trasparente). */
    private static final double SCALA = 0.60;

    private AppIcon() {}

    /** Icona JavaFX (per la barra del titolo della finestra) con margine. */
    public static Image fxIcon() {
        try (InputStream in = AppIcon.class.getResourceAsStream(LOGO)) {
            if (in == null) return null;
            Image logo = new Image(in);
            double lato = 256, inner = lato * SCALA, pos = (lato - inner) / 2;
            Canvas canvas = new Canvas(lato, lato);
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.drawImage(logo, pos, pos, inner, inner);
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            return canvas.snapshot(sp, null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Imposta l'icona del dock macOS (più piccola, con margine). Best-effort. */
    public static void applyDockIcon() {
        try {
            if (!Taskbar.isTaskbarSupported()) return;
            Taskbar taskbar = Taskbar.getTaskbar();
            if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return;
            try (InputStream in = AppIcon.class.getResourceAsStream(LOGO)) {
                if (in == null) return;
                BufferedImage logo = ImageIO.read(in);
                int lato = 1024, inner = (int) Math.round(lato * SCALA), pos = (lato - inner) / 2;
                BufferedImage padded = new BufferedImage(lato, lato, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = padded.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(logo, pos, pos, inner, inner, null);
                g.dispose();
                taskbar.setIconImage(padded);
            }
        } catch (Exception ignored) {
            // dock non disponibile (OS non supportato): ignora, resta l'icona finestra
        }
    }
}
