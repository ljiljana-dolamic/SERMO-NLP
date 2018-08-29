/**
 * 
 */
package ch.unine.ILCF.SERMO.TT;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

/**
 * @author dolamicl
 *
 */
public class TokenPOS {
	 private TreeTaggerHandler ttH; 
	 private Properties prop;
	 
	 private Connection connection;
	 

	 public TokenPOS(String propertiesFileName) throws Exception {
		 this.prop = SermoProperties.getProperties(propertiesFileName);
		 System.out.println(this.prop.toString());

		 //instantiate the treeTagger
		 if(this.prop.containsKey("treetagger.home") && this.prop.containsKey("treetagger.model")){
			 if(this.prop.containsKey("treetagger.lex")){
				 this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"), this.prop.getProperty("treetagger.lex") );
			 }else{
				 this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"));
			 }

		 }else{
			 throw(new Exception("Error: 'treetagger.home' or 'treetagger.model' propertiy  missing in "+ propertiesFileName));
		 }
	 }
	 
	 public void doPOStagging(String doc_id)throws Exception{
		 String [] tables = {"token_front","token_body"};
		 //String [] tables = {"token_body"};
		 
		 
		 for(String table : tables){
			 System.out.println("*Table*: "+ table );
		 LinkedList<String> sections = getSections(doc_id,table);
		 
		 for(String section: sections ){
			 System.out.println("*Section*: "+ section );
			 LinkedList<String> subs = getSubs(doc_id,table,section);
			 
			 for(String sub:subs){
				 System.out.println("*Sub*: "+ sub );
				 System.out.println("*Get paragraphs*");
				 LinkedList<String> pars = getParagraphs(doc_id,table,section,sub);
				 for(String par:pars){
					 
					 LinkedList<HashMap<String,String>> tokensPerParagraphe = getTokensPerPar(doc_id,table,section,sub,par);
					 tagParagraphe(tokensPerParagraphe);
					 updateDB(doc_id,table,section,sub,tokensPerParagraphe);
				 }
			 }
		 }
		 }	
		
	 }

	 private void updateDB(String doc_id, String table, String section, String sub,
			 LinkedList<HashMap<String, String>> tokensPerParagraphe) {
		 // TODO Auto-generated method stub
		 PreparedStatement preparedStatement =null;
		 try {
			 //connect to DB
			 connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					 this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
			 for(HashMap<String, String> token : tokensPerParagraphe){
				 
				 String selectSQL = "UPDATE "+ table +" SET pos='"+token.get("pos")+"', lemma='"+token.get("lemma")+"'"
						 + " where doc_id ='"+doc_id+"' AND token_id ='"+token.get("token_id")+"' AND section_id='"+section+"' AND sub_id='"+sub+"';";

System.out.println(selectSQL);

				 preparedStatement = connection.prepareStatement(selectSQL);


				 int affectedRows = preparedStatement.executeUpdate();

			 }


			 MySQLconnection.closeConnection(connection);
		 } catch (SQLException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }


	 }

