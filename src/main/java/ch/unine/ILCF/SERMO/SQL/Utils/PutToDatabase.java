/**
 * 
 */
package ch.unine.ILCF.SERMO.SQL.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;

/**
 * @author dolamicl
 *
 */
public class PutToDatabase {
	 /**
	  * 
	  * @param result
	  * @param connection
	  */
	
	public static void addTagToDb(HashMap<String, String> mapToWriteInput, Connection connection){
		HashMap<String, String> mapToWrite=mapToWriteInput;
		
		//get the last tag for the document from the database
		int tag_id = GetFromDatabase.getNextTagId(connection,mapToWrite.get("doc_id"));
		mapToWrite.put("tag_id", Integer.toString(tag_id));
		PutToDatabase.writeHashToDB(mapToWrite, "tag_body", connection);
		
	}
	
	/**
	 *- writes data into the database
	 * Database info passed by a property file 
	 **/

	public static void writeHashToDB(HashMap <String, String>  mapToWrite,String table,Connection connection ){

		try{
			MySQLconnection.insertHashRecordIntoTable(connection, table, mapToWrite);
		}catch(SQLException eSQL){
			eSQL.getMessage();
		}

	}
	
	
	/**
	 * 
	 * @param connection
	 * @param graphie
	 * @param pos
	 * @param lemma
	 * @param modern
	 */
	
	public static void writeModernCache(Connection connection, String graphie,String pos,String lemma, String modern ){
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("INSERT INTO modern_cache (word, pos, lemma, modern) values(?,?,?,?) "); 
try {
			
			preparedStatement = connection.prepareStatement(sql.toString());
			
		
			 preparedStatement.setString(1, graphie);
			 preparedStatement.setString(2, pos);
			 preparedStatement.setString(3, lemma);
			 preparedStatement.setString(4, modern);
			

			int affectedRows = preparedStatement.executeUpdate();
			
		} catch (SQLException e) {

			System.out.println(e.getMessage());
			try{
				connection.rollback();
				}catch(SQLException e2){
					
				}

		} finally {

			if (preparedStatement != null) {
				try{
				preparedStatement.close();
				}catch(SQLException e3){
					
				}
			}
			
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
