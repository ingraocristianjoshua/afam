package com.afam.server.api;

import com.afam.entities.EntityUtente;
import com.afam.server.control.autenticati.AuthCtrl;
import com.afam.server.control.autenticati.RecuperaPasswordCtrl;
import com.afam.server.control.autenticati.Verifica2FACtrl;
import com.afam.server.dao.DBMSBnd;
import com.afam.utils.Constants;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AuthApi – endpoint REST per il sottosistema Autenticati.
 * Base path: /api/auth
 *
 * Tutti gli endpoint ricevono e restituiscono JSON.
 * Il corpo della risposta ha sempre la struttura:
 *   { "success": true/false, "data": {...}, "errore": "..." }
 *
 * Header richiesto per le richieste autenticate: X-User-Id (UUID stringa).
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthApi {

    // ── Campi ──────────────────
    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Registrazione ─────────────────────────────────────────────────────────

    /**
     * POST /api/auth/registra
     * Body: { nome, cognome, email, password, numeroTelefono (opt) }
     * Risposta 201: { success:true, idUtente }
     * Risposta 400: { success:false, errore:"..." }
     * Risposta 409: email già in uso
     */
    @POST
    @Path("/registra")
    public Response registra(Map<String, Object> data) {
        AuthCtrl ctrl = new AuthCtrl();
        try {
            ctrl.compilaFormRegistrati(data);

            if (!ctrl.verificaDati(data)) {
                return bad(ctrl.getErrorMessage());
            }

            if (ctrl.isMailInUse((String) data.get("email"))) {
                return conflict("Email già in uso.");
            }

            ctrl.checkValid();

            UUID idUtente = ctrl.generaIdUtente();
            ctrl.creaAccount(data, idUtente);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("idUtente", idUtente.toString());
            return Response.status(201).entity(resp).build();

        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Login standard ────────────────────────────────────────────────────────

    /**
     * POST /api/auth/accedi
     * Body: { email, password }
     * Risposta 200: { success:true, idUtente, richiede2FA:bool }
     * Risposta 401: credenziali errate
     */
    @POST
    @Path("/accedi")
    public Response accedi(Map<String, Object> data) {
        AuthCtrl ctrl = new AuthCtrl();
        try {
            if (!ctrl.verificaDati(data)) return bad(ctrl.getErrorMessage());
            ctrl.checkValid();

            EntityUtente utente = ctrl.verificaCredenziali(data);
            if (utente == null) {
                return Response.status(401)
                        .entity(error("Credenziali non valide.")).build();
            }

            boolean richiede2FA = db.recuperaStato2FA();
            if (!richiede2FA) {
                db.aggiornaStatoSessione(Constants.SESSIONE_APERTA);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("idUtente", utente.getIdUtente().toString());
            resp.put("nome", utente.getNome());
            resp.put("cognome", utente.getCognome());
            resp.put("richiede2FA", richiede2FA);
            return Response.ok(resp).build();

        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Verifica 2FA ──────────────────────────────────────────────────────────

    /**
     * POST /api/auth/verifica-2fa
     * Header: X-User-Id
     * Body: { otp, scadenza (ISO-8601) }
     * Risposta 200: { success:true }
     */
    @POST
    @Path("/verifica-2fa")
    public Response verifica2FA(@HeaderParam("X-User-Id") String userId,
                                 Map<String, Object> data) {
        impostaUtente(userId);
        Verifica2FACtrl ctrl = new Verifica2FACtrl();
        try {
            String otp      = (String) data.get("otp");
            String scadStr  = (String) data.get("scadenza");
            OffsetDateTime scad = scadStr != null
                    ? OffsetDateTime.parse(scadStr) : OffsetDateTime.now().plusMinutes(1);

            if (!ctrl.recuperaOTP(otp, scad)) {
                return bad("OTP non valido o scaduto.");
            }

            db.aggiornaStatoSessione(Constants.SESSIONE_APERTA);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Invio OTP 2FA (per il flusso completo) ────────────────────────────────

    /**
     * POST /api/auth/invia-2fa
     * Header: X-User-Id
     * Genera e invia OTP via SMS al numero dell'utente.
     */
    @POST
    @Path("/invia-2fa")
    public Response invia2FA(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        Verifica2FACtrl ctrl = new Verifica2FACtrl();
        try {
            String otp = ctrl.generaOTP();
            OffsetDateTime scad = OffsetDateTime.now()
                    .plusMinutes(Constants.OTP_DURATION_MINUTES);
            ctrl.salvaCodiceOTP(otp, scad);

            // legge il numero dall'entity recuperato dal DB
            EntityUtente utente = db.recuperaUtente(UUID.fromString(userId));
            if (utente == null) return bad("Utente non trovato.");
            // 2FA: invio dell'OTP SOLO via SMS al numero di telefono (non via email)
            ctrl.inviaSMS(utente.getNumTelefono(), otp);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("scadenza", scad.toString());
            return Response.ok(resp).build();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Recupero password – fase 1: richiesta OTP ────────────────────────────

    /**
     * POST /api/auth/recupera-password/richiedi
     * Body: { email }
     */
    @POST
    @Path("/recupera-password/richiedi")
    public Response richiediRecuperoPassword(Map<String, Object> data) {
        RecuperaPasswordCtrl ctrl = new RecuperaPasswordCtrl();
        try {
            if (!ctrl.verificaDati(data)) return bad(ctrl.getErrorMessage());
            String email = (String) data.get("email");
            if (!ctrl.verificaEmailAssociata(email)) return bad(ctrl.getErrorMessage());
            ctrl.checkValid();

            // imposta utente corrente per salvaCodiceOTP
            EntityUtente utente = db.recuperaUtente(null); // recupera per email
            // cerca per email
            utente = cercaUtentePerEmail(email);
            if (utente != null) db.setCurrentUser(utente.getIdUtente());

            String otp = ctrl.generaOTP();
            db.salvaCodiceOTP(otp, ctrl.getOtpScadenza());
            ctrl.inviaOTPviaEmail(email, otp);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("scadenza", ctrl.getOtpScadenza().toString());
            return Response.ok(resp).build();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Recupero password – fase 2: verifica OTP ─────────────────────────────

    /**
     * POST /api/auth/recupera-password/verifica-otp
     * Body: { email, otp, scadenza }
     * Verifica che l'OTP sia valido senza ancora modificare la password.
     * Risposta 200: { success:true }
     * Risposta 400: OTP non valido o scaduto
     */
    @POST
    @Path("/recupera-password/verifica-otp")
    public Response verificaOTPRecupero(Map<String, Object> data) {
        RecuperaPasswordCtrl ctrl = new RecuperaPasswordCtrl();
        try {
            String email   = (String) data.get("email");
            String otp     = (String) data.get("otp");
            String scadStr = (String) data.get("scadenza");
            if (email == null || otp == null || scadStr == null) {
                return bad("Parametri mancanti: email, otp, scadenza obbligatori.");
            }
            OffsetDateTime scad = OffsetDateTime.parse(scadStr);

            EntityUtente utente = cercaUtentePerEmail(email);
            if (utente == null) return bad("Utente non trovato.");
            db.setCurrentUser(utente.getIdUtente());

            if (!ctrl.recuperaOTP(otp, scad)) return bad("OTP non valido o scaduto.");
            return ok();
        } catch (IllegalArgumentException e) {
            return bad("Formato scadenza non valido.");
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Recupero password – fase 3: reset con OTP ────────────────────────────

    /**
     * POST /api/auth/recupera-password/reimposta
     * Body: { email, otp, scadenza, nuovaPassword }
     */
    @POST
    @Path("/recupera-password/reimposta")
    public Response reimpostaPassword(Map<String, Object> data) {
        RecuperaPasswordCtrl ctrl = new RecuperaPasswordCtrl();
        try {
            String email   = (String) data.get("email");
            String otp     = (String) data.get("otp");
            String scadStr = (String) data.get("scadenza");
            OffsetDateTime scad = OffsetDateTime.parse(scadStr);

            EntityUtente utente = cercaUtentePerEmail(email);
            if (utente == null) return bad("Utente non trovato.");
            db.setCurrentUser(utente.getIdUtente());

            if (!ctrl.recuperaOTP(otp, scad)) return bad("OTP non valido o scaduto.");
            if (!ctrl.verificaDati(data, scad)) return bad(ctrl.getErrorMessage());
            ctrl.checkValid();

            ctrl.aggiornaPassword((String) data.get("nuovaPassword"));
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Helper privati ────────────────────────────────────────────────────────

    private void impostaUtente(String userId) {
        if (userId != null && !userId.isBlank()) {
            try { db.setCurrentUser(UUID.fromString(userId)); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    /** Cerca utente per email. */
    private EntityUtente cercaUtentePerEmail(String email) {
        return db.recuperaUtentePerEmail(email);
    }

    // ── Factory response ─────────────────────────────────────────────────────

    private Response ok() {
        return Response.ok(Map.of("success", true)).build();
    }

    /** Bad. */
    private Response bad(String msg) {
        return Response.status(400).entity(error(msg)).build();
    }

    /** Conflict. */
    private Response conflict(String msg) {
        return Response.status(409).entity(error(msg)).build();
    }

    /** Server. */
    private Response server(String msg) {
        return Response.serverError().entity(error(msg)).build();
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false);
        m.put("errore", msg);
        return m;
    }
}
