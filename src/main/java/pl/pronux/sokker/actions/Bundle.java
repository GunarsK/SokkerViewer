package pl.pronux.sokker.actions;

import pl.pronux.sokker.handlers.SettingsHandler;

/** a system's ready to run release zip and what it starts with */
public enum Bundle {

	WINDOWS("-win64-with-java.zip", "SokkerViewer.exe", "javaw.exe"),

	MACOS_ARM64("-macos-arm64-with-java.zip", "SokkerViewer.command", "java"),

	MACOS_X64("-macos-x64-with-java.zip", "SokkerViewer.command", "java");

	/** the end of the zip's file name */
	private final String zipSuffix;

	/** the file that starts SokkerViewer, next to Launcher.jar */
	private final String launcher;

	/** the java program in runtime/bin */
	private final String java;

	Bundle(String zipSuffix, String launcher, String java) {
		this.zipSuffix = zipSuffix;
		this.launcher = launcher;
		this.java = java;
	}

	public String getZipSuffix() {
		return zipSuffix;
	}

	public String getLauncher() {
		return launcher;
	}

	public String getJava() {
		return java;
	}

	/** this computer's bundle, null when none is built for it */
	public static Bundle current() {
		if (SettingsHandler.IS_WINDOWS) {
			return WINDOWS;
		}
		if (SettingsHandler.IS_MACOSX) {
			String arch = System.getProperty("os.arch");
			if ("aarch64".equals(arch)) {
				return MACOS_ARM64;
			}
			if ("x86_64".equals(arch)) {
				return MACOS_X64;
			}
		}
		return null;
	}
}
