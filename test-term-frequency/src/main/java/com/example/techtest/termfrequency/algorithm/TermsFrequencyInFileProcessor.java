package com.example.techtest.termfrequency.algorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.techtest.termfrequency.stream.Processor;
import com.example.techtest.termfrequency.util.ImmutablePair;

/**
 * Processor capable of computing the frequency information of existing terms in
 * a specific file.
 * 
 * Using the config class @see TermsFrequencyInFileConfig, the patterns used to
 * split the terms of a text file, and to do some normalization of the contents,
 * can be personalized.
 * 
 * Optionally, a list of terms/tokens to be informed can be included in the
 * config. If some terms were indicated, the processor will only produce
 * frequency results for the specific terms, and any other token extracted from
 * the file will be grouped by the special name "OTHER_TOKENS_GROUP" @see
 * TermsFrequencyInFile.OTHER_TOKENS_GROUP
 * 
 * @author dmacia
 *
 */
public class TermsFrequencyInFileProcessor implements Callable<Integer>, Processor<Path, TermFrequency> {

	private static final Logger log = LogManager.getLogger(TermsFrequencyInFileProcessor.class);

	public static final int RESULT_OK = 0;
	public static final int RESULT_ERROR = -1;
	public static final String OTHER_TOKENS_GROUP = "";

	private final TermsFrequencyInFileConfig config;
	private final Pattern tokenSplitPattern;
	private final Pattern termNormPattern;
	private final Set<String> normalizedTokensToInform = new LinkedHashSet<>();

	private BlockingQueue<Path> sourceQueue = null;
	private Queue<TermFrequency> outputQueue = null;

	/**
	 * New instance from config
	 * 
	 * @param config
	 */
	public TermsFrequencyInFileProcessor(TermsFrequencyInFileConfig config) {
		if (config != null) {
			this.config = config;
		} else {
			this.config = new TermsFrequencyInFileConfig();
		}

		// Parse config
		this.tokenSplitPattern = Pattern.compile(this.config.tokenSplitRegex());
		this.termNormPattern = Pattern.compile(this.config.tokenNormalizationRegex());
		this.config.tokensToInform().stream().forEach(t -> normalizedTokensToInform.add(normalizeToken(t)));
	}

	@Override
	public void from(BlockingQueue<Path> queue) {
		if (queue != null) {
			this.sourceQueue = queue;
		}
	}

	@Override
	public void output(BlockingQueue<TermFrequency> queue) {
		this.outputQueue = queue;
	}

	@Override
	public Integer call() throws Exception {

		if (this.outputQueue == null) {
			log.error("No output queue provided");
			return RESULT_ERROR;
		}

		while (!Thread.currentThread().isInterrupted()) {
			Path sourcePath = sourceQueue.take();
			if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
				continue;
			}

			Stream<String> fileLines = null;

			try {
				fileLines = Files.lines(sourcePath, config.charset());

				// Group and count tokens extracted from the file content
				final ConcurrentMap<String, Integer> tokenCounts = fileLines.flatMap(tokenSplitPattern::splitAsStream)
						.parallel().filter(token -> !token.isEmpty())
						.flatMap(token -> Stream.of(ImmutablePair.of(tokenGroupName(token), 1))).collect(Collectors
								.groupingByConcurrent(ImmutablePair::key, Collectors.summingInt(ImmutablePair::value)));

				// Compute frequency and publish the results
				final int tokenTotal = tokenCounts.values().parallelStream().reduce(0, Integer::sum);
				computeAndPublishFrequencies(sourcePath, tokenCounts, tokenTotal);

			} catch (java.io.UncheckedIOException | java.io.IOException ioex) {
				log.warn("Error reading file {} using UTF-8 encoding. The file is discarded", sourcePath, ioex);
			} catch (Exception e) {
				log.error("Unexpected error reading file {}. The processor will crash.", sourcePath, e);
				return RESULT_ERROR;
			} finally {
				if (fileLines != null) {
					try {
						fileLines.close();
					} catch (Exception fce) {
						log.warn("Error closing file lines stream", fce);
					}
				}
			}
		}

		return RESULT_OK;
	}

	private void computeAndPublishFrequencies(Path sourcePath, Map<String, Integer> tokenCounts, int tokenTotal) {
		tokenCounts.entrySet().parallelStream().forEach(e -> {

			// Set as "no rankable" the special OTHER_TOKENS_GROUP
			TermFrequency termFreq = new TermFrequency().path(sourcePath).term(e.getKey())
					.freq(e.getValue() / (float) tokenTotal).rankable(!OTHER_TOKENS_GROUP.equals(e.getKey()));

			outputQueue.offer(termFreq);
			log.trace("Published TermFrequency {}", termFreq);
		});
	}

	/**
	 * If some tokens are configured to be inform, any other will be grouped below a
	 * special token name to minimize the data produced by the processor.
	 * 
	 * @param token
	 * @return
	 */
	private String tokenGroupName(String token) {
		if (normalizedTokensToInform.isEmpty() || normalizedTokensToInform.contains(token)) {
			return normalizeToken(token);
		} else {
			return OTHER_TOKENS_GROUP;
		}
	}

	/**
	 * Normalize tokens to allow equals comparison between them.
	 * 
	 * @param token
	 * @return
	 */
	private String normalizeToken(String token) {
		if (token.isEmpty()) {
			return OTHER_TOKENS_GROUP;
		} else {
			return termNormPattern.matcher(token.toLowerCase()).replaceAll("");
		}
	}
}
