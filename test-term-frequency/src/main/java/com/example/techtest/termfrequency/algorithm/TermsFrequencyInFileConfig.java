package com.example.techtest.termfrequency.algorithm;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Auxiliary class to define configuration parameters used by the
 * TermsFrequencyInFile processor
 * 
 * @author dmacia
 *
 */
public class TermsFrequencyInFileConfig {

	private static final String TOKEN_SPLIT_REGEX = "[\\s,\\-_]";
	private static final String TERM_NORMALIZATION_REGEX = "[\\p{Punct}|¿]";

	private final Set<String> tokensToInform = new LinkedHashSet<>();
	private String tokenSplitRegex = TOKEN_SPLIT_REGEX;
	private String tokenNormalizationRegex = TERM_NORMALIZATION_REGEX;
	private Charset charset = StandardCharsets.UTF_8;

	/**
	 * Tokens to inform.
	 *
	 * @return the set of tokens configured to inform about
	 */
	public Set<String> tokensToInform() {
		return this.tokensToInform;
	}

	/**
	 * Adds the token to inform.
	 *
	 * @param token the token
	 * @return this TermsFrequencyInFileConfig instance
	 */
	public TermsFrequencyInFileConfig addTokenToInform(String token) {
		this.tokensToInform.add(token);
		return this;
	}

	/**
	 * Adds the tokens to inform.
	 *
	 * @param tokens the tokens
	 * @return this TermsFrequencyInFileConfig instance
	 */
	public TermsFrequencyInFileConfig addTokensToInform(Collection<String> tokens) {
		this.tokensToInform.addAll(tokens);
		return this;
	}

	/**
	 * Token split regex.
	 *
	 * @return the string
	 */
	public String tokenSplitRegex() {
		return this.tokenSplitRegex;
	}

	/**
	 * Token split regex.
	 *
	 * @param regex the regex
	 * @return this TermsFrequencyInFileConfig instance
	 */
	public TermsFrequencyInFileConfig tokenSplitRegex(String regex) {
		this.tokenSplitRegex = regex;
		return this;
	}

	/**
	 * Token normalization regex.
	 *
	 * @return the string
	 */
	public String tokenNormalizationRegex() {
		return this.tokenNormalizationRegex;
	}

	/**
	 * Token normalization regex.
	 *
	 * @param regex the regex
	 * @return this TermsFrequencyInFileConfig instance
	 */
	public TermsFrequencyInFileConfig tokenNormalizationRegex(String regex) {
		this.tokenNormalizationRegex = regex;
		return this;
	}

	/**
	 * Charset configured to file reading.
	 *
	 * @return the charset
	 */
	public Charset charset() {
		return this.charset;
	}

	/**
	 * Set the charset to be used during file reading.
	 *
	 * @param charset the charset
	 * @return this TermsFrequencyInFileConfig instance
	 */
	public TermsFrequencyInFileConfig charset(Charset charset) {
		this.charset = charset;
		return this;
	}
}
