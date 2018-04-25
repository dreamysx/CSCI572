//package csci572;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.File;




public class Controller {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String crawlStorageFolder = "data";
		int numberOfCrawlers = 10;
		String userAgentString = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

		CrawlConfig config = new CrawlConfig();

		config.setUserAgentString(userAgentString);
		config.setIncludeHttpsPages(true);
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(16);
		// config.setPolitenessDelay(1000);
		config.setMaxPagesToFetch(20000);
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setEnabled(false);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		/*
		 * For each crawl, you need to add some seed urls. These are the first URLs that
		 * are fetched and then the crawler starts following links which are found in
		 * these pages
		 */
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(new File("fetch_Chicago_Tribune.csv"),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("URL,HTTP status code\n");
		pw.write(sb.toString());
		pw.close();
		
		PrintWriter pw1 = null;
		try {
			pw1 = new PrintWriter(new FileOutputStream(new File("urls_Chicago_Tribune.csv"),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder sb1 = new StringBuilder();
		sb1.append("URL,OK or N_OK\n");
		pw1.write(sb1.toString());
		pw1.close();
		
		PrintWriter pw2 = null;
		try {
			pw2 = new PrintWriter(new FileOutputStream(new File("visit_Chicago_Tribune.csv"),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder sb2 = new StringBuilder();
		sb2.append("URL,size,# of outlinks,content-type\n");
		pw2.write(sb2.toString());
		pw2.close();
		
		controller.addSeed("http://www.foxnews.com/");

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code will
		 * reach the line after this only when crawling is finished.
		 */
		controller.start(MyCrawler.class, numberOfCrawlers);
	}

}
