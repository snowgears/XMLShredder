package com.tannerembry.xmlshredder.exporter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tannerembry.xmlshredder.importer.ImportEntry;
import com.tannerembry.xmlshredder.importer.ImportInstruction;

public class SpreadsheetExporter {

	private HashMap<String, XSSFSheet> tabColumns = new HashMap<>();
	private XSSFWorkbook wb;
	private String exportPath;

	public SpreadsheetExporter(String exportPath){
		this.exportPath = exportPath;
		
		this.wb = new XSSFWorkbook();
	}
	
	private XSSFSheet createSheet(List<String> columns){
		String sheetName = "Sheet" + (tabColumns.size() + 1);
		XSSFSheet sheet = wb.createSheet(sheetName);

		XSSFRow row = sheet.createRow(sheet.getLastRowNum());

		//iterate through columns to create the value labels on the top of the sheet
		for (int c=0;c < columns.size(); c++ )
		{
			XSSFCell cell = row.createCell(c);

			cell.setCellValue(columns.get(c));
		}
		
		XSSFRow blankRow = sheet.createRow(sheet.getLastRowNum()+1);
		
		for (int c=0;c < columns.size(); c++ )
		{
			XSSFCell cell = blankRow.createCell(c);
			cell.setCellValue("");
		}
		
		return sheet;
	}
	
	public void insertValues(ImportInstruction instruction, List<String> columns, List<String> values){
		if(instruction == null)
			return;
		
		String key;
		if(instruction.getParent() == null)
			key = instruction.toString();
		else
			key = instruction.getParent().toString();
		
		if(tabColumns.containsKey(key)){
			XSSFSheet sheet = tabColumns.get(key);
			
			XSSFRow row = sheet.createRow(sheet.getLastRowNum()+1);

			//iterating c number of columns
			for (int c=0;c < values.size(); c++) {
				XSSFCell cell = row.createCell(c);

				cell.setCellValue(values.get(c));
			}
		}
		else{
			//create a new tab and put in tabColumns map
			tabColumns.put(key, createSheet(columns));
			System.out.println(key);
			System.out.println(columns);
			//call insertValues with same arguments again
			insertValues(instruction, columns, values);
		}
	}
	
	public void export(){
		System.out.println("Exporting spreadsheet to path: "+exportPath);
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(exportPath);
			
			//write workbook to an output stream
			wb.write(fileOut);
			fileOut.flush();
			fileOut.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//TODO create a different tab for every defined parent entry

	//TODO for every passed in line (column names, values)
	//label the top row with each column in column name
	//insert values from that point on down

	//make sure to remove '' around strings if they are still there
}
