package com.tannerembry.xmlshredder.importer;

import java.sql.Connection;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tannerembry.xmlshredder.exporter.SpreadsheetExporter;

/**
 * This class handles the actual shredding of the data from the xml file
 * to the relational database. It is initialized from within the ImporterTask class
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class SAXImportHandler extends DefaultHandler {

	private ImportInstructionManager importInstructionManager;
	private Connection connection;

	private ArrayList<ImportInstruction> importInstructions;
	private ImportInstruction importInstruction;

	private HashMap<String, ImportEntry> parentEntries;

	private boolean readValue = false;

	private String fullPath = "";
	private int depth = 0;
	private ImporterSettings importerSettings;
	private SpreadsheetExporter exportSheet;

	/**
	 * Constructor for the SAXImportHandler object
	 * @param importInstructionManager The instruction manager that the handler will be referring to
	 * @param connection The active connection to the database
	 * @param printQueries Controls whether or not the executed queries are printed to the console
	 */
	public SAXImportHandler(ImportInstructionManager importInstructionManager, Connection connection, ImporterSettings importerSettings){
		this.importInstructionManager = importInstructionManager;
		this.connection = connection;
		this.importerSettings = importerSettings;

		parentEntries = new HashMap<String, ImportEntry>();

		if(importerSettings.exportSpreadsheet())
			exportSheet = new SpreadsheetExporter(importerSettings.getExportSpreadsheetPath());
	}

	/**
	 * This is called when the DefaultHandler encounters a new element in the xml file.
	 * The element will be checked against any import instructions and if one is a match
	 * it will be inserted into the parentEntries map as either a parent or a child.
	 */
	@Override
	public void startElement(String uri, String localName,String qName,
			Attributes attributes) throws SAXException {

		depth++;
		if(fullPath.isEmpty())
			fullPath = qName;
		else
			fullPath += "."+qName;

		importInstruction = null;

		importInstructions = importInstructionManager.getInstructions(qName, fullPath);

		for(ImportInstruction importInstruction : importInstructions){
			this.importInstruction = importInstruction;
			String value = null;
			if(importInstruction.getXAttribute() != null && !importInstruction.getXAttribute().isEmpty()){
				value = attributes.getValue(importInstruction.getXAttribute());

				handleValue(value);
			}
			else{
				//read value using the SAX characters() method which will be called below when this value is set
				readValue = true;
			}
		}
	}

	/**
	 * This is called when the DefaultHandler reaches the end of an element in the xml file. 
	 * The full path of the current location and the current depth will be updated.
	 */
	@Override
	public void endElement(String uri, String localName,
			String qName) throws SAXException {

		if(fullPath.contains("."))
			fullPath = fullPath.substring(0, fullPath.lastIndexOf("."));
		depth--;
	}

	/**
	 * This is called when the DefaultHandler attempts to read the string contained within an xml tag section. 
	 * If xAttributes of the instruction was null or empty, this method will read the value of the current element.
	 */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

		if (readValue) {
			String value = new String(ch, start, length);
			handleValue(value);
		}
	}

	/**
	 * This method handles the value that was collected in regards to the current instruction
	 * An ImportEntry will be created from the value and current instruction and stored somewhere
	 * in the parentEntries map. The entry will either be inserted into the database and overwritten
	 * or stored until it is ready to be inserted in to the database.
	 */
	private void handleValue(String value){

		if(value != null){

			ImportEntry entry = new ImportEntry(importInstruction.getTable(), importInstruction.getField(), value);

			String hashKey = importInstruction.getHashKey();

			ImportEntry parentEntry = parentEntries.get(hashKey);
			if(parentEntry == null){
				parentEntry = entry;
				parentEntries.put(hashKey, parentEntry); 
			}

			//the entry is a parent and is different than the previous parent
			if(importInstruction.getParent() == null && !parentEntry.equals(entry)){
				//insert the entry and update the current parent
				parentEntry.export(connection, importInstruction, exportSheet, importerSettings);
				parentEntry = entry;
				parentEntries.put(hashKey, parentEntry);
			}
			//the entry is a child
			else if(importInstruction.getParent() != null){
				if(parentEntry.hasChild(entry)){
					parentEntry.export(connection, importInstruction, exportSheet, importerSettings);
					parentEntry.clearChildren();
					handleValue(value);
				}
				else{
					parentEntry.addChild(entry);
				}
			}
		}

		readValue = false;
	}

	/**
	 * This method inserts any remaining entries in the map into the database.
	 * Since the end of the xml file may be reached with remaining entries in the map,
	 * this is called at the very end of parsing the xml file from ImporterTask.
	 */
	public void processFinalEntries(){
		for(ImportEntry parent : parentEntries.values()){
			parent.export(connection, importInstruction, exportSheet, importerSettings);
		}
		if(importerSettings.exportSpreadsheet())
			exportSheet.export();
	}
}
