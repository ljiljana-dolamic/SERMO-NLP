/**
 * 
 */
package ch.unine.ILCF.SERMO.LGeRM;

/**
 * @author dolamicl
 *
 */
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.*;

import javax.swing.JFileChooser;

import java.io.IOException;

import ch.unine.ILCF.SERMO.TranscriptionHandler;
import ch.unine.ILCF.SERMO.LGeRM.GraphicsWordInfo;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.XML.Utils.TeiHeaderData;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

public class GetInfoFromGraphicsPresto {

	private  LinkedList<GraphicsWordInfo> wordList;
	 private Properties prop;
	/**
//	 * @param args
	 * 
	 */
	
	public GetInfoFromGraphicsPresto(String propertiesFileName){
		this.prop = SermoProperties.getProperties(propertiesFileName);
		
	}
    private LinkedList<GraphicsWordInfo> loadWordList(File dictionary){
    	 //System.out.println("Loading dictionary...");
    	LinkedList<GraphicsWordInfo> tmpList = new LinkedList<GraphicsWordInfo>();
    	
    	 try{
    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dictionary.getAbsolutePath()),"ISO-8859-1");
    	 BufferedReader br = new BufferedReader(isr);
    	 String line=null;
         while ( (line = br.readLine()) != null)
         {
        	String [] parts = line.split(";");
        	System.out.println(parts[0]+"\t"+parts[2]+"\t"+parts[1]);
//        	String modern = getModern(parts[0],parts[3]);
//        	
//        	if(!modern.equals("")){
//        		GraphicsWordInfo graphicInfo= new GraphicsWordInfo(parts[0],parts[2],parts[1],modern); // token,pos,lemma,modern
//        		tmpList.add(graphicInfo);
//        	}
         }
      
         br.close();
         
        
    	 }catch(IOException e){
    		 System.out.println("Problem loading file");
    		 e.printStackTrace();
    	 }
    	 return tmpList;
     }
     
    public static String getModern(String token,String toSplit){
    	String [] split = toSplit.split("@");
    	
    	
    	if(split.length==1){
    		return "";
    	}else {
    		if(split[0].equals("OUTILS")||split[0].equals("s")||token.equals(split[1])||split[0].equals("LEMME_ABSENT")){
    			return "";
    		}else{
    			return split[1];
    		}
    		
    	}
    }
    
    /**
	 * creates data base record for the transcription
	 **/
	private HashMap<String,String> createGraphicInfo(GraphicsWordInfo graphic){
		HashMap <String, String> tmp = new HashMap<String, String>();
		
		
		
		tmp.put("graphic",graphic.getToken());
		tmp.put("lemma", graphic.getLemma());
		tmp.put("pos", graphic.getPos());
		tmp.put("modern", graphic.getModern());
		
		
		
		return tmp;
		
	}
	/**
	 *- writes word info to database
	 * Database info passed by a property file 
	 **/
	
	@SuppressWarnings("finally")
	private void writeWordInfoDataToDB(LinkedList<GraphicsWordInfo> wordList){
		
		try{
			
		
		Connection connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
				this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
		for(GraphicsWordInfo word:wordList){
			HashMap <String, String> mapToWrite = createGraphicInfo(word);
		
			MySQLconnection.insertHashRecordIntoTable(connection, "word_modern", mapToWrite);
		}
		}catch(SQLException eSQL){
			eSQL.getMessage();
		}finally{
			
		}
		
	}
	
	public static void main(String[] args) {
		 GetInfoFromGraphicsPresto gIGP = new  GetInfoFromGraphicsPresto("lilyann.properties");
		
			try{
			
			JFileChooser window= new JFileChooser();
			
			int rv= window.showOpenDialog(null);
			
			if(rv == JFileChooser.APPROVE_OPTION){
				
				File window_file = window.getSelectedFile();
				
				LinkedList<GraphicsWordInfo> wordList = gIGP.loadWordList( window_file);
				//gIGP.writeWordInfoDataToDB(wordList);
			}
			}catch(Exception e){
				System.out.println(e.getMessage());
				//e.getMessage();
			}
	

	}

}
