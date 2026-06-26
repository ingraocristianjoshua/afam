package com.afam.server.api;

import com.afam.entities.EntityLink;
import com.afam.entities.EntityPortfolio;
import com.afam.server.dao.DBMSBnd;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ShareApi – endpoint HTML per la landing page del link condiviso.
 * Quando il Soggetto Esterno clicca il link ricevuto via email, il browser
 * apre questa pagina che propone il pulsante "Apri in AFAM" (custom URL scheme afam://).
 *
 * GET /share/{token} → HTML landing page
 * @author Cristian Joshua Ingrao (0780672)
 */
@Path("/share")
public class ShareApi {

    private final DBMSBnd db = DBMSBnd.getInstance();

    @GET
    @Path("/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Response landingPage(@PathParam("token") String token) {
        EntityLink link = db.recuperaLinkByToken(token);

        String titolo    = "Portfolio condiviso";
        String messaggio = "";
        boolean valido   = false;

        if (link == null) {
            messaggio = "Il link non esiste o è stato revocato.";
        } else if (!"attivo".equals(link.getStato())) {
            messaggio = "Il link è scaduto o revocato.";
        } else {
            valido = true;
            EntityPortfolio p = db.recuperaPortfolio(link.getIdPortfolio());
            if (p != null) titolo = "Portfolio: " + p.getNome();
        }

        String html = buildHtml(token, titolo, messaggio, valido);
        return Response.ok(html).build();
    }

    private String buildHtml(String token, String titolo, String messaggio, boolean valido) {
        if (!valido) {
            return """
                <!DOCTYPE html><html lang="it"><head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>AFAM – Link non disponibile</title>
                  <style>
                    * { margin:0; padding:0; box-sizing:border-box; }
                    body { font-family: -apple-system,sans-serif;
                           background: linear-gradient(135deg,#1a0533,#2d1a5e);
                           min-height:100vh; display:flex; align-items:center;
                           justify-content:center; padding:20px; }
                    .card { background:rgba(255,255,255,0.07); border:1px solid rgba(196,181,253,0.2);
                            border-radius:20px; padding:48px 40px; max-width:460px;
                            width:100%%; text-align:center; }
                    .logo { font-size:56px; margin-bottom:16px; }
                    h1 { color:#f5f3ff; font-size:22px; font-weight:700; margin-bottom:12px; }
                    p  { color:#c4b5fd; font-size:15px; line-height:1.6; }
                  </style>
                </head><body>
                  <div class="card">
                    <div class="logo">⚠️</div>
                    <h1>Link non disponibile</h1>
                    <p>%s</p>
                  </div>
                </body></html>
                """.formatted(messaggio);
        }

        // Link valido: mostra pulsante subito + tenta redirect automatico
        return """
            <!DOCTYPE html>
            <html lang="it">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>AFAM – Apri portfolio</title>
              <style>
                * { margin:0; padding:0; box-sizing:border-box; }
                body {
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                  background: linear-gradient(135deg, #1a0533 0%%, #2d1a5e 100%%);
                  min-height: 100vh;
                  display: flex; align-items: center; justify-content: center;
                  padding: 20px;
                }
                .card {
                  background: rgba(255,255,255,0.07);
                  border: 1px solid rgba(196,181,253,0.2);
                  border-radius: 20px; padding: 48px 40px;
                  max-width: 460px; width: 100%%; text-align: center;
                }
                .logo { font-size: 56px; margin-bottom: 16px; }
                h1 { color: #f5f3ff; font-size: 22px; font-weight: 700; margin-bottom: 8px; }
                .subtitle { color: #c4b5fd; font-size: 15px; line-height: 1.6; margin-bottom: 32px; }
                .btn {
                  display: inline-block;
                  background: linear-gradient(135deg,#8b5cf6,#6d28d9);
                  color: white; text-decoration: none; padding: 15px 40px;
                  border-radius: 50px; font-size: 16px; font-weight: 600;
                  margin-bottom: 24px; transition: opacity 0.2s; cursor: pointer;
                  box-shadow: 0 4px 20px rgba(109,40,217,0.4);
                }
                .btn:hover { opacity: 0.85; }
                .divider { color: #5b21b6; font-size: 13px; margin: 8px 0 16px; }
                .token-box {
                  background: rgba(0,0,0,0.3); border: 1px solid rgba(167,139,250,0.2);
                  border-radius: 10px; padding: 12px 16px;
                  font-family: monospace; font-size: 13px; color: #a78bfa;
                  word-break: break-all; cursor: pointer; user-select: all;
                  margin-bottom: 8px;
                }
                .token-hint { color: #6d28d9; font-size: 12px; margin-bottom: 0; }
                .brand {
                  position: fixed; bottom: 20px; left: 0; right: 0; text-align: center;
                  color: #5b21b6; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;
                }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="logo">🎵</div>
                <h1>%s</h1>
                <p class="subtitle">Clicca il pulsante per aprire il portfolio nel client AFAM.</p>
                <a href="afam://%s" class="btn">Apri in AFAM</a>
                <p class="divider">— oppure usa il token —</p>
                <div class="token-box" onclick="this.select(); document.execCommand('copy');" title="Clicca per copiare">%s</div>
                <p class="token-hint">Incolla questo codice nella schermata "Accedi con link" del client AFAM</p>
              </div>
              <p class="brand">AFAM – Alta Formazione Artistica, Musicale e Coreutica</p>
              <script>
                // Redirect automatico: apre AFAM senza che l'utente clicchi nulla
                window.location.href = 'afam://%s';
              </script>
            </body>
            </html>
            """.formatted(titolo, token, token, token);
    }
}
