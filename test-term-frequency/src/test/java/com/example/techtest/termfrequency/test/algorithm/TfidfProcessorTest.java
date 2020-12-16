package com.example.techtest.termfrequency.test.algorithm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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
import com.example.techtest.termfrequency.algorithm.TfidfProcessor;
import com.example.techtest.termfrequency.algorithm.TfidfProcessorConfig;
import com.example.techtest.termfrequency.algorithm.TfidfProcessor.TfidfProcessorStats;
import com.example.techtest.termfrequency.algorithm.TfidfProcessorConfig.IDF_MODE;
import com.example.techtest.termfrequency.file.NewFilesWatcher;
import com.example.techtest.termfrequency.util.ImmutablePair;

/**
 * Test class for the TfidfProcessor component
 *
 */
public class TfidfProcessorTest {

	private static ExecutorService executorService;
	private static BlockingQueue<Path> pathQueue = null;
	private static BlockingQueue<TermFrequency> termFreqQueue = null;

	@BeforeAll
	public static void initBeforeAll() throws IOException {
		pathQueue = new LinkedBlockingQueue<>();
		termFreqQueue = new LinkedBlockingQueue<>();
	}

	@BeforeEach
	public void cleanBefore() throws IOException, InterruptedException {
		if (executorService != null) {
			executorService.shutdownNow();
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		}
		executorService = Executors.newCachedThreadPool();

		pathQueue.clear();
		termFreqQueue.clear();
	}

	@AfterAll
	public static void cleanAfterAll() throws IOException {
		if (executorService != null) {
			executorService.shutdownNow();
		}
	}

	@Test
	public void normalIDFAlgorithmTest() throws Exception {

		TfidfProcessorConfig config = new TfidfProcessorConfig();
		TfidfProcessor processor = new TfidfProcessor(config);
		processor.from(termFreqQueue);

		executorService.submit(processor);

		// Simulated computed terms frequency
		TermFrequency tf1 = new TermFrequency().path(Path.of("path1")).term("term1").freq(0.2f);
		TermFrequency tf2 = new TermFrequency().path(Path.of("path1")).term("term2").freq(0.4f);
		TermFrequency tf3 = new TermFrequency().path(Path.of("path2")).term("term1").freq(0.6f);

		termFreqQueue.offer(tf1);
		termFreqQueue.offer(tf2);
		termFreqQueue.offer(tf3);

		long maxTimeoutMs = 5000;
		long start = System.currentTimeMillis();
		TfidfProcessorStats stats = processor.getStats();

		// Wait maxTimeoutMs at most
		while (stats.analyzedPaths() < 2l && (System.currentTimeMillis() - start < maxTimeoutMs)) {
			stats = processor.getStats();
		}

		Assertions.assertEquals(2, stats.analyzedPaths(), "analyzed-paths");
		Assertions.assertEquals(TfidfProcessorConfig.IDF_MODE.NORMAL, stats.idfMode(), "mode");

		// Validate ranking contents
		double path1TFIDF = computeNormaTfIDF(0.2f, 2, 2) + computeNormaTfIDF(0.4f, 1, 2);
		double path2TFIDF = computeNormaTfIDF(0.6f, 2, 2);

		Collection<ImmutablePair<Path, Double>> ranking = stats.ranking();
		ranking.forEach(p -> {
			if (Path.of("path1").equals(p.key())) {
				Assertions.assertEquals(path1TFIDF, p.value(), "path1-value");
			} else if (Path.of("path1").equals(p.key())) {
				Assertions.assertEquals(path2TFIDF, p.value(), "path2-value");
			}
		});
	}

