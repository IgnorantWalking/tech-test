package com.example.techtest.termfrequency.boot;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.techtest.termfrequency.algorithm.TermFrequency;
import com.example.techtest.termfrequency.algorithm.TermsFrequencyInFileConfig;
import com.example.techtest.termfrequency.algorithm.TermsFrequencyInFileProcessor;
import com.example.techtest.termfrequency.algorithm.TfidfProcessor;
import com.example.techtest.termfrequency.algorithm.TfidfProcessorConfig;
import com.example.techtest.termfrequency.algorithm.TfidfProcessorConfig.IDF_MODE;
import com.example.techtest.termfrequency.file.NewFilesWatcher;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "TermFrequencyCalculator")
public class TermFrequencyCalculator implements Runnable {

	private static final Logger log = LogManager.getLogger(TermFrequencyCalculator.class);
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.###");

	@Option(names = { "-s",
			"--source-path" }, description = "Source path to read files from", required = true, paramLabel = "FILES_FOLDER")
	Path sourceFolder = null;

	@Option(names = { "-t",
			"--terms" }, description = "Terms to be analyzed", arity = "1..*", required = true, paramLabel = "TERMS")
	String[] terms = null;

	@Option(names = { "-n",
			"--top-n-results" }, description = "Number of top results to show. Default 5", defaultValue = "5", paramLabel = "TOP_RESULTS")
	int numResults = 0;

	@Option(names = { "-p",
			"--report-period" }, description = "Report period, in seconds. Default 5", defaultValue = "5", paramLabel = "PERIOD")
	int reportPeriod = 0;
	
	@Option(names = { "-m",
	"--idf-mode" }, description = "Mode used to compute the terms IDF: NORMAL or SMOOTH. Default NORMAL", paramLabel = "IDF_MODE")
	IDF_MODE idfMode = IDF_MODE.NORMAL;
	
	@Option(names = { "-c",
	"--charset" }, description = "Charset used to read source files. Default UTF-8", paramLabel = "CHARSET_NAME")
	String charsetName = null;

	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display the help")
	boolean usageHelpRequested;
	
	@Override
	public void run() {

		log.info("***************************************************************************************************");
		log.info("Executing TermFrequencyCalculator with source-path {}, terms: {}, top-results: {}, report-period: {}",
				sourceFolder, terms, numResults, reportPeriod);
		log.info("***************************************************************************************************");

		int cores = Runtime.getRuntime().availableProcessors();
		ExecutorService executorService = Executors.newCachedThreadPool();

		try {

			// Define queues to be used by the stream processors
			BlockingQueue<Path> filesToProcessQueue = new LinkedBlockingQueue<>();
			BlockingQueue<TermFrequency> termsFrequenciesQueue = new LinkedBlockingQueue<>();

			// Configure the stream processors
			// New files source
			NewFilesWatcher folderWatcher = NewFilesWatcher.watcherFor(sourceFolder);
			folderWatcher.output(filesToProcessQueue);

			// TF calculator processor config
			TermsFrequencyInFileConfig termsFreqConfig = new TermsFrequencyInFileConfig();
			termsFreqConfig.addTokensToInform(Arrays.asList(terms));
			if (charsetName != null) {
				termsFreqConfig.charset(Charset.forName(charsetName));
			}

			// TF-IDF processor sink
			TfidfProcessorConfig tfidfProcessorConfig = new TfidfProcessorConfig();
			tfidfProcessorConfig.rankingSize(numResults);
			tfidfProcessorConfig.mode(idfMode);

			TfidfProcessor tfidfProcessor = new TfidfProcessor(tfidfProcessorConfig);
			tfidfProcessor.from(termsFrequenciesQueue);

			// Begin the computation
			int numberOfTFProcessors = (cores > 1 ? cores - 1 : 1);
			// N TF processors
			for (int i = 0; i < numberOfTFProcessors; i++) {
				TermsFrequencyInFileProcessor tfProcessor = new TermsFrequencyInFileProcessor(termsFreqConfig);
				tfProcessor.from(filesToProcessQueue);
				tfProcessor.output(termsFrequenciesQueue);

				executorService.submit(tfProcessor);
			}

			// 1 source and 1 sink
			executorService.submit(folderWatcher);
			executorService.submit(tfidfProcessor);

			// Very basic and dumb loop only to print the algorithm results
			while (!Thread.currentThread().isInterrupted()) {
				printStats(tfidfProcessor.getStats());
				Thread.sleep(reportPeriod * 1000l);
			}

		} catch (Exception e) {
			log.error("Unexpected error", e);
			Thread.currentThread().interrupt();
		} finally {
			executorService.shutdownNow();
		}
	}

	private void printStats(TfidfProcessor.TfidfProcessorStats stats) {
		if (stats == null) {
			return;
		}

		long statsLastUpd = stats.rankingLastUpdated();
		StringBuilder rankingSB = new StringBuilder();
		stats.ranking().forEach(r -> rankingSB.append(this.sourceFolder.relativize(r.key()).toString())
				.append(" - ").append(decimalFormat.format(r.value())).append("\n"));

		log.info("Analyzed files: {}, ranking updated at: {}, idf-mode: {}, ranking: \n{}", stats.analyzedPaths(),
				(statsLastUpd > 0 ? Instant.ofEpochMilli(statsLastUpd) : "-"), stats.idfMode(), rankingSB);
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new TermFrequencyCalculator()).execute(args);
		System.exit(exitCode);
	}

}
