import java.io.File;

/**
 * The XML Shredder program takes a data file with the extension .xml and pulls
 * the configured xpath mappings out (from the provided configuration file). It
 * then inserts/updates these entries into the provided relational database.
 *  
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class Runner {

	protected static String XML_FILE;
	protected static String CONFIG_FILE;

	protected static boolean printQueries = true;

	/**
	 * This method runs the main program.
	 * @param args[0] The path to the xml file containing the data to be shredded
	 * @param args[1] The path to the config file containing the mapping and database information
	 * @return void
	 */
	public static void main (String[] args){
		if(args.length != 2){
			System.out.println("There must be two arguments provided: the XML_FILE to shred and the CONFIG_FILE.");
			return;
		}

		XML_FILE = args[0];
		CONFIG_FILE = args[1];

		File xmlFile = new File(XML_FILE);
		if(!xmlFile.exists()){
			System.out.println("The XML_FILE provided does not exist.");
			return;
		}

		File configFile = new File(CONFIG_FILE);
		if(!configFile.exists()){
			System.out.println("The CONFIG_FILE provided does not exist.");
			return;
		}

		ImporterTask task = new ImporterTask(XML_FILE, CONFIG_FILE, printQueries);
	}
}
