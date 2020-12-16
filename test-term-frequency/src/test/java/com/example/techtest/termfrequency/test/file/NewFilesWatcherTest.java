package com.example.techtest.termfrequency.test.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.techtest.termfrequency.file.NewFilesWatcher;

/**
 * Test class for the NewFilesWatcher component
 *
 */
public class NewFilesWatcherTest {

	private static ExecutorService executorService;
	private static BlockingQueue<Path> output;
	private static Path tempFolderPath = null;

	@BeforeAll
	public static void initBeforeAll() throws IOException {
		tempFolderPath = Files.createTempDirectory("NewFilesWatcherTest-" + System.currentTimeMillis());
		output = new LinkedBlockingQueue<>();
	}

	@BeforeEach
	public void cleanBefore() throws IOException, InterruptedException {
		if (tempFolderPath != null) {
			purgeFolder(tempFolderPath);
		}
		
		if (executorService != null) {
			executorService.shutdownNow();
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		}
		executorService = Executors.newCachedThreadPool();
		
		output.clear();
	}

	@AfterAll
	public static void cleanAfterAll() throws IOException {
		if (executorService != null) {
			executorService.shutdownNow();
		}

		if (tempFolderPath != null) {
			purgeFolder(tempFolderPath);
			tempFolderPath.toFile().delete();
		}
	}

	private static void purgeFolder(Path folderPath) throws IOException {

		Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				file.toFile().delete();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (exc == null && !dir.equals(folderPath)) {
					dir.toFile().delete();
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Test
	public void invalidPathTest() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> NewFilesWatcher.watcherFor("bad pad"));
	}

	@Test
	public void invalidStringPathTest() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> NewFilesWatcher.watcherFor(Paths.get("bad pad")));
	}

	@Test
	public void existingFilesTest() throws Exception {
		String path = Paths.get("src", "test", "resources", "scenarios", "basic").toString();

		NewFilesWatcher watcher = initFileWatcher(path);
		watcher.includeExistingFiles(true);

		executorService.submit(watcher);
		Path outputPath;

		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputPath.toString().equals(path + File.separator + "documento-1.md")
					|| outputPath.toString().equals(path + File.separator + "documento-2.md"));
		}
	}

	@Test
	public void existingFilesInSubfoldersTest() throws Exception {
		String path = "src/test/resources/scenarios/basic-subfolders";

		NewFilesWatcher watcher = initFileWatcher(path);
		watcher.includeExistingFiles(true);

		executorService.submit(watcher);
		Path outputPath;

		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputPath.toString().equals(path + File.separator + "documento-1.md")
					|| outputPath.toString().equals(path + File.separator + "documento-2.md")
					|| outputPath.toString().equals(path + File.separator + "subfolder/sub-documento-1.md")
					|| outputPath.toString().equals(path + File.separator + "subfolder/sub-documento-2.md"));
		}
	}

	@Test
	public void noWatchExistingFilesTest() throws Exception {
		String path = Paths.get("src", "test", "resources", "scenarios", "basic-subfolders").toString();

		NewFilesWatcher watcher = initFileWatcher(path);
		watcher.includeExistingFiles(false);

		executorService.submit(watcher);
		Path outputPath = output.poll(2, TimeUnit.SECONDS);
		Assertions.assertNull(outputPath);
	}

	@Test
	public void newCreatedFileTest() throws Exception {
		NewFilesWatcher watcher = initFileWatcher(tempFolderPath);
		watcher.includeExistingFiles(false);

		executorService.submit(watcher);
		File newFile = Path.of(tempFolderPath.toString(), "test.txt").toFile();
		newFile.createNewFile();

		Path outputPath;
		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputPath.toString().equals(newFile.toString()));
		}
	}

	@Test
	public void newCreatedFilesInSubfoldersTest() throws Exception {
		NewFilesWatcher watcher = initFileWatcher(tempFolderPath);
		watcher.includeExistingFiles(false);

		executorService.submit(watcher);
		File newFile = Path.of(tempFolderPath.toString(), "test.txt").toFile();
		File newFileInSubfolder = Path.of(tempFolderPath.toString(), "subfolder", "test-subfolder.txt").toFile();

		newFile.createNewFile();
		newFileInSubfolder.getParentFile().mkdirs();
		newFileInSubfolder.createNewFile();

		Path outputPath;
		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputPath.toString().equals(newFile.toString())
					|| outputPath.toString().equals(newFileInSubfolder.toString()));
		}
	}

	@Test
	public void noModificationOrDeleteEventsTest() throws Exception {
		NewFilesWatcher watcher = initFileWatcher(tempFolderPath);
		watcher.includeExistingFiles(false);

		executorService.submit(watcher);
		File newFile = Path.of(tempFolderPath.toString(), "test.txt").toFile();
		newFile.createNewFile();

		Path outputPath;
		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputPath.toString().equals(newFile.toString()));
		}

		Files.writeString(newFile.toPath(), "File contents updated", StandardOpenOption.APPEND);
		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertNull(outputPath);
		}

		newFile.delete();
		while ((outputPath = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertNull(outputPath);
		}
	}

	private NewFilesWatcher initFileWatcher(String path) throws IOException {
		NewFilesWatcher watcher = NewFilesWatcher.watcherFor(path);
		watcher.output(output);

		return watcher;
	}

	private NewFilesWatcher initFileWatcher(Path path) throws IOException {
		NewFilesWatcher watcher = NewFilesWatcher.watcherFor(path);
		watcher.output(output);

		return watcher;
	}
}
