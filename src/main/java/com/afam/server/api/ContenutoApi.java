package com.afam.server.api;

import com.afam.entities.EntityContenuto;
import com.afam.server.control.gestiscicontenuti.*;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ContenutoApi – endpoint REST per il sottosistema Gestisci Contenuti.
 * Base path: /api/contenuti
 * Header richiesto: X-User-Id (UUID stringa).
 */
@Path("/contenuti")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContenutoApi {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Lista contenuti utente ────────────────────────────────────────────────

    /** GET /api/contenuti */
    @GET
    public Response elencoContenuti(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        try {
            List<EntityContenuto> lista = db.recuperaContenuti();
            return ok(Map.of("contenuti", lista));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Carica contenuto ──────────────────────────────────────────────────────

    /**
     * POST /api/contenuti
     * Body: { titolo, tipoFile, dimensione, percorsoStorage, visibilita? }
     * Il client invia i metadati del file; il file fisico è gestito lato client.
     */
    @POST
    public Response caricaContenuto(@HeaderParam("X-User-Id") String userId,
                                     Map<String, Object> data) {
        impostaUtente(userId);
        CaricaContenutoCtrl ctrl = new CaricaContenutoCtrl();
        try {
            String titolo       = (String) data.get("titolo");
            String tipoFile     = (String) data.get("tipoFile");
            long   dimensione   = toLong(data.get("dimensione"));
            String percorso     = (String) data.get("percorsoStorage");
            String visibilita   = data.containsKey("visibilita")
                                    ? (String) data.get("visibilita")
                                    : Constants.VIS_PRIVATO;

            ctrl.setTitolo(titolo);
            ctrl.setTipoFile(tipoFile);
            ctrl.setDimensione(dimensione);
            ctrl.setPercorsoStorage(percorso);

            if (!ctrl.verificaDimensione(dimensione))          return bad(ctrl.getErrorMessage());

            UUID idContenuto = ctrl.generaIdContenuto();
            EntityContenuto contenuto = new EntityContenuto(
                    idContenuto, titolo, tipoFile, dimensione, percorso,
                    visibilita, db.getCurrentUserId());

            if (ctrl.isFileInUso(contenuto))                   return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            ctrl.caricaContenuto(contenuto);
            return ok(Map.of("idContenuto", idContenuto.toString()));
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Elimina contenuto ─────────────────────────────────────────────────────

    /** DELETE /api/contenuti/{idContenuto} */
    @DELETE
    @Path("/{idContenuto}")
    public Response eliminaContenuto(@HeaderParam("X-User-Id") String userId,
                                      @PathParam("idContenuto") String idContenutoStr) {
        impostaUtente(userId);
        EliminaCtrl ctrl = new EliminaCtrl();
        try {
            EntityContenuto c = db.recuperaContenuto(UUID.fromString(idContenutoStr));
            if (c == null) return notFound();
            ctrl.setContenuto(c);
            UUID id = ctrl.getIdContenuto();
            ctrl.eliminaContenuto(id);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Modifica contenuto ────────────────────────────────────────────────────

    /**
     * PUT /api/contenuti/{idContenuto}
     * Body: { titolo?, tipoFile?, percorsoStorage?, visibilita? }
     */
    @PUT
    @Path("/{idContenuto}")
    public Response modificaContenuto(@HeaderParam("X-User-Id") String userId,
                                       @PathParam("idContenuto") String idContenutoStr,
                                       Map<String, Object> data) {
        impostaUtente(userId);
        ModificaCtrl ctrl = new ModificaCtrl();
        try {
            UUID id = UUID.fromString(idContenutoStr);
            EntityContenuto c = ctrl.recuperaContenuto(id);
            if (c == null) return notFound();

            String nuovoTitolo = (String) data.getOrDefault("titolo", c.getTitolo());
            ctrl.setNuovoTitolo(nuovoTitolo);

            if (!ctrl.verificaNome(nuovoTitolo))               return bad(ctrl.getErrorMessage());

            // Controlla unicità solo se il titolo è cambiato
            if (!nuovoTitolo.equals(c.getTitolo())) {
                Map<String, Object> nomeData = new HashMap<>();
                nomeData.put("nome", nuovoTitolo);
                if (ctrl.isNomeInUso(nomeData))                return bad(ctrl.getErrorMessage());
            }
            ctrl.checkValid();

            EntityContenuto aggiornato = new EntityContenuto(
                    c.getIdContenuto(),
                    nuovoTitolo,
                    data.containsKey("tipoFile")        ? (String) data.get("tipoFile")        : c.getTipoFile(),
                    c.getDimensione(),
                    data.containsKey("percorsoStorage") ? (String) data.get("percorsoStorage") : c.getPercorsoStorage(),
                    data.containsKey("visibilita")      ? (String) data.get("visibilita")      : c.getVisibilita(),
                    c.getIdUtente()
            );
            ctrl.aggiornaDati(aggiornato);
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Visibilità contenuto ──────────────────────────────────────────────────

    /**
     * PATCH /api/contenuti/{idContenuto}/visibilita
     * Body: { visibilita } oppure {} per ciclare al livello successivo.
     */
    @PATCH
    @Path("/{idContenuto}/visibilita")
    public Response aggiornaVisibilita(@HeaderParam("X-User-Id") String userId,
                                        @PathParam("idContenuto") String idContenutoStr,
                                        Map<String, Object> data) {
        impostaUtente(userId);
        VisibilitaCtrl ctrl = new VisibilitaCtrl();
        try {
            EntityContenuto c = db.recuperaContenuto(UUID.fromString(idContenutoStr));
            if (c == null) return notFound();
            ctrl.setContenuto(c);
            UUID   id         = ctrl.getIdContenuto();
            String attuale    = ctrl.recuperaVisibilita(id);
            String nuova      = (data != null && data.containsKey("visibilita"))
                                    ? (String) data.get("visibilita")
                                    : ctrl.alternaVisibilita(attuale);
            ctrl.aggiornaLivelloVisibilita(id, nuova);
            return ok(Map.of("visibilita", nuova));
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

    /** To long. */
    private long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) return Long.parseLong(s);
        return 0L;
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

    /** Bad. */
    private Response bad(String errore) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("success", false, "errore", errore))
                .build();
    }

    /** Not found. */
    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("success", false, "errore", "Contenuto non trovato."))
                .build();
    }

    /** Server. */
    private Response server(String errore) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("success", false, "errore", errore != null ? errore : "Errore interno."))
                .build();
    }
}
