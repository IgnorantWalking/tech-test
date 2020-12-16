package com.example.techtest.termfrequency.util;

/**
 * Auxiliary class to store a immutable pair of key and value
 * 
 * @author dmacia
 *
 * @param <K> Class of the key
 * @param <V> Class of the value
 */

public class ImmutablePair<K, V> {

	private final K key;
	private final V value;

	private ImmutablePair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Creates a new ImmutablePair from the values specified.
	 * 
	 * @param <K>   Class of the key
	 * @param <V>   Class of the value
	 * @param key   Key of the pair
	 * @param value Value of the pair
	 * @return A instance of ImmutablePair
	 */
	public static <K, V> ImmutablePair<K, V> of(K key, V value) {
		return new ImmutablePair<>(key, value);
	}

	/**
	 * Gets the pair key
	 * 
	 * @return pair key
	 */
	public K key() {
		return key;
	}

	/**
	 * Gets the pair value
	 * 
	 * @return pair value
	 */
	public V value() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ImmutablePair [").append("key=").append(key).append(", value=").append(value).append("]");
		return sb.toString();
	}
}
