package com.tannerembry.xmlshredder.exporter;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This class represents a spreadsheet tab to be exported.
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class SpreadsheetTab {

	private XSSFSheet sheet;
	private List<String> columnHeaders;

	public SpreadsheetTab(String title, XSSFWorkbook wb, List<String> columnHeaders){
		this.columnHeaders = columnHeaders;
		init(title, wb);
	}

	public XSSFSheet getSheet(){
		return sheet;
	}

	public List<String> getColumnHeaders(){
		return columnHeaders;
	}

	private void init(String title, XSSFWorkbook wb){
		XSSFSheet sheet = wb.createSheet(title);

		//iterate through columns to create the value labels on the top of the sheet
		XSSFRow row = sheet.createRow(sheet.getLastRowNum());
		for (int c=0;c < columnHeaders.size(); c++ )
		{
			XSSFCell cell = row.createCell(c);
			cell.setCellValue(columnHeaders.get(c));
		}

		//create a blank row under the header row
		XSSFRow blankRow = sheet.createRow(sheet.getLastRowNum()+1);
		for (int c=0;c < columnHeaders.size(); c++ )
		{
			XSSFCell cell = blankRow.createCell(c);
			cell.setCellValue("");
		}

		//freeze the first two rows (the header)
		sheet.createFreezePane(0, 2);

		this.sheet = sheet;
	}
}
