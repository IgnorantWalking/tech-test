## test-term-frequency

Program to compute a ranking of documents based on the Tf/idf (term frequency / inverse document frequency) statistic of some of their contents.

The solution was modelled as a stream application. Three different main components were defined:

- **NewFilesWatcher source**: a component that watches some path in the filesystem and emits events as new files are created in that folder and subfolders. By default not only the new files are processed, but also the already existing ones in the path.

- **TermsFrequencyInFile processor**: the component subscribed to the events generated from the FilesWatcher source, and capable of reading the file contents and compute the TF (term-frequency) of a ser of terms. When a file was processed, some events are emitted with the frequency computed for every term.

- **TfidfProcessor sink**: a component that receives the TF events and computes the final TF-IDF value associated with a file. Keeps, also, a ranking of files classified by this scoring.

The implementation has been focused on achieving a good level of parallelism during the processing. Blocking queues are used to communicate and synchronize the different component and instances.

Some possible low-level improvements have not been considered and, instead, standard data structures and libraries were used. For example, some performance improvement points might be:

- avoid using regular expression patterns to perform splitting and normalization of terms in the processed files. Maybe using something more specific based on character comparisons would be more performant.

- or implementation of data structures of primitive types, when their representations as complex objects are not necessary, to avoid all the unnecessary types autoboxing.

### Build

Normal build, with tests:

    mvn clean package

Build without unit tests execution:

    mvn clean package -DskipTests

### Execution

Using the .sh script:

    bin/if-idf-test.sh 

Using the java client directly:

    java -jar target/tfidf-algorithm-0.1.0-jar-with-dependencies.jar

Two mandatory parameters are expected by the test:
- **s**: source folder
- **t**: list of terms to compute TF-IDF scoring used to rank the files

Other optional parameters allows to personalize the test execution:
- **p**: report period in seconds
- **n**: top N results to be shown in the rank
- **c**: by default UTF-8 charset was used to interpret the text files. This parameter allows to use another charset if needed
- **m**: type of IDF formula to apply in the computation. Normal IDF by default, with the possibility to use a Smooth mode (as defined in https://en.wikipedia.org/wiki/Tf%E2%80%93idf).


```bash
$ bin/if-idf-test.sh -h
Usage: TermFrequencyCalculator [-h] [-c=CHARSET_NAME] [-m=IDF_MODE]
                               [-n=TOP_RESULTS] [-p=PERIOD] -s=FILES_FOLDER
                               -t=TERMS... [-t=TERMS...]...
  -c, --charset=CHARSET_NAME
                            Charset used to read source files. Default UTF-8
  -h, --help                Display the help
  -m, --idf-mode=IDF_MODE   Mode used to compute the terms IDF: NORMAL or
                              SMOOTH. Default NORMAL
  -n, --top-n-results=TOP_RESULTS
                            Number of top results to show. Default 5
  -p, --report-period=PERIOD
                            Report period, in seconds. Default 5
  -s, --source-path=FILES_FOLDER
                            Source path to read files from
  -t, --terms=TERMS...      Terms to be analyzed
```

Execution example:

```bash
bin/if-idf-test.sh -s src/test/resources/scenarios/basic -t this example -n 5 -p 5
***************************************************************************************************
Executing TermFrequencyCalculator with source-path src/test/resources/scenarios/basic, terms: [this, example], top-results: 5, report-period: 5
***************************************************************************************************
Analyzed files: 0, ranking updated at: -, idf-mode: NORMAL, ranking: 

Analyzed files: 2, ranking updated at: 2020-12-16T04:11:17.813Z, idf-mode: NORMAL, ranking: 
documento-2.md - 0,129
documento-1.md - 0

Analyzed files: 2, ranking updated at: 2020-12-16T04:11:22.816Z, idf-mode: NORMAL, ranking: 
documento-2.md - 0,129
documento-1.md - 0

```
