package pl.pronux.sokker.ui.widgets.styledtexts;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.SVNumberFormat;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;

public class JuniorTrainedDescription extends StyledText implements IDescription {

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public JuniorTrainedDescription(Composite parent, int style) {
		super(parent, style);
		this.setBackground(parent.getBackground());
		this.setFont(ConfigBean.getFontDescription());
		// use a verify listener to keep the offsets up to date

	}

	private void addText(String text) {
		this.append(text);
	}

	public void setStatsJuniorInfo(Junior junior) {
		setStatsJuniorInfo(junior, 0);
	}

	private String getNumberSkill(int before, int now) {
		if (now - before != 0) {
			return String.format("[%s %s]", now, SVNumberFormat.formatIntegerWithSignZero(now - before)); 
		} else {
			return String.format("[%s]", now); 
		}
	}

	private void addSkill(String name, String value, int now, int begin, int offset) {
		String text = ""; 
		text = String.format("%8s%s %s", getNumberSkill(begin, now), value, name); 
		this.addText(text);
		addStyle(offset, 8, Colors.getMutedText(), SWT.NONE);
		addStyle(offset + 8, value.length(), Colors.getJuniorAccent(), SWT.BOLD);
	}
	
	private void addPlayerSkill(String name, String value, int now, int offset) {
		String text1 = ""; 
		text1 = String.format("%8s%s %s", getNumberSkill(now, now), value, name); 
		this.addText(String.format("%-35s", text1)); 
		addStyle(offset, 8, Colors.getMutedText(), SWT.NONE);
		addStyle(offset + 8, value.length(), null, SWT.BOLD);
	}

	private void addStyle(int start, int length, Color color, int style) {
		StyleRange range = new StyleRange();
		range.foreground = color;
		range.start = start;
		range.fontStyle = style;
		range.length = length;
		this.setStyleRange(range);
	}

