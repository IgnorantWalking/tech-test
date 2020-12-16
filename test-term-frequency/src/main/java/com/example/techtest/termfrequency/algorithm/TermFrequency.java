package com.example.techtest.termfrequency.algorithm;

import java.nio.file.Path;

/**
 * Term related frequency information for a specific term in a specific file
 * 
 * @author dmacia
 *
 */
public class TermFrequency {

	private Path path;
	private String term;
	private float freq;
	private boolean rankable = true;

	/**
	 * @return the path
	 */
	public Path path() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public TermFrequency path(Path path) {
		this.path = path;
		return this;
	}

	/**
	 * @return the term
	 */
	public String term() {
		return term;
	}

	/**
	 * @param term the term to set
	 */
	public TermFrequency term(String term) {
		this.term = term;
		return this;
	}

	/**
	 * @return the freq
	 */
	public float freq() {
		return freq;
	}

	/**
	 * @param freq the freq to set
	 */
	public TermFrequency freq(float freq) {
		this.freq = freq;
		return this;
	}

	/**
	 * @return the if the term frequency should be ranked
	 */
	public boolean rankable() {
		return rankable;
	}

	/**
	 * @param set if the term frequency should be ranked
	 */
	public TermFrequency rankable(boolean rankable) {
		this.rankable = rankable;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TermFrequency [").append("path=").append(path).append(", term=").append(term).append(", freq=")
				.append(freq).append(", rankable=").append(rankable).append("]");

		return sb.toString();
	}

	/**
	 * HashCode method based only in path and term fields to allow detection of
	 * TermFrequency updates
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	/**
	 * Equals method based only in path and term fields to allow detection of
	 * TermFrequency updates
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TermFrequency other = (TermFrequency) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (term == null) {
			if (other.term != null) {
				return false;
			}
		} else if (!term.equals(other.term)) {
			return false;
		}
		return true;
	}

}
