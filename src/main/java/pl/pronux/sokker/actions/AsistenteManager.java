package pl.pronux.sokker.actions;

import java.sql.SQLException;
import java.util.List;

import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.data.sql.dao.PlayersDao;
import pl.pronux.sokker.data.sql.dao.TeamsDao;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.Training;

/** writes sokker asistente's weeks into the training and player_skills tables */
public final class AsistenteManager {

	private static AsistenteManager instance = new AsistenteManager();

	private AsistenteManager() {
	}

	public static AsistenteManager getInstance() {
		return instance;
	}

	/** writes the week's training and its players' rows */
	public void importWeek(Date date, int[] types, List<Player> players) throws SQLException {
		TeamsDao teamsDao = new TeamsDao(SQLSession.getConnection());
		PlayersDao playersDao = new PlayersDao(SQLSession.getConnection());
		Training training = teamsDao.getTrainingForDay(date);
		if (training == null) {
			training = TrainingApiManager.newTraining(date, types);
			teamsDao.addTraining(training);
			training.setId(teamsDao.getTrainingId(training));
		} else if (types != null && !training.hasPositionTypes()) {
			TrainingApiManager.setPositionTypes(training, types);
			teamsDao.updateTrainingTypes(training);
		}
		for (Player player : players) {
			TrainingApiManager.copyUnreported(playersDao.getNearestPlayerSkills(player.getId(), date.getMillis()), player.getSkills()[0]);
		}
		PlayersManager.getInstance().importPlayers(players, training);
	}
}
