package com.tannerembry.xmlshredder.importer;

/**
 * Represents an instruction for what information to shred from the
 * xml data file and where to import it into the relational database 
 * 
 * @author      Tanner Embry, Claresco Corp <tembry@claresco.com>
 * @version     1.0         
 * @since       1.0         
 */

public class ImportInstruction implements Comparable<ImportInstruction> {

	private String xPath;
	private int xPathSegments;
	private String xAttribute;
	private String dbTable;
	private String dbField;

	private ImportInstruction parent; //this is the xPath of the parent (if it exists)
	private String hashKey;

	/**
	 * Constructor for the ImporterInstruction object
	 * @param xPath The path to the wanted information in the data file (full or partial)
	 * @param xAttribute The attribute (if it exists) of the wanted information at the specified xPath
	 * @param dbTable The table of the database to import into
	 * @param dbField The field in the table of the database to import into
	 */
	public ImportInstruction(String xPath, String xAttribute, String dbTable, String dbField){
		this.xPath = xPath;
		this.xPathSegments = xPath.split(".").length;
		this.xAttribute = xAttribute;
		this.dbTable = dbTable;
		this.dbField = dbField;

		this.generateHashKey();
	}

	/**
	 * Returns the xPath 
	 * @return xPath
	 */
	public String getXPath(){
		return xPath;
	}

	/**
	 * Returns the number of segments contained in the xPath
	 * (Since xPath can either be a partial or a full path to the data)
	 * @return xPathSegements
	 */
	public int getXPathSegments(){
		return xPathSegments;
	}

	/**
	 * Returns the xAttribute (if its exists)
	 * @return xAttribute
	 */
	public String getXAttribute(){
		return xAttribute;
	}

	/**
	 * Returns the name of the database table
	 * @return dbTable
	 */
	public String getTable(){
		return dbTable;
	}

	/**
	 * Returns the name of the field in the database
	 * @return dbField
	 */
	public String getField(){
		return dbField;
	}

	/**
	 * Returns the xPath of the parent ImportInstruction (if it exists)
	 * @return parent
	 */
	public ImportInstruction getParent(){
		return parent;
	}

	/**
	 * Sets the parent ImportInstruction to the one provided
	 * @param e The parent ImportInstruction
	 */
	public void setParent(ImportInstruction e){
		parent = e;
//		if(e.getXAttribute() != null && !e.getXAttribute().isEmpty())
//			parent+="+"+e.getXAttribute();

		this.generateHashKey();
	}

	/**
	 * Returns the hash key of the ImportInstruction (used in external mapping)
	 * @return hashKey
	 */
	public String getHashKey(){
		return hashKey;
	}

	/**
	 * Returns the query used to verify the field and table both exist in the database
	 * @return the verification query
	 */
	public String getVerificationQuery(){
		return "select "+this.dbField+" from "+this.dbTable;
	}

	/**
	 * Generates the hash key (using parent xPath and xAttribute)
	 */
	private void generateHashKey(){
		String hashKey;
		if(this.getParent() == null){
			hashKey = this.getXPath();
			if(this.getXAttribute() != null && !this.getXAttribute().isEmpty())
				hashKey += "+"+this.getXAttribute();
		}
		else
			hashKey = this.getParent().getHashKey();

		this.hashKey = hashKey;
	}

	/**
	 * Compares this ImportInstruction to a provided ImportInstruction based on parent.
	 * @return positive - this has children
	 *         zero - both have no children
	 *         negative - this has no children
	 */
	@Override
	public int compareTo(ImportInstruction e)
	{
		if(e.getParent() == null && this.getParent() == null)
			return 0;
		if(this.getParent() == null)
			return -1;
		return 1;
	}

	@Override
	public String toString(){
		return "importInstruction[xPath="+xPath+", xAttribute="+xAttribute+", dbTable="+dbTable+", dbField="+dbField+", parent="+parent+"]";
	}
}
