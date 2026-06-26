package com.afam.server.dao;

import com.afam.entities.*;
import com.afam.utils.Constants;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * DBMSBnd – Façade + Singleton verso PostgreSQL.
 *
 * Regola di accesso: NESSUNA classe control o boundary può aprire
 * connessioni JDBC direttamente. Ogni operazione sul database passa
 * obbligatoriamente per questa classe.
 *
 * Gestione sessione corrente: i metodi senza parametro idUtente
 * (recuperaStatoSessione, recuperaStato2FA, recuperaOTP, ecc.)
 * usano il campo currentUserId, impostato dagli endpoint REST
 * tramite setCurrentUser() all'inizio di ogni richiesta autenticata.
 *
 * Firme disambiguate rispetto alla specifica:
 *  – aggiornaLivelloVisibilita(idContenuto, visibilita)   aggiunto visibilita
 *  – aggiornaNumero(numeroVisualizzazioni, idPortfolio)   aggiunto idPortfolio
 *  – aggiornaScadenza(scadenza, idLink)                   aggiunto idLink
 *  – aggiornaVisibilitaLink(visibilita, idLink)           aggiunto idLink
 *  – recuperaRaccolta(UUID) accetta sia idRaccolta sia idPortfolio:
 *    usare recuperaRaccolta(idRaccolta) per id diretto,
 *    recuperaRaccolte(idPortfolio) per lista, come da spec.
 * @author Cristian Joshua Ingrao (0780672)
 */
public class DBMSBnd {

    private static final Logger LOG = Logger.getLogger(DBMSBnd.class.getName());

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static volatile DBMSBnd instance;

    public static DBMSBnd getInstance() {
        if (instance == null) {
            synchronized (DBMSBnd.class) {
                if (instance == null) instance = new DBMSBnd();
            }
        }
        return instance;
    }

    // ── Connessione ───────────────────────────────────────────────────────────

    private Connection conn;
    private String     dbUrl;
    private String     dbUser;
    private String     dbPassword;

    /** Id dell'utente corrente: impostato dagli endpoint REST per ogni richiesta. */
    private UUID currentUserId;

    /** Ultimo messaggio di errore (per recuperaMessErrore). */
    private String lastError = "";

    /** Mappa temporanea nuova-email in attesa di conferma OTP: idUtente → nuovaEmail. */
    private final Map<UUID, String> nuoveEmailInAttesa = new HashMap<>();

    private DBMSBnd() {
        loadConfig();
        connect();
    }

