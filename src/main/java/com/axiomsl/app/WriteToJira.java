package com.axiomsl.app;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import static com.google.common.collect.Iterables.transform;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class WriteToJira {

 	private static final String JIRA_URL = "https://jira.axiomsl.us/";
    private String filter;
    protected Project project;
    private static String JIRA_ADMIN_USERNAME = "ayuen"; //Login
    private static String JIRA_ADMIN_PASSWORD = "Qwer!4045"; //Password
    
    public static void main(String[] args) throws URISyntaxException, IOException {
    	final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
	    URI uri = new URI(JIRA_URL);
	    final JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, JIRA_ADMIN_USERNAME, JIRA_ADMIN_PASSWORD);

	    try {
	    	
	    	final List<Promise<BasicIssue>> promises = Lists.newArrayList();
			final IssueRestClient issueClient = client.getIssueClient();

			System.out.println("Sending issue creation requests...");
			
			final String summary = "NewIssue#" + 1;
			final IssueInput newIssue = new IssueInputBuilder("GQAT", 12345L, summary).build();
			System.out.println("\tCreating: " + summary);
			promises.add(issueClient.createIssue(newIssue));
			
			System.out.println("Collecting responses...");
			final Iterable<BasicIssue> createdIssues = transform(promises, new Function<Promise<BasicIssue>, BasicIssue>() {
				@Override
				public BasicIssue apply(Promise<BasicIssue> promise) {
					return promise.claim();
				}
			});
			
			System.out.println("Created issues:\n" + Joiner.on("\n").join(createdIssues));

	    }
	    catch (Exception e) {
	    	e.getLocalizedMessage();
	    }
	    finally {
	    	client.close();
	    }
	    
	    
    
    }
}
