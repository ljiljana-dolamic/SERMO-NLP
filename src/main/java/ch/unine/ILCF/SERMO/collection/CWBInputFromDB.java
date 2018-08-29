/**
 * 
 */
package ch.unine.ILCF.SERMO.collection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;
import ch.unine.ILCF.SERMO.collection.FullPartInfo;

/**
 * @author dolamicl
 *
 */
public class CWBInputFromDB {

	private Properties prop;
	private Connection connection;
	private String input;
	private String output;
	
	
	

	
	
	private CreateCwbVrtFile createVrt;
	
	
	public CWBInputFromDB(String propFile)throws Exception{
		
		this.prop = SermoProperties.getProperties(propFile);
		if(this.prop.containsKey("host")
				&&this.prop.containsKey("dbuser")
				&&this.prop.containsKey("dbpassword")
				&&this.prop.containsKey("input")
				&&this.prop.containsKey("vrt.home")){
			
			this.input= this.prop.getProperty("input");
			this.output= this.prop.getProperty("vrt.home");
			
			this.createVrt = new CreateCwbVrtFile(this.prop);
		
		try {
			
			this.connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
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
	 * @param docId
	 * @return
	 */
	public LinkedList<String> getSubs(String docId){
		LinkedList<String> subs=new LinkedList<String>();
		
		return subs;
		
	}
	
	/**
	 * 
	 * @param input
	 *
	 */
	public void createVrtFile(){
		
		String [] ids = this.input.split(";");
		
		LinkedList<String> docs;
		if(ids.length ==1 && ids[0].equals("all")){
			 docs = GetFromDatabase.getDocsId(this.connection);
			 System.out.println(docs.toString());
		}else{
			docs=new LinkedList<String>();
			for(String s : ids){
				docs.add(s);
			}
		}
		
		for(String id: docs){
			System.out.println("Doc id: "+id);
			LinkedList<TokenInfo> tokens;
			LinkedList<TagInfo> tags;
			
			String cqp_id = GetFromDatabase.getDocsCQPId(this.connection,id);//get cqp id
			
			FullPartInfo bodyInfo = buildBodyInfo(id);
			
			HashMap<Integer,FullPartInfo> bodyNotesInfo =buildBodyNotesInfo(id);
			
			this.createVrt.buildVrt(cqp_id, id, bodyInfo, bodyNotesInfo);
		}
		
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	
	public FullPartInfo buildBodyInfo( String doc_id ){
		
		// get all tags and tokens (body without notes)
		LinkedList<TagInfo>tags = GetFromDatabase.getNoNoteTagsInfoList(this.connection, doc_id);
		System.out.println("tags: "+tags.size());
		LinkedList<TokenInfo>tokens = GetFromDatabase.getNoNoteTokenInfoList(this.connection, doc_id);
		System.out.println("tokens: "+tokens.size());
		return buildFullPartInfo(tokens,tags);
		
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	public HashMap<Integer,FullPartInfo> buildBodyNotesInfo(String id){
		HashMap<Integer,FullPartInfo> buildBodyNotesInfo =new HashMap<Integer,FullPartInfo>();
		
		LinkedList<String> notes_ids=GetFromDatabase.getDistinctNotesIds(connection, id);
		for(String sub_id:notes_ids){
			LinkedList<TagInfo>tags = GetFromDatabase.getSubTagsInfoList(this.connection,id,sub_id);
			LinkedList<TokenInfo>tokens = GetFromDatabase.getSubTokensInfoList(this.connection, id ,sub_id);
			
			String[]id_parts=sub_id.split("_");// "note"; start offset of the note; code
			buildBodyNotesInfo.put(Integer.parseInt(id_parts[1]), buildFullPartInfo(tokens,tags));
			 
			
		}
		
		return buildBodyNotesInfo;
		
	}
	
	/**
	 * 
	 * @param docId
	 */
	public LinkedList<TagInfo> getTags(String docId){
		
		LinkedList<TagInfo> tmp = GetFromDatabase.getNoNoteTagsInfoList(this.connection, docId);
		
		return tmp; 
	}
	
	/**
	 * 
	 * @param docId
	 */
	public LinkedList<TokenInfo> getTokens(String docId){
		
		return  GetFromDatabase.getNoNoteTokenInfoList(this.connection, docId);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
		try{
			if (args.length < 1)
		        showUsage();
			CWBInputFromDB aNT = new CWBInputFromDB(args[0]);
            
			aNT.createVrtFile();
			//aNT.displaySegments(args[1]);
			

		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}


	}
	
	public static FullPartInfo buildFullPartInfo(LinkedList<TokenInfo> tokens,LinkedList<TagInfo> tmpTags){
		HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tmpTagStart = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
		HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tmpTagEnd = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
		for(TagInfo tmp : tmpTags){
		 Integer start = tmp.getStartOffset();
		 Integer end = tmp.getEndOffset();		 
		if(tmpTagStart.containsKey(start)){

			if(tmpTagStart.get(start).containsKey(end)){
				if(start==end){
					tmpTagStart.get(start).get(end).push(tmp);
				}else{
					tmpTagStart.get(start).get(end).add(tmp);
				}
			}else{
				LinkedList<TagInfo> tmpList=new LinkedList<TagInfo>();
				tmpList.add(tmp);
				tmpTagStart.get(start).put(end, tmpList);

			}

		}else{
			HashMap<Integer, LinkedList<TagInfo>> tmpHash= new HashMap<Integer, LinkedList<TagInfo>>();
			LinkedList<TagInfo> tmpList=new LinkedList<TagInfo>();
			tmpList.add(tmp);
			tmpHash.put(end, tmpList);
			tmpTagStart.put(start, tmpHash);
		}
         String tmpName= tmp.getName();
		if(!tmpName.equals("lb") 
				&& !tmpName.equals("pb") 
				&& !tmpName.equals("figure")
				&& !tmpName.equals("cb")
				&& !tmpName.equals("milestone")
				&& !tmpName.equals("#comment")
				&& !tmpName.equals("c")
				&& !tmpName.equals("g")){	
			if(tmpTagEnd.containsKey(end)){

				if(tmpTagEnd.get(end).containsKey(start)){
					tmpTagEnd.get(end).get(start).add(tmp);
				}else{
					LinkedList<TagInfo> tmpList=new LinkedList<TagInfo>();
					tmpList.add(tmp);
					tmpTagEnd.get(end).put(start, tmpList);
				}

			}else{
				HashMap<Integer, LinkedList<TagInfo>> tmpHash= new HashMap<Integer, LinkedList<TagInfo>>();
				LinkedList<TagInfo> tmpList=new LinkedList<TagInfo>();
				tmpList.add(tmp);
				tmpHash.put(start, tmpList);
				tmpTagEnd.put(end, tmpHash);
			}

		}							
		
		}
		return new FullPartInfo(tokens,tmpTagStart,tmpTagEnd);
	}
	
	 private static void showUsage() {
		    System.err.println("\nUsage: CWBInputFromDB <properties file path>\n");
		    System.err.println("Properties file needs to contain following: ");
		    System.err.println("  host   database host");
		    System.err.println("  dbuser  database user");
		    System.err.println("  dbpassword   database password");
		   
		    System.err.println("  input        list of ids of the documents to build ;");
		    System.err.println("  vrt.home       path to the output directory");
		    
		    System.exit(0);
		  }


    }