	public LinkedList<String> getSections(String doc_id, String table){
    	 LinkedList<String> sections =new LinkedList<String>();
    	 String selectSQL = "select distinct section_id from "+ table+" where doc_id ='"+doc_id+"';";
    	 
    	 try {
				//connect to DB
				connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
						this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
				
				LinkedList<HashMap<String,String>> result = MySQLconnection.readDBRecord(connection,selectSQL);
				System.out.println(result.size());
				for(HashMap<String,String> tmpHM: result){
					sections.add(tmpHM.get("section_id"));
					
				}
				
				
				 MySQLconnection.closeConnection(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 
    	 
    	
    	 return sections;
    	 
     }
     
     public LinkedList<String> getSubs(String doc_id, String table, String section ){
    	 LinkedList<String> subs =new LinkedList<String>();
    	 String selectSQL = "select distinct sub_id from "+ table+" where section_id ='"+section+"' and doc_id='"+doc_id+"';";
    	 try {
				//connect to DB
				connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
						this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
				
				LinkedList<HashMap<String,String>> result = MySQLconnection.readDBRecord(connection,selectSQL);
				System.out.println(result.size());
				for(HashMap<String,String> tmpHM: result){
					subs.add(tmpHM.get("sub_id"));
					
				}
				
				
				 MySQLconnection.closeConnection(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 
    	 
    	
    	 return subs;
    	 
     }

     public LinkedList<String> getParagraphs(String doc_id, String table, String section, String sub ){
    	 LinkedList<String> pars =new LinkedList<String>();
    	 String selectSQL = "select distinct par_id from "+ table+" where section_id ='"+section+"' and sub_id = '"+sub+"' and doc_id='"+doc_id+"';";
    	 try {
				//connect to DB
				connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
						this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
				
				LinkedList<HashMap<String,String>> result = MySQLconnection.readDBRecord(connection,selectSQL);
				System.out.println(result.size());
				for(HashMap<String,String> tmpHM: result){
					pars.add(tmpHM.get("par_id"));
					
				}
				
				
				 MySQLconnection.closeConnection(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 
    	 
    	
    	 return pars;
    	 
     }
     
     
    public LinkedList<HashMap<String,String>>  getTokensPerPar(String doc_id, String table,String section,String sub,String par){
    	LinkedList<HashMap<String,String>> tokens = new LinkedList<HashMap<String,String>>();
    	
    	String selectSQL = "select token_id, final_token from "+ table+" where section_id ='"+section+"' and sub_id = '"+sub+"'and par_id ='"+par+"' and doc_id='"+doc_id+"' order by token_id;";
   	 try {
				//connect to DB
				connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
						this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
				
				LinkedList<HashMap<String,String>> result = MySQLconnection.readDBRecord(connection,selectSQL);
				System.out.println(result.size());
				tokens = result;
//				for(HashMap<String,String> tmpHM: result){
//					String ft = tmpHM.get("final_token");
//					int noST = ft.split(",").length;
//					
//					
//				}
				
				
				 MySQLconnection.closeConnection(connection);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   	 
    	
    	return tokens;
    }
    
    public void tagParagraphe(LinkedList<HashMap<String,String>> paragrapheTokens){
    	ArrayList<String> toTag = new ArrayList<String>();
    	try{
    	for(HashMap<String,String> tmpHM : paragrapheTokens){
    		String fT = tmpHM.get("final_token");
    		int fTL = fT.split(",").length;
    		if(fTL > 1){
    			for(String s: fT.split(",")){
    				toTag.add(s.trim());
    			}
    			
    		}else{
    			toTag.add(fT);
    		}
    		
    	}
    	
    	
    	LinkedList<TtOutputLine> resultDec = ttH.run(toTag.toArray(new String [toTag.size()]));
    	
    	for(HashMap<String,String> tmpHM : paragrapheTokens){
    		TtOutputLine ttDL=resultDec.pop();
    		StringBuilder lemma = new StringBuilder();
			StringBuilder pos = new StringBuilder();
			lemma.append(ttDL.getLemma());
			pos.append(ttDL.getPos());
    		int fTL = tmpHM.get("final_token").split(",").length;
    		if(fTL > 1){
    			for(int i =1;i<fTL;i++){
    				ttDL=resultDec.pop();
    				lemma.append("+").append(ttDL.getLemma());
    				pos.append("+").append(ttDL.getPos());
    			}
    			
    		}
    		tmpHM.put("lemma", lemma.toString().replaceAll("'", "''"));
    		tmpHM.put("pos", pos.toString());
    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String doc_id;
		try{
			TokenPOS POStagger = new TokenPOS("pos.properties");
			if(POStagger.prop.containsKey("doc_id")){
				doc_id = POStagger.prop.getProperty("doc_id");
			}else{
				System.out.println("Doc id: ");
				Scanner docID = new Scanner(System.in);
				doc_id = docID.nextLine();
			}
			POStagger.doPOStagging(doc_id);
		}catch(Exception e){
			e.getMessage();
		}
		 
	}

}
