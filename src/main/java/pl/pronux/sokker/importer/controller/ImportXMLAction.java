package pl.pronux.sokker.importer.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import pl.pronux.sokker.actions.AsistenteManager;
import pl.pronux.sokker.actions.ConfigurationManager;
import pl.pronux.sokker.actions.JuniorsManager;
import pl.pronux.sokker.actions.LeaguesManager;
import pl.pronux.sokker.actions.MatchesManager;
import pl.pronux.sokker.actions.PlayersManager;
import pl.pronux.sokker.actions.TeamManager;
import pl.pronux.sokker.actions.TrainersManager;
import pl.pronux.sokker.data.sql.SQLQuery;
import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.downloader.managers.JuniorsXmlManager;
import pl.pronux.sokker.downloader.managers.LeagueXmlManager;
import pl.pronux.sokker.downloader.managers.MatchXmlManager;
import pl.pronux.sokker.downloader.managers.PlayersXmlManager;
import pl.pronux.sokker.downloader.managers.ReportsXmlManager;
import pl.pronux.sokker.downloader.managers.TeamsXmlManager;
import pl.pronux.sokker.downloader.managers.TrainersXmlManager;
import pl.pronux.sokker.downloader.managers.TransfersXmlManager;
import pl.pronux.sokker.downloader.managers.XmlManager;
import pl.pronux.sokker.downloader.managers.XmlManagerUtils;
import pl.pronux.sokker.downloader.xml.parsers.OldXmlParser;
import pl.pronux.sokker.importer.model.IXMLpack;
import pl.pronux.sokker.importer.model.SApack;
import pl.pronux.sokker.importer.model.XMLpack;
import pl.pronux.sokker.importer.model.XMLpackOld;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.interfaces.RunnableWithProgress;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.Coach;
import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.Report;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.model.Transfer;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.utils.Log;
import pl.pronux.sokker.utils.file.Database;
import pl.pronux.sokker.utils.file.OperationOnFile;

public class ImportXMLAction implements RunnableWithProgress {

	private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

	private TrainersManager trainersManager = TrainersManager.getInstance();

	private TeamManager teamManager = TeamManager.getInstance();

	private JuniorsManager juniorsManager = JuniorsManager.getInstance();

	private PlayersManager playersManager = PlayersManager.getInstance();

	private LeaguesManager leaguesManager = LeaguesManager.getInstance();

	private MatchesManager matchesManager = MatchesManager.getInstance();

	private AsistenteManager asistenteManager = AsistenteManager.getInstance();

	private List<IXMLpack> packages;

	public ImportXMLAction(List<IXMLpack> packages) {
		this.packages = packages;
	}

