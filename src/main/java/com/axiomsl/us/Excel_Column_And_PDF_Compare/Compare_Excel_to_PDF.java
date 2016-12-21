package com.axiomsl.us.Excel_Column_And_PDF_Compare;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.testautomationguru.utility.PDFUtil;

public class Compare_Excel_to_PDF {

	private static String output = "";

	// public static void main(String[] args) throws InvalidFormatException,
	// IOException {
	public static void startComparison(String excelPath, String pdfPath) throws InvalidFormatException, IOException {

		ArrayList<ArrayList<?>> parentList = new ArrayList<ArrayList<?>>();
		ArrayList<String> distinctDescStr = new ArrayList<String>();
		ArrayList<String> distinctDescStrPrepared = new ArrayList<String>();
		ArrayList<Boolean> distinctDescBool = new ArrayList<Boolean>();
		ArrayList<String> mdrmList = new ArrayList<String>();

		PDFUtil pdfutil = new PDFUtil();

		InputStream inp = new FileInputStream(excelPath);
		// "C:/Users/ayuen/Desktop/FRY_15_ax_rd_mdrm_info_ctrl_v20161231_Revised.xls");

		Workbook wb = null;
		wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);
		
		String pdfText = keepOnlyLetters(pdfutil.getText(pdfPath).toLowerCase().trim());

		int desc = 0;
		int mdrm = 0;

		for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {

			Row row = sheet.getRow(i);

			if (i == 0) {
				for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
					if (row.getCell(j).getStringCellValue().toLowerCase().contains("description")) {

						desc = j;
					} else if (row.getCell(j).getStringCellValue().toLowerCase().contains("mdrm_name")) {

						mdrm = j;

					}
				}
			} else {
				String description = row.getCell(desc).getStringCellValue();

				for (String t : description.split(":")) {
					// if (!distinctDescStr.contains(t.trim().toLowerCase())) {

					distinctDescStr.add(t.trim().toLowerCase());
					distinctDescStrPrepared.add(keepOnlyLetters(t.trim().toLowerCase()));
					mdrmList.add(row.getCell(mdrm).getStringCellValue());
					// }
				}
			}
		}

		parentList.add(distinctDescStr);
		parentList.add(distinctDescStrPrepared);

		for (int i = 0; i < parentList.get(1).size(); i++) {

			if (pdfText.contains(parentList.get(1).get(i).toString())) {

				distinctDescBool.add(true);
			} else {

				distinctDescBool.add(false);
			}
		}

		parentList.add(distinctDescBool);
		parentList.add(mdrmList);

		for (int j = 0; j < parentList.get(2).size(); j++) {
			if (parentList.get(2).get(j).equals(false)) {
				output = output + parentList.get(3).get(j) + "|" + parentList.get(0).get(j) + "\r\n";
				// System.out.println(parentList.get(3).get(j) + " | " +
				// parentList.get(0).get(j));
			}
		}
	}

	public static String keepOnlyLetters(String s) {

		String result = s.replaceAll("[^a-z,A-Z|1-9]", "");

		return result;
	}

	public String getOutput() {
		return output;
	}

	public void clearOutput() {
		output = "";
	}
}