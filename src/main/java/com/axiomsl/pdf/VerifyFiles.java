package com.axiomsl.pdf;

import java.io.File;
import java.io.IOException;

import com.itextpdf.text.pdf.PdfReader;

public class VerifyFiles {

	public static void main(String[] args) {

	}

	@SuppressWarnings("unused")
	public static void verifyFiles(String dir) throws IOException {

		int pass = 0;
		int fail = 0;

		File[] files = new File(dir).listFiles();

		for (File f : files) {

			if (f.isFile()) {
				try {
					PdfReader pdfReader = new PdfReader(f.getPath());
					pass++;
				} catch (Exception e) {
					File moveFile = new File(f.getPath());
					File moveDir = new File(f.getParentFile() + "\\badPDFs\\");

					if (!moveDir.exists())
						moveDir.mkdir();

					moveFile.renameTo(new File(f.getParentFile() + "\\badPDFs\\" + f.getName()));
					fail++;
				}
			}
		}

		System.out.println("Passed: " + pass);
		System.out.println("Failed: " + fail);
	}

	public static boolean verifyFile(String dir) throws IOException {

		PdfReader pdfReader = null;

		try {
			pdfReader = new PdfReader(dir);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (pdfReader != null)
				pdfReader.close();
		}

	}
}
