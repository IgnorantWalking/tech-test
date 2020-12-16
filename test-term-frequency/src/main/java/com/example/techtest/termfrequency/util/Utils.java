package com.example.techtest.termfrequency.util;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Auxiliary class with utility methods
 * 
 * @author dmacia
 */
public class Utils {

	private Utils() {
	}

	/**
	 * Recovers the top N entries of a Map, ordering by their values in a descendant
	 * manner. If two entries share the same value, they will be ordered by the
	 * keys.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param map   Map from which the entries will be recover
	 * @param limit Number of entries to be recovered
	 * @return A ordered Collection with the top N entries ordered by their values
	 */
	public static <K extends Comparable<? super K>, V extends Comparable<? super V>> Collection<Entry<K, V>> topNEntriesByValue(
			Map<K, V> map, int limit) {

		TreeMap<Map.Entry<K, V>, V> topN = new TreeMap<>(
				Map.Entry.<K, V>comparingByValue().reversed().thenComparing(Map.Entry.<K, V>comparingByKey()));

		map.entrySet().forEach(e -> {
			topN.put(e, null);
			if (topN.size() > limit) {
				topN.pollLastEntry();
			}
		});

		return topN.keySet();
	}

}
