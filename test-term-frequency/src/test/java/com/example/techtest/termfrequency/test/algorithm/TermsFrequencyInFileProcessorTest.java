package com.example.techtest.termfrequency.test.algorithm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
 * Test class for the TermsFrequencyInFileProcessor component
 *
 */
public class TermsFrequencyInFileProcessorTest {

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
	public void splitTermsBySpacesAndCommasTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			int current = termsCount.getOrDefault(outputTerm.term(), 0);
			termsCount.put(outputTerm.term(), current + 1);
		}

		Assertions.assertEquals(1, termsCount.get("one"), "one");
		Assertions.assertEquals(1, termsCount.get("two"), "two");
		Assertions.assertEquals(1, termsCount.get("three"), "three");
	}

	@Test
	public void splitTermsBySpacesAndCommasFrequencyTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(2 / 7f, termsFreq.get("one"), "one");
		Assertions.assertEquals(2 / 7f, termsFreq.get("two"), "one");
		Assertions.assertEquals(3 / 7f, termsFreq.get("three"), "one");
	}

	@Test
	public void splitTermsByNewLinesSpacesAndCommasTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-new-lines-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			int current = termsCount.getOrDefault(outputTerm.term(), 0);
			termsCount.put(outputTerm.term(), current + 1);
		}

		Assertions.assertEquals(1, termsCount.get("one"), "one");
		Assertions.assertEquals(1, termsCount.get("two"), "two");
		Assertions.assertEquals(1, termsCount.get("three"), "three");
	}

	@Test
	public void splitTermsByNewLinesSpacesAndCommasFrequencyTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-new-lines-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(2 / 7f, termsFreq.get("one"), "one");
		Assertions.assertEquals(2 / 7f, termsFreq.get("two"), "one");
		Assertions.assertEquals(3 / 7f, termsFreq.get("three"), "one");
	}

	@Test
	public void splitTermsBySpecialCharTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns", "split-special-char.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.tokenSplitRegex("\\|");

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			int current = termsCount.getOrDefault(outputTerm.term(), 0);
			termsCount.put(outputTerm.term(), current + 1);
		}

		Assertions.assertEquals(1, termsCount.get("one"), "one");
		Assertions.assertEquals(1, termsCount.get("two"), "two");
		Assertions.assertEquals(1, termsCount.get("three"), "three");
	}

	@Test
	public void splitTermsBySpecialCharFrequencyTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns", "split-special-char.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.tokenSplitRegex("\\|");

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(1 / 3f, termsFreq.get("one"), "one");
		Assertions.assertEquals(1 / 3f, termsFreq.get("two"), "two");
		Assertions.assertEquals(1 / 3f, termsFreq.get("three"), "three");
	}

	@Test
	public void normalizeTermsByDefaultTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"normalize-punctuation-marks.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(1 / 3f, termsFreq.get("one"), "one");
		Assertions.assertEquals(1 / 3f, termsFreq.get("two"), "one");
		Assertions.assertEquals(1 / 3f, termsFreq.get("three"), "one");
	}

	@Test
	public void normalizeTermsByCustomConfigTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"normalize-punctuation-marks.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.tokenNormalizationRegex("[¿^]");

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(1 / 3f, termsFreq.get("one?"), "one");
		Assertions.assertEquals(1 / 3f, termsFreq.get("$two&"), "two");
		Assertions.assertEquals(1 / 3f, termsFreq.get("three!"), "three");
	}

	@Test
	public void computeOnlySelectedTermsTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.addTokenToInform("one");
		config.addTokensToInform(Arrays.asList("two"));

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Integer> termsCount = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			int current = termsCount.getOrDefault(outputTerm.term(), 0);
			termsCount.put(outputTerm.term(), current + 1);
		}

		Assertions.assertEquals(1, termsCount.get("one"), "one");
		Assertions.assertEquals(1, termsCount.get("two"), "two");
		Assertions.assertEquals(1, termsCount.get(TermsFrequencyInFileProcessor.OTHER_TOKENS_GROUP), "other");
		Assertions.assertNull(termsCount.get("three"), "three");
	}

	@Test
	public void computeOnlySelectedTermsFrequencyTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.addTokenToInform("one");
		config.addTokensToInform(Arrays.asList("two"));

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;
		Map<String, Float> termsFreq = new HashMap<>();

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			float current = termsFreq.getOrDefault(outputTerm.term(), 0f);
			termsFreq.put(outputTerm.term(), current + outputTerm.freq());
		}

		Assertions.assertEquals(2 / 7f, termsFreq.get("one"), "one");
		Assertions.assertEquals(2 / 7f, termsFreq.get("two"), "two");
		Assertions.assertEquals(3 / 7f, termsFreq.get(TermsFrequencyInFileProcessor.OTHER_TOKENS_GROUP), "other");
		Assertions.assertNull(termsFreq.get("three"), "three");
	}

	@Test
	public void allTermsRankablesByDefaultTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {
			Assertions.assertTrue(outputTerm.rankable(), "rankable term");
		}
	}

	@Test
	public void onlySelectedTermsRankablesTest() throws Exception {
		Path testFile = Paths.get("src", "test", "resources", "scenarios", "test-patterns",
				"split-spaces-and-commas.txt");

		TermsFrequencyInFileConfig config = new TermsFrequencyInFileConfig();
		config.addTokenToInform("one");
		config.addTokensToInform(Arrays.asList("two"));

		TermsFrequencyInFileProcessor processor = new TermsFrequencyInFileProcessor(config);
		processor.from(source);
		processor.output(output);

		source.offer(testFile);

		executorService.submit(processor);
		TermFrequency outputTerm;

		while ((outputTerm = output.poll(2, TimeUnit.SECONDS)) != null) {

			if (config.tokensToInform().contains(outputTerm.term())) {
				Assertions.assertTrue(outputTerm.rankable(), "rankable term");
			} else {
				Assertions.assertFalse(outputTerm.rankable(), "not rankable term");
			}
		}
	}
}
