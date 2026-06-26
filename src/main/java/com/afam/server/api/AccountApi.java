package com.afam.server.api;

import com.afam.entities.EntityUtente;
import com.afam.server.control.gestisciaccount.*;
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
 * AccountApi – endpoint REST per il sottosistema Gestisci Account.
 * Base path: /api/account
 * Header richiesto: X-User-Id (UUID stringa) per tutte le operazioni.
 * @author Cristian Joshua Ingrao (0780672)
 */
@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountApi {

    private final DBMSBnd db = DBMSBnd.getInstance();

    // ── Logout ────────────────────────────────────────────────────────────────

    /** POST /api/account/logout */
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        LogoutCtrl ctrl = new LogoutCtrl();
        try {
            ctrl.recuperaStatoSessione();
            ctrl.aggiornaStatoSessione(Constants.SESSIONE_CHIUSA);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Reimposta password ────────────────────────────────────────────────────

    /** POST /api/account/reimposta-password
     *  Body: { vecchiaPassword, nuovaPassword } */
    @POST
    @Path("/reimposta-password")
    public Response reimpostaPassword(@HeaderParam("X-User-Id") String userId,
                                       Map<String, Object> data) {
        impostaUtente(userId);
        ReimpostaPasswordCtrl ctrl = new ReimpostaPasswordCtrl();
        try {
            if (!ctrl.verificaDati(data))                               return bad(ctrl.getErrorMessage());
            if (!ctrl.verificaVecchiaPassword((String) data.get("vecchiaPassword")))
                                                                        return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            ctrl.aggiornaPassword((String) data.get("nuovaPassword"));
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Modifica informazioni ─────────────────────────────────────────────────

    /** PUT /api/account/modifica
     *  Body: { nome, cognome }  — email e telefono si cambiano con flussi dedicati. */
    @PUT
    @Path("/modifica")
    public Response modificaInformazioni(@HeaderParam("X-User-Id") String userId,
                                          Map<String, Object> data) {
        impostaUtente(userId);
        ModificaInformazioniCtrl ctrl = new ModificaInformazioniCtrl();
        try {
            if (!ctrl.verificaDati(data))  return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            ctrl.aggiornaDati(data);
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Elimina account ───────────────────────────────────────────────────────

    /** DELETE /api/account
     *  Body: { password } */
    @DELETE
    public Response eliminaAccount(@HeaderParam("X-User-Id") String userId,
                                    Map<String, Object> data) {
        impostaUtente(userId);
        EliminaAccountCtrl ctrl = new EliminaAccountCtrl();
        try {
            UUID idUtente = ctrl.getIdUtente();
            if (!ctrl.verificaDati(data))               return bad(ctrl.getErrorMessage());
            if (!ctrl.verificaPassword((String) data.get("password")))
                                                        return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            ctrl.eliminaUtente(idUtente);
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Gestione 2FA ──────────────────────────────────────────────────────────

    /** GET /api/account/2fa */
    @GET
    @Path("/2fa")
    public Response getStato2FA(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        Gestione2FACtrl ctrl = new Gestione2FACtrl();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("stato2FA", ctrl.recuperaStato2FA());
        return Response.ok(resp).build();
    }

    /** POST /api/account/2fa/configura
     *  Body: { email, numero, abilita:bool } */
    @POST
    @Path("/2fa/configura")
    public Response configura2FA(@HeaderParam("X-User-Id") String userId,
                                  Map<String, Object> data) {
        impostaUtente(userId);
        Gestione2FACtrl ctrl = new Gestione2FACtrl();
        try {
            if (!ctrl.verificaDati(data))               return bad(ctrl.getErrorMessage());
            if (!ctrl.verificaStato2FA(
                    (String) data.get("email"),
                    (String) data.get("numero")))       return bad(ctrl.getErrorMessage());
            ctrl.checkValid();
            Boolean abilita = (Boolean) data.getOrDefault("abilita", false);
            ctrl.aggiornaStato2FA(abilita);
            return ok();
        } catch (IllegalStateException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Valida email ──────────────────────────────────────────────────────────

    /** POST /api/account/valida-email/richiedi
     *  Genera e invia OTP all'email dell'utente. */
    @POST
    @Path("/valida-email/richiedi")
    public Response richiediValidazioneEmail(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        ValidaEmailCtrl ctrl = new ValidaEmailCtrl();
        try {
            UUID           id       = ctrl.getIdUtente();
            OffsetDateTime scadenza = ctrl.avviaValidazione(id);
            if (scadenza == null) {
                // già validata
                Map<String, Object> r = new HashMap<>();
                r.put("success", true); r.put("giaValidata", true);
                return Response.ok(r).build();
            }
            Map<String, Object> r = new HashMap<>();
            r.put("success", true); r.put("scadenza", scadenza.toString());
            return Response.ok(r).build();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    /** POST /api/account/valida-email/conferma
     *  Body: { otp } */
    @POST
    @Path("/valida-email/conferma")
    public Response confermaValidazioneEmail(@HeaderParam("X-User-Id") String userId,
                                              Map<String, Object> data) {
        impostaUtente(userId);
        ValidaEmailCtrl ctrl = new ValidaEmailCtrl();
        try {
            String otp = (String) data.get("otp");
            if (!ctrl.completaValidazione(otp, true)) return bad("OTP non valido o scaduto.");
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Cambia email con OTP ──────────────────────────────────────────────────

    /** POST /api/account/cambia-email/richiedi
     *  Body: { nuovaEmail }
     *  Invia OTP alla nuova email; la salva temporaneamente in attesa di conferma. */
    @POST
    @Path("/cambia-email/richiedi")
    public Response richiediCambioEmail(@HeaderParam("X-User-Id") String userId,
                                         Map<String, Object> data) {
        impostaUtente(userId);
        try {
            String nuovaEmail = (String) data.get("nuovaEmail");
            if (nuovaEmail == null || nuovaEmail.isBlank()) return bad("Nuova email obbligatoria.");
            if (!com.afam.utils.Validators.isEmailValida(nuovaEmail))   return bad("Formato email non valido.");
            if (db.isMailInUse(nuovaEmail))                             return conflict("Email già in uso.");

            UUID           idUtente = UUID.fromString(userId);
            String         otp      = com.afam.utils.OTPGenerator.genera();
            OffsetDateTime scadenza = OffsetDateTime.now()
                    .plusMinutes(com.afam.utils.Constants.OTP_DURATION_MINUTES);

            db.salvaEmailTemporanea(idUtente, nuovaEmail);
            db.salvaCodiceOTP(otp, scadenza);
            com.afam.server.dao.MailServerBnd.getInstance().inviaEmail(nuovaEmail, otp);

            Map<String, Object> r = new HashMap<>();
            r.put("success", true);
            r.put("scadenza", scadenza.toString());
            return Response.ok(r).build();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    /** POST /api/account/cambia-email/conferma
     *  Body: { otp }
     *  Verifica OTP e aggiorna l'email con quella temporanea. */
    @POST
    @Path("/cambia-email/conferma")
    public Response confermaCambioEmail(@HeaderParam("X-User-Id") String userId,
                                         Map<String, Object> data) {
        impostaUtente(userId);
        try {
            UUID   idUtente  = UUID.fromString(userId);
            String otp       = (String) data.get("otp");
            String nuovaEmail = db.recuperaEmailTemporanea(idUtente);
            if (nuovaEmail == null) return bad("Nessuna richiesta di cambio email in corso.");

            String stored = db.recuperaOTP();
            OffsetDateTime scad = db.recuperaScadenzaOTP();
            if (stored == null || !stored.equals(otp))               return bad("OTP non valido.");
            if (scad == null || OffsetDateTime.now().isAfter(scad))  return bad("OTP scaduto.");

            db.aggiornaEmail(nuovaEmail);
            db.aggiornaStatoEmail(false);   // la nuova email va ri-validata
            db.rimuoviEmailTemporanea(idUtente);
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Valida numero ─────────────────────────────────────────────────────────

    /** POST /api/account/valida-numero/richiedi */
    @POST
    @Path("/valida-numero/richiedi")
    public Response richiediValidazioneNumero(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        ValidaNumeroCtrl ctrl = new ValidaNumeroCtrl();
        try {
            UUID           id       = ctrl.getIdUtente();
            OffsetDateTime scadenza = ctrl.avviaValidazione(id);
            if (scadenza == null) {
                Map<String, Object> r = new HashMap<>();
                r.put("success", true); r.put("giaValidato", true);
                return Response.ok(r).build();
            }
            Map<String, Object> r = new HashMap<>();
            r.put("success", true); r.put("scadenza", scadenza.toString());
            return Response.ok(r).build();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    /** POST /api/account/valida-numero/conferma
     *  Body: { otp } */
    @POST
    @Path("/valida-numero/conferma")
    public Response confermaValidazioneNumero(@HeaderParam("X-User-Id") String userId,
                                               Map<String, Object> data) {
        impostaUtente(userId);
        ValidaNumeroCtrl ctrl = new ValidaNumeroCtrl();
        try {
            String otp = (String) data.get("otp");
            if (!ctrl.completaValidazione(otp, true)) return bad("OTP non valido o scaduto.");
            return ok();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Info profilo (GET) ────────────────────────────────────────────────────

    /** GET /api/account/profilo */
    @GET
    @Path("/profilo")
    public Response getProfilo(@HeaderParam("X-User-Id") String userId) {
        impostaUtente(userId);
        try {
            EntityUtente utente = db.recuperaUtente(UUID.fromString(userId));
            if (utente == null) return Response.status(404).entity(error("Utente non trovato.")).build();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("idUtente",      utente.getIdUtente().toString());
            resp.put("nome",          utente.getNome());
            resp.put("cognome",       utente.getCognome());
            resp.put("email",         utente.getEmail());
            resp.put("numeroTelefono",utente.getNumTelefono());
            resp.put("dataNascita",   utente.getDataNascita());
            resp.put("emailValidata", utente.isEmailValidata());
            resp.put("numeroValidato",utente.isNumeroValidato());
            resp.put("stato2FA",      utente.isStato2FA());
            return Response.ok(resp).build();
        } catch (Exception e) {
            return server(e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void impostaUtente(String userId) {
        if (userId != null && !userId.isBlank()) {
            try { db.setCurrentUser(UUID.fromString(userId)); }
            catch (IllegalArgumentException ignored) {}
        }
    }

    private Response ok() {
        return Response.ok(Map.of("success", true)).build();
    }

    private Response bad(String msg) {
        return Response.status(400).entity(error(msg)).build();
    }

    private Response conflict(String msg) {
        return Response.status(409).entity(error(msg)).build();
    }

    private Response server(String msg) {
        return Response.serverError().entity(error(msg)).build();
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false); m.put("errore", msg);
        return m;
    }
}
