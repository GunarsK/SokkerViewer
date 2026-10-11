package pl.pronux.sokker.data.properties.dao;

import java.io.IOException;

import pl.pronux.sokker.data.properties.PropertiesSession;
import pl.pronux.sokker.data.properties.dto.SokkerViewerSettingsDto;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.utils.security.Crypto;

public class SokkerViewerSettingsDao {

	private PropertiesSession properties;

	public SokkerViewerSettingsDao(PropertiesSession properties) {
		this.properties = properties;
	}

	public SokkerViewerSettings getSokkerViewerSettings() {
		SokkerViewerSettings settings = new SokkerViewerSettingsDto(properties);
		settings.setProxySettings(new ProxyDao(properties).getProxySettings());
		return settings;
	}

	public void updateSokkerViewerSettings(SokkerViewerSettings sokkerViewerSettings) throws IOException {
		properties.setProperty("sk.login", sokkerViewerSettings.getUsername()); 
		
		properties.setProperty("sk.dir", sokkerViewerSettings.getBaseDirectory()); 
		properties.setProperty("backup.dir", sokkerViewerSettings.getBackupDirectory());
		properties.setProperty("conf.getXML", flag(sokkerViewerSettings.isUpdate()));
		String password = sokkerViewerSettings.isSavePassword() ? sokkerViewerSettings.getPassword() : "";
		properties.setProperty("sk.password", Crypto.encodeBase64(password.getBytes()));
		properties.setProperty("conf.savepass", flag(sokkerViewerSettings.isSavePassword()));
		properties.setProperty("info.update", flag(sokkerViewerSettings.isInfoUpdate()));
		properties.setProperty("info.close", flag(sokkerViewerSettings.isInfoClose()));
		properties.setProperty("check.properties", flag(sokkerViewerSettings.isCheckProperties()));
		properties.setProperty("sv.startup", flag(sokkerViewerSettings.isStartup()));
		properties.setProperty("sv.version", sokkerViewerSettings.getLastVersion());
		properties.setProperty("lang.code", sokkerViewerSettings.getLangCode());

		// saves the file with the proxy settings
		new ProxyDao(properties).updateProxySettings(sokkerViewerSettings.getProxySettings());
	}

	/** a flag as "1" or "0" */
	private static String flag(boolean on) {
		return on ? "1" : "0";
	}
}
