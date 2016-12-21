package com.axiomsl.app;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelProcessor<T> {
	private Project project;
	private String filePath;
	private String destPath;
	private Workbook workbook;
	private XSSFSheet sheet;
	private String coverName;
	private XSSFCell cell;
	private String date;
	private XSSFCellStyle cs;
	private String fileName;
	private String tempPath;
	private String vID;

	static ExecutorService executor = Executors.newCachedThreadPool();

	public ExcelProcessor() {

	}

	public ExcelProcessor(String file, String outputPath, String date, String version, String fileName)
			throws IOException, ParseException {

		this.project = new Project();
		this.filePath = file;
		this.destPath = outputPath;
		this.date = date;
		this.coverName = version;
		this.fileName = fileName;

		openFile();

	}

	public void openFile() throws IOException, ParseException {
		try {
			FileInputStream fis = new FileInputStream(filePath);

			workbook = new XSSFWorkbook(fis);

			populateCoverTab();
			populateChangeDetailsMain();
			populateChangeSummaryTab();
			writeToFile(fileName + vID);

			fis.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void populateCoverTab() throws ParseException {
		sheet = (XSSFSheet) workbook.getSheet("Cover");

		String temp = coverName;
		// Write Title
		sheet.getRow(9).getCell(1).setCellValue(temp);

		SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat format2 = new SimpleDateFormat("MMMM dd, yyyy");
		Date date = format1.parse(this.date);

		// Write Date
		sheet.getRow(11).getCell(1).setCellValue("Release Date: " + format2.format(date));
	}

	public void populateChangeSummaryTab() {
		sheet = (XSSFSheet) workbook.getSheet("Change Summary");
		int startRow = 5;

		for (Ticket t : project.getTickets()) {
			vID = t.getVID();

			if (t.getForm().equals(coverName)) {
				sheet.createRow(startRow);

				// Hard coded number of columns
				int numCols = 11;

				for (int j = 0; j < numCols; j++) {
					sheet.getRow(startRow).createCell(j);
					sheet.getRow(startRow).getCell(j).setCellStyle(createCellStyleSummary(startRow, j));
				}

				String scheduleSection;

				if (t.getCdTab().split("\\|")[0].toLowerCase().contains("ec")
						|| (t.getCdTab().split("\\|")[0].toLowerCase().contains("edit")
								&& t.getCdTab().split("\\|")[0].toLowerCase().contains("check"))) {

					scheduleSection = "Edit Checks";

				} else if (t.getCdTab().split("\\|")[0].toLowerCase().contains("\r\n")) {

					scheduleSection = "Mutliple";

				} else {

					scheduleSection = t.getCdTab().split("\\|")[0];

				}

				sheet.getRow(startRow).getCell(0).setCellValue(scheduleSection);
				sheet.getRow(startRow).getCell(1).setCellValue(t.getIssueNum());
				sheet.getRow(startRow).getCell(2).setCellValue(t.getCsDesc());
				sheet.getRow(startRow).getCell(3).setCellValue(t.getXForm());
				sheet.getRow(startRow).getCell(4).setCellValue(t.getXEC());
				sheet.getRow(startRow).getCell(5).setCellValue(t.getXCos());
				sheet.getRow(startRow).getCell(6).setCellValue(t.getXOth());
				sheet.getRow(startRow).getCell(7).setCellValue(t.getXReg());
				sheet.getRow(startRow).getCell(8).setCellValue(t.getXFix());
				sheet.getRow(startRow).getCell(9).setCellValue(t.getXEnh());
				sheet.getRow(startRow).getCell(10).setCellValue(t.getVID());

				createHyperLink(startRow);

				startRow++;
			}
		}

	}

	public void populateChangeDetailsMain() {
		sheet = (XSSFSheet) workbook.getSheet("Change Details (Main Report)");
		int startRow = 3;

		for (Ticket t : project.getTickets()) {

			if (t.getForm().equals(coverName)) {

				sheet.createRow(startRow);

				// Hard coded number of columns
				int numCols = 7;

				for (int j = 0; j < numCols; j++) {
					sheet.getRow(startRow).createCell(j);
					sheet.getRow(startRow).getCell(j).setCellStyle(createCellStyleDetailsMain(startRow, j));
				}

				t.setIssueNum(startRow - 2);

				sheet.getRow(startRow).getCell(0).setCellValue(t.getIssueNum());
				sheet.getRow(startRow).getCell(1).setCellValue(t.getCdTab().split("\\|")[0]);
				sheet.getRow(startRow).getCell(2).setCellValue(t.getCdTab().split("\\|")[1]);
				sheet.getRow(startRow).getCell(3).setCellValue(t.getCdTab().split("\\|")[2]);
				sheet.getRow(startRow).getCell(4).setCellValue(t.getCdDetails());
				sheet.getRow(startRow).getCell(5).setCellValue(t.getVID());
				sheet.getRow(startRow).getCell(6).setCellValue("");

				t.setRow(startRow);

				startRow++;
			}

		}

	}

	public XSSFCellStyle createCellStyleSummary(int row, int column) {

		cs = (XSSFCellStyle) workbook.createCellStyle();
		cs.setBorderRight(CellStyle.BORDER_THIN);
		cs.setBorderTop(CellStyle.BORDER_THIN);
		cs.setBorderBottom(CellStyle.BORDER_THIN);
		cs.setBorderLeft(CellStyle.BORDER_THIN);

		if (row % 2 != 0) {
			XSSFColor color = new XSSFColor(new java.awt.Color(225, 244, 250));
			cs.setFillForegroundColor(color);
			cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		} else {
			XSSFColor color = new XSSFColor(Color.white);
			cs.setFillForegroundColor(color);
			cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
		// cs.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		if (column != 2 && column != 3 && column != 11) {
			cs.setAlignment(CellStyle.ALIGN_CENTER);
		} else {
			cs.setAlignment(CellStyle.ALIGN_LEFT);

		}
		cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cs.setWrapText(true);

		return cs;
	}

	public XSSFCellStyle createCellStyleDetailsMain(int row, int column) {

		cs = (XSSFCellStyle) workbook.createCellStyle();
		cs.setBorderRight(CellStyle.BORDER_THIN);
		cs.setBorderTop(CellStyle.BORDER_THIN);
		cs.setBorderBottom(CellStyle.BORDER_THIN);
		cs.setBorderLeft(CellStyle.BORDER_THIN);

		if (row % 2 != 0) {
			XSSFColor color = new XSSFColor(new java.awt.Color(225, 244, 250));
			cs.setFillForegroundColor(color);
			cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		} else {
			XSSFColor color = new XSSFColor(Color.white);
			cs.setFillForegroundColor(color);
			cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}

		cs.setAlignment(CellStyle.ALIGN_LEFT);

		if (column == 4)
			cs.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
		else
			cs.setVerticalAlignment(CellStyle.VERTICAL_TOP);

		cs.setWrapText(true);

		return cs;
	}

	public void createTicketLink(String key) {
		CreationHelper createHelper = workbook.getCreationHelper();

		Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
		link.setAddress("https://jira.axiomsl.us/browse/" + key);

		cell.setHyperlink(link);

	}

	public void createHyperLink(int i) {
		CreationHelper createHelper = workbook.getCreationHelper();

		XSSFSheet sumSheet = (XSSFSheet) workbook.getSheet("Change Summary");

		Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
		link.setAddress("'Change Details (Main Report)'!A" + (i - 1));

		sumSheet.getRow(i).getCell(1).setHyperlink(link);
		setHyperlinkStyle(sumSheet.getRow(i).getCell(1));

	}

	public void setHyperlinkStyle(Cell cell) {

		CellStyle style = cell.getCellStyle();
		Font font = workbook.createFont();
		font.setUnderline(Font.U_SINGLE);
		font.setColor(IndexedColors.BLUE.getIndex());
		style.setFont(font);

		cell.setCellStyle(style);

	}

	void writeToFile(String fileName) throws IOException {

		int i = 1;

		File globalFile = new File(destPath + "\\" + fileName + ".xlsx");

		if (globalFile.exists())
			while (globalFile.exists()) {
				globalFile = new File(destPath + "\\" + fileName + "(" + i + ").xlsx");
				i++;
			}

		tempPath = globalFile.getPath();

		FileOutputStream output = new FileOutputStream(globalFile); // Open
		workbook.write(output); // write changes

		output.close(); // close the stream

	}

	public String returnFilePath() {
		return tempPath;

	}

	public void clearCached() {

	}
}
