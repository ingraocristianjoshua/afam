package com.afam.server.api;

import com.afam.entities.EntityLink;
import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityUtente;
import com.afam.server.control.gestiscicondivisione.*;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CondivisioneApi – endpoint REST per il sottosistema Gestisci Condivisione.
 * Base path: /api/condivisione
 * Header richiesto: X-User-Id (UUID stringa).
 */
@Path("/condivisione")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CondivisioneApi {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Lista link ────────────────────────────────────────────────────────────

    /** GET /api/condivisione/links */
    @GET
    @Path("/links")
    public Response visualizzaLinks(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        VisualizzaLinksCtrl ctrl = new VisualizzaLinksCtrl();
        try {
            String baseUrl = com.afam.server.dao.MailServerBnd.getInstance().getLinkBaseUrl();
            List<EntityLink> links = ctrl.recuperaLinks();
            List<Map<String, Object>> result = links.stream().map(l -> {
                Map<String, Object> m = new HashMap<>();
                m.put("idLink",      l.getIdLink() != null ? l.getIdLink().toString() : null);
                m.put("linkUrl",     baseUrl + l.getLink());
                m.put("stato",       l.getStato());
                m.put("visibilita",  l.getVisibilita());
                m.put("flagAperto",  l.isFlagAperto());
                m.put("scadenza",    l.getScadenza() != null ? l.getScadenza().toString() : null);
                return m;
            }).toList();
            return ok(Map.of("links", result));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Genera link ───────────────────────────────────────────────────────────

    /**
     * POST /api/condivisione/links
     * Body: { idPortfolio, visibilita?, scadenza?, flagAperto?, emailDestinatario? }
     */
    @POST
    @Path("/links")
    public Response generaLink(@HeaderParam("X-User-Id") String userId,
                                Map<String, Object> data) {
        impostaUtente(userId);
        GeneraLinkCtrl ctrl = new GeneraLinkCtrl();
        try {
            UUID idPortfolio = UUID.fromString((String) data.get("idPortfolio"));
            EntityPortfolio portfolio = db.recuperaPortfolio(idPortfolio);
            if (portfolio == null) return notFound("Portfolio non trovato.");
            ctrl.setPortfolio(portfolio);

            UUID   idPortfolioCtrl = ctrl.getIdPortfolio();
            UUID   idUtente        = ctrl.getIdUtente();
            String visibilita      = data.containsKey("visibilita")
                                        ? (String) data.get("visibilita")
                                        : Constants.VIS_PRIVATO;
            boolean flagAperto     = Boolean.TRUE.equals(data.get("flagAperto"));
            OffsetDateTime scadenza = data.containsKey("scadenza") && data.get("scadenza") != null
                                        ? OffsetDateTime.parse((String) data.get("scadenza"))
                                        : null;

            EntityLink link = ctrl.generaLink(idPortfolioCtrl, idUtente,
                                              visibilita, scadenza, flagAperto);
            ctrl.salvaNuovoLink(link);

            // invio opzionale via email
            String emailDest = (String) data.get("emailDestinatario");
            if (emailDest != null && !emailDest.isBlank()) {
                EntityUtente studente = db.recuperaUtente(idUtente);
                String nomeStudente = studente != null
                        ? studente.getNome() + " " + studente.getCognome()
                        : "Uno studente AFAM";
                ctrl.inviaLink(emailDest, link.getLink(), nomeStudente);
            }

            Map<String, Object> resp = new HashMap<>();
            String baseUrl = com.afam.server.dao.MailServerBnd.getInstance().getLinkBaseUrl();
            resp.put("idLink",    link.getIdLink().toString());
            resp.put("linkUrl",   baseUrl + link.getLink());
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Revoca link ───────────────────────────────────────────────────────────

    /** PATCH /api/condivisione/links/{idLink}/revoca */
    @PATCH
    @Path("/links/{idLink}/revoca")
    public Response revocaLink(@HeaderParam("X-User-Id") String userId,
                                @PathParam("idLink") String idLinkStr) {
        impostaUtente(userId);
        RevocaLinkCtrl ctrl = new RevocaLinkCtrl();
        try {
            EntityLink link = recuperaLinkONotFound(idLinkStr);
            if (link == null) return notFound("Link non trovato.");
            ctrl.setLink(link);
            UUID idLink = ctrl.getIdLink();
            ctrl.aggiornaStatoLink(idLink);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Imposta scadenza ──────────────────────────────────────────────────────

    /**
     * PATCH /api/condivisione/links/{idLink}/scadenza
     * Body: { scadenza } – ISO-8601; omettere o null per rimuovere la scadenza.
     */
    @PATCH
    @Path("/links/{idLink}/scadenza")
    public Response impostaScadenza(@HeaderParam("X-User-Id") String userId,
                                     @PathParam("idLink") String idLinkStr,
                                     Map<String, Object> data) {
        impostaUtente(userId);
        ImpostaScadenzaCtrl ctrl = new ImpostaScadenzaCtrl();
        try {
            EntityLink link = recuperaLinkONotFound(idLinkStr);
            if (link == null) return notFound("Link non trovato.");
            ctrl.setLink(link);
            UUID idLink = ctrl.getIdLink();
            ctrl.recuperaScadenza(idLink);
            OffsetDateTime nuovaScadenza = (data != null && data.containsKey("scadenza") && data.get("scadenza") != null)
                    ? OffsetDateTime.parse((String) data.get("scadenza"))
                    : null;
            ctrl.aggiornaScadenza(nuovaScadenza, idLink);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visibilità link ───────────────────────────────────────────────────────

    /**
     * PATCH /api/condivisione/links/{idLink}/visibilita
     * Body: { visibilita } – oppure {} per alternare privato/pubblico.
     */
    @PATCH
    @Path("/links/{idLink}/visibilita")
    public Response cambiaVisibilita(@HeaderParam("X-User-Id") String userId,
                                      @PathParam("idLink") String idLinkStr,
                                      Map<String, Object> data) {
        impostaUtente(userId);
        VisibilitaLinkCtrl ctrl = new VisibilitaLinkCtrl();
        try {
            EntityLink link = recuperaLinkONotFound(idLinkStr);
            if (link == null) return notFound("Link non trovato.");
            ctrl.setLink(link);
            UUID   idLink  = ctrl.getIdLink();
            String attuale = ctrl.recuperaVisibilitaLink(idLink);
            String nuova   = (data != null && data.containsKey("visibilita"))
                                ? (String) data.get("visibilita")
                                : ctrl.alternaVisibilita(attuale);
            ctrl.aggiornaVisibilitaLink(nuova, idLink);
            return ok(Map.of("visibilita", nuova));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Invio via email di un link esistente ────────────────────────────────────

    /**
     * POST /api/condivisione/links/{idLink}/invia
     * Body: { email } – invia via email un link già generato.
     */
    @POST
    @Path("/links/{idLink}/invia")
    public Response inviaLinkEmail(@HeaderParam("X-User-Id") String userId,
                                   @PathParam("idLink") String idLinkStr,
                                   Map<String, Object> data) {
        impostaUtente(userId);
        try {
            EntityLink link = recuperaLinkONotFound(idLinkStr);
            if (link == null) return notFound("Link non trovato.");
            String email = data != null ? (String) data.get("email") : null;
            if (email == null || email.isBlank()) return server("Indirizzo email mancante.");

            EntityUtente studente = db.recuperaUtente(db.getCurrentUserId());
            String nomeStudente = studente != null
                    ? studente.getNome() + " " + studente.getCognome()
                    : "Uno studente AFAM";

            new GeneraLinkCtrl().inviaLink(email, link.getLink(), nomeStudente);
            return ok(Map.of("email", email));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void impostaUtente(String userId) {
        if (userId != null && !userId.isBlank()) {
            db.setCurrentUser(UUID.fromString(userId));
        }
    }

    /**
     * Recupera il link per id; usa email="" e ricerca per id diretto.
     * DBMSBnd.recuperaLink(email, idLink) richiede entrambi i parametri;
     * per la revoca/modifica (owner always known) si usa la lista filtrata.
     */
    private EntityLink recuperaLinkONotFound(String idLinkStr) {
        UUID idLink = UUID.fromString(idLinkStr);
        // Cerca il link nell'elenco dell'utente corrente (sicurezza: owner check implicito)
        return db.recuperaLinks().stream()
                .filter(l -> idLink.equals(l.getIdLink()))
                .findFirst()
                .orElse(null);
    }

    /** Ok. */
    private Response ok() {
        return Response.ok(Map.of("success", true)).build();
    }

    /** Ok. */
    private Response ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return Response.ok(body).build();
    }

    /** Not found. */
    private Response notFound(String msg) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("success", false, "errore", msg))
                .build();
    }

    /** Server. */
    private Response server(String errore) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("success", false, "errore", errore != null ? errore : "Errore interno."))
                .build();
    }
}
