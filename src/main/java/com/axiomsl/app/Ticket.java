package com.axiomsl.app;

public class Ticket {
	
	private String vID;
	private String form;
	private int issueNum;
	private String jKey;
	private String csDesc;
	private String csImpact;
	private char csType;
	private String cdDetails;
	private String cdTab;
	private int rowNum;
	private String branch;
	private String qa_Actual_Results;
	

	public Ticket(String vID, String form, int issueNum, String jKey, String csDesc, String csImpact, char csType, String cdDetails, String cdTab, int rowNum
				, String branch, String qa_Actual_Results) {
		super();
		this.vID = vID;
		this.form = form;
		this.issueNum = issueNum;
		this.jKey = jKey;
		this.csDesc = csDesc;
		this.csImpact = csImpact;
		this.csType = csType;
		this.cdDetails = cdDetails;
		this.cdTab = cdTab;
		this.rowNum = rowNum;
		this.branch = branch;
		this.qa_Actual_Results = qa_Actual_Results;
	}

	public String getVID(){
		return vID;
	}
	
	public String getForm(){
		return form;
	}
	
	public int getIssueNum(){
		return issueNum;
	}
	
	public String getJKey(){
		return jKey;
	}
	
	public String getCsDesc(){
		return csDesc;
	}
	
	public String getCsImpact(){
		return csImpact;
	}
	
	public char getCsType(){
		return csType;
	}
	
	public String getCdDetails(){
		return cdDetails;
	}
	
	public String getCdTab(){
		return cdTab;
	}
	
	public int getRow(){
		return rowNum;
	}
	
	public String getBranch(){
		return branch;
	}
	
	public String getQAActual(){
		return qa_Actual_Results;
	}
	
	public void setRow(int row){
		this.rowNum = row;
	}
	
	public String getXForm(){
		if (csImpact.contains("F"))
			return "X";
		else
			return "";
	}
	
	public String getXEC(){
		if (csImpact.contains("E"))
			return "X";
		else
			return "";
	}
	
	public String getXCos(){
		if (csImpact.contains("C"))
			return "X";
		else
			return "";
	}
	
	public String getXOth(){
		if (csImpact.contains("O"))
			return "X";
		else
			return "";
	}
	
	public String getXReg(){
		if (csType == 'R')
			return "X";
		else
			return "";
	}
	
	public String getXFix(){
		if (csType == 'F')
			return "X";
		else
			return "";
	}
	
	public String getXEnh(){
		if (csType == 'E')
			return "X";
		else
			return "";
	}
	
	public void setIssueNum(int i){
		issueNum = i;
	}
	
}
	