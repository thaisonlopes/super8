-- Alterar tabela jogadores para usar ID sequencial
ALTER TABLE jogadores DROP CONSTRAINT IF EXISTS jogadores_pkey;
ALTER TABLE jogadores ALTER COLUMN id TYPE INTEGER USING id::INTEGER;
ALTER TABLE jogadores ALTER COLUMN id SET DEFAULT nextval('jogadores_id_seq');
ALTER TABLE jogadores ADD CONSTRAINT jogadores_pkey PRIMARY KEY (id);

-- Criar sequência se não existir
CREATE SEQUENCE IF NOT EXISTS jogadores_id_seq;
ALTER TABLE jogadores ALTER COLUMN id SET DEFAULT nextval('jogadores_id_seq');
ALTER SEQUENCE jogadores_id_seq OWNED BY jogadores.id; 