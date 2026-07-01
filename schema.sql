-- =============================================================================
-- AFAM – Alta Formazione Artistica, Musicale e Coreutica
-- Schema DDL PostgreSQL
-- =============================================================================
-- Prerequisiti:
--   psql -U postgres -c "CREATE DATABASE afam;"
--   psql -U postgres -d afam -f schema.sql
-- =============================================================================

-- Estensione per gen_random_uuid() (usata solo nella tabella otp)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- TABELLA: utente
-- Entity: EntityUtente
-- Nota: le PK uuid vengono generate lato applicativo (UUID.randomUUID())
--       tranne per otp dove usa gen_random_uuid() lato DB.
-- =============================================================================
CREATE TABLE IF NOT EXISTS utente (
    id_utente       uuid            PRIMARY KEY,
    nome            text            NOT NULL,
    cognome         text            NOT NULL,
    email           text            NOT NULL UNIQUE,
    hash_password   text            NOT NULL,
    numero_telefono text,
    email_validata  boolean         NOT NULL DEFAULT false,
    numero_validato boolean         NOT NULL DEFAULT false,
    stato_2fa       boolean         NOT NULL DEFAULT false,
    data_nascita    date,
    -- 'aperta' | 'chiusa'
    stato_sessione  text            NOT NULL DEFAULT 'chiusa'
        CHECK (stato_sessione IN ('aperta', 'chiusa'))
);

-- Aggiunta colonna per DB esistenti
ALTER TABLE utente ADD COLUMN IF NOT EXISTS data_nascita date;

-- =============================================================================
-- TABELLA: contenuto
-- Entity: EntityContenuto
-- =============================================================================
CREATE TABLE IF NOT EXISTS contenuto (
    id_contenuto        uuid        PRIMARY KEY,
    titolo              text        NOT NULL,
    tipo_file           text        NOT NULL,
    dimensione          bigint      NOT NULL CHECK (dimensione > 0),
    percorso_storage    text        NOT NULL,
    -- 'privato' | 'pubblico' | 'condiviso'
    visibilita          text        NOT NULL DEFAULT 'privato'
        CHECK (visibilita IN ('privato', 'pubblico', 'condiviso')),
    id_utente           uuid        NOT NULL
        REFERENCES utente(id_utente) ON DELETE CASCADE
);

-- =============================================================================
-- TABELLA: portfolio
-- Entity: EntityPortfolio
-- =============================================================================
CREATE TABLE IF NOT EXISTS portfolio (
    id_portfolio            uuid        PRIMARY KEY,
    nome                    text        NOT NULL,
    data_creazione          timestamptz NOT NULL DEFAULT now(),
    numero_visualizzazioni  integer     NOT NULL DEFAULT 0
        CHECK (numero_visualizzazioni >= 0),
    id_utente               uuid        NOT NULL
        REFERENCES utente(id_utente) ON DELETE CASCADE
);

-- =============================================================================
-- TABELLA: raccolta
-- Entity: EntityRaccolta
-- =============================================================================
CREATE TABLE IF NOT EXISTS raccolta (
    id_raccolta     uuid        PRIMARY KEY,
    nome            text        NOT NULL,
    ordine          integer     NOT NULL DEFAULT 0,
    id_portfolio    uuid        NOT NULL
        REFERENCES portfolio(id_portfolio) ON DELETE CASCADE
);

-- =============================================================================
-- TABELLA: link
-- Entity: EntityLink
-- Nota: il portfolio associato al link sta in link_portfolio (N:M).
--       getIdPortfolio(idLink) legge da lì.
-- =============================================================================
CREATE TABLE IF NOT EXISTS link (
    -- id_link è anche l'identificatore condivisibile del link (nessun token separato)
    id_link     uuid        PRIMARY KEY,
    scadenza    timestamptz,                    -- NULL = nessuna scadenza
    -- 'attivo' | 'revocato' | 'scaduto'
    stato       text        NOT NULL DEFAULT 'attivo'
        CHECK (stato IN ('attivo', 'revocato', 'scaduto')),
    flag_aperto boolean     NOT NULL DEFAULT false,
    -- 'privato' | 'pubblico'
    visibilita  text        NOT NULL DEFAULT 'privato'
        CHECK (visibilita IN ('privato', 'pubblico')),
    id_utente   uuid        NOT NULL
        REFERENCES utente(id_utente) ON DELETE CASCADE
);

