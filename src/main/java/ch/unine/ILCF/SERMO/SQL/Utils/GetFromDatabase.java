package ch.unine.ILCF.SERMO.SQL.Utils;


import java.sql.DriverManager;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ch.unine.ILCF.SERMO.TokenInfoFromXML;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.Utils.CharacterUtils;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.collection.TokenInfo;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFileChooser;

public class GetFromDatabase {
	
	private String host;
	private String database;
	private String user;
	private String password;
	private Properties prop;
	
	public GetFromDatabase(String propertiesFileName) {
		this.prop = SermoProperties.getProperties(propertiesFileName);
		this.host=this.prop.getProperty("host");
		this.database=this.prop.getProperty("database");
		this.user=this.prop.getProperty("user");
		this.password=this.prop.getProperty("password");
		
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the prop
	 */
	public Properties getProp() {
		return prop;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param prop the prop to set
	 */
	public void setProp(Properties prop) {
		this.prop = prop;
	}
	
/**
 * 
 * @param doc_id
 * @param par_no
 * @param connection
 * @return
 */
	public String getParagraphText(String doc_id, int par_no, Connection connection){
		LinkedList<HashMap<String,String>> result;
		StringBuilder paragraph= new StringBuilder();
		
		String selectSQL = "select  token_id,token, start_offset, end_offset from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' and  par_id ='"+par_no+"' order by token_id;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		for(HashMap<String,String> tmpHM: result){
			int start= Integer.parseInt(tmpHM.get("start_offset"));
			//int end= Integer.parseInt(tmpHM.get("end_offset"));
			if(currentOffset !=0 && currentOffset!=start){
				paragraph.append(" ");
			}
			paragraph.append(tmpHM.get("token"));
			currentOffset=Integer.parseInt(tmpHM.get("end_offset"));
		}
		return paragraph.toString();
	}
/**
 * 	
 * @param doc_id
 * @param par_no
 * @param connection
 * @return
 */
	public LinkedList<String> getSentencesText(String doc_id, int par_no, Connection connection){
		LinkedList<HashMap<String,String>> result;
		
		LinkedList<String> sentences= new LinkedList<String>();
		
		int currentSentenceNo=0;
		
		//StringBuilder paragraph= new StringBuilder();
		
		String selectSQL = "select  token_id,token, start_offset, end_offset, sent_no from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' and  par_id ='"+par_no+"' order by token_id;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		
		StringBuilder currentSentence=new StringBuilder();
		for(HashMap<String,String> tmpHM: result){
			int csn=Integer.parseInt(tmpHM.get("sent_no"));
			if(currentSentenceNo !=0 && currentSentenceNo!=csn){
				sentences.add(currentSentence.toString());
				currentSentence.setLength(0);
				currentSentenceNo=csn;
			}
			//int end= Integer.parseInt(tmpHM.get("end_offset"));
				
				int start= Integer.parseInt(tmpHM.get("start_offset"));
			if(currentOffset !=0 && currentOffset!=start){
				currentSentence.append(" ");
			}
			currentSentence.append(tmpHM.get("token"));
			currentOffset=Integer.parseInt(tmpHM.get("end_offset"));
		}
		
		return sentences;
	}
	/**
	 * 
	 * @param doc_id
	 * @param par_no
	 * @param connection
	 * @return
	 */
	public LinkedList<String> getSentencesTextPosLemma(String doc_id, int par_no, Connection connection){
		LinkedList<HashMap<String,String>> result;
		
		LinkedList<String> sentences= new LinkedList<String>();
		
		int currentSentenceNo=0;
		
		//StringBuilder paragraph= new StringBuilder();
		
		String selectSQL = "select  token_id,token, final_token, pos, lemma, start_offset, end_offset, sent_no from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' and  par_id ='"+par_no+"' order by token_id;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		
		StringBuilder currentSentence=new StringBuilder();
		for(HashMap<String,String> tmpHM: result){
			int csn=Integer.parseInt(tmpHM.get("sent_no"));
			if(currentSentenceNo !=0 && currentSentenceNo!=csn){
				sentences.add(currentSentence.toString());
				currentSentence.setLength(0);
				currentSentenceNo=csn;
			}
			//int end= Integer.parseInt(tmpHM.get("end_offset"));
				
				int start= Integer.parseInt(tmpHM.get("start_offset"));
			if(currentOffset !=0 && currentOffset!=start){
				currentSentence.append("\n");
			}
			currentSentence.append(tmpHM.get("token")).append("\t");
			currentSentence.append(tmpHM.get("final_token")).append("\t");
			currentSentence.append(tmpHM.get("pos")).append("\t");
			currentSentence.append(tmpHM.get("lemma"));
			currentOffset=Integer.parseInt(tmpHM.get("end_offset"));
		}
		
		return sentences;
	}
	/**
	 * 
	 * @param doc_id
	 * @param par_no
	 * @param connection
	 * @return
	 */
	public LinkedList<HashMap<String,String>> getSentencesInfo(String doc_id, int par_no, Connection connection){
		LinkedList<HashMap<String,String>> result;
		
		LinkedList<HashMap<String,String>> sentences= new LinkedList<HashMap<String,String>>();
		
		int currentSentenceNo=1;
		int currentSentenceStart=0; 
		boolean first=true;
		//StringBuilder paragraph= new StringBuilder();
		
		String selectSQL = "select  token_id,token, start_offset, end_offset, sent_no from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' and  par_id ='"+par_no+"' order by token_id;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		
		StringBuilder currentSentence=new StringBuilder();
		
		for(HashMap<String,String> tmpHM: result){
			if(first){
				currentSentenceStart=Integer.parseInt(tmpHM.get("start_offset"));

				first=false;
			}
			int csn=Integer.parseInt(tmpHM.get("sent_no"));
			int start= Integer.parseInt(tmpHM.get("start_offset"));
			if(currentSentenceNo !=0 && currentSentenceNo!=csn){
				HashMap<String,String> tmp= new HashMap<String,String>();
				
				tmp.put("text", currentSentence.toString());
				tmp.put("start", Integer.toString(currentSentenceStart));
				tmp.put("end", Integer.toString(currentOffset ));
				tmp.put("sent_no", Integer.toString(currentSentenceNo));
			
				sentences.add(tmp);
				
				currentSentence.setLength(0);
				currentSentenceNo=csn;
				currentSentenceStart=start;
			}
			int end= Integer.parseInt(tmpHM.get("end_offset"));
				
				
			if(currentOffset !=0 && currentOffset!=start){
				currentSentence.append(" ");
			}
			currentSentence.append(tmpHM.get("token"));
			currentOffset=Integer.parseInt(tmpHM.get("end_offset"));
			//System.out.println(currentSentence.toString());
			//System.out.println(sentences.size());
		}
		
		//if(sentences.size()==0){
			HashMap<String,String> tmp= new HashMap<String,String>();
			
			tmp.put("text", currentSentence.toString());
			tmp.put("start", Integer.toString(currentSentenceStart));
			tmp.put("end", Integer.toString(currentOffset ));
			tmp.put("sent_no", Integer.toString(currentSentenceNo));
			
			sentences.add(tmp);
		//}
		return sentences;
	}
	/**
	 * 
	 * @param doc_id
	 * @param par_no
	 * @param connection
	 * @return
	 */
	public LinkedList<HashMap<String,String>> getSentencesInfoFull(String doc_id, int par_no, Connection connection){
		LinkedList<HashMap<String,String>> result;
		
		LinkedList<HashMap<String,String>> sentences= new LinkedList<HashMap<String,String>>();
		
		int currentSentenceNo=1;
		int currentSentenceStart=0; 
		boolean first=true;
		//StringBuilder paragraph= new StringBuilder();
		
		String selectSQL = "select  token_id,token, final_token, pos, lemma, start_offset, end_offset, sent_no from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' and  par_id ='"+par_no+"' order by token_id;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		
		StringBuilder currentSentence=new StringBuilder();
		
		for(HashMap<String,String> tmpHM: result){
			if(first){
				currentSentenceStart=Integer.parseInt(tmpHM.get("start_offset"));

				first=false;
			}
			int csn=Integer.parseInt(tmpHM.get("sent_no"));
			int start= Integer.parseInt(tmpHM.get("start_offset"));
			if(currentSentenceNo !=0 && currentSentenceNo!=csn){
				HashMap<String,String> tmp= new HashMap<String,String>();
				
				tmp.put("text", currentSentence.toString());
				tmp.put("start", Integer.toString(currentSentenceStart));
				tmp.put("end", Integer.toString(currentOffset ));
				tmp.put("sent_no", Integer.toString(currentSentenceNo));
				
				// check if sentence contains italic parts
				 boolean hasItalic = sentenceHasItalic(connection, doc_id,currentSentenceStart,currentOffset);
				 if(hasItalic){
					 System.out.println(doc_id+" - "+ currentSentenceNo+" has italic");
					 tmp.put("italic", "1");
				 }else{
					 tmp.put("italic", "0");
					 System.out.println(doc_id+" - "+ currentSentenceNo+" doesn't have italic");
				 }
			
				sentences.add(tmp);
				
				currentSentence.setLength(0);
				currentSentenceNo=csn;
				currentSentenceStart=start;
			}
			int end= Integer.parseInt(tmpHM.get("end_offset"));
				
				
		
				currentSentence.append("\n");
			
			currentSentence.append(tmpHM.get("token")).append("\t");
			currentSentence.append(tmpHM.get("final_token")).append("\t");
			currentSentence.append(tmpHM.get("pos")).append("\t");
			currentSentence.append(tmpHM.get("lemma"));
			currentOffset=Integer.parseInt(tmpHM.get("end_offset"));
			
		}
		
		//if(sentences.size()==0){
		
		
		    
			HashMap<String,String> tmp= new HashMap<String,String>();
			
			tmp.put("text", currentSentence.toString());
			tmp.put("start", Integer.toString(currentSentenceStart));
			tmp.put("end", Integer.toString(currentOffset ));
			tmp.put("sent_no", Integer.toString(currentSentenceNo));
			
			// check if sentence contains italic parts- last sentence
			 boolean hasItalic = sentenceHasItalic(connection, doc_id,currentSentenceStart,currentOffset);
			 if(hasItalic){
				 System.out.println(doc_id+" - "+ currentSentenceNo+" has italic");
				 tmp.put("italic", "1");
			 }else{
				 tmp.put("italic", "0");
				 System.out.println(doc_id+" - "+ currentSentenceNo+" doesn't have italic");
			 }
			sentences.add(tmp);
		//}
		return sentences;
	}
	/**
	 *  
	 * @param doc_id
	 * @param connection
	 * @return
	 */
	public LinkedList<Integer> getParagraphs(String doc_id, Connection connection){
		LinkedList<Integer> result = new LinkedList<Integer>();
		
		
		String selectSQL = "select distinct par_id from token_body where doc_id='"+doc_id+"' and section_id = 'body_sermon' order by par_id;";
		
		LinkedList<HashMap<String,String>> tmpHM = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int currentOffset=0;
		for(HashMap<String,String> tmp: tmpHM){
			result.add(Integer.parseInt(tmp.get("par_id")));
		}
		return result;
	}
	
	
	//!!!!!!STATIC!!!!!/
  /**
   * 
   * @param connection
   * @return
   */
	public static LinkedList<String> getDocsId(Connection connection){
		LinkedList<HashMap<String,String>> result;
		LinkedList<String> docs = new LinkedList<String>();
		
		String selectSQL = "select distinct doc_id from token_body ;";
		
		result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		for(HashMap<String,String> tmpHM: result){
			docs.add(tmpHM.get("doc_id"));
		}
		return docs;
	}
	
	/**
	 * 
	 * @param connection
	 * @param doc_id
	 * @return
	 */
	public static LinkedList<String> getDistinctNotesIds(Connection connection, String doc_id){
		LinkedList<String> noteIds =  new LinkedList<String>();
       String selectSQL = "select distinct sub_id from token_body where doc_id = '"+doc_id+"' and sub_id LIKE  'note%'";
		
       LinkedList<HashMap<String,String>>result = MySQLconnection.readDBRecord(connection,selectSQL);
		
		for(HashMap<String,String> tmpHM: result){
			noteIds.add(tmpHM.get("sub_id"));
		}
		return noteIds;
		
	}
	
	/**
	 * 
	 * 
	 * @param id
	 * @param sub_id
	 * @return
	 */
	public static LinkedList<TagInfo>getSubTagsInfoList(Connection connection,String doc_id,String sub_id){
		
		LinkedList<TagInfo>tags=new LinkedList<TagInfo>();
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("select tag_name, start_offset, end_offset, attributes from tag_body where doc_id = ? and sub_id = ? and "
				+ "(tag_name IN ('body','div','p','s','head','date','hi','quote','foreign','seg','q','note') or (tag_name = 'ref' and (attributes LIKE '%type=bible%' or attributes LIKE '%type=bible%' ))) "
				+ "order by start_offset, end_offset DESC "); 
      
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
	        preparedStatement.setString(1, doc_id);
	        preparedStatement.setString(2, sub_id);
	        
	        tags= getTagsInfoList(preparedStatement, connection);

			
		} catch (SQLException e) {
			System.out.println("Problem");
			System.out.println(e.getMessage());
			try{
				connection.rollback();
			}catch(SQLException e2){
				System.out.println("rollback2");
			}

		} finally {

			if (preparedStatement != null) {
				try{
					preparedStatement.close();
				}catch(SQLException e3){
					System.out.println("rollback3");
				}
			}

		}
		return tags;
		
		
	}
	
