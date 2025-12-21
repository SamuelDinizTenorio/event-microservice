-- Adiciona a coluna 'status' à tabela 'event'
ALTER TABLE event ADD COLUMN status VARCHAR(20);

-- Define 'ACTIVE' como o status padrão para todos os eventos existentes
UPDATE event SET status = 'ACTIVE' WHERE status IS NULL;

-- Torna a coluna 'status' obrigatória
ALTER TABLE event ALTER COLUMN status SET NOT NULL;
