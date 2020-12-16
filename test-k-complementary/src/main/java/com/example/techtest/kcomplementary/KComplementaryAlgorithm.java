package com.example.techtest.kcomplementary;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "KComplementaryAlgorithm")
public class KComplementaryAlgorithm implements Runnable {

	@Option(names = { "-i" }, description = "Array of integers", arity = "1..*", required = true)
	int[] arr;

	@Option(names = { "-k" }, description = "K value", required = true)
	int k;

	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display the help")
	boolean usageHelpRequested;
	
	@Override
	public void run() {
		System.out.println("kComplementaryPairs: " + complementaryKPairs(arr, k));
	}

	/**
	 * Compute the collection of K-Complementary pairs from an array.
	 * 
	 * Given Array "arr", pair (i, j) is K-complementary if k = arr[i] + arr[j];
	 * 
	 * The method returns a collection of pairs in format [i,j], with always i < j.
	 * Negative numbers are supported in the array values and in K aswell
	 * 
	 * Complexity: O(n) and extra memory required: O(n), to store intermediate Maps
	 * and Sets of diffs and positions.
	 * 
	 * @param arr Array to analyze
	 * @param k K value
	 * @return Collection of K-complementary pairs
	 */
	public static Collection<Pair> complementaryKPairs(int[] arr, int k) {
		Collection<Pair> kpairs = new HashSet<>();

		// For every arr item the difference with K
		Map<Integer, Integer> diffs = new HashMap<>();
		// For every arr value the array positions in which exists
		Map<Integer, Set<Integer>> positions = new HashMap<>();

		// Fill the maps with the data extracted from the array
		for (int i = 0; i < arr.length; i++) {
			diffs.put(arr[i], k - arr[i]);
			Set<Integer> positionsInArr = positions.get(arr[i]);
			if (positionsInArr == null) {
				positionsInArr = new HashSet<>();
				positions.put(arr[i], positionsInArr);
			}
			positionsInArr.add(i);
		}

		// Iterate over the diffs map and look for "complementary" values and their
		// positions in the original array
		diffs.entrySet().parallelStream().forEach(e -> {
			// check if exists complementary
			if (diffs.containsKey(e.getValue())) {
				Set<Integer> posE = positions.get(e.getKey());
				Set<Integer> posComp = positions.get(e.getValue());

				posE.forEach(posEVal -> posComp.forEach(posCompVal -> {
					// Avoid duplicates (i,j) -> (j,i)
					if (posEVal < posCompVal) {
						kpairs.add(Pair.of(posEVal, posCompVal));
					}
				}));
			}
		});

		return kpairs;
	}

	/**
	 * Auxiliary class to store a Pair of integers.
	 * 
	 * The class overwrites the equals and hashCode methods to allow comparisons by
	 * content.
	 */
	public static class Pair {
		public final int left;
		public final int right;

		private Pair(int left, int right) {
			this.left = left;
			this.right = right;
		}

		public static Pair of(int left, int right) {
			return new Pair(left, right);
		}

		@Override
		public int hashCode() {
			return Objects.hash(left, right);
		}

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
			Pair other = (Pair) obj;
			return left == other.left && right == other.right;
		}

		@Override
		public String toString() {
			return "(" + left + "," + right + ")";
		}
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new KComplementaryAlgorithm()).execute(args);
		System.exit(exitCode);
	}
}