	@Test
	public void smoothIDFAlgorithmTest() throws Exception {

		TfidfProcessorConfig config = new TfidfProcessorConfig();
		config.mode(IDF_MODE.SMOOTH);
		TfidfProcessor processor = new TfidfProcessor(config);
		processor.from(termFreqQueue);

		executorService.submit(processor);

		// Simulated computed terms frequency
		TermFrequency tf1 = new TermFrequency().path(Path.of("path1")).term("term1").freq(0.2f);
		TermFrequency tf2 = new TermFrequency().path(Path.of("path1")).term("term2").freq(0.4f);
		TermFrequency tf3 = new TermFrequency().path(Path.of("path2")).term("term1").freq(0.6f);

		termFreqQueue.offer(tf1);
		termFreqQueue.offer(tf2);
		termFreqQueue.offer(tf3);

		long maxTimeoutMs = 5000;
		long start = System.currentTimeMillis();
		TfidfProcessorStats stats = processor.getStats();

		// Wait maxTimeoutMs at most
		while (stats.analyzedPaths() < 2l && (System.currentTimeMillis() - start < maxTimeoutMs)) {
			stats = processor.getStats();
		}

		Assertions.assertEquals(2, stats.analyzedPaths(), "analyzed-paths");
		Assertions.assertEquals(TfidfProcessorConfig.IDF_MODE.SMOOTH, stats.idfMode(), "mode");

		// Validate ranking contents
		double path1TFIDF = computeSmoothTfIDF(0.2f, 2, 2) + computeSmoothTfIDF(0.4f, 1, 2);
		double path2TFIDF = computeSmoothTfIDF(0.6f, 2, 2);

		Collection<ImmutablePair<Path, Double>> ranking = stats.ranking();
		ranking.forEach(p -> {
			if (Path.of("path1").equals(p.key())) {
				Assertions.assertEquals(path1TFIDF, p.value(), "path1-value");
			} else if (Path.of("path1").equals(p.key())) {
				Assertions.assertEquals(path2TFIDF, p.value(), "path2-value");
			}
		});
	}

	@Test
	public void end2endNormalTest() throws Exception {
		String path = Paths.get("src", "test", "resources", "scenarios", "basic").toString();

		// file watcher
		NewFilesWatcher watcher = NewFilesWatcher.watcherFor(path);
		watcher.output(pathQueue);
		watcher.includeExistingFiles(true);

		// TF processor
		TermsFrequencyInFileConfig tfProcessorConfig = new TermsFrequencyInFileConfig();
		tfProcessorConfig.addTokenToInform("this").addTokenToInform("example");
		TermsFrequencyInFileProcessor tfProcessor = new TermsFrequencyInFileProcessor(tfProcessorConfig);
		tfProcessor.from(pathQueue);
		tfProcessor.output(termFreqQueue);

		// TF-IDF processor
		TfidfProcessor tfIdfProcessor = new TfidfProcessor(null);
		tfIdfProcessor.from(termFreqQueue);

		executorService.submit(watcher);
		executorService.submit(tfProcessor);
		executorService.submit(tfIdfProcessor);

		long maxTimeoutMs = 5000;
		long start = System.currentTimeMillis();
		TfidfProcessorStats stats = tfIdfProcessor.getStats();

		// Wait maxTimeoutMs at most
		while (stats.analyzedPaths() < 2l && (System.currentTimeMillis() - start < maxTimeoutMs)) {
			stats = tfIdfProcessor.getStats();
		}

		Assertions.assertEquals(2, stats.analyzedPaths(), "analyzed-paths");
		Assertions.assertEquals(TfidfProcessorConfig.IDF_MODE.NORMAL, stats.idfMode(), "mode");

		// Validate ranking contents (TFIDF("this") + TFIDF("example")
		double documento1TFIDF = computeNormaTfIDF(1 / 5f, 2, 2) + computeNormaTfIDF(0f, 1, 2);
		double documento2TFIDF = computeNormaTfIDF(1 / 7f, 2, 2) + computeNormaTfIDF(3 / 7f, 1, 2);

		Collection<ImmutablePair<Path, Double>> ranking = stats.ranking();
		ranking.forEach(p -> {
			if (p.key().endsWith("documento-1.md")) {
				Assertions.assertEquals(documento1TFIDF, p.value(), "documento1TFIDF-value");
			} else {
				Assertions.assertEquals(documento2TFIDF, p.value(), "documento2TFIDF-value");
			}
		});
	}

	private double computeNormaTfIDF(double freq, int termInDocs, int totalDocs) {
		return freq * Math.log10(totalDocs / termInDocs);
	}

	private double computeSmoothTfIDF(double freq, int termInDocs, int totalDocs) {
		return freq * (1f + Math.log10(totalDocs / (1f + termInDocs)));
	}
}
