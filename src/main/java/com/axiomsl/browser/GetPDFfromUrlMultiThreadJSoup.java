package com.axiomsl.browser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetPDFfromUrlMultiThreadJSoup {

	static ArrayList<String> iteratedLinks = new ArrayList<String>();

	public static void downloadFiles(ExecutorService executor, String url, String dir, int childLevel,
			String parentFilter, String childFilter, boolean getAllChildPDF) throws IOException, URISyntaxException {

		try {

			String consoleOutput = "";

			String request = encodeURL(url);

			if (!iteratedLinks.contains(request)) {

				Document doc = null;

				// need http protocol
				request = encodeURL(
						java.util.Objects.toString(Jsoup.connect(request).followRedirects(true).execute().url()));

				iteratedLinks.add(request);

				int i = 0;

				while (i < 3) {

					try {
						doc = Jsoup.connect(request).userAgent("Mozilla").ignoreHttpErrors(true)
								.referrer("http://www.google.com").timeout(0).ignoreContentType(true)
								.followRedirects(true).get();
						break;
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
					}

					i++;
				}

				// get page title
				String title = doc.title();
				consoleOutput = consoleOutput + "-----------------------------------" + "\r\n";
				consoleOutput = consoleOutput + "Iterated Page: " + title + " | " + request + "\r\n";
				consoleOutput = consoleOutput + "Child Level: " + childLevel + "\r\n";

				if (childLevel == 0 || getAllChildPDF) {

					// get all links
					Elements links = doc.select(childFilter);

					// Use this to debug jsoup queries
//					 System.out.println(links.toString());

					for (Element link : links) {

						String childURL = encodeURL(link.attr("abs:href"));

						// Set File Name
						String fileName = childURL.split("/")[childURL.split("/").length - 1].split("[?]")[0];

						// Downloads files
						File file = new File(dir + fileName);

						// Create URL object
						URL childURLObj = new URL(childURL);

						// HTTP to HTTPS 301 Redirects
						boolean loopRedirects = true;
						while (loopRedirects) {
							try {
								Connection.Response redirRequest = Jsoup
										.connect(java.util.Objects.toString(childURLObj)).method(Method.GET)
										.followRedirects(false).execute();

								childURLObj = new URL(encodeURL(redirRequest.header("location")));
							} catch (Exception e) {
								loopRedirects = false;
							}
						}

						// Copy Files from URL if it does not exist
						if (!file.exists()) {
							// Define thread task
							FutureTask<String> task = new FutureTask<>(new urlToFile(childURLObj, file));
							executor.submit(task);
							consoleOutput = consoleOutput + task.get();
						}
					}

				}

				if (childLevel > 0) {

					// Get all child links
					Elements links = doc.select(parentFilter);

					childLevel--;

					for (Element link : links) {

						downloadFiles(executor, java.util.Objects.toString(link.attr("abs:href")), dir, childLevel,
								parentFilter, childFilter, getAllChildPDF);

					}

				}

			}

			System.out.println(consoleOutput);

		} catch (Exception e) {

			System.out.println(e.getMessage());

		}

	}

	@SuppressWarnings("unused")
	private static String redirectFileUrl(String url) throws IOException {

		// Returns a redirect URL
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		HttpURLConnection.setFollowRedirects(true);
		connection.setRequestProperty("User-Agent", "Mozilla");
		connection.connect();
		InputStream response = connection.getInputStream();

		try {
			return encodeURL(java.util.Objects.toString(connection.getURL()));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return "";
		}

	}

	public static String encodeURL(String urlStr) throws URISyntaxException, MalformedURLException {

		URL urlObj = new URL(urlStr);

		URI uri = new URI(urlObj.getProtocol(), urlObj.getHost(), urlObj.getPath(), urlObj.getQuery(), null);

		// Workaround for %2520 error
		String request = urlObj.getPath().contains("%20") ? uri.toASCIIString().replaceAll("%25", "%")
				: uri.toASCIIString();

		return request;
	}

	public static void clearLinks() {

		iteratedLinks.clear();

	}

	public static class urlToFile implements Callable<String> {
		private final URL url;
		private final File dest;
		private String consoleOutput = "";

		public urlToFile(URL url, File dest) {

			this.url = url;
			this.dest = dest;
		}

		@Override
		public String call() throws Exception {

			FileUtils.copyURLToFile(url, dest);
			consoleOutput = consoleOutput + "Downloaded file:" + url + " -> " + dest + "\r\n";
			return consoleOutput;
		}
	}

}
