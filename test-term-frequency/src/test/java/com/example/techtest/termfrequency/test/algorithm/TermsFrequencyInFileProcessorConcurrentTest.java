package com.example.techtest.termfrequency.test.algorithm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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

import com.example.techtest.termfrequency.algorithm.TermFrequency;
import com.example.techtest.termfrequency.algorithm.TermsFrequencyInFileConfig;
import com.example.techtest.termfrequency.algorithm.TermsFrequencyInFileProcessor;

/**
 * Test class for the TermsFrequencyInFileProcessor component and concurrent
 * behavior
 *
 */
public class TermsFrequencyInFileProcessorConcurrentTest {

	private static ExecutorService executorService;
	private static BlockingQueue<Path> source = null;
	private static BlockingQueue<TermFrequency> output = null;

	@BeforeAll
	public static void initBeforeAll() throws IOException {
		source = new LinkedBlockingQueue<>();
		output = new LinkedBlockingQueue<>();
	}

	@BeforeEach
	public void cleanBefore() throws IOException, InterruptedException {
		if (executorService != null) {
			executorService.shutdownNow();
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		}
		executorService = Executors.newCachedThreadPool();

		source.clear();
		output.clear();
	}

	@AfterAll
	public static void cleanAfterAll() throws IOException {
		if (executorService != null) {
			executorService.shutdownNow();
		}
	}

	@Test
	public void concurrentReadFromSingleFileTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();

		TermsFrequencyInFileProcessor processor1 = new TermsFrequencyInFileProcessor(config);
		processor1.from(source);
		processor1.output(output);

		TermsFrequencyInFileProcessor processor2 = new TermsFrequencyInFileProcessor(config);
		processor2.from(source);
		processor2.output(output);

		source.offer(testFile);

		executorService.submit(processor1);
		executorService.submit(processor2);

		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();
		int numberOfEvents = 0;

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			int current = termsCount.getOrDefault(outputTerm.term(), 0);
			termsCount.put(outputTerm.term(), current + 1);
			numberOfEvents++;
		}

		// Only one processor read the file and generate the results
		Assertions.assertEquals(1, termsCount.get("one"), "one");
		Assertions.assertEquals(1, termsCount.get("two"), "two");
		Assertions.assertEquals(1, termsCount.get("three"), "three");
		Assertions.assertEquals(3, numberOfEvents, "events");
	}

	@Test
	public void concurrentReadFromMultipleFilesTest() throws Exception {
		Path testFile1 = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");
		Path testFile2 = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-new-lines-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();

		TermsFrequencyInFileProcessor processor1 = new TermsFrequencyInFileProcessor(config);
		processor1.from(source);
		processor1.output(output);

		TermsFrequencyInFileProcessor processor2 = new TermsFrequencyInFileProcessor(config);
		processor2.from(source);
		processor2.output(output);

		source.offer(testFile1);
		source.offer(testFile2);

		executorService.submit(processor1);
		executorService.submit(processor2);

		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();
		Map<String, Integer> eventsByPath = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			termsCount.put(outputTerm.term(), termsCount.getOrDefault(outputTerm.term(), 0) + 1);
			eventsByPath.put(outputTerm.path().toString(),
					eventsByPath.getOrDefault(outputTerm.path().toString(), 0) + 1);
		}

		// Terms detected in both files
		Assertions.assertEquals(2, termsCount.get("one"), "one");
		Assertions.assertEquals(2, termsCount.get("two"), "two");
		Assertions.assertEquals(2, termsCount.get("three"), "three");

		// Only one event by term and path
		eventsByPath.entrySet().forEach(e -> {
			Assertions.assertEquals(3, e.getValue(), "path: " + e.getKey());
		});

	}

	@Test
	public void concurrentReadFromMultipleFilesFrequencyTest() throws Exception {
		Path testFile1 = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");
		Path testFile2 = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-new-lines-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();

		TermsFrequencyInFileProcessor processor1 = new TermsFrequencyInFileProcessor(config);
		processor1.from(source);
		processor1.output(output);

		TermsFrequencyInFileProcessor processor2 = new TermsFrequencyInFileProcessor(config);
		processor2.from(source);
		processor2.output(output);

		source.offer(testFile1);
		source.offer(testFile2);

		executorService.submit(processor1);
		executorService.submit(processor2);

		TermFrequency outputTerm;
		Map<String, Float> termsCount = new HashMap<>();
		Map<String, Integer> eventsByPath = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			termsCount.put(outputTerm.term(), termsCount.getOrDefault(outputTerm.term(), 0f) + outputTerm.freq());
			eventsByPath.put(outputTerm.path().toString(),
					eventsByPath.getOrDefault(outputTerm.path().toString(), 0) + 1);
		}

		// Terms detected in both files (frequencies added in the map)
		Assertions.assertEquals(2f * 2 / 7, termsCount.get("one"), "one");
		Assertions.assertEquals(2f * 2 / 7, termsCount.get("two"), "two");
		Assertions.assertEquals(2f * 3 / 7, termsCount.get("three"), "three");

		// Only one event by term and path
		eventsByPath.entrySet().forEach(e -> {
			Assertions.assertEquals(3, e.getValue(), "path: " + e.getKey());
		});

	}

}
