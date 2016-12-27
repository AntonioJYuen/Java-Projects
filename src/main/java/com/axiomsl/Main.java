package com.axiomsl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.axiomsl.browser.GetPDFfromUrlMultiThreadJSoup;
import com.axiomsl.browser.GetPDFfromUrlMultiThreadJSoup.urlToFile;
import com.axiomsl.email.SendEmail;
import com.axiomsl.pdf.ComparePDFs;
import com.axiomsl.pdf.VerifyFiles;
import com.axiomsl.utility.MoveFiles;
import com.itextpdf.text.log.SysoCounter;

import bsh.This;

@SuppressWarnings("unused")
public class Main {

	static String newDir;
	static String oldDir;
	static String email;
	static String emailFrom;
	static ArrayList<String> urlConfig = new ArrayList<String>();

	static ExecutorService executor = Executors.newCachedThreadPool();

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
		
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		
		System.out.println(decodedPath);
		
		parseConfig();

		long start = System.currentTimeMillis();

		MoveFiles.moveFiles(newDir, oldDir);
		FileUtils.cleanDirectory(new File(newDir));

		for (String s : urlConfig) {

			String url, parentFilter, childFilter;
			int childLevels;
			boolean getAllPDFs;

			url = s.split(",")[0];
			parentFilter = s.split(",")[2];
			childFilter = s.split(",")[3];

			childLevels = Integer.parseInt(s.split(",")[1]);

			getAllPDFs = Boolean.getBoolean(s.split(",")[4]);

			GetPDFfromUrlMultiThreadJSoup.downloadFiles(executor, url, newDir, childLevels, parentFilter, childFilter,
					getAllPDFs);

		}

		// Wait for thread complete
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		// GetPDFfromUrlMultiThreadJSoup.downloadFiles("https://www.newyorkfed.org/financial-services-and-infrastructure/financial-market-infrastructure-and-reform/money-market-funds",
		// newDir, 0, "a[href*=form]", "a[href*=.pdf]", true, true);

		long time = System.currentTimeMillis() - start;
		System.out.println(time);

		ComparePDFs.startCompare(oldDir, newDir);

		GetPDFfromUrlMultiThreadJSoup.clearLinks();

		SendEmail.sendEmail(emailFrom, email.split(","), "PDF Documents comparison", bodyMessage(), newDir, oldDir);
	}

	public static String bodyMessage() {

		ComparePDFs comparePDFs = new ComparePDFs();

		List<String> miscPDFs = comparePDFs.getMiscPDfs();
		List<String> failedPDFs = comparePDFs.getFailedPDfs();
		List<String> corruptPDFs = comparePDFs.getCorruptPDfs();
		List<String> passedPDFs = comparePDFs.getPassedPDfs();

		String bodyText = "";

		bodyText = bodyText
				+ bodyText("PDFs that were not compared because there is no subsequent file to compare to", miscPDFs);
		bodyText = bodyText + bodyText("PDFs that failed the comparison check", failedPDFs);
		bodyText = bodyText + bodyText("PDFs that are corrupt and cannot be compared", corruptPDFs);
		bodyText = bodyText + bodyText("PDFs that have passed validation", passedPDFs);

		return bodyText;
	}

	public static String bodyText(String prefix, List<String> list) {

		String bodyText = "";

		if (list.size() > 0) {

			bodyText = bodyText + "\r\n";
			bodyText = bodyText + "-------------" + prefix + "-------------\r\n";
			bodyText = bodyText + "\r\n";
			for (String s : list) {
				bodyText = bodyText + s + "\r\n";

			}

		}

		return bodyText;
	}

	public static void parseConfig() throws IOException {
		
		FileReader input = new FileReader(new File("").getAbsolutePath() + "/data/config.config");
		@SuppressWarnings("resource")
		BufferedReader bufRead = new BufferedReader(input);
		String myLine = null;

		while ((myLine = bufRead.readLine()) != null) {
			if (!myLine.startsWith("*")) {

				if (newDir == null) {

					try {

						newDir = myLine.split("newDir=")[1].trim();

					} catch (Exception e) {}

				}

				if (oldDir == null) {

					try {

						oldDir = myLine.split("oldDir=")[1].trim();

					} catch (Exception e) {}

				}

				if (email == null) {

					try {

						email = myLine.split("email=")[1].trim();

					} catch (Exception e) {}

				}

				if (emailFrom == null) {

					try {

						emailFrom = myLine.split("emailFrom=")[1].trim();

					} catch (Exception e) {}

				}

				if (myLine.startsWith("http")) {

					urlConfig.add(myLine);
				}
			}
		}
	}
	
}