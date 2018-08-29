/**
 * 
 */
package ch.unine.ILCF.SERMO.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JFileChooser;

import ch.unine.ILCF.SERMO.XMLtoDbVrtXMLw;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

/**
 * @author dolamicl
 *
 */
public class CreateModernCache {
	
	private Properties prop;
	private Connection connection;
	private String input;
	
	
	private HashMap<String,HashMap<String,HashMap<String,String>>> cache;//graphie > pos >lemme > modern
	
	public CreateModernCache(String propFile)throws Exception{
		this.prop = SermoProperties.getProperties(propFile);
		if(this.prop.containsKey("host")
				&&this.prop.containsKey("database")
				&&this.prop.containsKey("user")
				&&this.prop.containsKey("password")
				&&this.prop.containsKey("input")
				){
			
			this.input= this.prop.getProperty("input");
			
		
		try {
			
			this.connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("user"), this.prop.getProperty("password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}else{
			showUsage();
			
		}
		
	}
	
	/**
	 * 
	 */
	public void readCache(){

		this.cache = new HashMap<String,HashMap<String,HashMap<String,String>>>();
		 File inputFD = new File(this.input);
		 
		 if(inputFD.isDirectory()){
				File[] files = inputFD.listFiles();
				for(File f:files){
					readVrt(f);
				}
			}else{
				readVrt(inputFD);
			}

		 writeModernCache();
	}
	/**
	 * 
	 * 
	 * @param file
	 */
	
	public void readVrt(File file){
		
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(new FileInputStream(file.getAbsolutePath()));
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null)
			{
		//	System.out.println(line);
			String []  parts= line.split("\\t");
			if(parts.length == 4
					&&!parts[2].equals("Mo")
					&&!parts[2].equals("Mc")
					&&!parts[2].equals("Fo")
					&&!parts[2].equals("Fw")
					&&!parts[2].equals("Fs")
					&&!parts[2].equals("Np")
					){	
			//	System.out.println("YES");
				HashMap<String,HashMap<String,String>> tmpG = null;
				HashMap<String,String> tmpL =null;
				if(this.cache.containsKey(parts[0].toLowerCase())){
				//	System.out.println("OLD");
					tmpG=this.cache.get(parts[0].toLowerCase());	
					if(tmpG.containsKey(parts[2])){
				//		System.out.println("OLD2");
						tmpL = tmpG.get(parts[2]);
						if(!tmpL.containsKey(parts[3])){
							tmpL.put(parts[3], parts[1]);
							//tmpG.put(parts[2], tmpL); // pos: lemma : modern
							//this.cache.put(parts[0], tmpG); // graphie : pos: lemma : modern
						}
						
					}else{
						
						tmpL = new HashMap<String,String>();
						tmpL.put(parts[3], parts[1]); // lemma : modern
						tmpG.put(parts[2], tmpL); // pos: lemma : modern
					}
				}else{
				//	System.out.println("NEW");
					tmpG = new HashMap<String,HashMap<String,String>>();
					tmpL = new HashMap<String,String>();
					tmpL.put(parts[3], parts[1]); // lemma : modern
					tmpG.put(parts[2], tmpL); // pos: lemma : modern
					this.cache.put(parts[0].toLowerCase(), tmpG); // graphie : pos: lemma : modern
				}
			}
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		
		
	}
	
	public void writeModernCache(){
		
		for(String keyG : this.cache.keySet()){
			HashMap<String,HashMap<String,String>> tmpG = this.cache.get(keyG);
			for(String keyP : tmpG.keySet()){
				HashMap<String,String> tmpP = tmpG.get(keyP);
				for(String lemma : tmpP.keySet()){
					System.out.println("G: "+keyG+"; P: "+keyP+ "; L: "+lemma+"; M: "+tmpP.get(lemma));
					GetFromDatabase.writeModernCache(this.connection, keyG, keyP, lemma, tmpP.get(lemma));
				}
				
			}
			
		}
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		try{
           
			CreateModernCache cMC = new CreateModernCache("db.properties");
			cMC.readCache();

		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}
	}

	
	 private static void showUsage() {
		    System.err.println("\nUsage: CreateModernCache <properties file path>\n");
		    System.err.println("Properties file needs to contain following: ");
		    System.err.println("  host   database host");
		    System.err.println("  dbuser  database user");
		    System.err.println("  dbpassword   database password");
		    System.err.println("  table_name   database password");
		   
		   
		    
		    System.exit(0);
		  }
}
