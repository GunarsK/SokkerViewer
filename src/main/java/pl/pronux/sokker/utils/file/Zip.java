package pl.pronux.sokker.utils.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import pl.pronux.sokker.utils.Log;

public class Zip {

	public static final int BUFFER_SIZE = 10240;

	public static void createZipArchive(File archiveFile, File[] tobeZippedFiles) throws IOException {
		byte buffer[] = new byte[BUFFER_SIZE];
		// Open archive file
		FileOutputStream stream = new FileOutputStream(archiveFile);
		ZipOutputStream out = new ZipOutputStream(stream);

		for (int i = 0; i < tobeZippedFiles.length; i++) {
			if (tobeZippedFiles[i] == null || !tobeZippedFiles[i].exists() || tobeZippedFiles[i].isDirectory()) {
				continue;
			}
			// Add archive entry
			ZipEntry zipAdd = new ZipEntry(tobeZippedFiles[i].getName());
			zipAdd.setTime(tobeZippedFiles[i].lastModified());
			out.putNextEntry(zipAdd);

			// Read input & write to output
			FileInputStream in = new FileInputStream(tobeZippedFiles[i]);
			while (true) {
				int nRead = in.read(buffer, 0, buffer.length);
				if (nRead <= 0) {
					break;
				}
				out.write(buffer, 0, nRead);
			}
			in.close();
		}

		out.close();
		stream.close();
		Log.info("Adding completed OK"); 
	}

	/** unpacks archive into target; no entry may point outside it */
	public static void unzip(File archive, File target) throws IOException {
		String root = target.getCanonicalPath() + File.separator;
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				File file = new File(target, entry.getName());
				if (!file.getCanonicalPath().startsWith(root)) {
					throw new IOException("zip entry outside the target folder: " + entry.getName());
				}
				File dir = entry.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs()) {
					throw new IOException("cannot create " + dir);
				}
				if (entry.isDirectory()) {
					continue;
				}
				OutputStream out = new FileOutputStream(file);
				try {
					int read;
					while ((read = zin.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}
				} finally {
					out.close();
				}
			}
		} finally {
			zin.close();
		}
	}
}