	public void run(ProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.getString("ImportXMLAction.start"), packages.size()); 
		try {
			// the copy File > Restore database lists
			Database.backup(SQLQuery.getSettings(), Database.IMPORT);
			SQLSession.connect();
			int teamID = configurationManager.getTeamId();
			Set<Integer> knownWeeks = playersManager.getTrainingWeeks();
			List<Junior> school = juniorsManager.getJuniors(Junior.STATUS_IN_SCHOOL);
			Set<String> juniorRows = new HashSet<String>();
			for (IXMLpack child : packages) {
				if (monitor.isCanceled()) {
					break;
				}

				monitor.setTaskName(Messages.getString("ImportXMLAction.import")); 
				monitor.subTask(child.getDate().toDateTimeString());
				if (knownWeeks.contains(Integer.valueOf(child.getDate().getSokkerDate().getTrainingWeek()))) {
					child.setSkipped(true);
				} else if (child instanceof XMLpack) {
					XMLpack pack = (XMLpack) child;
					if (pack.isComplete()) {
						try {
							SQLSession.beginTransaction();
							TeamsXmlManager teamXmlManager = new TeamsXmlManager(
								OperationOnFile.readFromFile(pack.getTeam(), "UTF-8"), pack.getDate(), pack.getTeamId()); 
							PlayersXmlManager playersXmlManager = new PlayersXmlManager(
								OperationOnFile.readFromFile(pack.getPlayers(), "UTF-8"), pack.getDate(), pack.getTeamId()); 
							JuniorsXmlManager juniorsXmlManager = new JuniorsXmlManager(
								OperationOnFile.readFromFile(pack.getJuniors(), "UTF-8"), pack.getDate(), pack.getTeamId());

							Club club = teamXmlManager.parseXML(teamID);
							List<Player> players = playersXmlManager.parseXML();
							List<Junior> juniors = juniorsXmlManager.parseXML();

							if (club.getId() == teamID) {
								if (club.getId() == pack.getTeamId()) {
									teamManager.importerTeam(club, pack.getDate());
								}

								Training training = club.getTraining();
								if (pack.getTrainers() != null) {
									TrainersXmlManager trainersXMLManager = new TrainersXmlManager(
										OperationOnFile.readFromFile(pack.getTrainers(), "UTF-8"), pack.getDate(), pack.getTeamId());
									List<Coach> trainers = trainersXMLManager.parseXML();
									trainersManager.importerTrainers(trainers);
									trainersXMLManager.importCoachesAtTraining(training);
								}
								playersManager.importPlayers(players, training);
								juniorsManager.importJuniors(juniors, training, juniorRows);

								if (pack.getReports() != null) {
									ReportsXmlManager reportsManager = new ReportsXmlManager(
										OperationOnFile.readFromFile(pack.getReports(), "UTF-8"), pack.getDate(), pack.getTeamId());
									List<Report> reports = reportsManager.parseXML();
									teamManager.importerReports(reports);
								}

								if (pack.getTransfers() != null) {
									TransfersXmlManager transfersManager = new TransfersXmlManager(
										OperationOnFile.readFromFile(pack.getTransfers(), "UTF-8"), pack.getDate(), pack.getTeamId());
									List<Transfer> transfers = transfersManager.parseXML();
									teamManager.importerTransfers(transfers);
								}

								pack.setImported(true);
							}

							SQLSession.commit();

						} catch (Exception e) {
							pack.setImported(false);
							SQLSession.rollback();
							Log.error("XML Importer ", e); 
						} finally {
							SQLSession.endTransaction();
						}
					}
				} else if (child instanceof XMLpackOld) {
					XMLpackOld pack = (XMLpackOld) child;
					try {
						SQLSession.beginTransaction();
						OldXmlParser oldXMLParser = new OldXmlParser();
						String xml = OperationOnFile.readFromFile(pack.getFile(), "UTF-8"); 
						InputSource input = new InputSource(new StringReader(xml));
						try {
							oldXMLParser.parseXmlSax(input, null);
						} catch (SAXException ex) {
							input = new InputSource(new StringReader(XmlManagerUtils.filterCharacters(xml)));
							oldXMLParser.parseXmlSax(input, null);
						}
						Club club = oldXMLParser.getClub();
						if (club.getId() == teamID) {

							trainersManager.importerTrainers(club.getCoaches());
							teamManager.importerTeam(club, pack.getDate());
							Training training = club.getTraining();
							if ((training.getStatus() & Training.NEW_TRAINING) != 0) {
								trainersManager.importTrainersAtTraining(club.getCoaches(), training);
							} else if ((training.getStatus() & Training.UPDATE_TRAINING) != 0) {
								trainersManager.updateTrainersAtTraining(club.getCoaches(), training);
							}
							playersManager.importPlayers(club.getPlayers(), training);
							juniorsManager.importJuniors(club.getJuniors(), training, juniorRows);

							pack.setImported(true);
						}
						SQLSession.commit();
					} catch (Exception e) {
						pack.setImported(false);
						SQLSession.rollback();
						Log.error("XML Importer ", e);
					} finally {
						SQLSession.endTransaction();
					}

				} else if (child instanceof SApack) {
					SApack pack = (SApack) child;
					try {
						SQLSession.beginTransaction();
						asistenteManager.importWeek(pack.getDate(), pack.getTypes(), pack.getPlayers());
						SQLSession.commit();
						pack.setImported(true);
					} catch (Exception e) {
						SQLSession.rollback();
						Log.error("Sokker Asistente importer", e);
					} finally {
						SQLSession.endTransaction();
					}
				}
				// matches only from the team's own, complete syncs
				if (child instanceof XMLpack && child.isComplete()) {
					importMatches((XMLpack) child);
				}
				monitor.worked(1);
			}
			// juniors only in the imported files have left the school
			SQLSession.beginTransaction();
			juniorsManager.removeJuniorsNotIn(school, teamID);
			SQLSession.commit();
			SQLSession.endTransaction();
			SQLSession.close();
		} catch (IOException e) {
			Log.error("XML Importer -> database backup, nothing imported", e);
		} catch (SQLException e) {
			try {
				SQLSession.rollback();
				SQLSession.close();
			} catch (SQLException e1) {
				Log.error("Synchronizer -> SQL Importing Rollback", e1); 
			}
			Log.error("Synchronizer -> SQL Importing", e); 
		} finally {
			monitor.done();
		}
	}

	/** the pack's leagues, then its finished matches, in one transaction */
	private void importMatches(XMLpack pack) throws SQLException {
		try {
			SQLSession.beginTransaction();
			leaguesManager.importLeagues(parse(new LeagueXmlManager(), pack.getLeagues()));
			matchesManager.importerMatches(parse(new MatchXmlManager(), pack.getMatches()));
			SQLSession.commit();
		} catch (Exception e) {
			SQLSession.rollback();
			Log.error("XML Importer ", e);
		} finally {
			SQLSession.endTransaction();
		}
	}

	/** items of every file that parses */
	private static <T> List<T> parse(XmlManager<T> manager, List<File> files) {
		List<T> items = new ArrayList<T>();
		for (File file : files) {
			try {
				items.addAll(manager.parseXML(OperationOnFile.readFromFile(file, "UTF-8")));
			} catch (Exception e) {
				Log.warning("XML Importer " + file.getName(), e);
			}
		}
		return items;
	}

	public void onFinish() {
	}
}
