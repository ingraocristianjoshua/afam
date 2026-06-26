package com.afam.server.db;

import com.afam.shared.entity.EntityUtente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DBMSBnd {

    public static DBMSBnd instance = null;

    private DBMSBnd() {
    }

    public static DBMSBnd getInstance() {
        if (instance == null) {
            instance = new DBMSBnd();
        }
        return instance;
    }

    public boolean isMailInUse(String email) {
        String sql = "SELECT COUNT(*) FROM utente WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registraUtente(EntityUtente utente) {
        String sql = "INSERT INTO utente (id_utente, nome, cognome, email, hash_password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, utente.getIdUtente());
            pstmt.setString(2, utente.getNome());
            pstmt.setString(3, utente.getCognome());
            pstmt.setString(4, utente.getEmail());
            pstmt.setString(5, utente.getPassword());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public EntityUtente recuperaUtentePerEmail(String email) {
        String sql = "SELECT * FROM utente WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                EntityUtente utente = new EntityUtente();
                utente.setIdUtente((UUID) rs.getObject("id_utente"));
                utente.setNome(rs.getString("nome"));
                utente.setCognome(rs.getString("cognome"));
                utente.setEmail(rs.getString("email"));
                utente.setPassword(rs.getString("hash_password"));
                utente.setStato2fa(rs.getBoolean("stato_2fa") ? 1 : 0);
                utente.setEmailValidata(rs.getBoolean("email_validata") ? 1 : 0);
                utente.setNumeroValidato(rs.getBoolean("numero_validato") ? 1 : 0);
                utente.setNumeroTelefono(rs.getString("numero_telefono"));
                utente.setStatoSessione(rs.getString("stato_sessione"));
                return utente;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
