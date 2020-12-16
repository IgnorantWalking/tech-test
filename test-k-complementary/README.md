## test-k-complementary

Algorithm that computes the collection of K-Complementary pairs from an array.
Given Array A, pair (i,j) is K-complementary if K = A[i] + A[j];
	 
The algorithm returns the full collection of pairs [i,j] with the positions of the original array that are K complementary. Negative numbers are supported as part of the array values and, aswell, as value of K.

Complexity: O(n) and extra memory required: O(n), to store an intermediate Map.

### Build

Normal build, with tests:

    mvn clean package

Build without unit tests execution:

    mvn clean package -DskipTests

### Execution

Using the .sh script:

    bin/kcomplementary-test.sh 

Using the java client directly:

    java -jar target/kcomplementary-algorithm-0.1.0-jar-with-dependencies.jar

Two parameters are expected by the test:
- **i**: array of integers to evaluate
- **k**: target value


```bash
$ bin/kcomplementary-test.sh -h
Usage: KComplementaryAlgorithm [-h] -k=<k> -i=<arr>... [-i=<arr>...]...
  -h, --help     Display the help
  -i=<arr>...    Array of integers
  -k=<k>         K value
```

Execution examples:

```bash
$ bin/kcomplementary-test.sh -k 5 -i 1 2 3
kComplementaryPairs: [(1,2)]
```

```bash
$ bin/kcomplementary-test.sh -k 5 -i 6 -1 3 2
kComplementaryPairs: [(2,3), (0,1)]
```
