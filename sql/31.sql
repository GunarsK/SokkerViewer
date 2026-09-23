ALTER TABLE player_skills ADD COLUMN training_injury_days INTEGER DEFAULT -1 NOT NULL;
UPDATE training SET api_confirmed = false;
UPDATE SYSTEM SET VERSION = 31;