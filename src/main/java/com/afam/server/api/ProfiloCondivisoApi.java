package com.afam.server.api;

import com.afam.entities.EntityContenuto;
import com.afam.entities.EntityLink;
import com.afam.entities.EntityPortfolio;
import com.afam.entities.EntityRaccolta;
import com.afam.entities.EntityUtente;
import com.afam.server.control.visualizzaprofilocondiviso.*;
import com.afam.server.dao.DBMSBnd;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ProfiloCondivisoApi – endpoint REST per il sottosistema Visualizza Profilo Condiviso.
 * Base path: /api/pubblico
 *
 * Questi endpoint sono accessibili senza autenticazione (nessun header X-User-Id richiesto).
 * Espongono solo dati pubblici: nessun hashPassword, solo portfolio/contenuti visibili.
 */
@Path("/pubblico")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfiloCondivisoApi {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Accesso ospite ────────────────────────────────────────────────────────

    /**
     * GET /api/pubblico/ospite
     * Verifica che l'accesso come ospite sia consentito e restituisce un id ospite.
     */
    @GET
    @Path("/ospite")
    public Response entraComeOspite() {
        EntraComeOspiteCtrl ctrl = new EntraComeOspiteCtrl();
        if (!ctrl.verificaAccessoOspite()) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("success", false, "errore", "Accesso come ospite non consentito."))
                    .build();
        }
        return ok(Map.of("idOspite", ctrl.getIdOspite()));
    }

    // ── Ricerca studenti ──────────────────────────────────────────────────────

    /**
     * GET /api/pubblico/studenti?nome=X
     * Cerca studenti per nome/cognome. Se nome è omesso restituisce tutti.
     */
    @GET
    @Path("/studenti")
    public Response ricercaStudenti(@QueryParam("nome") String nome) {
        RicercaStudenteCtrl ctrl = new RicercaStudenteCtrl();
        try {
            List<EntityUtente> lista = (nome != null && !nome.isBlank())
                    ? ctrl.recuperaElencoStudenti(nome)
                    : ctrl.recuperaElencoStudenti();
            List<Map<String, Object>> sanitized = lista.stream()
                    .map(ProfiloCondivisoApi::sanitizzaUtente)
                    .toList();
            return ok(Map.of("studenti", sanitized));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visualizza profilo studente ───────────────────────────────────────────

    /** GET /api/pubblico/studenti/{idUtente}/profilo */
    @GET
    @Path("/studenti/{idUtente}/profilo")
    public Response visualizzaProfilo(@PathParam("idUtente") String idUtenteStr) {
        VisualizzaProfiloCtrl ctrl = new VisualizzaProfiloCtrl();
        try {
            UUID        idUtente   = UUID.fromString(idUtenteStr);
            EntityUtente utente    = ctrl.recuperaInfoProfilo(idUtente);
            if (utente == null) return notFound("Studente non trovato.");
            List<EntityPortfolio> portfolios = ctrl.recuperaElencoPortfoli(idUtente);
            Map<String, Object> resp = new HashMap<>();
            resp.put("profilo",    sanitizzaUtente(utente));
            resp.put("portfolios", portfolios);
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visualizza portfolio condiviso ────────────────────────────────────────

    /** GET /api/pubblico/studenti/{idUtente}/portfolio/{idPortfolio} */
    @GET
    @Path("/studenti/{idUtente}/portfolio/{idPortfolio}")
    public Response visualizzaPortfolioCondiviso(
            @PathParam("idUtente")   String idUtenteStr,
            @PathParam("idPortfolio") String idPortfolioStr) {
        VisualizzaPortfolioCondivisoCtrl ctrl = new VisualizzaPortfolioCondivisoCtrl();
        try {
            UUID            idPortfolio = UUID.fromString(idPortfolioStr);
            EntityPortfolio portfolio   = ctrl.recuperaPortfolio(idPortfolio);
            if (portfolio == null) return notFound("Portfolio non trovato.");
            List<EntityContenuto> contenuti = ctrl.recuperaContenutiPortfolio(idPortfolio);
            int n = ctrl.recuperaVisualizzazioni(idPortfolio);
            ctrl.aggiornaNumero(n + 1, idPortfolio);
            Map<String, Object> resp = new HashMap<>();
            resp.put("portfolio", portfolio);
            // Raccolte (con i loro contenuti) e tutti i contenuti caricati nel portfolio
            resp.put("raccolte",  costruisciRaccolte(idPortfolio));
            resp.put("contenuti", contenuti);
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Accesso tramite link ──────────────────────────────────────────────────

    /**
     * GET /api/pubblico/link/{idLink}
     * Verifica la validità del link e restituisce il portfolio associato.
     * L'identificatore del link è il suo id_link (UUID): non esiste un token separato.
     */
    @GET
    @Path("/link/{idLink}")
    public Response accediTramiteLink(@PathParam("idLink") String idLinkStr) {
        AccediTramiteLinkCtrl ctrl = new AccediTramiteLinkCtrl();
        try {
            // Recupera il link per id senza richiedere currentUserId (accesso pubblico)
            UUID idLinkParam;
            try {
                idLinkParam = UUID.fromString(idLinkStr);
            } catch (IllegalArgumentException ex) {
                return notFound("Link non valido.");
            }
            EntityLink link = db.recuperaLinkById(idLinkParam);

            if (link == null) return notFound("Link non trovato.");
            ctrl.setLink(link);

            UUID idLink = link.getIdLink();
            if (!ctrl.isLinkValido(idLink)) {
                return Response.status(Response.Status.GONE)
                        .entity(Map.of("success", false,
                                "errore", "Il link è scaduto o revocato."))
                        .build();
            }

            UUID            idPortfolio = ctrl.getIdPortfolio(idLink);
            EntityPortfolio portfolio   = ctrl.recuperaPortfolio(idPortfolio);
            if (portfolio == null) return notFound("Portfolio non trovato.");

            // Recupera e incrementa le visualizzazioni
            VisualizzaPortfolioCondivisoCtrl viewCtrl = new VisualizzaPortfolioCondivisoCtrl();
            List<EntityContenuto> contenuti = viewCtrl.recuperaContenutiPortfolio(idPortfolio);
            int n = viewCtrl.recuperaVisualizzazioni(idPortfolio);
            viewCtrl.aggiornaNumero(n + 1, idPortfolio);

            Map<String, Object> resp = new HashMap<>();
            resp.put("portfolio", portfolio);
            // Raccolte (con i loro contenuti) e tutti i contenuti caricati nel portfolio
            resp.put("raccolte",  costruisciRaccolte(idPortfolio));
            resp.put("contenuti", contenuti);
            resp.put("flagAperto", link.isFlagAperto());
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Costruisce l'elenco delle raccolte del portfolio, ciascuna con i propri
     * contenuti, per la visualizzazione condivisa.
     */
    private List<Map<String, Object>> costruisciRaccolte(UUID idPortfolio) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (EntityRaccolta r : db.recuperaRaccolte(idPortfolio)) {
            Map<String, Object> rm = new HashMap<>();
            rm.put("idRaccolta", r.getIdRaccolta() != null ? r.getIdRaccolta().toString() : null);
            rm.put("nome",       r.getNome());
            rm.put("contenuti",  db.recuperaContenutiRaccolta(r.getIdRaccolta()));
            out.add(rm);
        }
        return out;
    }

    private static Map<String, Object> sanitizzaUtente(EntityUtente u) {
        Map<String, Object> m = new HashMap<>();
        m.put("idUtente", u.getIdUtente() != null ? u.getIdUtente().toString() : null);
        m.put("nome",     u.getNome());
        m.put("cognome",  u.getCognome());
        m.put("email",    u.getEmail());
        return m;
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
