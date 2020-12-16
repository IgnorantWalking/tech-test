package com.example.techtest.termfrequency.algorithm;

/**
 * Auxiliary class to define configuration parameters used by the
 * TfidfProcessorConfig processor
 * 
 * @author dmacia
 *
 */
public class TfidfProcessorConfig {

	/**
	 * Type of IDF computation to apply
	 * 
	 */
	public enum IDF_MODE {
		NORMAL, SMOOTH
	}

	public static final int DEFAULT_POLL_TIMEOUT_MS = 1000;
	public static final int DEFAULT_MAX_TF_BUFFER_SIZE = 1000;
	public static final int DEFAULT_RANKING_SIZE = Integer.MAX_VALUE;

	private int pollTimeoutMs = TfidfProcessorConfig.DEFAULT_POLL_TIMEOUT_MS;
	private int maxTfBufferSize = TfidfProcessorConfig.DEFAULT_MAX_TF_BUFFER_SIZE;
	private int rankingSize = TfidfProcessorConfig.DEFAULT_RANKING_SIZE;
	private IDF_MODE mode = IDF_MODE.NORMAL;

	/**
	 * Poll timeout reading data from the source queue
	 *
	 * @return the timeout value in ms
	 */
	public int pollTimeoutMs() {
		return this.pollTimeoutMs;
	}

	/**
	 * Set the poll timeout reading data from the source queue in ms
	 *
	 * @return this TfidfProcessorConfig instance
	 */
	public TfidfProcessorConfig pollTimeoutMs(int pollTimeoutMs) {
		this.pollTimeoutMs = pollTimeoutMs;
		return this;
	}

	/**
	 * Max TermFrequency events to buffer before doing the TF-IDF computation and
	 * rank update
	 *
	 * @return the buffer size
	 */
	public int maxTfBufferSize() {
		return this.maxTfBufferSize;
	}

	/**
	 * Set the max TermFrequency events to buffer before doing the TF-IDF
	 * computation and rank update
	 *
	 * @return this TfidfProcessorConfig instance
	 */
	public TfidfProcessorConfig maxTfBufferSize(int maxTfBufferSize) {
		this.maxTfBufferSize = maxTfBufferSize;
		return this;
	}

	/**
	 * Number of paths to be included in the ranking information. By default all are
	 * included.
	 *
	 * @return the ranking size
	 */
	public int rankingSize() {
		return this.rankingSize;
	}

	/**
	 * Set the number of paths to be included in the ranking information
	 *
	 * @return this TfidfProcessorConfig instance
	 */
	public TfidfProcessorConfig rankingSize(int rankingSize) {
		this.rankingSize = rankingSize;
		return this;
	}

	/**
	 * Mode to be used when computing the terms IDF
	 *
	 * @return the IDF computation mode
	 */
	public IDF_MODE mode() {
		return this.mode;
	}

	/**
	 * Set the mode to be used when computing the terms IDF
	 *
	 * @return this TfidfProcessorConfig instance
	 */
	public TfidfProcessorConfig mode(IDF_MODE mode) {
		this.mode = mode;
		return this;
	}
}
