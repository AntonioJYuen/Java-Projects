package com.axiomsl.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.axiomsl.api.view.AbstractController;
import com.axiomsl.api.view.LoadingIndicator;
import com.axiomsl.app.ExcelProcessor;
import com.axiomsl.app.JiraReader;
import com.axiomsl.app.Project;
import com.axiomsl.app.Ticket;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

@SuppressWarnings("restriction")
public class AutoJiraController extends AbstractController {
	@FXML
	private Button automateButoon;

	@FXML
	private Button chooseDestButton;

	@FXML
	private TextField destPathText;

	@FXML
	private TextField filterText;

	@FXML
	private DatePicker datePicker;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private CheckBox excelCB;

	@FXML
	private CheckBox propertiesCB;

	private Project project;
	private String destPath;
	private String lastDestPath;
	private String filter;
	private String form;
	private Date date;
	private String username;
	private String password;
	private String inputFile;

	@Override
	public void errorAlert(Exception e) {
	}

	@Override
	public void errorAlert(String e) {
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

				filter = filterText.getText();
				username = usernameField.getText();
				password = passwordField.getText();

				// Pull date from datePicker
				LocalDate localDate = datePicker.getValue();
				Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
				date = Date.from(instant);

				return super.init();
			}

			@Override
			public void start() throws Exception {

				ExecutorService executor = Executors.newCachedThreadPool();

				JiraReader jr = null;
				// TODO Auto-generated method stub

				// start the jira process
				jr = new JiraReader(filter, form, username, password);
				jr.retrieveInfo();
				jr.printInfo();

				project = new Project();

				// Create array to seperate different forms
				ArrayList<String> formNames = new ArrayList<String>();

				for (Ticket t : project.getTickets()) {

					if (!formNames.contains(t.getForm()))
						formNames.add(t.getForm());

				}

				for (String name : formNames) {

					form = name;

					// Format date into a string to be used in the excel title
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

					// Start the Excel Process
					if (excelCB.isSelected()) {

						Runnable worker = new excelCreation("data/Template.xlsx", destPath, format.format(date), form,
								"Functional Release Notes " + form + "_");
						executor.execute(worker);

					}

				}

				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

				// Format date into a string to be used in the excel title
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

				inputFile = destPath + "/" + format.format(date);
				// Create a properties file
				if (propertiesCB.isSelected()) {

					System.out.println(inputFile);
					createReleaseFiles(FilenameUtils.removeExtension(inputFile));

				}

				project.clearTickets();
				JOptionPane.showMessageDialog(null, "Complete", "Complete", JOptionPane.INFORMATION_MESSAGE);

			}

			@Override
			public void end() throws IOException, InterruptedException {

			}

		});

	}

	public void createReleaseFiles(String inputFile) throws IOException {

		new ExcelProcessor<Object>();
		File file = new File(inputFile + "_Partial.properties");

		if (!file.exists()) {

			file.createNewFile();

		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		bw.newLine();
		bw.write("# Added CV Objects that must be in the partial release files");
		bw.newLine();

		ArrayList<String> writeToFile = new ArrayList<String>();

		for (Ticket t : project.getTickets()) {

			for (String temp : t.getQAActual().split("\r\n")) {

				String branch = t.getBranch(), result, tempProject, tempBranch, tempObject, tempObjectName;

				if (t.getQAActual() != null && !t.getQAActual().toLowerCase().equals("null")) {

					if (StringUtils.countMatches(temp, "/") < 2) {

						if (t.getProject().contains("/")) {
							System.out.println(t.getProject());
							tempProject = t.getProject().split("/")[t.getProject().split("/").length - 1].trim();

						} else {
							tempProject = t.getProject().trim();
						}

						result = tempProject.replaceAll("(?i)_dev", "") + "*/" + stageBranch(branch) + "/" + temp;

					} else {

						tempProject = (temp.split("/").length > 0) ? temp.split("/")[0].replaceAll("(?i)_dev", "")
								: "MISSING PROJECT";
						tempBranch = (temp.split("/").length > 1) ? temp.split("/")[1] : "MISSING BRANCH";
						tempObject = (temp.split("/").length > 2) ? temp.split("/")[2] : "MISSING OBJECT TYPE";
						tempObjectName = (temp.split("/").length > 3) ? temp.split("/")[3] : "MISSING OBJECT";

						tempBranch = stageBranch(tempBranch);

						result = tempProject + "*/" + tempBranch + "/" + tempObject + "/" + tempObjectName;

					}

					if (!writeToFile.contains(result)) {

						writeToFile.add(result);

					}

				}

			}

		}

		// System.out.println(writeToFile.size());
		for (String s : writeToFile) {

			bw.write(s);
			bw.newLine();

		}

		bw.close();

	}

	private String stageBranch(String branch) {

		String output = "";

		if (branch.toLowerCase().matches("^.*[v][\\d].*")) {
			output = branch.substring(0, branch.lastIndexOf('v') + 1).trim() + "*";
		} else {
			output = branch + "*";
		}

		return output;

	}

	public static class excelCreation implements Runnable {
		private final String file, outputPath, date, version, fileName;

		excelCreation(String file, String outputPath, String date, String version, String fileName) {
			this.file = file;
			this.outputPath = outputPath;
			this.date = date;
			this.version = version;
			this.fileName = fileName;
		}

		@Override
		public void run() {

			try {
				ExcelProcessor<Object> er = new ExcelProcessor<Object>(file, outputPath, date, version, fileName);
				System.out.println(er.returnFilePath());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
