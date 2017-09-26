import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * This class represents the actual task that is called to shred the
 * data from the xml data file into the relational database.
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class ImporterTask {

	private String XML_FILE;
	private String CONFIG_FILE;

	private ImporterSettings importerSettings;
	private ImportInstructionManager importInstructionManager;
	private Connection connection;

	/**
	 * Constructor for the ImporterSettings object
	 * @param xmlFile The path to the xml file containing the data to be shredded
	 * @param configFile The path to the configuration file needed to run the program
	 * @param printQueries Controls whether or not the executed queries are printed to the console
	 */
	public ImporterTask(String xmlFile, String configFile, boolean printQueries){
		XML_FILE = xmlFile;
		CONFIG_FILE = configFile;

		initFromConfig();

		try {
			importInstructionManager.verifyInstructions(connection);
			run(printQueries);
		} catch(SQLException e){
			System.out.println("The configuration file provided is not mapped correctly to the fields in the database.");
			e.printStackTrace();

			this.closeConnection();
			return;
		}
	}

	/**
	 * Runs the actual task of shredding the data from the data file into the relational database
	 * @param printQueries Controls whether or not the executed queries are printed to the console
	 */
	private void run (boolean printQueries) {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			SAXImportHandler handler = new SAXImportHandler(importInstructionManager, connection, printQueries);
			saxParser.parse(XML_FILE, handler);

			handler.processFinalEntries();

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} finally {
			this.closeConnection();
		}
	}

	/**
	 * Initializes the importer settings, instruction manager, and the actual database connection
	 */
	private void initFromConfig(){
		try {
			Class.forName("org.postgresql.Driver");
			importerSettings = new ImporterSettings(CONFIG_FILE);
			importInstructionManager = new ImportInstructionManager(importerSettings.getImportInstructionMap());
			connection = DriverManager.getConnection(importerSettings.getDatabaseHost(), importerSettings.getDataBaseUsername(), importerSettings.getDatabasePassword());
		} catch (SQLException | ClassNotFoundException e){
			e.printStackTrace();
			this.closeConnection();
		}
	}

	/**
	 * Closes the connection to the database, if it exists
	 */
	private void closeConnection(){
		if(connection != null)
			try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
	}
}
