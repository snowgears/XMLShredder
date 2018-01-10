package com.tannerembry.xmlshredder.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tannerembry.xmlshredder.exporter.SpreadsheetExporter;

/**
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class ImportEntry {

	private String dbTable;
	private String dbFieldKey;
	private String dbFieldValue;

	private List<ImportEntry> children;

	/**
	 * Constructor for the ImportEntry object
	 * @param dbTable The table in the database this entry will be inserted into
	 * @param dbFieldKey The field in the table this entry will be inserted into
	 * @param dbFieldValue The value to be inserted into the field
	 */
	public ImportEntry(String dbTable, String dbFieldKey, String dbFieldValue){
		this.dbTable = dbTable;
		this.dbFieldKey = dbFieldKey;
		this.dbFieldValue = dbFieldValue;
	}

	/**
	 * Returns the name of the database table 
	 * @return dbTable
	 */
	public String getTable(){
		return dbTable;
	}

	/**
	 * Returns the name of the database field
	 * @return dbFieldKey
	 */
	public String getKey(){
		return dbFieldKey;
	}

	/**
	 * Returns the value to be inserted in the database field
	 * @return dbFieldValue
	 */
	public String getValue(){
		return dbFieldValue;
	}

	/**
	 * Adds a child ImportEntry to the current parent (this)
	 * @param entry The child ImportEntry to be added
	 */
	public void addChild(ImportEntry entry){
		if(children == null){
			children = new ArrayList<ImportEntry>();
		}
		//if(!hasChild(entry))
			children.add(entry);
	}

	/**
	 * Checks to see if there is a child with fields matching the provided ImportEntry
	 * @param entry The child ImportEntry to be checked
	 * @return true - this has a child that matches the provided ImportEntry
	 * 		 false - this does not have a child that matches the provided ImportEntry
	 */
	public boolean hasChild(ImportEntry entry){
		if(children == null || children.isEmpty())
			return false;

		for(ImportEntry e : children){
			if(e.getKey().equals(entry.getKey()))
				return true;
		}
		return false;
	}
	
	public void clearChildren(){
		this.children.clear();
	}

	/**
	 * Checks to see if there is this ImportEntry has any child ImportEntries
	 * @param entry The child ImportEntry to be checked
	 * @return true - this has a child that matches the provided ImportEntry
	 * 		 false - this does not have a child that matches the provided ImportEntry
	 */
	public boolean isParent(){
		return (this.children == null || this.children.isEmpty());
	}

	/**
	 * Inserts this ImportEntry into the database (if upload=true in settings)
	 * Inserts this ImportEntry into a spreadsheet (if exportSheet=true in settings)
	 * @param con The connection to the database
	 * @param printQueries Controls whether or not the executed queries are printed to the console
	 * @return true - the database insertion was successful
	 * 		 false - the database insertion failed
	 */
	public boolean export(Connection con, ImportInstruction instruction, SpreadsheetExporter exportSheet, ImporterSettings importerSettings){

		if(this.children == null || this.children.isEmpty())
			return false;

		//first need to check if you are doing an insert or an update in the database
		boolean doInsert = true;

		if(importerSettings.upload()){
			try {
				String checkQuery = "select "+dbFieldKey+" from "+dbTable+" where "+dbFieldKey+"='"+dbFieldValue+"'";
				PreparedStatement stat = con.prepareStatement(checkQuery);

				ResultSet rs = stat.executeQuery();

				//if there already exists an entry with this value, do an update instead of an insert
				if(rs.next()){
					doInsert = false;
				}

				rs.close();
				stat.close();

			} catch (SQLException e){
				e.printStackTrace();
				return false;
			}
		}

		String insertColumnNames = "("+dbFieldKey+", ";
		String insertColumnValues = "('"+dbFieldValue+"', ";
		String updateColumns = "";
		
		List<String> columnNames = new ArrayList<>();
		List<String> columnValues = new ArrayList<>();
		columnNames.add(dbFieldKey);
		columnValues.add(dbFieldValue);

		int i=0;
		for(ImportEntry child : this.children){
			String value = child.getValue();
			String key = child.getKey();
			
			columnNames.add(key);
			columnValues.add(value);

			insertColumnNames += key;

			if(value != null){
				updateColumns += key+"='"+value+"'";
				insertColumnValues += "'"+value+"'";
			}
			else{
				updateColumns += key+"="+value;
				insertColumnValues += value;
			}

			if(i == this.children.size() - 1){
				insertColumnNames += ")";
				insertColumnValues += ")";
			}
			else{
				insertColumnNames += ", ";
				insertColumnValues += ", ";
				updateColumns += ", ";
			}
			i++;
		}

		try {
			String insertFilterQuery;
			if(doInsert){
				insertFilterQuery = "insert into "+dbTable+" "+ insertColumnNames +" values "+insertColumnValues;
			}
			else{
				insertFilterQuery = "update "+dbTable+" set "+updateColumns+" where "+dbFieldKey+"='"+dbFieldValue+"'";
			}

			if(importerSettings.printQueries()){
				System.out.println(insertColumnNames+" - "+insertColumnValues);
			}

			if(importerSettings.upload()){
				PreparedStatement psInsertLog = con.prepareStatement(insertFilterQuery);

				psInsertLog.executeUpdate();
				if (psInsertLog != null)
					psInsertLog.close();
			}
			
			if(importerSettings.exportSpreadsheet() && exportSheet != null){
				exportSheet.insertValues(instruction, columnNames, columnValues);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		this.children.clear();
		return true;
	}

	@Override
	public String toString(){
		String toString = "importEntry[table="+dbTable+", field="+dbFieldKey+", value="+dbFieldValue+"]";
		if(this.children != null && !this.children.isEmpty()){
			toString += "\nChildren:";
			for(ImportEntry child : this.children){
				toString += "\n\t"+child.toString();
			}
		}
		return toString;
	}

}