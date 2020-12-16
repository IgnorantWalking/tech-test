package com.example.techtest.palindrome;

public class PalindromeAlgorithm {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.exit(-1);
		}

		String str = String.join(" ", args);
		System.out.println("isPalindrome: " + isPalindrome(str));
	}

	/**
	 * Checks if a string is as palindrome. A string is a palindrome if the string
	 * matches the reverse of string.
	 * 
	 * For textual characters, the method made a case-insensitive comparison.
	 * 
	 * Complexity: O(n)
	 * 
	 * @param str The string to evaluate
	 * @return true if palindrome
	 */
	public static boolean isPalindrome(String str) {
		int ini = 0;
		int end = str.length() - 1;
		boolean result = (str.length() > 1);

		while (ini < end) {
			char left = str.charAt(ini);
			char right = str.charAt(end);

			if (left != right && !caseInsentiveEquals(left, right)) {
				result = false;
				break;
			}
			ini++;
			end--;
		}
		return result;
	}

	/**
	 * Case insensitive comparison between two characters using the ASCII code
	 * table.
	 * 
	 * @param a
	 * @param b
	 * @return true if the characters are equals ignoring case
	 */
	private static boolean caseInsentiveEquals(char a, char b) {
		int ai = a;
		int bi = b;

		return ((ai > bi && ai >= 97 && ai <= 122 && ai - bi == 32)
				|| (ai < bi && bi >= 97 && bi <= 122 && bi - ai == 32));
	}

}
