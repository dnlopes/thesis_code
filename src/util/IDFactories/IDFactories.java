package util.IDFactories;

import java.util.HashMap;

import util.ExitCode;

// TODO: Auto-generated Javadoc
/**
 * The Class IDFactories.
 */
public class IDFactories {

	/** The ID generator list. */
	HashMap<String, HashMap<String, IDGenerator>> IDGeneratorList;

	/**
	 * Instantiates a new iD factories.
	 */
	public IDFactories() {
		IDGeneratorList = new HashMap<String, HashMap<String, IDGenerator>>();
	}

	/**
	 * Instantiates a new iD factories.
	 *
	 * @param IDL the idl
	 */
	IDFactories(HashMap<String, HashMap<String, IDGenerator>> IDL) {
		IDGeneratorList = IDL;
	}

	/**
	 * Add_ i d_ generator.
	 *
	 * @param tableName the table name
	 * @param dataFieldName the data field name
	 */
	public synchronized void add_ID_Generator(String tableName, String dataFieldName) {
		if (IDGeneratorList == null) {
			try {
				throw new RuntimeException("GeneratorList is not initilized");
			} catch (RuntimeException e) {
				System.exit(ExitCode.NOINITIALIZATION);
			}
		}

		if (!IDGeneratorList.containsKey(tableName)) {
			HashMap<String, IDGenerator> IDM = new HashMap<>();
			IDGeneratorList.put(tableName, IDM);
		} else {
			HashMap<String, IDGenerator> IDM = IDGeneratorList.get(tableName);
			if (IDM.containsKey(dataFieldName)) {
				try {
					throw new RuntimeException("IDGenerator already exists "
							+ tableName + " " + dataFieldName);
				} catch (RuntimeException e) {
					System.exit(ExitCode.HASHMAPDUPLICATE);
				}
			}
		}

		IDGenerator iDG = new IDGenerator(tableName, dataFieldName);
		HashMap<String, IDGenerator> IDM = IDGeneratorList.get(tableName);
		IDM.put(dataFieldName, iDG);
	}

	/**
	 * Gets the _ id generator.
	 *
	 * @param tableName the table name
	 * @param dataFieldName the data field name
	 * @return the _ id generator
	 */
	private IDGenerator get_IDGenerator(String tableName, String dataFieldName) {
		if (IDGeneratorList == null) {
			try {
				throw new RuntimeException("GeneratorList is not initilized");
			} catch (RuntimeException e) {
				System.exit(ExitCode.NOINITIALIZATION);
			}
		}

		if (!IDGeneratorList.containsKey(tableName)) {
			try {
				throw new RuntimeException("IDGenerator table is not found "
						+ tableName);
			} catch (RuntimeException e) {
				System.exit(ExitCode.HASHMAPNOEXIST);
			}
		}

		if (!IDGeneratorList.get(tableName).containsKey(dataFieldName)) {
			try {
				throw new RuntimeException("IDGenerator is not found "
						+ tableName + " " + dataFieldName);
			} catch (RuntimeException e) {
				System.exit(ExitCode.HASHMAPNOEXIST);
			}
		}

		return IDGeneratorList.get(tableName).get(dataFieldName);

	}

	/**
	 * Gets the next id.
	 *
	 * @param tableName the table name
	 * @param dataFieldName the data field name
	 * @return the next id
	 */
	public int getNextId(String tableName, String dataFieldName) {
		IDGenerator iDG = this.get_IDGenerator(tableName, dataFieldName);
		int idNum = iDG.getNextId();
		//Debug.println("replace identifier for " + tableName + " " + dataFieldName + " id: " + idNum);
		return idNum;
	}

}