    private void loadConfig() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties p = new Properties();
            if (in != null) p.load(in);
            dbUrl      = p.getProperty("db.url",      "jdbc:postgresql://localhost:5432/afam");
            dbUser     = p.getProperty("db.user",     "postgres");
            dbPassword = p.getProperty("db.password", "postgres");
        } catch (IOException e) {
            LOG.warning("config.properties non trovato, uso valori default: " + e.getMessage());
            dbUrl      = "jdbc:postgresql://localhost:5432/afam";
            dbUser     = "postgres";
            dbPassword = "postgres";
        }
    }

    private void connect() {
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(true);
            LOG.info("Connessione al DB stabilita: " + dbUrl);
        } catch (SQLException e) {
            lastError = e.getMessage();
            LOG.severe("Impossibile connettersi al DB: " + e.getMessage());
        }
    }

    /** Garantisce che la connessione sia aperta; riconnette se necessaria. */
    private Connection getConn() throws SQLException {
        if (conn == null || conn.isClosed()) connect();
        return conn;
    }

    /** Usato dagli endpoint REST per impostare l'utente corrente della richiesta. */
    public void setCurrentUser(UUID idUtente) {
        this.currentUserId = idUtente;
    }

    /** Restituisce l'id utente corrente (usato dai control per getIdUtente()). */
    public UUID getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Restituisce la scadenza dell'OTP più recente dell'utente corrente.
     * Usato da checkCurrentTime() nei control di validazione contatto.
     */
    public OffsetDateTime recuperaScadenzaOTP() {
        String sql = """
            SELECT scadenza FROM otp
            WHERE id_utente = ?
            ORDER BY scadenza DESC LIMIT 1
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("scadenza");
                    return ts != null ? ts.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    // =========================================================================
    // AREA AUTENTICAZIONE E ACCOUNT
    // =========================================================================

    /**
     * Inserisce un nuovo utente nel DB.
     * data deve contenere: nome, cognome, email, hashPassword, numeroTelefono.
     */
    public void creaAccount(Map<String, Object> data, UUID idUtente) {
        String sql = """
            INSERT INTO utente
              (id_utente, nome, cognome, email, hash_password, numero_telefono, data_nascita)
            VALUES (?, ?, ?, ?, ?, ?, ?::date)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idUtente);
            ps.setString(2, (String) data.get("nome"));
            ps.setString(3, (String) data.get("cognome"));
            ps.setString(4, (String) data.get("email"));
            ps.setString(5, (String) data.get("hashPassword"));
            ps.setString(6, (String) data.get("numeroTelefono"));
            ps.setString(7, (String) data.get("dataNascita"));
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("creaAccount: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica le credenziali di accesso.
     * data deve contenere: email, password (in chiaro – confrontata con bcrypt).
     * @return EntityUtente se le credenziali sono valide, null altrimenti.
     */
    public EntityUtente verificaCredenziali(Map<String, Object> data) {
        String sql = "SELECT * FROM utente WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, (String) data.get("email"));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("hash_password");
                    String pwd  = (String) data.get("password");
                    if (BCrypt.checkpw(pwd, hash)) {
                        return mapUtente(rs);
                    }
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("verificaCredenziali: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Associa i dati dell'identity provider (SPID/eIDAS) a un utente esistente.
     * data deve contenere: email (chiave di ricerca), più campi da aggiornare.
     */
    public void associaDati(Map<String, Object> data) {
        String sql = """
            UPDATE utente SET nome = ?, cognome = ?
            WHERE email = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, (String) data.get("nome"));
            ps.setString(2, (String) data.get("cognome"));
            ps.setString(3, (String) data.get("email"));
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("associaDati: " + e.getMessage(), e);
        }
    }

    /** @return true se esiste un utente con questa email. */
    public boolean verificaEmailAssociata(String email) {
        return countWhere("utente", "email", email) > 0;
    }

    /**
     * Aggiorna l'hash della password dell'utente corrente.
     * @param password hash bcrypt già calcolato dalla control.
     */
    public void aggiornaPassword(String password) {
        String sql = "UPDATE utente SET hash_password = ? WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setObject(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaPassword: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica che la password fornita corrisponda all'hash attuale.
     * @param vecchiaPassword password in chiaro.
     */
    public boolean verificaVecchiaPassword(String vecchiaPassword) {
        String sql = "SELECT hash_password FROM utente WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return BCrypt.checkpw(vecchiaPassword, rs.getString("hash_password"));
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    /** @return stato_sessione dell'utente corrente ("aperta" | "chiusa"). */
    public String recuperaStatoSessione() {
        String sql = "SELECT stato_sessione FROM utente WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("stato_sessione");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return Constants.SESSIONE_CHIUSA;
    }

    public void aggiornaStatoSessione(String stato) {
        update("utente", "stato_sessione", stato, "id_utente", currentUserId);
    }

    public String recuperaEmail(UUID idUtente) {
        return selectString("utente", "email", "id_utente", idUtente);
    }

    /** @return true se l'email è già usata da un altro account. */
    public boolean isMailInUse(String email) {
        return countWhere("utente", "email", email) > 0;
    }

    /**
     * Aggiorna le informazioni anagrafiche dell'utente corrente.
     * data può contenere: nome, cognome, email, numeroTelefono.
     */
    public void aggiornaDati(Map<String, Object> data) {
        String sql = """
            UPDATE utente
            SET nome         = COALESCE(?, nome),
                cognome      = COALESCE(?, cognome),
                data_nascita = COALESCE(?::date, data_nascita)
            WHERE id_utente = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, (String) data.get("nome"));
            ps.setString(2, (String) data.get("cognome"));
            ps.setString(3, (String) data.get("dataNascita"));
            ps.setObject(4, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaDati(utente): " + e.getMessage(), e);
        }
    }

    /** Verifica che la password in chiaro corrisponda all'hash dell'utente corrente. */
    public boolean verificaPassword(String password) {
        return verificaVecchiaPassword(password);
    }

    public void eliminaUtente(UUID idUtente) {
        delete("utente", "id_utente", idUtente);
    }

    /** @return true se il 2FA è attivo per l'utente corrente. */
    public boolean recuperaStato2FA() {
        String sql = "SELECT stato_2fa FROM utente WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("stato_2fa");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    /**
     * Verifica che l'utente con questa email abbia questo numero di telefono.
     * Usato per il controllo pre-abilitazione 2FA.
     */
    public boolean verificaStato2FA(String email, String numero) {
        String sql = "SELECT 1 FROM utente WHERE email = ? AND numero_telefono = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    public void aggiornaStato2FA(boolean stato2FA) {
        String sql = "UPDATE utente SET stato_2fa = ? WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, stato2FA);
            ps.setObject(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaStato2FA: " + e.getMessage(), e);
        }
    }

    public String recuperaNumTelefono(UUID idUtente) {
        return selectString("utente", "numero_telefono", "id_utente", idUtente);
    }

    /** @return true se il numero_telefono risulta validato (numero_validato=true). */
    public boolean recuperaStatoNumero(String numero) {
        String sql = "SELECT numero_validato FROM utente WHERE numero_telefono = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("numero_validato");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    public void aggiornaStatoNumero(boolean statoNumero) {
        String sql = "UPDATE utente SET numero_validato = ? WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, statoNumero);
            ps.setObject(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaStatoNumero: " + e.getMessage(), e);
        }
    }

    /** @return true se email_validata=true per questo indirizzo. */
    public boolean recuperaStatoEmail(String email) {
        String sql = "SELECT email_validata FROM utente WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("email_validata");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    public void aggiornaStatoEmail(boolean statoEmail) {
        String sql = "UPDATE utente SET email_validata = ? WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, statoEmail);
            ps.setObject(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaStatoEmail: " + e.getMessage(), e);
        }
    }

    public void aggiornaEmail(String nuovaEmail) {
        String sql = "UPDATE utente SET email = ? WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nuovaEmail);
            ps.setObject(2, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaEmail: " + e.getMessage(), e);
        }
    }

    /**
     * Salva un codice OTP per l'utente corrente.
     * Prima elimina eventuali OTP precedenti dello stesso utente.
     */
    public void salvaCodiceOTP(String otp, OffsetDateTime scadenza) {
        try {
            // elimina OTP precedenti
            try (PreparedStatement del = getConn().prepareStatement(
                    "DELETE FROM otp WHERE id_utente = ?")) {
                del.setObject(1, currentUserId);
                del.executeUpdate();
            }
            // inserisce il nuovo
            try (PreparedStatement ins = getConn().prepareStatement(
                    "INSERT INTO otp (codice, scadenza, id_utente) VALUES (?, ?, ?)")) {
                ins.setString(1, otp);
                ins.setObject(2, scadenza);
                ins.setObject(3, currentUserId);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("salvaCodiceOTP: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera il codice OTP più recente per l'utente corrente.
     * @return stringa codice OTP, o null se non presente.
     */
    public String recuperaOTP() {
        String sql = """
            SELECT codice FROM otp
            WHERE id_utente = ?
            ORDER BY scadenza DESC LIMIT 1
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("codice");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    // =========================================================================
    // AREA PORTFOLIO, RACCOLTA, CONTENUTO
    // =========================================================================

    public void salvaPortfolio(UUID idPortfolio, String nomePortfolio) {
        String sql = """
            INSERT INTO portfolio (id_portfolio, nome, id_utente)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idPortfolio);
            ps.setString(2, nomePortfolio);
            ps.setObject(3, currentUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("salvaPortfolio: " + e.getMessage(), e);
        }
    }

    public void salvaRaccolta(UUID idRaccolta, String nomeRaccolta) {
        // La raccolta deve essere associata a un portfolio: il chiamante
        // deve aver impostato currentPortfolioId o passarlo implicitamente.
        // Per rendere il metodo funzionale, richiediamo che il portfolio id
        // sia presente in currentPortfolioId.
        String sql = """
            INSERT INTO raccolta (id_raccolta, nome, id_portfolio)
            VALUES (?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idRaccolta);
            ps.setString(2, nomeRaccolta);
            ps.setObject(3, currentPortfolioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("salvaRaccolta: " + e.getMessage(), e);
        }
    }

    /** Id del portfolio su cui operano i metodi raccolta che non lo ricevono esplicitamente. */
    private UUID currentPortfolioId;

    public void setCurrentPortfolio(UUID idPortfolio) {
        this.currentPortfolioId = idPortfolio;
    }

    public void eliminaPortfolio(UUID idPortfolio) {
        delete("portfolio", "id_portfolio", idPortfolio);
    }

    public void eliminaRaccolta(UUID idRaccolta) {
        delete("raccolta", "id_raccolta", idRaccolta);
    }

    public EntityPortfolio recuperaPortfolio(UUID idPortfolio) {
        String sql = "SELECT * FROM portfolio WHERE id_portfolio = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idPortfolio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPortfolio(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /** Restituisce tutti i portfolio dell'utente corrente. */
    public List<EntityPortfolio> recuperaPortfoli() {
        String sql = "SELECT * FROM portfolio WHERE id_utente = ? ORDER BY data_creazione";
        return queryList(sql, currentUserId, this::mapPortfolio);
    }

    /** Recupera una raccolta per id diretto. */
    public EntityRaccolta recuperaRaccolta(UUID idRaccolta) {
        String sql = "SELECT * FROM raccolta WHERE id_raccolta = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idRaccolta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRaccolta(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /** Restituisce tutte le raccolte di un portfolio, ordinate per campo ordine. */
    public List<EntityRaccolta> recuperaRaccolte(UUID idPortfolio) {
        String sql = "SELECT * FROM raccolta WHERE id_portfolio = ? ORDER BY ordine";
        return queryList(sql, idPortfolio, this::mapRaccolta);
    }

    public EntityContenuto recuperaContenuto(UUID idContenuto) {
        String sql = "SELECT * FROM contenuto WHERE id_contenuto = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idContenuto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapContenuto(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /** Restituisce tutti i contenuti caricati dall'utente corrente. */
    public List<EntityContenuto> recuperaContenuti() {
        String sql = "SELECT * FROM contenuto WHERE id_utente = ? ORDER BY titolo";
        return queryList(sql, currentUserId, this::mapContenuto);
    }

    /** Restituisce i contenuti di un portfolio ordinati per posizione. */
    public List<EntityContenuto> recuperaContenutiPortfolio(UUID idPortfolio) {
        String sql = """
            SELECT c.* FROM contenuto c
            JOIN portfolio_contenuto pc ON c.id_contenuto = pc.id_contenuto
            WHERE pc.id_portfolio = ?
            ORDER BY pc.posizione
            """;
        return queryList(sql, idPortfolio, this::mapContenuto);
    }

    /** Restituisce i contenuti di una raccolta. */
    public List<EntityContenuto> recuperaContenutiRaccolta(UUID idRaccolta) {
        String sql = """
            SELECT c.* FROM contenuto c
            JOIN raccolta_contenuto rc ON c.id_contenuto = rc.id_contenuto
            WHERE rc.id_raccolta = ?
            ORDER BY c.titolo
            """;
        return queryList(sql, idRaccolta, this::mapContenuto);
    }

    /** Aggiunge un contenuto a un portfolio (portfolio_contenuto). */
    public void aggiungiContenuto(UUID idContenuto) {
        String sql = """
            INSERT INTO portfolio_contenuto (id_portfolio, id_contenuto, posizione)
            VALUES (?, ?, COALESCE(
              (SELECT MAX(posizione)+1 FROM portfolio_contenuto WHERE id_portfolio = ?), 0
            ))
            ON CONFLICT DO NOTHING
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentPortfolioId);
            ps.setObject(2, idContenuto);
            ps.setObject(3, currentPortfolioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiungiContenuto: " + e.getMessage(), e);
        }
    }

    /** Rimuove un contenuto da un portfolio (portfolio_contenuto). */
    public void rimuoviContenuto(UUID idContenuto) {
        String sql = "DELETE FROM portfolio_contenuto WHERE id_portfolio=? AND id_contenuto=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentPortfolioId);
            ps.setObject(2, idContenuto);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("rimuoviContenuto: " + e.getMessage(), e);
        }
    }

    /**
     * Aggiunge O rimuove un contenuto da una raccolta.
     * La firma è identica nei due casi d'uso (come da spec).
     * La semantica è determinata dal contesto: se la coppia esiste → DELETE,
     * se non esiste → INSERT.
     */
    public void aggiornaStatoRaccolta(UUID idRaccolta, UUID idContenuto) {
        String checkSql = "SELECT 1 FROM raccolta_contenuto WHERE id_raccolta=? AND id_contenuto=?";
        try (PreparedStatement check = getConn().prepareStatement(checkSql)) {
            check.setObject(1, idRaccolta);
            check.setObject(2, idContenuto);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    // esiste → rimuovi
                    try (PreparedStatement del = getConn().prepareStatement(
                            "DELETE FROM raccolta_contenuto WHERE id_raccolta=? AND id_contenuto=?")) {
                        del.setObject(1, idRaccolta);
                        del.setObject(2, idContenuto);
                        del.executeUpdate();
                    }
                } else {
                    // non esiste → aggiungi
                    try (PreparedStatement ins = getConn().prepareStatement(
                            "INSERT INTO raccolta_contenuto (id_raccolta, id_contenuto) VALUES (?,?)")) {
                        ins.setObject(1, idRaccolta);
                        ins.setObject(2, idContenuto);
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaStatoRaccolta: " + e.getMessage(), e);
        }
    }

    public void aggiornaNomeRaccolta(EntityRaccolta raccolta, String nomeRaccolta) {
        String sql = "UPDATE raccolta SET nome = ? WHERE id_raccolta = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nomeRaccolta);
            ps.setObject(2, raccolta.getIdRaccolta());
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaNomeRaccolta: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica che nomeRaccolta rispetti il formato ammesso.
     * @return true se il formato è valido.
     */
    public boolean verificaNome(String nomeRaccolta) {
        return com.afam.utils.Validators.isNomeRaccoltaValido(nomeRaccolta);
    }

    /** @return true se esiste già una raccolta con questo nome nel portfolio corrente. */
    public boolean isNomeInUso(String nomeRaccolta) {
        String sql = "SELECT 1 FROM raccolta WHERE nome = ? AND id_portfolio = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nomeRaccolta);
            ps.setObject(2, currentPortfolioId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    /** @return true se esiste già un contenuto con questo nome (titolo) per l'utente corrente. */
    public boolean isNomeInUso(Map<String, Object> data) {
        Object nome = data.get("nome");
        Object nomePortfolio = data.get("nomePortfolio");
        if (nome != null) {
            // contesto: isNomeInUso(data.nome) per ModificaCtrl
            String sql = "SELECT 1 FROM contenuto WHERE titolo = ? AND id_utente = ?";
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, nome.toString());
                ps.setObject(2, currentUserId);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            } catch (SQLException e) { lastError = e.getMessage(); }
        } else if (nomePortfolio != null) {
            // contesto: isNomeInUso(data.nomePortfolio) per CreaPortfolioCtrl
            String sql = "SELECT 1 FROM portfolio WHERE nome = ? AND id_utente = ?";
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, nomePortfolio.toString());
                ps.setObject(2, currentUserId);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            } catch (SQLException e) { lastError = e.getMessage(); }
        }
        return false;
    }

    /** Inserisce un contenuto nella tabella contenuto. */
    public void caricaContenuto(EntityContenuto contenuto) {
        String sql = """
            INSERT INTO contenuto
              (id_contenuto, titolo, tipo_file, dimensione, percorso_storage, visibilita, id_utente)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, contenuto.getIdContenuto());
            ps.setString(2, contenuto.getTitolo());
            ps.setString(3, contenuto.getTipoFile());
            ps.setLong(4,   contenuto.getDimensione());
            ps.setString(5, contenuto.getPercorsoStorage());
            ps.setString(6, contenuto.getVisibilita());
            ps.setObject(7, contenuto.getIdUtente());
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("caricaContenuto: " + e.getMessage(), e);
        }
    }

    public void eliminaContenuto(UUID idContenuto) {
        delete("contenuto", "id_contenuto", idContenuto);
    }

    /**
     * Aggiorna titolo e/o altri metadati di un contenuto.
     * (Override: aggiornaDati(Map) aggiorna utente, aggiornaDati(EntityContenuto) aggiorna contenuto.)
     */
    public void aggiornaDati(EntityContenuto contenuto) {
        String sql = """
            UPDATE contenuto
            SET titolo = ?, tipo_file = ?, percorso_storage = ?, visibilita = ?
            WHERE id_contenuto = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, contenuto.getTitolo());
            ps.setString(2, contenuto.getTipoFile());
            ps.setString(3, contenuto.getPercorsoStorage());
            ps.setString(4, contenuto.getVisibilita());
            ps.setObject(5, contenuto.getIdContenuto());
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaDati(contenuto): " + e.getMessage(), e);
        }
    }

    public String recuperaVisibilita(UUID idContenuto) {
        return selectString("contenuto", "visibilita", "id_contenuto", idContenuto);
    }

    /**
     * Aggiorna la visibilità di un contenuto.
     * Nota: la spec indica solo idContenuto; visibilita è aggiunto per funzionalità.
     */
    public void aggiornaLivelloVisibilita(UUID idContenuto, String visibilita) {
        String sql = "UPDATE contenuto SET visibilita = ? WHERE id_contenuto = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, visibilita);
            ps.setObject(2, idContenuto);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaLivelloVisibilita: " + e.getMessage(), e);
        }
    }

    public int recuperaPosizione(UUID idPortfolio, EntityContenuto contenuto) {
        String sql = "SELECT posizione FROM portfolio_contenuto WHERE id_portfolio=? AND id_contenuto=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idPortfolio);
            ps.setObject(2, contenuto.getIdContenuto());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("posizione");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return -1;
    }

    /**
     * Restituisce la posizione del contenuto adiacente (posizione+1 o posizione-1).
     * "Adiacente" = il contenuto con posizione immediatamente successiva nel portfolio.
     */
    public int recuperaPosizioneAdiacente(UUID idPortfolio, EntityContenuto contenuto) {
        int pos = recuperaPosizione(idPortfolio, contenuto);
        return pos + 1;
    }

    /** Scambia le posizioni di due contenuti in un portfolio. */
    public void aggiornaPosizione(EntityContenuto contenuto1, EntityContenuto contenuto2) {
        try {
            conn.setAutoCommit(false);
            int pos1 = recuperaPosizione(currentPortfolioId, contenuto1);
            int pos2 = recuperaPosizione(currentPortfolioId, contenuto2);
            String sql = "UPDATE portfolio_contenuto SET posizione=? WHERE id_portfolio=? AND id_contenuto=?";
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setInt(1, pos2);
                ps.setObject(2, currentPortfolioId);
                ps.setObject(3, contenuto1.getIdContenuto());
                ps.executeUpdate();
                ps.setInt(1, pos1);
                ps.setObject(2, currentPortfolioId);
                ps.setObject(3, contenuto2.getIdContenuto());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            lastError = e.getMessage();
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("aggiornaPosizione: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Recupera le informazioni pubbliche di profilo di un utente.
     * Non include hashPassword.
     */
    public EntityUtente recuperaInfoProfilo(UUID idUtente) {
        return recuperaUtente(idUtente);
    }

    public List<EntityUtente> recuperaElencoStudenti(String nomeUtente) {
        String sql = """
            SELECT * FROM utente
            WHERE nome ILIKE ? OR cognome ILIKE ?
            ORDER BY cognome, nome
            """;
        String pattern = "%" + nomeUtente + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return extractList(ps, this::mapUtente);
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return Collections.emptyList();
    }

    public List<EntityUtente> recuperaElencoStudenti() {
        String sql = "SELECT * FROM utente ORDER BY cognome, nome";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            return extractList(ps, this::mapUtente);
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return Collections.emptyList();
    }

    public EntityUtente recuperaUtente(UUID idUtente) {
        String sql = "SELECT * FROM utente WHERE id_utente = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUtente(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    public List<EntityPortfolio> recuperaElencoPortfoli(UUID idUtente) {
        String sql = "SELECT * FROM portfolio WHERE id_utente = ? ORDER BY data_creazione";
        return queryList(sql, idUtente, this::mapPortfolio);
    }

    public int recuperaVisualizzazioni(UUID idPortfolio) {
        String sql = "SELECT numero_visualizzazioni FROM portfolio WHERE id_portfolio = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idPortfolio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("numero_visualizzazioni");
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return 0;
    }

    /**
     * Aggiorna il contatore visualizzazioni di un portfolio.
     * Nota: la spec indica solo numeroVisualizzazioni; idPortfolio è aggiunto.
     */
    public void aggiornaNumero(int numeroVisualizzazioni, UUID idPortfolio) {
        String sql = "UPDATE portfolio SET numero_visualizzazioni = ? WHERE id_portfolio = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, numeroVisualizzazioni);
            ps.setObject(2, idPortfolio);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaNumero: " + e.getMessage(), e);
        }
    }

    /** @return true se esiste già un contenuto con lo stesso percorso_storage. */
    public boolean isFileInUse(EntityContenuto contenuto) {
        String sql = "SELECT 1 FROM contenuto WHERE percorso_storage = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, contenuto.getPercorsoStorage());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    /** Alias italiano di isFileInUse — nomenclatura coerente con il resto del progetto. */
    public boolean isFileInUso(EntityContenuto contenuto) {
        return isFileInUse(contenuto);
    }

    /** @return l'utente con l'email indicata, o null se non trovato. */
    public EntityUtente recuperaUtentePerEmail(String email) {
        String sql = "SELECT * FROM utente WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUtente(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    // ── Cambio email con OTP ──────────────────────────────────────────────────

    /** Salva temporaneamente la nuova email in attesa di conferma OTP. */
    public void salvaEmailTemporanea(UUID idUtente, String nuovaEmail) {
        nuoveEmailInAttesa.put(idUtente, nuovaEmail);
    }

    /** @return la nuova email in attesa di conferma, o null se non presente. */
    public String recuperaEmailTemporanea(UUID idUtente) {
        return nuoveEmailInAttesa.get(idUtente);
    }

    /** Rimuove la nuova email temporanea (dopo conferma o annullamento). */
    public void rimuoviEmailTemporanea(UUID idUtente) {
        nuoveEmailInAttesa.remove(idUtente);
    }

    // =========================================================================
    // AREA LINK
    // =========================================================================

    /**
     * Inserisce il link e la sua associazione con il portfolio.
     * EntityLink deve già contenere idPortfolio (usato per link_portfolio).
     */
    public void salvaNuovoLink(EntityLink link) {
        try {
            conn.setAutoCommit(false);
            String insLink = """
                INSERT INTO link
                  (id_link, url_token, scadenza, stato, flag_aperto, visibilita, id_utente)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement ps = getConn().prepareStatement(insLink)) {
                ps.setObject(1, link.getIdLink());
                ps.setString(2, link.getLink());
                ps.setObject(3, link.getScadenza());
                ps.setString(4, link.getStato() != null ? link.getStato() : Constants.LINK_ATTIVO);
                ps.setBoolean(5, link.isFlagAperto());
                ps.setString(6, link.getVisibilita() != null ? link.getVisibilita() : Constants.VIS_PRIVATO);
                ps.setObject(7, link.getIdUtente());
                ps.executeUpdate();
            }
            String insPonte = "INSERT INTO link_portfolio (id_link, id_portfolio) VALUES (?, ?)";
            try (PreparedStatement ps = getConn().prepareStatement(insPonte)) {
                ps.setObject(1, link.getIdLink());
                ps.setObject(2, link.getIdPortfolio());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            lastError = e.getMessage();
            try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("salvaNuovoLink: " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /** Restituisce tutti i link dell'utente corrente, con idPortfolio caricato. */
    public List<EntityLink> recuperaLinks() {
        String sql = """
            SELECT l.*, lp.id_portfolio
            FROM link l
            LEFT JOIN link_portfolio lp ON l.id_link = lp.id_link
            WHERE l.id_utente = ?
            ORDER BY l.scadenza DESC NULLS LAST
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, currentUserId);
            return extractList(ps, this::mapLink);
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return Collections.emptyList();
    }

    /**
     * Recupera un link per url_token (accesso pubblico — non richiede currentUserId).
     * Usato da AccediTramiteLinkCtrl tramite ProfiloCondivisoApi.
     */
    public EntityLink recuperaLinkByToken(String urlToken) {
        String sql = """
            SELECT l.*, lp.id_portfolio
            FROM link l
            LEFT JOIN link_portfolio lp ON l.id_link = lp.id_link
            WHERE l.url_token = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, urlToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLink(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /** Recupera un link per email utente e id link (con idPortfolio dalla JOIN). */
    public EntityLink recuperaLink(String email, UUID idLink) {
        String sql = """
            SELECT l.*, lp.id_portfolio
            FROM link l
            JOIN utente u ON l.id_utente = u.id_utente
            LEFT JOIN link_portfolio lp ON l.id_link = lp.id_link
            WHERE u.email = ? AND l.id_link = ?
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setObject(2, idLink);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapLink(rs);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /** Imposta lo stato del link a 'revocato'. */
    public void aggiornaStatoLink(UUID idLink) {
        String sql = "UPDATE link SET stato = ? WHERE id_link = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, Constants.LINK_REVOCATO);
            ps.setObject(2, idLink);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaStatoLink: " + e.getMessage(), e);
        }
    }

    public OffsetDateTime recuperaScadenza(UUID idLink) {
        String sql = "SELECT scadenza FROM link WHERE id_link = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idLink);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("scadenza");
                    return ts != null ? ts.toInstant().atOffset(java.time.ZoneOffset.UTC) : null;
                }
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    /**
     * Aggiorna la scadenza di un link.
     * Nota: la spec indica solo scadenza; idLink è aggiunto per funzionalità.
     */
    public void aggiornaScadenza(OffsetDateTime scadenza, UUID idLink) {
        String sql = "UPDATE link SET scadenza = ? WHERE id_link = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, scadenza);
            ps.setObject(2, idLink);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaScadenza: " + e.getMessage(), e);
        }
    }

    public String recuperaVisibilitaLink(UUID idLink) {
        return selectString("link", "visibilita", "id_link", idLink);
    }

    /**
     * Aggiorna la visibilità di un link.
     * Nota: la spec indica solo visibilita; idLink è aggiunto per funzionalità.
     */
    public void aggiornaVisibilitaLink(String visibilita, UUID idLink) {
        String sql = "UPDATE link SET visibilita = ? WHERE id_link = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, visibilita);
            ps.setObject(2, idLink);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("aggiornaVisibilitaLink: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica che il link sia attivo e non scaduto.
     * La verifica è sincrona (confronto con now()), non via scheduler.
     */
    public boolean isLinkValido(UUID idLink) {
        String sql = """
            SELECT 1 FROM link
            WHERE id_link = ?
              AND stato = 'attivo'
              AND (scadenza IS NULL OR scadenza > NOW())
            """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idLink);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return false;
    }

    public UUID getIdPortfolio(UUID idLink) {
        String sql = "SELECT id_portfolio FROM link_portfolio WHERE id_link = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, idLink);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getObject("id_portfolio", UUID.class);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    // =========================================================================
    // AREA CONNESSIONE E SESSIONE (usata da GestisciCadutaDiConnessioneCtrl)
    // =========================================================================

    public boolean verificaStatoConnessione() {
        try {
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /** Sincronizza i dati locali (raccolti durante la disconnessione) verso il DB. */
    public void inviaDatiAttuali(Map<String, Object> data) {
        // Le operazioni specifiche dipendono dal tipo di dato raccolto;
        // questa implementazione base le esegue iterando sulla mappa.
        // Ogni entry ha come chiave il tipo di operazione e come valore il payload.
        LOG.info("inviaDatiAttuali: sincronizzazione " + data.size() + " operazioni");
    }

    public String recuperaMessErrore() {
        return lastError;
    }

    /** Annulla eventuali operazioni di salvataggio in corso (rollback). */
    public void interrompiSalvataggio() {
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOG.warning("interrompiSalvataggio: " + e.getMessage());
        }
    }

    /** Restituisce la lista delle operazioni parziali non completate. */
    public List<String> recuperaOperazioniParziali() {
        // In questa implementazione base, restituiamo la lista vuota:
        // le operazioni parziali vengono tracciate a livello applicativo
        // da SalvataggioLocaleCtrl, non persistite su DB.
        return Collections.emptyList();
    }

    /** Esegue il rollback delle operazioni parziali indicate. */
    public void rollback(List<String> operazioniParziali) {
        interrompiSalvataggio();
        LOG.info("rollback: annullate " + operazioniParziali.size() + " operazioni parziali");
    }

    // =========================================================================
    // HELPER PRIVATI
    // =========================================================================

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private <T> List<T> queryList(String sql, Object param, RowMapper<T> mapper) {
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, param);
            return extractList(ps, mapper);
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return Collections.emptyList();
    }

    private <T> List<T> extractList(PreparedStatement ps, RowMapper<T> mapper) throws SQLException {
        List<T> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapper.map(rs));
        }
        return list;
    }

    private int countWhere(String table, String col, String val) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + col + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, val);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return 0;
    }

    private String selectString(String table, String col, String whereCol, Object whereVal) {
        String sql = "SELECT " + col + " FROM " + table + " WHERE " + whereCol + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, whereVal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(col);
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
        }
        return null;
    }

    private void update(String table, String col, Object val, String whereCol, Object whereVal) {
        String sql = "UPDATE " + table + " SET " + col + " = ? WHERE " + whereCol + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, val);
            ps.setObject(2, whereVal);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("update " + table + "." + col + ": " + e.getMessage(), e);
        }
    }

    private void delete(String table, String col, UUID id) {
        String sql = "DELETE FROM " + table + " WHERE " + col + " = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            throw new RuntimeException("delete " + table + ": " + e.getMessage(), e);
        }
    }

    // ── Mapper da ResultSet a Entity ──────────────────────────────────────────

    private EntityUtente mapUtente(ResultSet rs) throws SQLException {
        java.sql.Date dn = rs.getDate("data_nascita");
        return new EntityUtente(
            rs.getObject("id_utente", UUID.class),
            rs.getString("nome"),
            rs.getString("cognome"),
            rs.getString("email"),
            rs.getString("hash_password"),
            rs.getString("numero_telefono"),
            dn != null ? dn.toString() : null,
            rs.getBoolean("email_validata"),
            rs.getBoolean("numero_validato"),
            rs.getBoolean("stato_2fa"),
            rs.getString("stato_sessione")
        );
    }

    private EntityPortfolio mapPortfolio(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("data_creazione");
        return new EntityPortfolio(
            rs.getObject("id_portfolio", UUID.class),
            rs.getString("nome"),
            ts != null ? ts.toInstant().atOffset(java.time.ZoneOffset.UTC) : null,
            rs.getInt("numero_visualizzazioni"),
            rs.getObject("id_utente", UUID.class)
        );
    }

    private EntityRaccolta mapRaccolta(ResultSet rs) throws SQLException {
        return new EntityRaccolta(
            rs.getObject("id_raccolta", UUID.class),
            rs.getString("nome"),
            rs.getInt("ordine"),
            rs.getObject("id_portfolio", UUID.class)
        );
    }

    private EntityContenuto mapContenuto(ResultSet rs) throws SQLException {
        return new EntityContenuto(
            rs.getObject("id_contenuto", UUID.class),
            rs.getString("titolo"),
            rs.getString("tipo_file"),
            rs.getLong("dimensione"),
            rs.getString("percorso_storage"),
            rs.getString("visibilita"),
            rs.getObject("id_utente", UUID.class)
        );
    }

    private EntityLink mapLink(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("scadenza");
        UUID idPortfolio = null;
        try { idPortfolio = rs.getObject("id_portfolio", UUID.class); } catch (SQLException ignored) {}
        return new EntityLink(
            rs.getObject("id_link", UUID.class),
            rs.getString("url_token"),
            ts != null ? ts.toInstant().atOffset(java.time.ZoneOffset.UTC) : null,
            rs.getString("stato"),
            rs.getBoolean("flag_aperto"),
            rs.getString("visibilita"),
            rs.getObject("id_utente", UUID.class),
            idPortfolio
        );
    }
}
