package com.axiomsl.app;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class JiraReader {

	private static final String JIRA_URL = "https://jira.axiomsl.us/";
	private String filter;
	protected Project project;
	private static String JIRA_ADMIN_USERNAME = ""; // Login
	private static String JIRA_ADMIN_PASSWORD = ""; // Password

	public JiraReader() {
	}

	public JiraReader(String filter, String version, String username, String password) {
		this.filter = filter;
		this.project = new Project();
		JiraReader.JIRA_ADMIN_USERNAME = username;
		JiraReader.JIRA_ADMIN_PASSWORD = password;
	}

	@SuppressWarnings("unused")
	public void retrieveInfo()
			throws URISyntaxException, InterruptedException, ExecutionException, MalformedURLException {

		// Print usage instructions
		StringBuilder intro = new StringBuilder();
		intro.append(
				"**********************************************************************************************\r\n");
		intro.append(
				"* JIRA Java REST Client ('JRJC') example.                                                    *\r\n");
		intro.append(
				"* NOTE: Start JIRA using the Atlassian Plugin SDK before running this example.               *\r\n");
		intro.append(
				"* (for example, use 'atlas-run-standalone --product jira --version 6.0 --data-version 6.0'.) *\r\n");
		intro.append(
				"**********************************************************************************************\r\n");
		// System.out.println(intro.toString());

		// Connection String
		JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		URI uri = new URI(JIRA_URL);
		JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, JIRA_ADMIN_USERNAME,
				JIRA_ADMIN_PASSWORD);

		try {
			Promise<User> promise = client.getUserClient().getUser(JIRA_ADMIN_USERNAME);
			User user = promise.claim();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Wrong username/password combination",
					"Wrong username/password combination", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Jql Query Search
		try {
			Promise<SearchResult> searchJqlPromise = client.getSearchClient().searchJql(filter, 99999, 0, null);

			String temp[] = null;

			String form = "";

			String vID = "";
			String jKey = "";

			String csDesc = "";
			String csImpact = "";
			char csType;

			String cdDetails = "";
			String cdTab = "";

			int issueNum = 1;

			String cv_Project = "";
			String cv_Branch = "";
			String qa_Actual_Result = "";

			// Defining strings in case they change in the future
			String global_Form_Enhancement = "Global_Form_Enhancement";
			String global_Form_Regulatory_Change = "Global_Form_Regulatory_Change";
			String description_of_Requirement = "Description of Requirement";
			String incident_Detail = "Incident Detail";
			String affected_Components = "Affected components";

			String form_Field = "Form Name";
			String cdDetail_Field = "QA Test_Cases";
			String cdTab_Field = "QA Impact_Analysis";
			String vID_Field = "Expected Release Period";

			String cv_Project_Field = "Fixed in CV Project  (Dev. Team)";
			String cv_Branch_Field = "Fixed in CV Branch. (Dev. Team)";
			String qa_Actual_Result_Field = "QA Actual_Result";

			this.project = new Project();

			for (Issue issue : searchJqlPromise.claim().getIssues()) {

				// Return Change Type
				if (issue.getIssueType().getName().equals(global_Form_Enhancement))
					csType = 'E';
				else if (issue.getIssueType().getName().equals(global_Form_Regulatory_Change))
					csType = 'R';
				else
					csType = 'F';

				jKey = java.util.Objects.toString(issue.getKey());

				// Query details
				for (IssueField i : issue.getFields()) {

					// Return form
					if (i.getName().trim().equals(form_Field)) {
						temp = java.util.Objects.toString(i.getValue()).split("\"child\":");

						for (int j = 0; j < temp.length; j++) {

							if (!(temp[j].equals("]"))) {
								form = form + temp[j].split("value\":\"")[1].split("\"")[0].trim() + "_";
							}
						}
						form = form.replaceAll("_$", "");
					}

					// Return Description
					if (issue.getIssueType().getName().equals(global_Form_Enhancement)
							|| issue.getIssueType().getName().equals(global_Form_Regulatory_Change)) {
						if (i.getName().trim().equals(description_of_Requirement)) {
							csDesc = java.util.Objects.toString(i.getValue());
						}
						;
					} else {
						if (i.getName().trim().equals(incident_Detail)) {
							csDesc = java.util.Objects.toString(i.getValue());
						}
						;
					}

					// Return Impacted components
					if (i.getName().trim().equals(affected_Components)) {
						temp = java.util.Objects.toString(i.getValue()).split("}");

						for (int j = 0; j < temp.length; j++) {

							if (!(temp[j].equals("]")))
								switch (temp[j].split("value\":\"")[1].split("\"")[0]) {
								case "FreeFormReport":
									csImpact = csImpact + "F";
									break;
								case "Edit Check":
									csImpact = csImpact + "E";
									break;
								case "Cosmetic":
									csImpact = csImpact + "C";
									break;
								default:
									csImpact = csImpact + "O";
									break;
								}
						}

					}

					// Return Details on change details
					if (i.getName().trim().equals(cdDetail_Field)) {
						cdDetails = java.util.Objects.toString(i.getValue());
					}

					// Return Tab on change details
					if (i.getName().trim().equals(cdTab_Field)) {
						cdTab = java.util.Objects.toString(i.getValue());
					}

					// Return Version ID
					if (i.getName().trim().matches(vID_Field)) {
						vID = java.util.Objects.toString(i.getValue());
					}

					// Return Project
					if (i.getName().trim().equals(cv_Project_Field)) {
						cv_Project = java.util.Objects.toString(i.getValue());
					}

					// Return Branch
					if (i.getName().trim().equals(cv_Branch_Field)) {
						cv_Branch = java.util.Objects.toString(i.getValue());
					}

					// Return QA Actual Result
					if (i.getName().trim().equals(qa_Actual_Result_Field)) {
						qa_Actual_Result = java.util.Objects.toString(i.getValue());
					}

				}

				// Stage cdTab - Tab|Cell Address|Value/Formula
				ArrayList<String> tabs = new ArrayList<String>();
				ArrayList<String> cellAddresses = new ArrayList<String>();
				ArrayList<String> valuesAndFormulas = new ArrayList<String>();
				for (String s : cdTab.split("\r\n")) {

					String[] str = s.split("\\|");

					if (str.length >= 1) {
						if (!tabs.contains(str[0]))
							if (s.split("\\|")[0] == null)
								tabs.add("N/A");
							else
								tabs.add(s.split("\\|")[0]);
					} else
						tabs.add("N/A");

					if (str.length >= 2) {
						if (!cellAddresses.contains(str[1]))
							cellAddresses.add(s.split("\\|")[1]);
					} else
						cellAddresses.add("N/A");

					if (str.length >= 3) {
						if (!valuesAndFormulas.contains(str[2]))
							valuesAndFormulas.add(s.split("\\|")[2]);
					} else
						valuesAndFormulas.add("N/A");

				}

				String cdTabStr = "";

				for (String s : tabs)
					cdTabStr = cdTabStr + s + "\r\n";

				cdTabStr = cdTabStr.replaceAll("\r\n$", "") + "|";

				for (String s : cellAddresses)
					cdTabStr = cdTabStr + s + "\r\n";

				cdTabStr = cdTabStr.replaceAll("\r\n$", "") + "|";

				for (String s : valuesAndFormulas)
					cdTabStr = cdTabStr + s + "\r\n";

				cdTabStr = cdTabStr.replaceAll("\r\n$", "");
				// System.out.println(cdTabStr);

				// System.out.println("Item #: " + issueNum + " | Key: " + jKey
				// + " | Description: " + csDesc.replaceAll("\r\n", " ") + " |
				// Impact: " + csImpact +
				// " | Change_Type: " + csType + " | Version: " + vID + " |
				// Change Details: " + cdDetails.replaceAll("\r\n", " ") + " |
				// Change Tab:" + cdTab);

				this.project.addTicket(new Ticket(vID, form, issueNum, jKey, csDesc, csImpact, csType, cdDetails,
						cdTabStr, 0, cv_Project, cv_Branch, qa_Actual_Result));

				// Garbage Collection
				temp = null;
				form = "";
				vID = "";
				jKey = "";
				csDesc = "";
				csImpact = "";
				cdDetails = "";
				cdTab = "";
				cv_Project = "";
				cv_Branch = "";
				qa_Actual_Result = "";
				issueNum++;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please check your Ticket Filters", "Please check your Ticket Filters",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	public void printInfo() {
		project.sortTickets();
		// project.printTickets();
	}

	public void sortTickets() {
		project.sortTickets();
	}

	public void clearCached() {
		project.getTickets().clear();
	}
}
