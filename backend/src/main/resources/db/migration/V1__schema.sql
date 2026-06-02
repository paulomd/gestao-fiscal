CREATE TABLE aliquota (
    id              BIGSERIAL PRIMARY KEY,
    regime_tributario VARCHAR(50) NOT NULL,
    pis             NUMERIC(8, 4) NOT NULL,
    cofins          NUMERIC(8, 4) NOT NULL,
    irpj            NUMERIC(8, 4) NOT NULL,
    csll            NUMERIC(8, 4) NOT NULL,
    vigencia        DATE NOT NULL,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE apuracao_fiscal (
    id               BIGSERIAL PRIMARY KEY,
    competencia      VARCHAR(7) NOT NULL,
    receita_bruta    NUMERIC(15, 2) NOT NULL,
    regime_tributario VARCHAR(50) NOT NULL,
    pis              NUMERIC(15, 2) NOT NULL,
    cofins           NUMERIC(15, 2) NOT NULL,
    irpj             NUMERIC(15, 2) NOT NULL,
    csll             NUMERIC(15, 2) NOT NULL,
    total_tributos   NUMERIC(15, 2) NOT NULL,
    carga_tributaria NUMERIC(8, 4) NOT NULL,
    usuario          VARCHAR(100) NOT NULL,
    data_calculo     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_aliquota_regime ON aliquota(regime_tributario);
CREATE INDEX idx_apuracao_competencia ON apuracao_fiscal(competencia);
