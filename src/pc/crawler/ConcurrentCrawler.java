package pc.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Concurrent crawler.
 *
 */ 

public class ConcurrentCrawler extends BaseCrawler {

	private Thread[] thread;
	private final int numberOfThreads;
	private AtomicInteger rid = new AtomicInteger(0);
	private LinkedList<URL> toVisit;
	private HashSet<URL> seen;
	private boolean[] threadB;

	public static void main(String[] args) throws IOException {
		int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
		String rootPath = args.length > 1 ? args[1] : "http://localhost:8123";
		ConcurrentCrawler cc = new ConcurrentCrawler(threads);
		cc.setVerboseOutput(false);
		cc.crawl(new URL(rootPath));
	
	}

	public ConcurrentCrawler(int threads) throws IOException {
		this.numberOfThreads = threads;
		this.thread = new Thread[numberOfThreads];
		this.threadB = new boolean[numberOfThreads];
		toVisit = new LinkedList<>();
		seen = new HashSet<>();

	}

	synchronized URL removeList() {
		if (toVisit.isEmpty())
			return null;
		else
			return (toVisit.removeFirst());
	}

	synchronized void parse(ArrayList<URL> links) {
		for (URL newURL : links) {
			if (seen.add(newURL)) {
				// URL not seen before
				toVisit.addLast(newURL);
			}
		}
	}

	@Override
	public void crawl(URL root) throws IOException {
		long t = System.currentTimeMillis();
		seen.add(root);
		toVisit.add(root);
		log("Starting at %s", root);
		for (int i = 0; i < numberOfThreads; i++) {
			// System.out.println(i);
			int curr = i;
			thread[i] = new Thread(() -> {
				int count = 0;

				while (count != numberOfThreads) {
					URL url = removeList();
					// System.out.println(curr);
					if (url != null) {
						threadB[curr] = true;
						File htmlContents = download(rid.incrementAndGet(), url);
						if (htmlContents != null) {
							parse(parseLinks(url, htmlContents));
							}
						threadB[curr] = false;

					} else {
						count = 0;
						for (int j = 0; j < numberOfThreads; j++)
							if (threadB[j] == false) {
								count++;
							}
						if (count == numberOfThreads)
							System.out.println("fechou thread " + curr);

					}
				}

			});

			thread[i].start();
		}

		for (int i = 0; i < numberOfThreads; i++) {
			try {
				thread[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		t = System.currentTimeMillis() - t;
		System.out.printf("Done: %d transfers in %d ms (%.2f transfers/s)%n", rid.get(), t, (1e+03 * rid.get()) / t);
	}

}