-- 1. Renomeia a coluna 'date' existente para refletir seu novo propósito.
ALTER TABLE event RENAME COLUMN date TO start_date_time;

-- 2. Adiciona a nova coluna 'end_date_time', permitindo nulos temporariamente.
ALTER TABLE event ADD COLUMN end_date_time TIMESTAMP;

-- 3. Atualiza os registros existentes com um valor padrão para a nova coluna.
--    (Assumindo uma duração padrão de 1 hora para eventos antigos).
UPDATE event SET end_date_time = start_date_time + INTERVAL '1 hour' WHERE end_date_time IS NULL;

-- 4. Agora que todas as linhas têm um valor, define a coluna como não nula.
ALTER TABLE event ALTER COLUMN end_date_time SET NOT NULL;
