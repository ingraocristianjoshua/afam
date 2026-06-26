package com.afam.server.api;

import com.afam.entities.*;
import com.afam.server.control.gestisciportfolio.*;
import com.afam.server.dao.DBMSBnd;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PortfolioApi – endpoint REST per il sottosistema Gestisci Portfolio.
 * Base path: /api/portfolio
 * Header richiesto: X-User-Id (UUID stringa) per tutte le operazioni.
 * @author Cristian Joshua Ingrao (0780672)
 */
@Path("/portfolio")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioApi {

    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Crea Portfolio ────────────────────────────────────────────────────────

    /** POST /api/portfolio
     *  Body: { nomePortfolio } */
    @POST
    public Response creaPortfolio(@HeaderParam("X-User-Id") String userId,
                                   Map<String, Object> data) {
        impostaUtente(userId);
        CreaPortfolioCtrl ctrl = new CreaPortfolioCtrl();
        try {
            String nome = (String) data.get("nomePortfolio");
            ctrl.setNomePortfolio(nome);
            if (!ctrl.verificaNomePortfolio(nome))             return bad(ctrl.getErrorMessage());
            data.put("nomePortfolio", nome);
            if (ctrl.isNomeInUso(data))                        return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            UUID idPortfolio = ctrl.generaIdPortfolio();
            ctrl.salvaPortfolio(idPortfolio, nome);
            return ok(Map.of("idPortfolio", idPortfolio.toString()));
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Elimina Portfolio ─────────────────────────────────────────────────────

    /** DELETE /api/portfolio/{idPortfolio} */
    @DELETE
    @Path("/{idPortfolio}")
    public Response eliminaPortfolio(@HeaderParam("X-User-Id") String userId,
                                      @PathParam("idPortfolio") String idPortfolioStr) {
        impostaUtente(userId);
        EliminaPortfolioCtrl ctrl = new EliminaPortfolioCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            ctrl.setPortfolio(p);
            UUID id = ctrl.getIdPortfolio();
            ctrl.eliminaPortfolio(id);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visualizza Portfolio ──────────────────────────────────────────────────

    /** GET /api/portfolio/{idPortfolio} */
    @GET
    @Path("/{idPortfolio}")
    public Response visualizzaPortfolio(@HeaderParam("X-User-Id") String userId,
                                         @PathParam("idPortfolio") String idPortfolioStr) {
        impostaUtente(userId);
        VisualizzaPortfolioCtrl ctrl = new VisualizzaPortfolioCtrl();
        try {
            UUID idPortfolio = UUID.fromString(idPortfolioStr);
            EntityPortfolio p = ctrl.recuperaPortfolio(idPortfolio);
            if (p == null) return notFound();
            ctrl.setPortfolio(p);
            List<EntityContenuto> contenuti = ctrl.recuperaContenutiPortfolio(idPortfolio);
            List<EntityRaccolta> raccolte = db.recuperaRaccolte(idPortfolio);
            int n = ctrl.recuperaVisualizzazioni(idPortfolio);
            ctrl.aggiornaNumero(n + 1, idPortfolio);
            Map<String, Object> resp = new HashMap<>();
            resp.put("portfolio", p);
            resp.put("contenuti", contenuti);
            resp.put("raccolte", raccolte);
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Lista Portfolio utente ────────────────────────────────────────────────

    /** GET /api/portfolio */
    @GET
    public Response elencoPortfolio(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        try {
            List<EntityPortfolio> lista = db.recuperaPortfoli();
            return ok(Map.of("portfolios", lista));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Crea Raccolta ─────────────────────────────────────────────────────────

    /** POST /api/portfolio/{idPortfolio}/raccolte
     *  Body: { nomeRaccolta } */
    @POST
    @Path("/{idPortfolio}/raccolte")
    public Response creaRaccolta(@HeaderParam("X-User-Id") String userId,
                                  @PathParam("idPortfolio") String idPortfolioStr,
                                  Map<String, Object> data) {
        impostaUtente(userId);
        CreaRaccoltaCtrl ctrl = new CreaRaccoltaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            ctrl.setPortfolio(p);
            String nome = (String) data.get("nomeRaccolta");
            ctrl.setNomeRaccolta(nome);
            if (!ctrl.verificaNomeRaccolta(nome))              return bad(ctrl.getErrorMessage());
            if (ctrl.isNomeInUso(nome))                        return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            UUID idRaccolta = ctrl.generaIdRaccolta();
            ctrl.salvaRaccolta(idRaccolta, nome);
            return ok(Map.of("idRaccolta", idRaccolta.toString()));
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Elimina Raccolta ──────────────────────────────────────────────────────

    /** DELETE /api/portfolio/{idPortfolio}/raccolte/{idRaccolta} */
    @DELETE
    @Path("/{idPortfolio}/raccolte/{idRaccolta}")
    public Response eliminaRaccolta(@HeaderParam("X-User-Id") String userId,
                                     @PathParam("idPortfolio") String idPortfolioStr,
                                     @PathParam("idRaccolta") String idRaccoltaStr) {
        impostaUtente(userId);
        EliminaRaccoltaCtrl ctrl = new EliminaRaccoltaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            EntityRaccolta r = db.recuperaRaccolta(UUID.fromString(idRaccoltaStr));
            if (r == null) return notFound();
            ctrl.setPortfolio(p);
            ctrl.setRaccolta(r);
            UUID idPortfolio = ctrl.getIdPortfolio();
            UUID idRaccolta  = ctrl.getIdRaccolta(idPortfolio);
            ctrl.eliminaRaccolta(idRaccolta);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Rinomina Raccolta ─────────────────────────────────────────────────────

    /** PATCH /api/portfolio/{idPortfolio}/raccolte/{idRaccolta}/nome
     *  Body: { nomeRaccolta } */
    @PATCH
    @Path("/{idPortfolio}/raccolte/{idRaccolta}/nome")
    public Response rinominaRaccolta(@HeaderParam("X-User-Id") String userId,
                                      @PathParam("idPortfolio") String idPortfolioStr,
                                      @PathParam("idRaccolta") String idRaccoltaStr,
                                      Map<String, Object> data) {
        impostaUtente(userId);
        RinominaCtrl ctrl = new RinominaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            EntityRaccolta r = db.recuperaRaccolta(UUID.fromString(idRaccoltaStr));
            if (r == null) return notFound();
            ctrl.setPortfolio(p);
            ctrl.setNuovoNome((String) data.get("nomeRaccolta"));
            UUID idPortfolio = ctrl.getIdPortfolio();
            ctrl.getIdRaccolta(idPortfolio);
            EntityRaccolta loaded = ctrl.recuperaRaccolta(idPortfolio);
            String nuovoNome = ctrl.getNomeRaccolta();
            if (!ctrl.verificaNomeRaccolta(nuovoNome))         return bad(ctrl.getErrorMessage());
            if (!ctrl.verificaNome(nuovoNome))                 return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            ctrl.aggiornaNomeRaccolta(loaded != null ? loaded : r, nuovoNome);
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visualizza Raccolta ───────────────────────────────────────────────────

    /** GET /api/portfolio/{idPortfolio}/raccolte/{idRaccolta} */
    @GET
    @Path("/{idPortfolio}/raccolte/{idRaccolta}")
    public Response visualizzaRaccolta(@HeaderParam("X-User-Id") String userId,
                                        @PathParam("idPortfolio") String idPortfolioStr,
                                        @PathParam("idRaccolta") String idRaccoltaStr) {
        impostaUtente(userId);
        VisualizzaRaccoltaCtrl ctrl = new VisualizzaRaccoltaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            EntityRaccolta r = db.recuperaRaccolta(UUID.fromString(idRaccoltaStr));
            if (r == null) return notFound();
            ctrl.setPortfolio(p);
            ctrl.setRaccolta(r);
            UUID idPortfolio = ctrl.getIdPortfolio();
            UUID idRaccolta  = ctrl.getIdRaccolta(idPortfolio);
            ctrl.recuperaRaccolta(idRaccolta);
            List<EntityContenuto> contenuti = ctrl.recuperaContenutiRaccolta(idRaccolta);
            Map<String, Object> resp = new HashMap<>();
            resp.put("raccolta", r);
            resp.put("contenuti", contenuti);
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Aggiungi Contenuto al Portfolio ───────────────────────────────────────

    /** POST /api/portfolio/{idPortfolio}/contenuti
     *  Body: { idContenuto } */
    @POST
    @Path("/{idPortfolio}/contenuti")
    public Response aggiungiContenuto(@HeaderParam("X-User-Id") String userId,
                                       @PathParam("idPortfolio") String idPortfolioStr,
                                       Map<String, Object> data) {
        impostaUtente(userId);
        AggiungiContenutoCtrl ctrl = new AggiungiContenutoCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            ctrl.setPortfolio(p);
            UUID idContenuto = UUID.fromString((String) data.get("idContenuto"));
            ctrl.getIdPortfolio();
            EntityContenuto c = ctrl.recuperaContenuto(idContenuto);
            if (c == null)                                     return bad(ctrl.getErrorMessage());
            ctrl.setContenuto(c);
            ctrl.checkValid();
            ctrl.aggiungiContenuto(ctrl.getIdContenuto(p.getIdPortfolio()));
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Rimuovi Contenuto dal Portfolio ───────────────────────────────────────

    /** DELETE /api/portfolio/{idPortfolio}/contenuti/{idContenuto} */
    @DELETE
    @Path("/{idPortfolio}/contenuti/{idContenuto}")
    public Response rimuoviContenuto(@HeaderParam("X-User-Id") String userId,
                                      @PathParam("idPortfolio") String idPortfolioStr,
                                      @PathParam("idContenuto") String idContenutoStr) {
        impostaUtente(userId);
        RimuoviContenutoCtrl ctrl = new RimuoviContenutoCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            EntityContenuto c = db.recuperaContenuto(UUID.fromString(idContenutoStr));
            if (c == null) return notFound();
            ctrl.setPortfolio(p);
            ctrl.setContenuto(c);
            UUID idPortfolio = ctrl.getIdPortfolio();
            UUID idContenuto = ctrl.getIdContenuto(idPortfolio);
            ctrl.rimuoviContenuto(idContenuto);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Aggiungi / Rimuovi Contenuto dalla Raccolta (toggle) ─────────────────

    /** POST /api/portfolio/{idPortfolio}/raccolte/{idRaccolta}/contenuti
     *  Body: { idContenuto }
     *  Usa aggiornaStatoRaccolta (toggle): se non presente → aggiunge, se presente → rimuove. */
    @POST
    @Path("/{idPortfolio}/raccolte/{idRaccolta}/contenuti")
    public Response toggleContenutoRaccolta(@HeaderParam("X-User-Id") String userId,
                                             @PathParam("idPortfolio") String idPortfolioStr,
                                             @PathParam("idRaccolta") String idRaccoltaStr,
                                             Map<String, Object> data) {
        impostaUtente(userId);
        AggiungiAllaRaccoltaCtrl ctrl = new AggiungiAllaRaccoltaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            EntityRaccolta r = db.recuperaRaccolta(UUID.fromString(idRaccoltaStr));
            if (r == null) return notFound();
            EntityContenuto c = db.recuperaContenuto(UUID.fromString((String) data.get("idContenuto")));
            if (c == null) return notFound();
            ctrl.setPortfolio(p);
            ctrl.setRaccolta(r);
            ctrl.setContenuto(c);
            UUID idPortfolio = ctrl.getIdPortfolio();
            UUID idRaccolta  = ctrl.getIdRaccolta(idPortfolio);
            UUID idContenuto = ctrl.getIdContenuto(idRaccolta);
            ctrl.aggiornaStatoRaccolta(idRaccolta, idContenuto);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visualizza Contenuto ──────────────────────────────────────────────────

    /** GET /api/portfolio/{idPortfolio}/contenuti/{idContenuto} */
    @GET
    @Path("/{idPortfolio}/contenuti/{idContenuto}")
    public Response visualizzaContenuto(@HeaderParam("X-User-Id") String userId,
                                         @PathParam("idPortfolio") String idPortfolioStr,
                                         @PathParam("idContenuto") String idContenutoStr) {
        impostaUtente(userId);
        VisualizzaContenutoCtrl ctrl = new VisualizzaContenutoCtrl();
        try {
            EntityContenuto c = ctrl.recuperaContenuto(UUID.fromString(idContenutoStr));
            if (c == null) return notFound();
            return ok(Map.of("contenuto", c));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Ordina Sequenza ───────────────────────────────────────────────────────

    /** POST /api/portfolio/{idPortfolio}/ordina
     *  Body: { idContenuto1, idContenuto2 } – scambia le posizioni dei due contenuti */
    @POST
    @Path("/{idPortfolio}/ordina")
    public Response ordinaSequenza(@HeaderParam("X-User-Id") String userId,
                                    @PathParam("idPortfolio") String idPortfolioStr,
                                    Map<String, Object> data) {
        impostaUtente(userId);
        OrdinaSequenzaCtrl ctrl = new OrdinaSequenzaCtrl();
        try {
            EntityPortfolio p = db.recuperaPortfolio(UUID.fromString(idPortfolioStr));
            if (p == null) return notFound();
            ctrl.setPortfolio(p);
            UUID idContenuto1 = UUID.fromString((String) data.get("idContenuto1"));
            UUID idContenuto2 = UUID.fromString((String) data.get("idContenuto2"));
            EntityContenuto c1 = ctrl.recuperaContenuto(idContenuto1);
            EntityContenuto c2 = db.recuperaContenuto(idContenuto2);
            if (c1 == null || c2 == null) return bad("Contenuto non trovato.");
            ctrl.setContenuto2(c2);
            UUID idPortfolio = ctrl.getIdPortfolio();
            ctrl.recuperaPosizione(idPortfolio, c1);
            ctrl.recuperaPosizioneAdiacente(idPortfolio, c1);
            ctrl.aggiornaPosizione(c1, c2);
            return ok();
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

    private Response ok() {
        return Response.ok(Map.of("success", true)).build();
    }

    private Response ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return Response.ok(body).build();
    }

    private Response bad(String errore) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("success", false, "errore", errore))
                .build();
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("success", false, "errore", "Risorsa non trovata."))
                .build();
    }

    private Response server(String errore) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("success", false, "errore", errore != null ? errore : "Errore interno."))
                .build();
    }
}
