package com.example.techtest.termfrequency.file;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;

import com.example.techtest.termfrequency.stream.Source;

import org.apache.logging.log4j.LogManager;

/**
 * Watcher to generate events as new files are created in a specific path,
 * including all the possible subfolders.
 * 
 * By default the watcher informs, also, about all the already existing files in
 * the selected path when the watcher was created. This behavior can be altered
 * using the {@link #includeExistingFiles(boolean) includeExistingFiles} method
 * 
 * @author dmacia
 */
public class NewFilesWatcher implements Callable<Integer>, Source<Path> {

	private static final Logger log = LogManager.getLogger(NewFilesWatcher.class);

	public static final int RESULT_OK = 0;
	public static final int RESULT_ERROR = -1;

	private WatchService watchService;
	private Path sourcePath;
	private boolean includeExistingFiles = true;
	private Queue<Path> outputQueue = null;

	private NewFilesWatcher(Path sourcePath) throws IOException {
		watchService = FileSystems.getDefault().newWatchService();
		this.sourcePath = sourcePath;
	}

	/**
	 * Watch for new files in the specific path
	 * 
	 * @param path folder to watch
	 * @return
	 * @throws IOException
	 */
	public static NewFilesWatcher watcherFor(Path path) throws IOException {
		if (path == null || Files.notExists(path)) {
			throw new IllegalArgumentException("Invalid path");
		}

		return new NewFilesWatcher(path);
	}

	/**
	 * Watch for new files in the specific path
	 * 
	 * @param path folder to watch
	 * @return
	 * @throws IOException
	 */
	public static NewFilesWatcher watcherFor(String path) throws IOException {
		return watcherFor(Paths.get(path));
	}

	/**
	 * Inform about already existing files in the source path when the watcher is
	 * created. If false, any existing file in the source path are ignored and only
	 * new ones are computed
	 * 
	 * @param include include or filter existing files
	 * @return This instance
	 */
	public NewFilesWatcher includeExistingFiles(boolean include) {
		this.includeExistingFiles = include;
		return this;
	}

	@Override
	public void output(BlockingQueue<Path> queue) {
		if (queue != null) {
			this.outputQueue = queue;
		}
	}

	@Override
	public Integer call() throws Exception {

		if (this.outputQueue == null) {
			log.error("No output queue was provided");
			return RESULT_ERROR;
		}

		try {
			// Start with the folder current contents and/or register for new files
			watchService = FileSystems.getDefault().newWatchService();
			if (this.includeExistingFiles) {
				visitFolder(this.sourcePath);
			} else {
				watchFolder(this.sourcePath);
			}

			WatchKey key;
			while (!Thread.currentThread().isInterrupted() && (key = watchService.take()) != null) {
				Path path = (Path) key.watchable();

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> eventKind = event.kind();

					if (eventKind == StandardWatchEventKinds.ENTRY_CREATE) {
						processNewFile(path.resolve((Path) event.context()));

					} else if (eventKind == StandardWatchEventKinds.OVERFLOW) {
						log.warn("Overflow detected. File watch service should be reset");
						watchService.close();
						// Restart consumption from the beginning, visiting the already (maybe pending)
						// existing files
						watchService = FileSystems.getDefault().newWatchService();
						visitFolder(this.sourcePath);
					}
				}

				key.reset();
			}

		} catch (IOException e) {
			log.error("Error monitoring filesystem for new files", e);
			return RESULT_ERROR;
		} catch (InterruptedException e) {
			log.warn("Filesystem monitoring interrupted");
			Thread.currentThread().interrupt();
		}

		return RESULT_OK;
	}

	/**
	 * Visit a path processing all the existing files on it and registering all the
	 * existing subfolders
	 * 
	 * @param folderPath
	 * @throws IOException
	 */
	private void visitFolder(Path folderPath) throws IOException {
		Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				processNewFile(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc == null) {
					watchFolder(dir);
				} else {
					throw exc;
				}

				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Register a folder to be watched
	 * 
	 * @param folderPath
	 * @throws IOException
	 */
	private void watchFolder(Path folderPath) throws IOException {
		folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		log.debug("New folder {} registered to watch", folderPath);
	}

	/**
	 * Process any new file in the paths registered for watching
	 * 
	 * @param newFilePath
	 * @throws IOException
	 */
	private void processNewFile(Path newFilePath) throws IOException {

		log.debug("New file observed {} isDirectory: {} isReadable: {} isHidden: {}", newFilePath,
				Files.isDirectory(newFilePath), Files.isReadable(newFilePath), Files.isHidden(newFilePath));

		if (Files.isDirectory(newFilePath)) {
			visitFolder(newFilePath);
		} else if (Files.isReadable(newFilePath) && !Files.isHidden(newFilePath)) {
			this.outputQueue.offer(newFilePath);
		}
	}
}
