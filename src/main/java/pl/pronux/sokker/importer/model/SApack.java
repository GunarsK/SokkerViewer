package pl.pronux.sokker.importer.model;

import java.util.ArrayList;
import java.util.List;

import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.SokkerDate;

/** one week of sokker asistente's player history */
public class SApack implements IXMLpack {

	private Date date;

	/** each with the week's row as its only skills */
	private List<Player> players = new ArrayList<Player>();

	/** training type per formation, null when not known */
	private int[] types;

	private boolean complete = true;

	private boolean imported;

	private boolean skipped;

	/** dated on the thursday of the training week */
	public SApack(int week) {
		date = new Date(SokkerDate.weekToMillis(week, SokkerDate.THURSDAY));
		date.setSokkerDate(new SokkerDate(SokkerDate.THURSDAY, week));
	}

	public List<Player> getPlayers() {
		return players;
	}

	public int[] getTypes() {
		return types;
	}

	public void setTypes(int[] types) {
		this.types = types;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}

	public boolean isSkipped() {
		return skipped;
	}

	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}
}
