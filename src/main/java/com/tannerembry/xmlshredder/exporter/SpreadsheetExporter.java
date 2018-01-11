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

import com.tannerembry.xmlshredder.importer.ImportInstruction;

/**
 * This class is stores data from the importer as it runs and then
 * exports it to a spreadsheet once the importer finishes.
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class SpreadsheetExporter {

	private HashMap<String, SpreadsheetTab> tabs = new HashMap<>();
	private XSSFWorkbook wb;
	private String exportPath;

	public SpreadsheetExporter(String exportPath){
		this.exportPath = exportPath;

		this.wb = new XSSFWorkbook();
	}

	public void insertValues(ImportInstruction instruction, List<String> columns, List<String> values){
		if(instruction == null)
			return;

		String key;
		if(instruction.getParent() == null)
			key = instruction.toString();
		else
			key = instruction.getParent().toString();

		if(tabs.containsKey(key)){
			SpreadsheetTab tab = tabs.get(key);
			XSSFSheet sheet = tab.getSheet();

			XSSFRow row = sheet.createRow(sheet.getLastRowNum()+1);

			for(int i = 0; i < tab.getColumnHeaders().size(); i++){
				String headerColumn = tab.getColumnHeaders().get(i);

				int index = columns.indexOf(headerColumn);
				if(index != -1){
					XSSFCell cell = row.createCell(i);
					cell.setCellValue(values.get(index));
				}
				else{
					XSSFCell cell = row.createCell(i);
					cell.setCellValue("");
				}
			}
		}
		else{
			//create a new tab and put in tabColumns map
			String tabTitle = "Sheet" + (tabs.size() + 1);
			tabs.put(key, new SpreadsheetTab(tabTitle, wb, columns));

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
}
