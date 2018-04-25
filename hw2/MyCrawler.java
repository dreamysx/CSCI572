//package crawlerTest;

import java.io.IOException;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.apache.http.HttpStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.util.Set;
import java.util.regex.Pattern;


public class MyCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|xml|rss" + "|mp3|mp4|zip|gz))$");

	/**
	 * This method receives two parameters. The first parameter is the page in which
	 * we have discovered this new url and the second parameter is the new url. You
	 * should implement this function to specify whether the given url should be
	 * crawled or not (based on your crawling logic). In this example, we are
	 * instructing the crawler to ignore urls that have css, js, git, ... extensions
	 * and to only accept urls that start with "http://www.ics.uci.edu/". In this
	 * case, we didn't need the referringPage parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		//
		System.out.println("URL: " + url);
		//
		String href = url.getURL().toLowerCase().replaceAll(",", "-");
		System.out.println("new URL: " + url);
		PrintWriter pw1 = null;
		try {
			pw1 = new PrintWriter(new FileOutputStream(new File("urls_FOX_News.csv"),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder sb1 = new StringBuilder();
		sb1.append(url.toString().replaceAll(",", "-"));

		if(href.startsWith("https://www.foxnews.com/")||href.startsWith("http://www.foxnews.com/")) {
			sb1.append(",OK\n");
		} else {
			sb1.append(",N_OK\n");
		}
		pw1.write(sb1.toString());
		pw1.close();
		return !FILTERS.matcher(href).matches() && href.startsWith("https://www.foxnews.com/") ||href.startsWith("http://www.foxnews.com/");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed by
	 * your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL().replaceAll(",", "-");
		//
		System.out.println("URL: " + url);
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			String contentType = page.getContentType();
			int size = page.getContentData().length;
			
			PrintWriter pw = null;

			try {
				pw = new PrintWriter(new FileOutputStream(new File("visit_FOX_News.csv"),true));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			StringBuilder sb = new StringBuilder();
			sb.append(url);
			sb.append(",");
			sb.append(size/1024);
			sb.append(",");
			sb.append(links.size());
			sb.append(",");
			if(contentType.equals("text/html;charset=UTF-8")) {
				sb.append("text/html");
			} else {
				sb.append(contentType);
			}
			sb.append("\n");
			pw.append(sb.toString());
			pw.close();
			//
			System.out.println("Content type:" + contentType);
			System.out.println("Content Size:" + size/1024+"KB");
			 System.out.println("Text length: " + text.length());
			 System.out.println("Html length: " + html.length());
			System.out.println("Number of outgoing links: " + links.size());
			//
		} else {
			String contentType = page.getContentType();
			int size = page.getContentData().length;
			if(!(contentType.equals("image/gif")||contentType.equals("image/jpeg")||contentType.equals("image/png")||contentType.equals("application/pdf"))) {
				return;
			}
			PrintWriter pw = null;

			try {
				pw = new PrintWriter(new FileOutputStream(new File("visit_FOX_News.csv"),true));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			StringBuilder sb = new StringBuilder();
			sb.append(url);
			sb.append(",");
			sb.append(size/1024);
			sb.append(",");
			sb.append(0);
			sb.append(",");
			sb.append(contentType);
			sb.append("\n");
			pw.append(sb.toString());
			pw.close();
		}
	}

	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {

		PrintWriter pw = null;

		try {
			pw = new PrintWriter(new FileOutputStream(new File("fetch_Chicago_Tribune.csv"),true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		
		sb.append(webUrl.toString().replaceAll(",", "-"));
		
		sb.append(",");
		sb.append(statusCode);
		sb.append("\n");
		pw.append(sb.toString());
		pw.close();

	}
}
