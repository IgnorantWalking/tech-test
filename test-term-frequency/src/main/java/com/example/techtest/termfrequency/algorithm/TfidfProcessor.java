package com.example.techtest.termfrequency.algorithm;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.techtest.termfrequency.algorithm.TfidfProcessorConfig.IDF_MODE;
import com.example.techtest.termfrequency.stream.Sink;
import com.example.techtest.termfrequency.util.ImmutablePair;
import com.example.techtest.termfrequency.util.Utils;

/**
 * Processor capable of computing the Tf-idf of a stream of TermFrequency
 * items @see TermFrenquency.
 * 
 * Using the config class @see TfidfProcessorConfig, a list of terms can be
 * indicated, the processor will compute a ranking of the different paths
 * observed in the TermFrequency stream as a result of adding the Tf-idf value
 * computed for every term in them.
 * 
 * @author dmacia
 *
 */
public class TfidfProcessor implements Callable<Integer>, Sink<TermFrequency> {

	private static final Logger log = LogManager.getLogger(TfidfProcessor.class);

	public static final int RESULT_OK = 0;
	public static final int RESULT_ERROR = -1;

	private final TfidfProcessorConfig config;
	private BlockingQueue<TermFrequency> sourceQueue = null;
	private Collection<Entry<Path, Double>> ranking = Collections.emptyList();
	private long totalNumberOfPaths = 0l;
	private long rankingLastUpdated = 0l;

	private final List<TermFrequency> termFrequencyBuffer = new LinkedList<>();
	private final Map<String, Set<TermFrequency>> frequenciesByTerm = new HashMap<>();

	public TfidfProcessor(TfidfProcessorConfig config) {
		if (config != null) {
			this.config = config;
		} else {
			this.config = new TfidfProcessorConfig();
		}
	}

	@Override
	public void from(BlockingQueue<TermFrequency> queue) {
		this.sourceQueue = queue;
	}

	/**
	 * Recovers the TF-IDF processing stats, including the ranking of the top paths
	 * 
	 * @return An instance of TfidfProcessorStats with the algorithm execution info
	 */
	public TfidfProcessorStats getStats() {
		TfidfProcessorStats stats = new TfidfProcessorStats();

		stats.analyzedPaths = this.totalNumberOfPaths;
		stats.rankingLastUpdated = this.rankingLastUpdated;
		stats.idfMode = config.mode();

		Collection<Entry<Path, Double>> currentRanking = this.ranking;
		currentRanking.forEach(e -> stats.ranking.add(ImmutablePair.of(e.getKey(), e.getValue())));

		return stats;
	}

	@Override
	public Integer call() throws Exception {

		if (this.sourceQueue == null) {
			log.error("No source queue provided");
			return RESULT_ERROR;
		}

		while (!Thread.currentThread().isInterrupted()) {
			try {
				TermFrequency termFreq = sourceQueue.poll(config.pollTimeoutMs(), TimeUnit.MILLISECONDS);
				bufferTF(termFreq);
				checkAndUpdateRanking();

			} catch (Exception e) {
				log.error("Error computing tf-idf", e);
				return RESULT_ERROR;
			}
		}

		return RESULT_OK;
	}

	private void bufferTF(TermFrequency termFreq) {
		if (termFreq != null) {
			termFrequencyBuffer.add(termFreq);
		}
	}

	private void checkAndUpdateRanking() {

		// Check ranking update criteria
		if (termFrequencyBuffer.size() < config.maxTfBufferSize()
				&& System.currentTimeMillis() - rankingLastUpdated < config.pollTimeoutMs()) {

			log.trace("Ranking update discarded buffer-size: {} last-upd: {}", termFrequencyBuffer.size(),
					rankingLastUpdated);
			return;
		}

		// Ranking update
		termFrequencyBuffer.forEach(tf -> {
			Set<TermFrequency> termFreqs = frequenciesByTerm.get(tf.term());
			if (termFreqs == null) {
				termFreqs = new HashSet<>();
				frequenciesByTerm.put(tf.term(), termFreqs);
			}
			// TermFrequency equals and hashCode methods allows us to update the set if
			// TF data already exists for the same path and term
			termFreqs.add(tf);
		});

		// Update TF-IDF
		long newTotalNumberOfPaths = frequenciesByTerm.values().parallelStream().flatMap(Set::parallelStream)
				.map(TermFrequency::path).distinct().count();

		Map<Path, Double> computedIdfByPath = frequenciesByTerm.values().parallelStream().flatMap(Set::parallelStream)
				.filter(TermFrequency::rankable)
				.map(tf -> ImmutablePair.of(tf.path(),
						tf.freq() * computeIDF(frequenciesByTerm.get(tf.term()).size(), newTotalNumberOfPaths)))
				.collect(Collectors.groupingByConcurrent(ImmutablePair::key,
						Collectors.summingDouble(ImmutablePair::value)));

		// Update ranking
		ranking = Collections.synchronizedCollection(Utils.topNEntriesByValue(computedIdfByPath, config.rankingSize()));
		totalNumberOfPaths = newTotalNumberOfPaths;

		log.trace("Ranking updated from {} buffered TFs. Distinct paths: {}", termFrequencyBuffer.size(),
				totalNumberOfPaths);

		rankingLastUpdated = System.currentTimeMillis();
	}

	/**
	 * Apply IDF formula, NORMAL or SMOOTH 
	 */
	private double computeIDF(float termDf, long totalNumberOfPaths) {

		if (IDF_MODE.SMOOTH.equals(config.mode())) {
			return Math.log10(totalNumberOfPaths / (termDf + 1f)) + 1f;
		} else {
			return Math.log10(totalNumberOfPaths / termDf);
		}
	}

	/**
	 * Basic statistics of the TF-IDF algorithm execution
	 *
	 */
	public static class TfidfProcessorStats {

		private long analyzedPaths = 0l;
		private long rankingLastUpdated = 0l;
		private IDF_MODE idfMode = null;
		private Collection<ImmutablePair<Path, Double>> ranking = new ArrayList<>();

		/**
		 * Number of files/paths analyzed by the algorithm
		 * 
		 */
		public long analyzedPaths() {
			return this.analyzedPaths;
		}

		/**
		 * Ranking last update timestamp
		 * 
		 */
		public long rankingLastUpdated() {
			return this.rankingLastUpdated;
		}

		/**
		 * The IDF computation mode used by the processor
		 * 
		 */
		public IDF_MODE idfMode() {
			return this.idfMode;
		}

		/**
		 * Descendant ordered collection of paths/files according to their TF-IDF score
		 * 
		 */
		public Collection<ImmutablePair<Path, Double>> ranking() {
			return this.ranking;
		}
	}
}
