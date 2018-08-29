/**
 * 
 */
package ch.unine.ILCF.SERMO.SQL;

/**
 * @author dolamicl
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.mysql.jdbc.ResultSetMetaData;

public class MySQLconnection {

	/**
	 * @param args
	 */
	private static Connection connection;
	
	/**
	 * 
	 * @param host
	 * @param database
	 * @param user
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public static Connection createConnection(String host, String database, String user, String password)throws SQLException{
		final String DB_CONNECTION ="jdbc:mysql://"+host+":3306/"+database;
		Connection dbConnection =  DriverManager.getConnection(DB_CONNECTION,user,password);
		return dbConnection;
	}
	/**
	 * 
	 * @throws SQLException
	 */
	public static void closeConnection()throws SQLException{
		if (connection != null) {
		
			connection.close();
			
		}
		
	}
	/**
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	public static void closeConnection(Connection connection)throws SQLException{
		if (connection != null) {
		
			connection.close();
			
		}
		
	}
	
	
	public static void cleanTable(Connection connection,String tableName,String key, String value )throws SQLException{
		 StringBuilder sql= new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE ").append(key).append("=").append(value); 
		 
		 PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
		 preparedStatement.executeUpdate();
		 
		 if (preparedStatement != null) {
				preparedStatement.close();
			}
		
	}
	/**
	 * 
	 * @param connection
	 * @param tableName
	 * @param map
	 * @throws SQLException
	 */
	public static void insertHashRecordIntoTable(Connection connection,String tableName, HashMap<String,String> map) throws SQLException {
          StringBuilder sql= new StringBuilder("INSERT INTO ").append(tableName).append(" ("); 
		
		 PreparedStatement preparedStatement = null;
		 StringBuilder placeholders = new StringBuilder();

		 for (Iterator <String> iter = map.keySet().iterator(); iter.hasNext();) {
		     sql.append(iter.next());
		     placeholders.append("?");

		     if (iter.hasNext()) {
		         sql.append(",");
		         placeholders.append(",");
		     }
		 }

		 sql.append(") VALUES (").append(placeholders).append(")");
		// System.out.println(sql.toString());

		try {
			
			preparedStatement = connection.prepareStatement(sql.toString());
			int i = 1;

			for (String value : map.values()) {
			    preparedStatement.setObject(i++, value);
			}

			int affectedRows = preparedStatement.executeUpdate();
			
		} catch (SQLException e) {

			System.out.println(e.getMessage());
			connection.rollback();

		} finally {

			if (preparedStatement != null) {
				preparedStatement.close();
			}

			
		}

	}
	
	/**
	 * 
	 * @param connection
	 * @param statement
	 * @return
	 */
	public static LinkedList<HashMap<String, String>> readDBRecord(Connection connection, String statement){
		 LinkedList<HashMap<String, String>> resultList = new  LinkedList<HashMap<String, String>>();
		 PreparedStatement preparedStatement =null;
		 try{
			 preparedStatement =  connection.prepareStatement(statement);
			 ResultSet rs = preparedStatement.executeQuery();

				while (rs.next()) {
					HashMap<String, String> tmp = new HashMap<String, String>();
					java.sql.ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();

					// The column count starts from 1
					for (int i = 1; i <= columnCount; i++ ) {
					  String name = rsmd.getColumnName(i);
					  String value = rs.getString(name);
					  tmp.put(name, value);
					 
					}
					resultList.add(tmp);
					

				}
			 
		 }catch(SQLException e) {

				System.out.println(e.getMessage());

		}finally {

				if (preparedStatement != null) {
					try{
					preparedStatement.close();
					}catch(SQLException e){
						
					}
				}
		}
		 
		 return resultList;
		
	}
	 /**
	  * 
	  * @param args
	  */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		}
		System.out.println("MySQL JDBC Driver Registered!");
		connection = null;

		try {
			//connection = DriverManager
			//.getConnection("jdbc:mysql://localhost:3306/mydb","root", "v1707n3010D!");
			connection = createConnection("localhost","mydb","root", "v1707n3010D!");

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}
      
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
		String selectSQL = "select  token_id, final_token  from token_body where section_id = 'body_sermon'  and sub_id = '0'and par_id ='0' order by token_id;";
			
			LinkedList<HashMap<String,String>> result = readDBRecord(connection,selectSQL);
			System.out.println(result.size());
			for(HashMap<String,String> tmpHM: result){
				
				for(String key: tmpHM.keySet()){
					System.out.println(key + ": "+ tmpHM.get(key));
					
				}
			}
			

			if (connection != null) {
				try{
				connection.close();
				}catch(SQLException e){
					
				}
			}

		}
       
       
	  }

	


