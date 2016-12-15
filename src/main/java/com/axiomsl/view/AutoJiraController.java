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
	private String filter;
	private String form;
	private Date date;
	private String username;
	private String password;	
	private String inputFile;

    
    @Override
	public void errorAlert(Exception e) {}
    
    @Override
	public void errorAlert(String e) {}
	
	@FXML
	void chooseDestination(ActionEvent event) {
		
		DirectoryChooser dc = new DirectoryChooser();
		File selectedDirectory = dc.showDialog(null);
		
		if (selectedDirectory == null) {
			
			destPathText.setText("No Directory selected");
			
		} else {
			
			destPathText.setText(selectedDirectory.getAbsolutePath());
			destPath = destPathText.getText();
			
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
			
				
				
				JiraReader jr = null;
				// TODO Auto-generated method stub
					
				//	 start the jira process 
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
						
						//Format date into a string to be used in the excel title
						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
						
					//	 Start the Excel Process 
						if (excelCB.isSelected()) {
							
							ExcelProcessor<Object> er = new ExcelProcessor<Object>("data/Template.xlsx", destPath,format.format(date), form, form + "_" + format.format(date));
							System.out.println(er.returnFilePath());
							
						}
						
					}
					

					//Format date into a string to be used in the excel title
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
					
					inputFile = destPath + "/" + format.format(date);
					//Create a properties file
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
	
	public void createReleaseFiles(String inputFile) throws IOException{
		
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

		for(Ticket t : project.getTickets()) {
			
			for(String temp : t.getQAActual().split("\r\n")) {
				
				String branch, result, tempProject, tempBranch, tempObject, tempObjectName;
				
				if (t.getQAActual() != null && !t.getQAActual().toLowerCase().equals("null")) {
				
					if (t.getBranch().toLowerCase().matches("^.*[v][\\d]*$")) {
						
						branch = t.getBranch().substring(0,t.getBranch().toLowerCase().lastIndexOf('v')+1).trim() + "*";
						
					}
					else {
						
						branch = t.getBranch().trim();
						
					}
					
					if (StringUtils.countMatches(temp, "/") < 2) {						
						
						result = t.getForm().toLowerCase().replaceAll("_dev", "") + "*/" + branch + "/" + temp;
						
					}
					else {
						
						tempProject = (temp.split("/").length > 0) ? temp.split("/")[0].toLowerCase().replaceAll("_dev",  "") : "MISSING PROJECT";
						tempBranch = (temp.split("/").length > 1) ? temp.split("/")[1] : "MISSING BRANCH";
						tempObject = (temp.split("/").length > 2) ? temp.split("/")[2] : "MISSING OBJECT TYPE";
						tempObjectName = (temp.split("/").length > 3) ? temp.split("/")[3] : "MISSING OBJECT";
						
						if(tempBranch.toLowerCase().matches("^.*[v][\\d]*$")) {
							tempBranch = tempBranch.substring(0,tempBranch.toLowerCase().lastIndexOf('v')+1).trim() + "*";
						}
						else {
							tempBranch = tempBranch + "*";
						}
						
						result = tempProject + "*/" + tempBranch + "/" + tempObject+ "/" + tempObjectName;

					}
					
					if (!writeToFile.contains(result)) {
						
						writeToFile.add(result);
						
					}
					
				}
		
			}
			
		}
		

//		System.out.println(writeToFile.size());
		for(String s : writeToFile) {
		
			bw.write(s);
			bw.newLine();
			
		}
		
		bw.close();
		
	}

}
