package pl.pronux.sokker.data.properties.dto;

import pl.pronux.sokker.data.properties.PropertiesSession;
import pl.pronux.sokker.exceptions.BadArgumentException;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.utils.security.Crypto;

public class SokkerViewerSettingsDto extends SokkerViewerSettings {

	public SokkerViewerSettingsDto(PropertiesSession properties) {
		this.setBackupDirectory(properties.getProperty("backup.dir")); 
		this.setLangCode(properties.getProperty("lang.code")); 
		this.setUsername(properties.getProperty("sk.login")); 
		
		this.setBaseDirectory(properties.getProperty("sk.dir")); 
		
		try {
			this.setPassword(new String(Crypto.decodeBase64(properties
					.getProperty("sk.password")))); 
		} catch (BadArgumentException e1) {
			this.setPassword("");
		}

		this.setCheckProperties(flag(properties, "check.properties", true));
		this.setInfoClose(flag(properties, "info.close", false));
		this.setInfoUpdate(flag(properties, "info.update", false));
		this.setSavePassword(flag(properties, "conf.savepass", false));
		this.setStartup(flag(properties, "sv.startup", false));
		this.setUpdate(flag(properties, "conf.getXML", false));
		String lastVersion = properties.getProperty("sv.version");
		this.setLastVersion(lastVersion == null ? "" : lastVersion);
	}

	/** "1" as true; a missing key gives the default */
	private static boolean flag(PropertiesSession properties, String key, boolean missing) {
		String value = properties.getProperty(key);
		return value == null ? missing : value.equals("1");
	}

}
