package org.terasology.logic.world.generator.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.logic.world.generator.core.ChunkGeneratorManager;

/**
 * Load and save the ChunkGeneratorManager.
 * 
 * @author Mathias Kalb
 */
public class ChunkGeneratorPersister {

	private Logger logger = Logger.getLogger(getClass().getName());

	public void save(File file, ChunkGeneratorManager chunkGeneratorManager)
			throws IOException {
		File parentFile = file.getParentFile();
		if (parentFile != null) {
			parentFile.mkdirs();
		}
		FileOutputStream out = new FileOutputStream(file);

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(out));
			ChunkGeneratorJSONFormat.write(chunkGeneratorManager, bufferedWriter);
			bufferedWriter.flush();
		} finally {
			// JAVA7 : Replace with improved resource handling
			try {
				out.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to close file", e);
			}
		}
	}

	public ChunkGeneratorManager load(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		ChunkGeneratorManager chunkGeneratorManager = null;
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(in));
			chunkGeneratorManager = ChunkGeneratorJSONFormat.read(bufferedReader);
		} finally {
			// JAVA7: Replace with improved resource handling
			try {
				in.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to close file", e);
			}
		}
		return chunkGeneratorManager;
	}

}