	/**
	 * 
	 * @param id
	 * @param sub_id
	 * @return
	 */
	
	public static LinkedList<TokenInfo>getSubTokensInfoList(Connection connection, String doc_id,String sub_id){
		LinkedList<TokenInfo> tokens= new LinkedList<TokenInfo>();
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("SELECT token, modern_token, pos, lemma, start_offset, end_offset, sub_id, page_no  FROM token_body where doc_id = ? and sub_id = ? order by start_offset "); 

		try {
			preparedStatement= connection.prepareStatement(sql.toString());
			preparedStatement.setString(1, doc_id);
			preparedStatement.setString(2, sub_id);

			tokens = getTokenInfoList(preparedStatement, connection);



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
		return tokens;
	}
	
	/**
	   * 
	   * @param connection
	   * @return
	   */
		public static LinkedList<String> getDistinctSentenceNumber(Connection connection, String doc){
			LinkedList<HashMap<String,String>> result;
			LinkedList<String> sentence = new LinkedList<String>();
			
			String selectSQL = "select distinct sent_no from token_body where doc_id = '"+ doc +"' and sub_id NOT LIKE 'note%' ;";
			
			result = MySQLconnection.readDBRecord(connection,selectSQL);
			
			for(HashMap<String,String> tmpHM: result){
				sentence.add(tmpHM.get("sent_no"));
			}
			return sentence;
		}
		
		/**
		 * 
		 * @param connection
		 * @param doc
		 * @param sent
		 * @return
		 */
		
		public static HashMap<String,Integer> getDDAttributes(Connection connection,String doc_id,String sent_no){
			HashMap<String,Integer> attributes = new HashMap<String,Integer>();
			LinkedList<HashMap<String,String>> result;
			// sentence text to be used in the incise detection
			StringBuilder tmpSent= new StringBuilder();// reconstruct the sentence just by gluing the pos and lemma together like: ",_Fw"
			
			String selectSQL = "select  token, final_token, pos, lemma, start_offset, end_offset from token_body where doc_id='"+doc_id+"' and (section_id = 'body_sermon' or section_id = 'body_text') and  sent_no ='"+sent_no+"' order by start_offset;";
			
			result = MySQLconnection.readDBRecord(connection,selectSQL);
			
			int start = 0,end = 0;
			boolean first=true;
			
			String previous= "";
			boolean commaMaj= false;
			
			
			
			for(HashMap<String,String> tmpHM: result){
				if(first){
					start=Integer.parseInt(tmpHM.get("start_offset"));
					attributes.put("start_offset",start);
					first=false;
				}
				
				
				String final_token = "final_"+tmpHM.get("final_token").replaceAll("'", "£");
				
				if(attributes.containsKey(final_token)){
					attributes.put(final_token, attributes.get(final_token)+1);
				}else{
					attributes.put(final_token,1);
				}
				
				String lemma = "lemma_"+tmpHM.get("lemma");
				
				if(attributes.containsKey(lemma)){
					attributes.put(lemma, attributes.get(lemma)+1);
				}else{
					attributes.put(lemma,1);
				}
				
				String pos = "pos_"+tmpHM.get("pos");
				
				if(attributes.containsKey(pos)){
					attributes.put(pos, attributes.get(pos)+1);
				}else{
					attributes.put(pos,1);
				}
				
				// 
				tmpSent.append(tmpHM.get("pos").equals("Nc") && tmpHM.get("token").matches("\\p{Lu}.*")? "Np": tmpHM.get("lemma"));// to deal with Apôtre and others
				tmpSent.append("_").append(tmpHM.get("pos")).append(" ");
				
				if(!commaMaj && previous.equals(",") && Character.isUpperCase(tmpHM.get("token").trim().charAt(0)) && 
						!pos.equals("pos_Np") && !pos.equals("pos_Nc")){
					commaMaj=true;
				}
				previous=tmpHM.get("token");
				
			    end= Integer.parseInt(tmpHM.get("end_offset"));
			    attributes.put("end_offset",end);	
					
				
			}
			// check if the sentence has incise and add the attribute
			 attributes.put("has_ins",CharacterUtils.hasIncise(tmpSent.toString())); 
			 
			   if(commaMaj){
				   
				   attributes.put("comma_maj_mid",1); 
				   
			   }else{
				   attributes.put("comma_maj_mid",0); 
				   
			   }
			
			  if(GetFromDatabase.sentenceHasItalic(connection, doc_id, start, end)){
				  
				  attributes.put("italic", 1);
			  }else{
				  attributes.put("italic", 0);
			  }
			
			return attributes;
			
		}
	/**
	   * 
	   * @param connection
	   * @return
	   */
		public static String getDocsCQPId(Connection connection,String id){
			
			PreparedStatement preparedStatement = null;
			
			StringBuilder sql= new StringBuilder("select doc_id_cqp from sermo_cqp where doc_id= ? ");
			try {
				preparedStatement= connection.prepareStatement(sql.toString());
		        preparedStatement.setString(1,id);
		      
		        
					ResultSet rs = preparedStatement.executeQuery();
					while (rs.next()) {
						
						//java.sql.ResultSetMetaData rsmd = rs.getMetaData();
						//int columnCount = rsmd.getColumnCount();
						  String value = rs.getString("doc_id_cqp");
						
					
					return value;
					}
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
			
			
			return null;
		}
	/**
	 * 
	 * @param connection
	 * @param doc_id
	 * @param start_off
	 * @param end_off
	 * @return
	 */
	public static boolean sentenceHasItalic(Connection connection,String doc_id,int start_off,int end_off){
		boolean result = false;
		
		String selectSQL ="select count(*) as cnt from tag_body where doc_id='"+doc_id+"' and tag_name='hi' and attributes='{rend=I}' and sub_id NOT LIKE '%note%' and start_offset between "+ start_off+" and "+end_off+" ;";
		
		LinkedList<HashMap<String,String>> tmpHM = MySQLconnection.readDBRecord(connection,selectSQL);
		
		int cnt = Integer.parseInt(tmpHM.get(0).get("cnt"));
		
		result = cnt > 0 ? true :false;
		return result;
		
	}
	
	/**
	 * Get modern orthographe of the word based on the
	 * @param args
	 */
	
	public static String getModern(Connection connection, String word, String pos, String lemma) {
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("SELECT modern FROM word_modern WHERE graphic= ?  and lemma =?");
      try {
		preparedStatement= connection.prepareStatement(sql.toString());
        preparedStatement.setString(1, word);
        preparedStatement.setString(2, lemma);
        
	
		
			ResultSet rs = preparedStatement.executeQuery();
			HashMap<String, String> tmp = new HashMap<String, String>();
			while (rs.next()) {
				
				java.sql.ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();

				  String value = rs.getString("modern");
				  tmp.put("modern", value);
				 
			}
			
			String modern = tmp.get("modern");
			return modern;
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
		
		return "";
		
	}
	
	 
    
    /**
	 * 
	 * @param connection
	 * @param lemme
	 * @param pos
	 * @return
	 */
			
    public static String getFromModernCache(Connection connection, String word, String lemme, String pos) {
    	
    	
    	if(pos.equals("Fw")||pos.equals("Fs")){// it is modern
			return word;

		}else if(pos.equals("Np")||pos.equals("Nc")){
			if(pos.equals("Nc")){ // if the Nc is plural it will be different than lemma
				if(word.endsWith("s") && !lemme.toLowerCase().endsWith("s") ){
					return lemme.toLowerCase()+"s";
				}else if(word.endsWith("ux") && lemme.toLowerCase().endsWith("s")){
					String plural = lemme.toLowerCase().substring(0, lemme.toLowerCase().length()-2)+"ux";
					return plural;
				}	
			}

			return lemme.toLowerCase();     //in the case of the name  already represents the modern version of the word

		}else if(word.toLowerCase().equals(lemme.toLowerCase())){		
			return lemme.toLowerCase();
		}else{
			String modern;
			PreparedStatement preparedStatement = null;
			StringBuilder sql= new StringBuilder("SELECT modern FROM modern_cache where word = ? and pos =? and lemma = ?"); 

			try {
				preparedStatement= connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, word);
				preparedStatement.setString(2, pos);
				preparedStatement.setString(3, lemme);

				ResultSet rs = preparedStatement.executeQuery();
				if(rs.next()){
					modern = rs.getString("modern");
				} else{

					modern = word;
				}


				return modern;
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
			return word;
		}
    	

    }
	
	
	/**
	 * 
	 * @param connection
	 * @param lemme
	 * @param pos
	 * @return
	 */
			
	public static Set<String> getModernCache(Connection connection, String lemme,String pos) {
				HashSet<String> cache= new HashSet<String>();
				PreparedStatement preparedStatement = null;
				StringBuilder sql= new StringBuilder("SELECT modern FROM word_modern where pos =? and lemma = ?"); 
		      
				try {
					preparedStatement= connection.prepareStatement(sql.toString());
			        preparedStatement.setString(1, pos);
			        preparedStatement.setString(2, lemme);
				
		       
				
					ResultSet rs = preparedStatement.executeQuery();
					
					while (rs.next()) {
						java.sql.ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						cache.add(rs.getString("modern"));
							  
															
					}
			return cache;
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
		
		return cache;
		
	}
	
	public static HashMap<String,String> readModernCache(Connection connection){
		HashMap<String,String> cache = new HashMap<String,String>();
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("SELECT * FROM modern_cache");
		
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
			
       
		
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				java.sql.ResultSetMetaData rsmd = rs.getMetaData();
				String key= rs.getString("word")+";"+rs.getString("pos")+";"+rs.getString("lemma");
				cache.put(key,rs.getString("modern"));
					  
													
			}
	return cache;
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
		
		return cache;
		
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
//	/**
//	 * 
//	 * @param connection
//	 * @param docId
//	 * @return
//	 */
//	public static LinkedList<TokenInfo> getTokenInfoList(Connection connection, String docId){
//		LinkedList<TokenInfo> tokens= new LinkedList<TokenInfo>();
//		
//		PreparedStatement preparedStatement = null;
//		StringBuilder sql= new StringBuilder("SELECT token, start_offset, end_offset, sub_id FROM token_body where doc_id = ? and section_id='body_sermon' order by start_offset "); 
//      
//		try {
//			preparedStatement= connection.prepareStatement(sql.toString());
//	        preparedStatement.setString(1, docId);
//	       
//		
//       
//		
//			ResultSet rs = preparedStatement.executeQuery();
//			
//			while (rs.next()) {
//				TokenInfo tmp = new TokenInfo();
//				java.sql.ResultSetMetaData rsmd = rs.getMetaData();
//				int columnCount = rsmd.getColumnCount();
//				tmp.setToken(rs.getString("token"));
//				tmp.setStartOffset(Integer.parseInt(rs.getString("start_offset")));
//				tmp.setEndOffset(Integer.parseInt(rs.getString("end_offset")));
//				tmp.setSub_id(rs.getString("sub_id"));	  
//				tokens.add(tmp);									
//			}
//	return tokens;
//} catch (SQLException e) {
//
//	System.out.println(e.getMessage());
//	try{
//	connection.rollback();
//	}catch(SQLException e2){
//		
//	}
//
//} finally {
//
//	if (preparedStatement != null) {
//		try{
//		preparedStatement.close();
//		}catch(SQLException e3){
//			
//		}
//	}
//
//}
//		return tokens;
//	}
	
	/**
	 * 
	 * 
	 * @param preparedStatement
	 * @param connection
	 * @return
	 */
	public static LinkedList<TokenInfo> getTokenInfoList(PreparedStatement preparedStatement, Connection connection){
		LinkedList<TokenInfo> tokens= new LinkedList<TokenInfo>();
		
		try {
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
			TokenInfo tmp = new TokenInfo();
			
			String word = rs.getString("token");
			String lemma =rs.getString("lemma");
			String pos = rs.getString("pos");
			
			tmp.setToken(word);
			tmp.setPos(pos);
			tmp.setLemma(lemma);
			//tmp.setModern(getFromModernCache(connection,rs.getString("final_token"),lemma,pos ));
			tmp.setModern(rs.getString("modern_token"));
			tmp.setPageNo(rs.getString("page_no"));
			tmp.setStartOffset(Integer.parseInt(rs.getString("start_offset")));
			tmp.setEndOffset(Integer.parseInt(rs.getString("end_offset")));
		    tmp.setSub_id(rs.getString("sub_id"));	  
			tokens.add(tmp);
			}
			return tokens;
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
		return tokens;
	}
	
	/**
	 * 
	 * @param connection
	 * @param docId
	 * @return
	 */
	public static LinkedList<TokenInfo> getNoNoteTokenInfoList(Connection connection, String docId){
		LinkedList<TokenInfo> tokens= new LinkedList<TokenInfo>();
		
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("SELECT token,modern_token, pos, lemma, start_offset, end_offset, sub_id, page_no  FROM token_body where doc_id = ? and sub_id NOT LIKE 'note%' order by start_offset "); 
     
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
	        preparedStatement.setString(1, docId);
	       
		    tokens = getTokenInfoList(preparedStatement, connection);
      

	return tokens;
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
		return tokens;
	}
	
	/**
	 * 
	 * @param preparedStatement
	 * @param connection
	 * @return
	 */
	
	public static LinkedList<TagInfo> getTagsInfoList(PreparedStatement preparedStatement, Connection connection){
		LinkedList<TagInfo> tags= new LinkedList<TagInfo>();
		
		try {
			ResultSet rs = preparedStatement.executeQuery();
		
			while (rs.next()) {
				TagInfo tmp = new TagInfo();
				
				tmp.setName(rs.getString("tag_name"));
				tmp.setStartOffset(Integer.parseInt(rs.getString("start_offset")));
				tmp.setEndOffset(Integer.parseInt(rs.getString("end_offset")));
				//get attributes
				if(!rs.getString("attributes").equals("{}")){
					String attr_trimmed = rs.getString("attributes").substring(1, rs.getString("attributes").length()-1);
					String[] attrs = attr_trimmed.split(",");
					Map<String, String> attributes=new HashMap<String,String>();
				
					for(String s:attrs){
						String [] parts = s.split("=");
						if(parts.length >= 2){
						attributes.put(parts[0], parts[1]);
						}else{
							attributes.put(parts[0], "X");
						}
					}
					tmp.setTagAttributes(attributes);	
				}
				
				tags.add(tmp);
				
			}
			
		} catch (SQLException e) {
			System.out.println("Problem");
			System.out.println(e.getMessage());
			try{
				connection.rollback();
			}catch(SQLException e2){
				System.out.println("rollback2");
			}

		} finally {

			if (preparedStatement != null) {
				try{
					preparedStatement.close();
				}catch(SQLException e3){
					System.out.println("rollback3");
				}
			}

		}
		return tags;
		
	} 
	
	/**
	 * 
	 * @param connection
	 * @param docId
	 * @return
	 */
	public static LinkedList<TagInfo> getNoNoteTagsInfoList(Connection connection, String docId){
		LinkedList<TagInfo> tags= new LinkedList<TagInfo>();
		
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("select tag_name, start_offset, end_offset, attributes from tag_body where doc_id = ? and sub_id NOT LIKE 'note%' and"
			
				+ "(tag_name IN ('body','div','p','s','head','date','hi','quote','foreign','seg','q') or (tag_name = 'ref' and (attributes LIKE '%type=bible%' or attributes LIKE '%type=bible%' ))) "
				+ "order by start_offset, end_offset DESC "); 
      
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
	        preparedStatement.setString(1, docId);
	        
	        tags=getTagsInfoList(preparedStatement, connection);
	        

			
		} catch (SQLException e) {
			
			System.out.println(e.getMessage());
			try{
				connection.rollback();
			}catch(SQLException e2){
				System.out.println(e2.getMessage());
			}

		} finally {
			
			if (preparedStatement != null) {
				try{
					preparedStatement.close();
					
				}catch(SQLException e3){
					System.out.println(e3.getMessage());
				}
			}else{
				System.out.println("prepared statemant null");
			}

		}
		return tags;
		
	} 
	
	/**
	 * 
	 * @param connection
	 * @param docId
	 * @return
	 */
	public static LinkedList<TagInfo> getNoNoteTagsInfoListLimit(Connection connection, String docId){
		LinkedList<TagInfo> tags= new LinkedList<TagInfo>();
		
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("select tag_name, start_offset, end_offset, attributes from tag_body where doc_id = ? and sub_id NOT LIKE 'note%' and"
			
				+ "(tag_name IN ('body','div','p','head','quote','q','lb','pb') or (tag_name = 'ref' and (attributes LIKE '%type=bible%' or attributes LIKE '%type=bible%' ))) "
				+ "order by start_offset, end_offset DESC "); 
      
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
	        preparedStatement.setString(1, docId);
	        
	        tags=getTagsInfoList(preparedStatement, connection);
	        

			
		} catch (SQLException e) {
			
			System.out.println(e.getMessage());
			try{
				connection.rollback();
			}catch(SQLException e2){
				System.out.println(e2.getMessage());
			}

		} finally {
			
			if (preparedStatement != null) {
				try{
					preparedStatement.close();
					
				}catch(SQLException e3){
					System.out.println(e3.getMessage());
				}
			}else{
				System.out.println("prepared statemant null");
			}

		}
		return tags;
		
	} 
	/**
	 * 
	 * @param connection
	 * @param docId
	 * @return
	 */
	public static int getNextTagId(Connection connection, String docId){
		int nextTagId=0;
		PreparedStatement preparedStatement = null;
		StringBuilder sql= new StringBuilder("SELECT tag_id FROM tag_body WHERE doc_id = ?  ORDER BY tag_id DESC LIMIT 1 "); 
      
		try {
			preparedStatement= connection.prepareStatement(sql.toString());
	        preparedStatement.setString(1, docId);
	       
		
       
		
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				nextTagId = Integer.parseInt(rs.getString("tag_id"))+1;
												
			}
	return nextTagId;
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
		return nextTagId;
		
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
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		try{

			//GetFromDatabase databaseReader = new GetFromDatabase("db.properties");
			
			Connection connection = MySQLconnection.createConnection("localhost","sermo","root" , "v0717n1030D!");
			
			System.out.println(GetFromDatabase.getDocsCQPId(connection, "_1555_Jean_Calvin_RSL"));
			
		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}

	}

	


}
