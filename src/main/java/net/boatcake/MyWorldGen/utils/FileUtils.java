package net.boatcake.MyWorldGen.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.boatcake.MyWorldGen.MyWorldGen;

public class FileUtils {

	public static void writeStream(InputStream inStream, String outName)
			throws IOException {
		// Used for self-extracting files
		OutputStream outStream = new FileOutputStream(new File(MyWorldGen.globalSchemDir,
				new File(outName).getName()));
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

}
