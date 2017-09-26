import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class manages all of the ImportInstructions that were
 * collected from the configuration file
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class ImportInstructionManager {

	private HashMap<String, ArrayList<ImportInstruction>> instructionMap;

	/**
	 * Constructor for the ImporterInstructionManager object
	 * @param instructionMap The instructionMap that was built when initializing ImporterSettings
	 */
	public ImportInstructionManager(HashMap<String, ArrayList<ImportInstruction>> instructionMap){
		this.instructionMap = instructionMap;
	}

	/**
	 * Returns the list of ImportInstructions that matches the element and xPath 
	 * @return list of ImportInstruction objects that are a potential match
	 */
	public ArrayList<ImportInstruction> getInstructions(String element, String fullXPath){
		ArrayList<ImportInstruction> instructionList = instructionMap.get(element);
		if(instructionList == null)
			return new ArrayList<ImportInstruction>();

		ArrayList<ImportInstruction> potentialInstructions = new ArrayList<ImportInstruction>();
		for(ImportInstruction instruction : instructionList){
			if(fullXPath.contains(instruction.getXPath())){
				potentialInstructions.add(instruction);
			}
		}

		//sort potential instructions with parents first
		Collections.sort(potentialInstructions);

		return potentialInstructions;
	}

	/**
	 * Returns a list of all ImportInstruction objects that the manager has stored
	 * @return a list of all ImportInstructions
	 */
	public ArrayList<ImportInstruction> getAllInstructions(){
		ArrayList<ImportInstruction> allInstructions = new ArrayList<>();

		for(ArrayList<ImportInstruction> instructions : instructionMap.values()){
			allInstructions.addAll(instructions);
		}

		//sort potential instructions with parents first
		Collections.sort(allInstructions);

		return allInstructions;
	}

	/**
	 * Verifies that all of the instructions collected have existing tables and fields in the provided database
	 * @throws SQLException if a table or field does not exist in the database
	 */
	public void verifyInstructions(Connection connection) throws SQLException {
		ArrayList<ImportInstruction> instructions = this.getAllInstructions();

		PreparedStatement stat;
		ResultSet rs;

		for(ImportInstruction instruction : instructions){
			stat = connection.prepareStatement(instruction.getVerificationQuery());
			rs = stat.executeQuery();

			rs.close();
			stat.close();
		}
	}

}
