package net.boatcake.MyWorldGen.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.boatcake.MyWorldGen.MyWorldGen;

public class FileUtils {

	public static void writeStream(InputStream inStream, String outName) throws IOException {
		// Used for self-extracting files
		OutputStream outStream = new FileOutputStream(new File(MyWorldGen.globalSchemDir, new File(outName).getName()));
		byte[] buffer = new byte[256];
		int readLen;
		while (true) {
			readLen = inStream.read(buffer, 0, buffer.length);
			if (readLen <= 0) {
				break;
			}
			outStream.write(buffer, 0, readLen);
		}
		inStream.close();
		outStream.close();
	}

	/**
	 * Self-extract bundled schematics into the worldgen directory so that the
	 * players have something to start with
	 */
	public static void extractSchematics(File sourceFile) {
		try {
			ZipFile zf = new ZipFile(sourceFile);
			ZipEntry worldGenDir = zf.getEntry(MyWorldGen.resourcePath + "/");
			if (worldGenDir != null && worldGenDir.isDirectory()) {
				for (Enumeration<? extends ZipEntry> e = zf.entries(); e.hasMoreElements();) {
					ZipEntry ze = e.nextElement();
					if (!ze.isDirectory() && ze.getName().startsWith(worldGenDir.getName())) {
						writeStream(zf.getInputStream(ze), ze.getName());
					}
				}
			}
			zf.close();
		} catch (FileNotFoundException e) {
			// Not in a jar
			File f = new File(MyWorldGen.class.getClassLoader().getResource(MyWorldGen.resourcePath).getPath());
			if (f.isDirectory()) {
				for (String s : f.list()) {
					try {
						writeStream(new FileInputStream(new File(f, s)), s);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
