package com.example.techtest.palindrome.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.techtest.palindrome.PalindromeAlgorithm;


/**
 * Test class for the PalindromeAlgorithm
 *
 */
public class PalindromeAlgorithmTest {

	@Test
	public void emptyStringTest() {
		Assertions.assertFalse(PalindromeAlgorithm.isPalindrome(""));
	}
	
	@Test
	public void monosyllableTest() {
		Assertions.assertFalse(PalindromeAlgorithm.isPalindrome("a"));
	}
	
	@Test
	public void oddPalindromeTest() {
		Assertions.assertTrue(PalindromeAlgorithm.isPalindrome("aa b aa"));
	}
	
	@Test
	public void pairPalindromeTest() {
		Assertions.assertTrue(PalindromeAlgorithm.isPalindrome("laal"));
	}
	
	@Test
	public void caseInsensitivePalindromeTest() {
		Assertions.assertTrue(PalindromeAlgorithm.isPalindrome("AbcDcBa"));
	}
	
	@Test
	public void caseInsensitiveSpecialCharsPalindromeTest() {
		StringBuilder test = new StringBuilder();
		test.append("!$1423 Ab JLI(/%&/N c ");
		Assertions.assertTrue(PalindromeAlgorithm.isPalindrome(test.toString() + test.reverse().toString()));
	}
}
