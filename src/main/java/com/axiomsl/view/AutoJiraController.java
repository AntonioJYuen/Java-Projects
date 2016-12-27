package com.axiomsl.view;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import com.axiomsl.api.view.AbstractController;
import com.axiomsl.api.view.LoadingIndicator;
import com.testautomationguru.utility.PDFUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;

public class AutoJiraController extends AbstractController {

	@FXML
	private TextField pdfText1;

	@FXML
	private TextField pdfText2;

	@FXML
	private TextField destPathText;

	@FXML
	private CheckBox allPages;

	@FXML
	private TextField pageBox;

	// private Project project;
	private String pdfPath1;
	private String lastPDFPath1;

	private String pdfPath2;
	private String lastPDFPath2;

	@FXML
	void chooseExcel(ActionEvent event) {

		FileChooser fc = new FileChooser();

		if (lastPDFPath1 != null) {
			fc.setInitialDirectory(new File(lastPDFPath1));
		} else {
			fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		}

		File selectedFile = fc.showOpenDialog(null);
		if (selectedFile == null && pdfText1.getText().equals("")) {
			pdfText1.setText("No file selected");
		} else if (selectedFile == null) {
			System.out.println("Canceled");
		} else if (!selectedFile.getAbsoluteFile().getName().contains(".pdf")) {
			pdfText1.setText("Wrong File Type");
		} else {
			pdfText1.setText(selectedFile.getAbsolutePath());
			pdfPath1 = pdfText1.getText();
			lastPDFPath1 = selectedFile.getParent();
		}
	}

	@FXML
	void choosePDF(ActionEvent event) {

		FileChooser fc = new FileChooser();

		if (lastPDFPath2 != null) {
			fc.setInitialDirectory(new File(lastPDFPath2));
		} else {
			fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		}

		File selectedFile = fc.showOpenDialog(null);
		if (selectedFile == null && pdfText2.getText().equals("")) {
			pdfText2.setText("No file selected");
		} else if (selectedFile == null) {
			System.out.println("Canceled");
		} else if (!selectedFile.getAbsoluteFile().getName().contains(".pdf")) {
			pdfText2.setText("Wrong File Type");
		} else {
			pdfText2.setText(selectedFile.getAbsolutePath());
			pdfPath2 = pdfText2.getText();
			lastPDFPath2 = selectedFile.getParent();
		}
	}

	@FXML
	void disablePageBox() {

		pageBox.setDisable(allPages.isSelected());
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

				PDFUtil pdfUtil = new PDFUtil();

				try {
					if (allPages.isSelected()) {
						pdfUtil.compare(pdfPath1, pdfPath2, 0, 0, true, true);
					} else {
						String[] pages = pageBox.getText().split(";");

						for (String s : pages) {

							String[] range = s.split("-");

							if (range.length == 1) {

								int startPage = Integer.valueOf(range[0]);
								pdfUtil.compare(pdfPath1, pdfPath2, startPage, startPage, true, false);
							} else if (range.length == 2) {

								int startPage = Integer.valueOf(range[0]);
								int endPage = Integer.valueOf(range[1]);

								for (int iPage = startPage; iPage <= endPage; iPage++) {
									pdfUtil.compare(pdfPath1, pdfPath2, iPage, iPage, true, false);
								}
							} else {

								System.out.println("Not a valid page format");
							}
						}
					}

					JOptionPane.showMessageDialog(null, "Complete", "Complete", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, "Error", "Error", JOptionPane.WARNING_MESSAGE);
					e.printStackTrace();
				}

			}

			@Override
			public void end() throws IOException, InterruptedException {

			}

		});

	}
}