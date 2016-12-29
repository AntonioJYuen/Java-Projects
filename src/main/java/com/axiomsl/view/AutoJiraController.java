package com.axiomsl.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import com.axiomsl.api.view.AbstractController;
import com.axiomsl.api.view.LoadingIndicator;
import com.axiomsl.us.Compare_Excel_to_PDF;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;

@SuppressWarnings("restriction")
public class AutoJiraController extends AbstractController {

	// @FXML
	// private Button chooseDestButton;

	@FXML
	private TextField excelPathText;

	@FXML
	private TextField pdfPathText;

	@FXML
	private TextField destPathText;
	
	@FXML
	private TextArea outputText;

	// private Project project;
	private String excelPath;
	private String lastExcelPath;
	private String pdfPath;
	private String lastPDFPath;
	private String destPath;
	private String lastDestPath;

	@FXML
	void chooseExcel(ActionEvent event) {

		FileChooser fc = new FileChooser();

		if (lastExcelPath != null) {
			fc.setInitialDirectory(new File(lastExcelPath));
		} else {
			fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		}

		File selectedFile = fc.showOpenDialog(null);
		if (selectedFile == null && excelPathText.getText().equals("")) {
			excelPathText.setText("No file selected");
		} else if (selectedFile == null) {
			System.out.println("Canceled");
		} else if (!selectedFile.getAbsoluteFile().getName().contains(".xls")) {
			excelPathText.setText("Wrong File Type");
		} else {
			excelPathText.setText(selectedFile.getAbsolutePath());
			excelPath = excelPathText.getText();
			lastExcelPath = selectedFile.getParent();
		}
	}

	@FXML
	void choosePDF(ActionEvent event) {

		FileChooser fc = new FileChooser();

		if (lastPDFPath != null) {
			fc.setInitialDirectory(new File(lastPDFPath));
		} else {
			fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		}

		File selectedFile = fc.showOpenDialog(null);
		if (selectedFile == null && pdfPathText.getText().equals("")) {
			pdfPathText.setText("No file selected");
		} else if (selectedFile == null) {
			System.out.println("Canceled");
		} else if (!selectedFile.getAbsoluteFile().getName().contains(".pdf")) {
			pdfPathText.setText("Wrong File Type");
		} else {
			pdfPathText.setText(selectedFile.getAbsolutePath());
			pdfPath = pdfPathText.getText();
			lastPDFPath = selectedFile.getParent();
		}
	}

	@FXML
	void chooseDestination(ActionEvent event) {

		DirectoryChooser dc = new DirectoryChooser();

		if (lastDestPath != null) {
			dc.setInitialDirectory(new File(lastDestPath));
		} else {
			dc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		}

		File selectedDir = dc.showDialog(null);
		if (selectedDir == null && destPathText.getText().equals("")) {
			destPathText.setText("No directory selected");
		} else if (selectedDir == null) {
			System.out.println("Canceled");
		} else {
			destPathText.setText(selectedDir.getAbsolutePath());
			destPath = destPathText.getText();
			lastDestPath = selectedDir.getParent();
		}
	}

	@FXML
	void automate(ActionEvent event) throws URISyntaxException, IOException {

		runLater(new LoadingIndicator(this) {
			@Override
			public boolean init() {

				return super.init();
			}

			@Override
			public void start() throws Exception {

				System.out.println(excelPath + " | " + pdfPath + " | " + destPath);
				
				Compare_Excel_to_PDF etp = new Compare_Excel_to_PDF();
				Compare_Excel_to_PDF.startComparison(excelPath, pdfPath);
				
				// System.out.println(etp.getOutput());

				if (destPath!=null) {
					@SuppressWarnings("resource")
					PrintStream out = new PrintStream(new FileOutputStream(destPath + "\\Output.txt"));
					out.print(etp.getOutput());
				}

				outputText.setText(etp.getOutput());
				
				etp.clearOutput();
			}

			@Override
			public void end() throws IOException, InterruptedException {
					
			}

		});

	}
}