## test-palindrome

Algorithm that checks if a string is as palindrome. A string is a palindrome if the string matches the reverse of the string. 

The algorithm supports case-insensitive comparisons for textual characters, to allow a more "realistic" palindrome definition.

Complexity: O(n), and no extra memory required.

### Build

Normal build, with tests:

    mvn clean package

Build without unit tests execution:

    mvn clean package -DskipTests

### Execution

Using the .sh script:

    bin/palindrome-test.sh string to evaluate

Using the java client directly:

    java -jar target/palindrome-algorithm-0.1.0.jar string to evaluate

The algorithm will print on the console the result of the computation:

    isPalindrome: false|true