	public void setStatsJuniorInfo(Junior junior, int index) {
		this.setRedraw(false);
		// Send all output to the Appendable object sb
		int max = junior.getSkills().length - 1 - index;
		String text;
		this.setText(""); 

		if (junior.getPlayer() != null) {
			this.addText(String.format(" %s %s, %s: %d ", junior.getName(), junior.getSurname(), Messages.getString("player.age"), junior.getPlayer().getSkills()[0].getAge()));  
			addStyle(1, junior.getName().length() + junior.getSurname().length() + 1, null, SWT.BOLD);
			addStyle(getText().length() - 3, 2, null, SWT.BOLD);
		} else {
			this.addText(String.format("%s %s ", junior.getName(), junior.getSurname())); 
		}

		text = String.format("[%s]", SVNumberFormat.formatIntegerWithSign(junior.getEstimatedAge())); 
		this.addText(text);
		
		text = String.format("\t(ID %d)", junior.getId()); 
		this.addText(text);
		addStyle(getText().length() - text.length(), text.length(), Colors.getMutedText(), SWT.NONE);
		this.addText(NEW_LINE);
		
		addSkill(Messages.getString("junior"), Messages.getString("skill.a" + junior.getSkills()[max].getSkill()), junior.getSkills()[max].getSkill(), junior.getSkills()[0].getSkill(), getText().length());  
		
		if(junior.getWeeksWithoutJump() > 0) {
			text = String.format(" %s", SVNumberFormat.formatIntegerWithSignZero(junior.getWeeksWithoutJump())); 
			this.addText(text);
		}
		
		if(junior.getAveragePops() > 0 && junior.getPops() < 2) {
			text = String.format(" (%d,?)", junior.getSkills()[max].getSkill()); 
			this.addText(text);
		} else {
			text = String.format(" (%.2f)", junior.getSkills()[max].getSkill() + (junior.getWeeksWithoutJump()/ junior.getAveragePops())); 
			this.addText(text);
		}

		this.addText(NEW_LINE);
		
		text = String.format("%8s%s: ", "", Messages.getString("junior.averageJumps"));   
		this.addText(text);
		if(junior.getPops() < 2) {
			text = String.format("~%.2f", new BigDecimal(junior.getAveragePops()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()); 
			this.addText(text);
			this.addStyle(this.getText().length() - text.length(), text.length(), Colors.getMutedText(), SWT.BOLD);
		} else {
			text = String.format("%.2f", new BigDecimal(junior.getAveragePops()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()); 
			this.addText(text);
			this.addStyle(this.getText().length() - text.length(), text.length(), Colors.getJuniorAccent(), SWT.BOLD);
		}
		
		if (junior.getPlayer() != null) {
			Player player = junior.getPlayer();

			this.addText(NEW_LINE);
			
			addPlayerSkill(Messages.getString("player.form"), Messages.getString("skill.b" + player.getSkills()[0].getForm()), player.getSkills()[0].getForm(), getText().length());			  
			this.addText(NEW_LINE);
			this.addText(NEW_LINE);

			addPlayerSkill(Messages.getString("player.stamina"), Messages.getString("skill.b" + player.getSkills()[0].getStamina()), player.getSkills()[0].getStamina(), getText().length());  
			addPlayerSkill(Messages.getString("player.keeper"), Messages.getString("skill.a" + player.getSkills()[0].getKeeper()), player.getSkills()[0].getKeeper(), getText().length());  
			this.addText(NEW_LINE);
			
			addPlayerSkill(Messages.getString("player.pace"), Messages.getString("skill.b" + player.getSkills()[0].getPace()), player.getSkills()[0].getPace(), getText().length());  
			addPlayerSkill(Messages.getString("player.defender"), Messages.getString("skill.a" + player.getSkills()[0].getDefender()), player.getSkills()[0].getDefender(), getText().length());  
			this.addText(NEW_LINE);
			
			addPlayerSkill(Messages.getString("player.technique"), Messages.getString("skill.b" + player.getSkills()[0].getTechnique()), player.getSkills()[0].getTechnique(), getText().length());  
			addPlayerSkill(Messages.getString("player.playmaker"), Messages.getString("skill.a" + player.getSkills()[0].getPlaymaker()), player.getSkills()[0].getPlaymaker(), getText().length());  
			this.addText(NEW_LINE);
			
			addPlayerSkill(Messages.getString("player.passing"), Messages.getString("skill.c" + player.getSkills()[0].getPassing()), player.getSkills()[0].getPassing(), getText().length());  
			addPlayerSkill(Messages.getString("player.scorer"), Messages.getString("skill.a" + player.getSkills()[0].getScorer()), player.getSkills()[0].getScorer(), getText().length());  
			this.addText(NEW_LINE);
			this.addText(NEW_LINE);
			
			String sum = String.valueOf(player.getSkills()[0].getSummarySkill() + player.getSkills()[0].getStamina());
			text = String.format(" %s: [%s]", Messages.getString("player.sum"), sum);  
			this.addText(text);
			addStyle(this.getText().length() - sum.length()-2, sum.length()+2, Colors.getJuniorAccent(), SWT.NORMAL);
			
			text = String.format(" = [%d]", player.getSkills()[0].getSummarySkill()); 
			this.addText(text);
			
			text = String.format(" + [%d] %s", player.getSkills()[0].getStamina(), Messages.getString("player.stamina"));  
			this.addText(text);
			this.addStyle(this.getText().length() - text.length(), text.length(), Colors.getMutedText(), SWT.NORMAL);
		}
		
		this.addText(NEW_LINE);
		this.addText(NEW_LINE);
		
		for(int i = 0 ; i <= max; i++) {
			String value = String.valueOf(junior.getSkills()[i].getSkill());
			text = String.format("%s ", value); 
			if (i > 0) {
				int missing_weeks = junior.getSkills()[i - 1].getWeeks() - junior.getSkills()[i].getWeeks() - 1;
				for (int j = 0; j < missing_weeks; j++) {
					this.addText("? "); 
				}
			}
			this.addText(text);
			this.addStyle(this.getText().length() - value.length() - 1, value.length(), Colors.getMutedText(), SWT.NORMAL);
			if(i > 0 && (junior.getSkills()[i].getSkill() - junior.getSkills()[i-1].getSkill() > 0)) {
				this.addStyle(this.getText().length() - value.length() - 1, value.length(), null, SWT.BOLD);
			}
		}
		
		this.setRedraw(true);

	}

}