-- =============================================================================
-- TABELLA: otp
-- NON ha una entity dedicata (EntityOTP non esiste).
-- Gestita esclusivamente tramite DBMSBnd.salvaCodiceOTP / recuperaOTP.
-- La PK usa gen_random_uuid() lato DB perché l'applicazione non la espone.
-- =============================================================================
CREATE TABLE IF NOT EXISTS otp (
    id_otp      uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    codice      text        NOT NULL,
    scadenza    timestamptz NOT NULL,
    id_utente   uuid        NOT NULL
        REFERENCES utente(id_utente) ON DELETE CASCADE
);

-- =============================================================================
-- TABELLA PONTE: portfolio_contenuto
-- Associa i contenuti a un portfolio; tiene traccia dell'ordine (posizione).
-- Usata da: aggiungiContenuto, rimuoviContenuto, recuperaPosizione,
--           recuperaPosizioneAdiacente, aggiornaPosizione.
-- =============================================================================
CREATE TABLE IF NOT EXISTS portfolio_contenuto (
    id_portfolio    uuid        NOT NULL
        REFERENCES portfolio(id_portfolio) ON DELETE CASCADE,
    id_contenuto    uuid        NOT NULL
        REFERENCES contenuto(id_contenuto) ON DELETE CASCADE,
    posizione       integer     NOT NULL DEFAULT 0,
    PRIMARY KEY (id_portfolio, id_contenuto)
);

-- =============================================================================
-- TABELLA PONTE: raccolta_contenuto
-- Associa i contenuti a una raccolta dentro un portfolio.
-- Usata da: aggiornaStatoRaccolta (aggiunge O rimuove secondo il contesto).
-- =============================================================================
CREATE TABLE IF NOT EXISTS raccolta_contenuto (
    id_raccolta     uuid        NOT NULL
        REFERENCES raccolta(id_raccolta) ON DELETE CASCADE,
    id_contenuto    uuid        NOT NULL
        REFERENCES contenuto(id_contenuto) ON DELETE CASCADE,
    PRIMARY KEY (id_raccolta, id_contenuto)
);

-- =============================================================================
-- TABELLA PONTE: link_portfolio
-- Collega un link a uno o più portfolio condivisi.
-- Usata da: salvaNuovoLink, getIdPortfolio(idLink).
-- =============================================================================
CREATE TABLE IF NOT EXISTS link_portfolio (
    id_link         uuid        NOT NULL
        REFERENCES link(id_link) ON DELETE CASCADE,
    id_portfolio    uuid        NOT NULL
        REFERENCES portfolio(id_portfolio) ON DELETE CASCADE,
    PRIMARY KEY (id_link, id_portfolio)
);

-- =============================================================================
-- INDICI aggiuntivi per prestazioni sulle query più frequenti
-- =============================================================================

-- Ricerca studente per nome/cognome (visualizza profilo condiviso)
CREATE INDEX IF NOT EXISTS idx_utente_nome     ON utente(nome);
CREATE INDEX IF NOT EXISTS idx_utente_cognome  ON utente(cognome);

-- Recupero portfolio per utente
CREATE INDEX IF NOT EXISTS idx_portfolio_utente ON portfolio(id_utente);

-- Recupero contenuti per utente
CREATE INDEX IF NOT EXISTS idx_contenuto_utente ON contenuto(id_utente);

-- Recupero raccolte per portfolio
CREATE INDEX IF NOT EXISTS idx_raccolta_portfolio ON raccolta(id_portfolio);

-- Recupero link per utente
CREATE INDEX IF NOT EXISTS idx_link_utente ON link(id_utente);

-- Recupero OTP per utente (salvaCodiceOTP / recuperaOTP)
CREATE INDEX IF NOT EXISTS idx_otp_utente ON otp(id_utente);

-- =============================================================================
-- FINE SCHEMA
-- =============================================================================
