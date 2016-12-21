package com.axiomsl.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//Creates an arraylist of Tickets 
public class Project {
	// private Ticket ticket;
	private static List<Ticket> tickets = new ArrayList<Ticket>();

	public void addTicket(Ticket ticket) {
		Project.tickets.add(ticket);
	}

	public List<Ticket> getTickets() {
		return Project.tickets;
	}

	public void printTickets() {

		for (Ticket t : tickets) {
			System.out.println(t.getForm() + " | " + t.getIssueNum() + " | " + t.getJKey() + " | " + t.getCsDesc()
					+ " | " + t.getCsImpact() + " | " + t.getCsType() + " | " + t.getCdTab() + " | " + t.getCdTab());
		}
	}

	public void sortTickets() {
		Collections.sort(tickets, new Comparator<Ticket>() {

			@Override
			public int compare(Ticket t1, Ticket t2) {
				Integer issue1 = t1.getIssueNum();
				Integer issue2 = t2.getIssueNum();

				return issue1.compareTo(issue2);
			}

		});

	}

	public void clearTickets() {

		Project.tickets.clear();

	}

}
