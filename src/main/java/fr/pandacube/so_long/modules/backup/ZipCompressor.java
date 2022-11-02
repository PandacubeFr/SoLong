package fr.pandacube.so_long.modules.backup;

import fr.pandacube.lib.chat.Chat;
import fr.pandacube.lib.util.MemoryUtil;
import fr.pandacube.lib.util.TimeUtil;
import net.md_5.bungee.api.chat.BaseComponent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor {
	private final File srcDir, destFile;
	private final int compressionLevel;
	private final FilenameFilter filter;
	
	private final List<Entry> entriesToCompress;
	private ZipOutputStream zipOutStream;

	private final Object stateLock = new Object();
	private final long inputByteSize;
	private long startTime;
	private long elapsedByte = 0;
	private Exception exception = null;
	private boolean started = false;
	private boolean finished = false;
	
	public ZipCompressor(File s, File d, int c, FilenameFilter f) {
		srcDir = s;
		destFile = d;
		compressionLevel = c;
		filter = f;
		
		entriesToCompress = new ArrayList<>();
		inputByteSize = addEntry("");
		
	}
	
	
	public BaseComponent getState() {
		synchronized (stateLock) {
			if (!started) {
				return Chat.text("Démarrage...").get();
			}
			else if (!finished && exception == null) {
				float progress = elapsedByte / (float) inputByteSize;
				long elapsedTime = System.nanoTime() - startTime;
				long remainingTime = (long)(elapsedTime / progress) - elapsedTime;
				return Chat.chat()
						.infoColor()
						.thenData(Math.round(progress*100*10)/10 + "% ")
						.thenText("(")
						.thenData(MemoryUtil.humanReadableSize(elapsedByte) + "/" + MemoryUtil.humanReadableSize(inputByteSize))
						.thenText(") - Temps restant estimé : ")
						.thenData(TimeUtil.durationToString(remainingTime / 1000000))
						.get();
			}
			else if (exception != null) {
				return Chat.failureText("Erreur lors de l'archivage (voir console pour les détails)").get();
			}
			else { // finished
				return Chat.successText("Terminé !").get();
			}
		}
	}
	
	public float getProgress() {
		if (!started)
			return 0;
		if (!finished)
			return elapsedByte / (float) inputByteSize;
		return 1;
	}
	
	/**
	 * Run the compression on the current thread, and returns after the end of the compression.
	 * Should be run asynchronously (not on Server Thread).
	 * @throws Exception
	 */
	public void compress() throws Exception {
		destFile.getParentFile().mkdirs();
		
		try(ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destFile), BackupManager.bufferSize))) {
			zipOutStream = zipStream;
			zipOutStream.setLevel(compressionLevel);
			
			synchronized (stateLock) {
				startTime = System.nanoTime();
				started = true;
			}
			
			for (Entry entry : entriesToCompress) {
				entry.zip();
			}
			
			synchronized (stateLock) {
				finished = true;
			}
		} catch (Exception e) {
			synchronized (stateLock) {
				exception = e;
			}
			throw e;
		}
	}

	
	private long addEntry(String currentEntry) {
		final File currentFile = new File(srcDir, currentEntry);
		if (!currentFile.exists())
			return 0;
		if (currentFile.isDirectory()) {
			if (!currentEntry.isEmpty()) { // it's not the zip root directory
				currentEntry += "/";
				entriesToCompress.add(new Entry(currentFile, currentEntry));
			}
			
			long sum = 0;
			for (String child : currentFile.list(filter)) {
				sum += addEntry(currentEntry + child);
			}
			
			return sum;
		}
		else { // is a file
			entriesToCompress.add(new Entry(currentFile, currentEntry));
			return currentFile.length();
		}
	}
	
	private class Entry {
		File file;
		String entry;
		Entry(File f, String e) {
			file = f;
			entry = e;
		}
		
		void zip() throws IOException {
			ZipEntry zipEntry = new ZipEntry(entry);
			if (file.isDirectory()) {
				zipOutStream.putNextEntry(zipEntry);
				zipOutStream.closeEntry();
			}
			else {

				zipEntry.setTime(file.lastModified());
				zipOutStream.putNextEntry(zipEntry);
				
				try {
					Files.copy(file.toPath(), zipOutStream);
				}
				finally {
					zipOutStream.closeEntry();
				}
				
				synchronized (stateLock) {
					elapsedByte += file.length();
				}
			}
		}
		
	}
}
