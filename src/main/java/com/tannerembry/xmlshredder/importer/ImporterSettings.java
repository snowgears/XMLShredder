package com.tannerembry.xmlshredder.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This represents all of the information in the configuration file
 * including database information and xpath/database mapping information.
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class ImporterSettings {

	private boolean printQueries;
	private boolean upload;

	private boolean exportSpreadsheet;
	private String exportSpreadsheetPath;

	private String dbUsername;
	private String dbPassword;
	private String dbHost;

	private HashMap<String, ArrayList<ImportInstruction>> importInstructionMap; //these are all parent entries. Some contain children entries (key is xPath)

	/**
	 * Constructor for the ImporterSettings object
	 * @param configFile The path to the configuration file needed to run the program
	 */
	public ImporterSettings(String configFile){
		try{
			initSettings(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the username used to access the database
	 * @return dbUsername
	 */
	public String getDataBaseUsername(){
		return dbUsername;
	}

	/**
	 * Returns the password used to access the database
	 * @return dbPassword
	 */
	public String getDatabasePassword(){
		return dbPassword;
	}

	/**
	 * Returns the host string used to access the database
	 * @return dbHost
	 */
	public String getDatabaseHost(){
		return dbHost;
	}

	/**
	 * Returns whether or not to print queries to console
	 * @return printQueries
	 */
	public boolean printQueries(){
		return printQueries;
	}

	/**
	 * Returns the whether or not to actually try to upload to the database
	 * @return upload
	 */
	public boolean upload(){
		return upload;
	}

	/**
	 * Returns the whether or not to export shredded values into a spreadsheet
	 * @return exportSpreadsheet
	 */
	public boolean exportSpreadsheet(){
		return exportSpreadsheet;
	}

	/**
	 * Returns the file path to the exported spreadsheet (if applicable)
	 * @return exportSpreadsheetPath
	 */
	public String getExportSpreadsheetPath(){
		if(!exportSpreadsheet)
			return null;
		return exportSpreadsheetPath;
	}

	/**
	 * Returns the map of import instructions
	 * @return importInstructionMap
	 */
	public HashMap<String, ArrayList<ImportInstruction>> getImportInstructionMap(){
		return importInstructionMap;
	}

	/**
	 * Initializes all of the database information and instruction map information from the provided configuration file
	 * @param configFile The path to the configuration file
	 */
	private void initSettings(String configFile){
		Document document = this.createDocument(configFile);

		// normalize text representation
		document.getDocumentElement().normalize();

		NodeList ssInfo = document.getElementsByTagName("spreadsheet");
		for (int d = 0; d < ssInfo.getLength(); d++) {

			Node ssNode = ssInfo.item(d);
			if (ssNode.getNodeType() == Node.ELEMENT_NODE) {

				Element ssElement = (Element) ssNode;

				this.exportSpreadsheet = Boolean.parseBoolean(ssElement.getElementsByTagName("create").item(0).getTextContent());
				this.exportSpreadsheetPath = ssElement.getElementsByTagName("file").item(0).getTextContent();
			}
		}

		NodeList databaseInfo = document.getElementsByTagName("connection");
		for (int d = 0; d < databaseInfo.getLength(); d++) {

			Node databaseNode = databaseInfo.item(d);
			if (databaseNode.getNodeType() == Node.ELEMENT_NODE) {

				Element databaseElement = (Element) databaseNode;

				this.dbUsername = databaseElement.getElementsByTagName("username").item(0).getTextContent();
				this.dbPassword = databaseElement.getElementsByTagName("password").item(0).getTextContent();
				this.dbHost = databaseElement.getElementsByTagName("host").item(0).getTextContent();
			}
		}

		importInstructionMap = new HashMap<String, ArrayList<ImportInstruction>>();

		NodeList mapNodeList = document.getElementsByTagName("mapping");
		for (int d = 0; d < mapNodeList.getLength(); d++) {

			Node mapNode = mapNodeList.item(d);
			if (mapNode.getNodeType() == Node.ELEMENT_NODE) {

				Element mapElement = (Element) mapNode;

				String xPath = mapElement.getElementsByTagName("xpath").item(0).getTextContent();
				String xAttribute = mapElement.getElementsByTagName("xattribute").item(0).getTextContent();
				String dbTable = mapElement.getElementsByTagName("dbtable").item(0).getTextContent();
				String dbField = mapElement.getElementsByTagName("dbfield").item(0).getTextContent();

				ImportInstruction iEntry = new ImportInstruction(xPath, xAttribute, dbTable, dbField);

				NodeList childMapNodeList = mapElement.getElementsByTagName("child_mapping");
				if(childMapNodeList != null){

					for (int f = 0; f < childMapNodeList.getLength(); f++) {

						Node childMapNode = childMapNodeList.item(f);
						if (childMapNode.getNodeType() == Node.ELEMENT_NODE) {

							Element childMapElement = (Element) childMapNode;

							String xChildPath = childMapElement.getElementsByTagName("xpath").item(0).getTextContent();
							String xChildAttribute = null;
							try{
								xChildAttribute = childMapElement.getElementsByTagName("xattribute").item(0).getTextContent();
							} catch (NullPointerException e){ 
								//xAttribute not required
							}
							String dbChildTable = childMapElement.getElementsByTagName("dbtable").item(0).getTextContent();
							String dbChildField = childMapElement.getElementsByTagName("dbfield").item(0).getTextContent();

							ImportInstruction iChildEntry = new ImportInstruction(xChildPath, xChildAttribute, dbChildTable, dbChildField);
							iChildEntry.setParent(iEntry);


							this.putInMap(xChildPath, iChildEntry);
						}
					}
				}
				this.putInMap(xPath, iEntry);
			}
		}
	}

	/**
	 * Puts the collected instruction into the map under the xPath key
	 * @return dbUsername
	 */
	private void putInMap(String xPath, ImportInstruction instruction){
		String xPathElement;
		if(xPath.contains(".")){
			xPathElement = xPath.substring(xPath.lastIndexOf(".")+1);
		}
		else{
			xPathElement = xPath;
		}
		ArrayList<ImportInstruction> instructions = importInstructionMap.get(xPathElement);
		if(instructions == null)
			instructions = new ArrayList<>();
		instructions.add(instruction);
		importInstructionMap.put(xPathElement, instructions);
	}

	/**
	 * This method is used to get an xml document type from a provided file path.
	 * @param path The path to the xml file
	 * @return the parsed xml document
	 */
	private Document createDocument(String path) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document ret = null;

		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			ret = builder.parse(path);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
