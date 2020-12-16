package com.example.techtest.kcomplementary.test;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.techtest.kcomplementary.KComplementaryAlgorithm;
import com.example.techtest.kcomplementary.KComplementaryAlgorithm.Pair;


/**
 * Test class for the KComplementaryAlgorithm
 *
 */
public class KComplementaryAlgorithmTest {

	@Test
	public void uniqueValueTest() {
		int[] array = new int[]{1};
		int k = 2;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
		Assertions.assertEquals(0, pairs.size());
	}
	
	@Test
	public void noPairWithItselfTest() {
		int[] array = new int[]{1,5,3,4,2};
		int k = 2;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
		Assertions.assertEquals(0, pairs.size());
	}
	
	@Test
	public void pairWithSameValueTest() {
		int[] array = new int[]{1,1};
		int k = 2;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
				
		Assertions.assertEquals(1, pairs.size());
		Assertions.assertTrue(pairs.contains(Pair.of(0, 1)));
	}
	
	@Test
	public void unsortedArrayValueTest() {
		int[] array = new int[]{1,5,3,4,2};
		int k = 5;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
				
		Assertions.assertEquals(2, pairs.size());
		Assertions.assertTrue(pairs.contains(Pair.of(0, 3)));
		Assertions.assertTrue(pairs.contains(Pair.of(2, 4)));
	}
	
	@Test
	public void sortedArrayTest() {
		int[] array = new int[]{1,2,3,4,5};
		int k = 5;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
				
		Assertions.assertEquals(2, pairs.size());
		Assertions.assertTrue(pairs.contains(Pair.of(0, 3)));
		Assertions.assertTrue(pairs.contains(Pair.of(1, 2)));
	}
	
	@Test
	public void unsortedNegativeArrayValueTest() {
		int[] array = new int[]{-1,6,-3,4,8,1};
		int k = 5;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
				
		Assertions.assertEquals(3, pairs.size());
		Assertions.assertTrue(pairs.contains(Pair.of(0, 1)));
		Assertions.assertTrue(pairs.contains(Pair.of(2, 4)));
		Assertions.assertTrue(pairs.contains(Pair.of(3, 5)));
	}
	
	@Test
	public void negativeKValueTest() {
		int[] array = new int[]{-2,2,-3,4,-9};
		int k = -5;
		
		Collection<Pair> pairs = KComplementaryAlgorithm.complementaryKPairs(array, k);
				
		Assertions.assertEquals(2, pairs.size());
		Assertions.assertTrue(pairs.contains(Pair.of(0, 2)));
		Assertions.assertTrue(pairs.contains(Pair.of(3, 4)));
	}	
	
}
