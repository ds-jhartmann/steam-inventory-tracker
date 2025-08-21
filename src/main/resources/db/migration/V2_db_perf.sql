-- V2: performance & integrity improvements

-- 1) Ensure buy_infos points to items and cascades on delete
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'fk_buy_item'
  ) THEN
ALTER TABLE buy_infos
    ADD CONSTRAINT fk_buy_item
        FOREIGN KEY (item_name) REFERENCES items(item_name)
            ON DELETE CASCADE;
END IF;
END $$;

-- 2) Main covering index for "latest price per item"
DO $$
BEGIN
  -- Covering index enables index-only scans to fetch price/median without heap lookups
  IF NOT EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relname = 'ix_price_item_ts_desc_inc' AND n.nspname = 'public'
  ) THEN
    EXECUTE 'CREATE INDEX ix_price_item_ts_desc_inc
             ON price_history (item_name, timestamp DESC)
             INCLUDE (price, median)';
END IF;
END $$;

-- 3) Optional helper index for time-range queries per item (keeps scans/snaps fast)
CREATE INDEX IF NOT EXISTS ix_price_item_ts
    ON price_history (item_name, timestamp);

-- 4) Speed up joins/filters on buy_infos by item
CREATE INDEX IF NOT EXISTS ix_buy_infos_item
    ON buy_infos (item_name);
