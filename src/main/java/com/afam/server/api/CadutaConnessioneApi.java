package com.afam.server.api;

import com.afam.server.control.gestiscicadutaconnessione.GestisciCadutaDiConnessioneCtrl;
import com.afam.server.dao.DBMSBnd;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CadutaConnessioneApi – endpoint REST per il sottosistema Gestisci Caduta di Connessione.
 * Base path: /api/sessione
 *
 * Utilizzato dal client per:
 *   - verificare lo stato della connessione al DB
 *   - tentare il ripristino delle operazioni sospese
 *   - attivare il rollback in caso di connessione non recuperabile
 */
@Path("/sessione")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CadutaConnessioneApi {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Stato connessione ─────────────────────────────────────────────────────

    /**
     * GET /api/sessione/stato
     * Verifica se il DB è raggiungibile e se ci sono dati locali sospesi.
     */
    @GET
    @Path("/stato")
    public Response verificaStato(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        GestisciCadutaDiConnessioneCtrl ctrl = new GestisciCadutaDiConnessioneCtrl();
        try {
            boolean connesso = ctrl.verificaStatoConnessione();
            Map<String, Object> resp = new HashMap<>();
            resp.put("connesso",       connesso);
            resp.put("hasDatiLocali",  ctrl.hasDatiLocali());
            resp.put("dimensioneCoda", ctrl.dimensioneCoda());
            return ok(resp);
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Ripristino dati ───────────────────────────────────────────────────────

    /**
     * POST /api/sessione/ripristina
     * Tenta di inviare al DB le operazioni accumulate localmente durante la disconnessione.
     */
    @POST
    @Path("/ripristina")
    public Response ripristina(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        GestisciCadutaDiConnessioneCtrl ctrl = new GestisciCadutaDiConnessioneCtrl();
        try {
            int n = ctrl.tentaRipristino();
            if (n == -1) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(Map.of("success", false,
                                "errore", "Il database non è ancora raggiungibile."))
                        .build();
            }
            return ok(Map.of("operazioniRipristinate", n));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Rollback esplicito ────────────────────────────────────────────────────

    /**
     * POST /api/sessione/rollback
     * Richiede il rollback delle operazioni parziali e la notifica dell'errore.
     */
    @POST
    @Path("/rollback")
    public Response rollback(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        GestisciCadutaDiConnessioneCtrl ctrl = new GestisciCadutaDiConnessioneCtrl();
        try {
            String messaggio = ctrl.gestisciCaduta();
            return ok(Map.of("messaggio", messaggio));
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Salva operazione sospesa ──────────────────────────────────────────────

    /**
     * POST /api/sessione/salva-locale
     * Salva un'operazione nella coda locale del server (usato in caso di errore DB parziale).
     * Body: { tipoOperazione, payload, … }
     */
    @POST
    @Path("/salva-locale")
    public Response salvaLocale(@HeaderParam("X-User-Id") String userId,
                                 Map<String, Object> data) {
        impostaUtente(userId);
        GestisciCadutaDiConnessioneCtrl ctrl = new GestisciCadutaDiConnessioneCtrl();
        try {
            ctrl.salvaDatiLocali(data);
            return ok(Map.of("salvato", true));
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

    /** Ok. */
    private Response ok(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return Response.ok(body).build();
    }

    /** Server. */
    private Response server(String errore) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("success", false, "errore", errore != null ? errore : "Errore interno."))
                .build();
    }
}
