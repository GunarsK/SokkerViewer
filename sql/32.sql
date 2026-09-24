ALTER TABLE player_skills ADD COLUMN made_up BOOLEAN DEFAULT false NOT NULL;
UPDATE training SET api_confirmed = false;
UPDATE SYSTEM SET VERSION = 32;