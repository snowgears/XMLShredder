package com.tannerembry.xmlshredder.exporter;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;

public class SpreadsheetTab {

	private XSSFSheet sheet;
	private List<String> columnHeaders;
	
	public SpreadsheetTab(XSSFSheet sheet, List<String> columnHeaders){
		this.sheet = sheet;
		this.columnHeaders = columnHeaders;
	}
	
	public XSSFSheet getSheet(){
		return sheet;
	}
	
	public List<String> getColumnHeaders(){
		return columnHeaders;
	}
}
