package com.axiomsl.pdf;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.axiomsl.utility.Pair;
import com.testautomationguru.utility.CompareMode;
import com.testautomationguru.utility.PDFUtil;

public class ComparePDFs {

	static List<String> miscPDFs = new ArrayList<String>(); // Files not
															// compared
	static List<String> failedPDFs = new ArrayList<String>(); // Failed
																// comparisons
	static List<String> corruptPDFs = new ArrayList<String>(); // Corrupted
																// files
	static List<String> comparedPDFs = new ArrayList<String>(); // Compared List
	static List<String> passedPDFs = new ArrayList<String>();

	public ComparePDFs() {
	}

	public static void startCompare(String oldDirStr, String newDirStr) throws IOException {

		ComparePDFs comparePDFs = new ComparePDFs();

		PDFUtil pdfutil = new PDFUtil();
		pdfutil.setCompareMode(CompareMode.TEXT_MODE);

		List<Pair<?, ?>> oldPDFs = new ArrayList<Pair<?, ?>>();
		List<Pair<?, ?>> newPDFs = new ArrayList<Pair<?, ?>>();

		Path oldDir = Paths.get(oldDirStr);
		Path newDir = Paths.get(newDirStr);

		comparePDFs.getFileNames(oldPDFs, oldDir);
		comparePDFs.getFileNames(newPDFs, newDir);

		// PDF Compare goes here
		for (Pair<?, ?> oldPDF : oldPDFs) {
			for (Pair<?, ?> newPDF : newPDFs) {
				if (oldPDF.getRight().equals(newPDF.getRight())) {
					System.out.println("Comparing: " + LocalDateTime.now() + " | " + oldPDF.getRight());
					comparedPDFs.add(java.util.Objects.toString(oldPDF.getRight()).trim());

					if (!pdfutil.compare(java.util.Objects.toString(oldPDF.getLeft()).trim(),
							java.util.Objects.toString(newPDF.getLeft()).trim())) {
						// System.out.println("Does not match: " +
						// oldPDF.getRight());
						failedPDFs.add(java.util.Objects.toString(oldPDF.getRight()).trim());
					}
				}
			}
		}

		// Print out file that weren't compared
		compareToPair(oldPDFs, comparedPDFs, miscPDFs);
		compareToPair(newPDFs, comparedPDFs, miscPDFs);

		// for (String miscPDF : miscPDFs){
		// System.out.println("Files not compared " + miscPDF);
		// }

		List<String> tempList = new ArrayList<String>();
		tempList.addAll(miscPDFs);
		tempList.addAll(failedPDFs);
		tempList.addAll(corruptPDFs);

		compareToList(comparedPDFs, tempList, passedPDFs);

		System.out.println("Comparisons Complete");
	}

	public List<Pair<?, ?>> getFileNames(List<Pair<?, ?>> fileList, Path dir) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path path : stream) {
				if (path.toFile().isDirectory()) {
					getFileNames(fileList, path);
				} else {
					if (VerifyFiles.verifyFile(java.util.Objects.toString(path.toAbsolutePath()))) {
						Pair<Path, Path> pair = new Pair<Path, Path>(path.toAbsolutePath(), path.getFileName());
						fileList.add(pair);
					} else {

						// Write into a list - error files
						if (!corruptPDFs.contains(java.util.Objects.toString(path.getFileName())))
							corruptPDFs.add(java.util.Objects.toString(path.getFileName()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileList;
	}

	private static void compareToPair(List<Pair<?, ?>> pdfList, List<String> compareToList, List<String> writeList) {

		for (Pair<?, ?> pdf : pdfList) {

			boolean found = false;

			for (String compareTo : compareToList) {
				if (compareTo.equals(java.util.Objects.toString(pdf.getRight()).trim())) {
					found = true;
					break;
				}
			}
			if (!found)
				writeList.add(java.util.Objects.toString(pdf.getRight()));
		}
	}

	private static void compareToList(List<String> pdfList, List<String> compareToList, List<String> writeList) {

		for (String pdf : pdfList) {

			boolean found = false;

			for (String compareTo : compareToList) {
				if (compareTo.equals(pdf)) {
					found = true;
					break;
				}
			}
			if (!found)
				writeList.add(pdf);
		}
	}

	public List<String> getMiscPDfs() {
		return miscPDFs;
	}

	public List<String> getFailedPDfs() {
		return failedPDFs;
	}

	public List<String> getCorruptPDfs() {
		return corruptPDFs;
	}

	public List<String> getPassedPDfs() {
		return passedPDFs;
	}

	public void clearLists() {
		miscPDFs = null;
		failedPDFs = null;
		corruptPDFs = null;
		comparedPDFs = null;
	}
}
