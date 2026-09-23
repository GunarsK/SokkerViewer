ALTER TABLE player_skills ADD COLUMN training_intensity INTEGER DEFAULT -1 NOT NULL;
ALTER TABLE player_skills ADD COLUMN minutes_official INTEGER DEFAULT -1 NOT NULL;
ALTER TABLE player_skills ADD COLUMN minutes_friendly INTEGER DEFAULT -1 NOT NULL;
ALTER TABLE player_skills ADD COLUMN minutes_national INTEGER DEFAULT -1 NOT NULL;
UPDATE training SET api_confirmed = false;
UPDATE SYSTEM SET VERSION = 28;