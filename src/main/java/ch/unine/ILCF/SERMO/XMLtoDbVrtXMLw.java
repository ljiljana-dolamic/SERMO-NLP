/**
 * Starting from XML TEI document, this class extracts,performs tokenization and tags the text 
 * Tokenization: ch.unine.ILCF.SERMO.PRESTO.Tokenizer + sermoLex.csv;
 * Tagging: TreeTagger + sermoTTdicoShortNc.tsv
 * Post Processing: sermoTTdico.tsv
 * 
 * 
 * Output:
 * 1. token + tag information in the MySQL DB
 * 2. XML TEI containing tokenisation and tagging information
 * 3. CQPWeb vrt file
 */
package ch.unine.ILCF.SERMO;

/**
 * @author dolamicl
 *
 */
/**
 * 


import java.io.BufferedReader;

/**
 * @author dolamicl
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ch.unine.ILCF.SERMO.File.Utils.*;
import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;
import ch.unine.ILCF.SERMO.TT.TreeTaggerHandler;
import ch.unine.ILCF.SERMO.TT.TtOutputLine;
import ch.unine.ILCF.SERMO.XML.Utils.BuildTranscriptionXML;
import ch.unine.ILCF.SERMO.XML.Utils.BuildXMLw;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.XML.Utils.TeiHeaderData;
import ch.unine.ILCF.SERMO.propreties.*;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.SSplit.SCNlpSSplit;
import ch.unine.ILCF.SERMO.SSplit.SentenceInfo;
import ch.unine.ILCF.SERMO.LGeRM.*;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
//import ch.unine.ILCF.SERMO.XMLtoDbVrtXMLw.FullPartInfo;
import ch.unine.ILCF.SERMO.collection.FullPartInfo;
import ch.unine.ILCF.SERMO.Utils.CharacterUtils;
import ch.unine.ILCF.SERMO.collection.*;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public class XMLtoDbVrtXMLw {


	private boolean debug = false;

	private String doc_id;


	
	private String docIdPath="/TEI/teiHeader/fileDesc";
	static HashMap<String,String> mainParts;
	static{
		mainParts=new HashMap<String,String>();
		mainParts.put("body","/TEI/text/body");
		mainParts.put("front","/TEI/text/front");

	}
	

	private Document domDocument;
	private File xmlFile;

	private String doc_part;
	private String page_id;
	private static String section_id;
	private String sub_id;
	private int paragraph_id;// 0 for head
	private int ln_no=0; // line number on the current page
	private int col_no=0; // 0- no column; 
	private int sent_no=1;

	private Tokenizer tokenizer;
	private TreeTaggerHandler ttH; 
	//private SCNlpSSplit sSplitter;
	
	private Properties prop;
	
	private FullPartInfo frontInfo;
	private FullPartInfo bodyInfo;
	private HashMap<Integer,FullPartInfo> frontNotesInfo;
	private HashMap<Integer,FullPartInfo> bodyNotesInfo;
	
	private LinkedList<TokenInfo> front_tokens;
	private LinkedList<TokenInfo> body_tokens;
	private LinkedList<TagInfo> front_tags;
	private LinkedList<TagInfo> body_tags;
	
	private  LinkedList<TokenInfo> tmp_tokens;
	private LinkedList<TagInfo> tmp_tags;
	private HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tmpTagStart;
	private HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tmpTagEnd;

	//XML w reconstruction

	private int currentOffset=0;
	private int tokenizerStartOffset=0;
	
	private int currentSentenceStartOffset=0;
	
    //parts of documents that are treated separately 
	private HashMap<Integer,LinkedList<RemovedTag>> removedNodesFront;
	private HashMap<Integer,LinkedList<RemovedTag>> removedNodesBody;
	
	//use these as tmp when treating notes
	private boolean in_note=false;
	

	private StringBuilder textToTokenize;
	private HashMap<Integer,Integer> line_change;
	private HashMap<Integer,String> page_change;
	private HashMap<Integer,Integer> column_change;
	private Queue<Integer> bible_in;
	private Queue<Integer> bible_out;

	private boolean inParHead = false;
	private int tokenNo;
	private int tagNo;

	private Connection connection;
	
	private static HashMap<String,HashMap<String,Set<String>>> alreadyModernCache=new HashMap<String,HashMap<String,Set<String>>>();
	private static HashMap<String,String> modernCache=new HashMap<String,String>();
	
	private BuildXMLw buildXMLw;
	
	private CreateCwbVrtFile createVrt;
	private BuildLGeRMfromXML buildLGeRM; 

	public XMLtoDbVrtXMLw(String propertiesFileName) throws Exception{
		this.prop = SermoProperties.getProperties(propertiesFileName);
		
		// get the tokenizer
		System.out.println("Loading tokenizer");
		if(this.prop.containsKey("tokenizer.path")){
			if(this.prop.containsKey("tokenizer.window")){
				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"),Integer.parseInt(this.prop.getProperty("tokenizer.window")));
			}else{
				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"));// default tokenizer window
			}

		}else{
			
			throw(new Exception("Error: 'tokenizer.path' property  missing in "+ propertiesFileName));
		}

		// dictionary
		
		if(this.prop.containsKey("debug")&&this.prop.get("debug").equals("true")){
			debug=true;
		}else{
			debug=false;
		}
		System.out.println("Loading TreeTagger");
		if(this.prop.containsKey("treetagger.home") && this.prop.containsKey("treetagger.model")){
			if(this.prop.containsKey("treetagger.lex")){
				this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"), this.prop.getProperty("treetagger.lex") );
				//CharacterUtils.loadDictionary(this.prop.getProperty("charUtils.dico"));
			}else{
				this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"));
			}
			
			if(this.prop.containsKey("postProc.dico")){
				System.out.println("Loading TreeTagger postDict");
				this.ttH.loadPostDictionary(this.prop.getProperty("postProc.dico"));
				if(this.prop.containsKey("dictionary.lex")){
					System.out.println("Loading dictionary");
					CharacterUtils.fullDictionary = this.ttH.postProcDict;
					
				}else{
					CharacterUtils.fullDictionary = new HashMap<String,HashMap<String,HashMap<String,String>>>();
				}
			}else{
				CharacterUtils.fullDictionary = new HashMap<String,HashMap<String,HashMap<String,String>>>();
			}
			

		}else{
			throw(new Exception("Error: 'treetagger.home' or 'treetagger.model' property  missing in "+ propertiesFileName));
		}
		//Building CWB vrt
		if(this.prop.containsKey("buildVrt") && this.prop.get("buildVrt").equals("true") ){
			
			this.createVrt= new CreateCwbVrtFile(prop);
			
		}
		
		//Building w-xml
		
		if(this.prop.containsKey("buildWxml") && this.prop.get("buildWxml").equals("true") ){
			
			this.buildXMLw= new BuildXMLw(prop);
			
		}
		
		//Building lgerm
		
				if(this.prop.containsKey("buildLGeRM") && this.prop.get("buildLGeRM").equals("true") ){
					
					this.buildLGeRM= new BuildLGeRMfromXML(prop);
					
				}
				
		
		this.connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
						this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
		
		System.out.println("*Reading modern cache*");
	    modernCache= GetFromDatabase.readModernCache(this.connection);
		System.out.println("Cache size:"+ modernCache.size());
		
		//get the already modern in one passage		
		System.out.println("*DONE*");
		
	}

	/**
	 *parseXMLdoc(String)
	 *@param path to the xml file to be treated
	 * 
	 **/

	public void parseXMLdoc(String filePath){
		//create a file
		File file  = new File(filePath);

		parseXMLFile(file);

	}

	/**
	 *parseXMLFile(File)
	 *@param path to the xml file to be treated
	 *@return list of tokens

	 *-creates data base record for the file if it doesn't exist
	 *
	 **/
	public  void parseXMLFile(File file){
		try{
			//create a file
			this.xmlFile = file;
			System.out.println("Treating file:" + this.xmlFile.getName());
			//get documents part
			this.domDocument = XMLutils.getDoc(this.xmlFile);
			// get document id from xml file

			this.doc_id = getDocId(XMLutils.getNode(this.domDocument, docIdPath));
			if(debug){
				System.out.println("Doc_Id: "+ doc_id);
			}

			// initialization of variables

			this.front_tokens = new LinkedList<TokenInfo>();
			this.body_tokens = new LinkedList<TokenInfo>();
			this.front_tags = new LinkedList<TagInfo>();
			this.body_tags = new  LinkedList<TagInfo>();
			
			
//taking care of notes to be able to reconstruct them in the w xml and vrt
		     
			this.removedNodesFront = new HashMap<Integer,LinkedList<RemovedTag>>();
			this.removedNodesBody = new HashMap<Integer,LinkedList<RemovedTag>>();
			
			this.frontNotesInfo=new HashMap<Integer,FullPartInfo>();
			this.bodyNotesInfo=new HashMap<Integer,FullPartInfo>();
//			

			this.textToTokenize=new StringBuilder();
			this.line_change = new  HashMap<Integer,Integer>();
			this.page_change = new HashMap<Integer,String>();
			this.column_change =  new  HashMap<Integer,Integer>();
			this.bible_in = new LinkedList<Integer>();
			this.bible_out = new LinkedList<Integer>();

			this.connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));

			for(String s :  mainParts.keySet()){
				this.doc_part=s;
				section_id=s;
				this.tokenNo=0;
				this.tagNo=0;
				if(debug){
					System.out.println("Section_Id: "+ section_id);
				}
				this.tmp_tags =  new LinkedList<TagInfo>();
				this.tmp_tokens = new LinkedList<TokenInfo>();
				this.tmpTagStart = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
				this.tmpTagEnd = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
				parsePartNode(XMLutils.getNode(this.domDocument, mainParts.get(s)));
				// tokenizeAndClean();
				if(s.equals("front")){
					this.frontInfo = new FullPartInfo(this.tmp_tokens,this.tmpTagStart,this.tmpTagEnd);
					this.front_tags=this.tmp_tags;
					
				}else if(s.equals("body")){
					this.bodyInfo = new FullPartInfo(this.tmp_tokens,this.tmpTagStart,this.tmpTagEnd);
					this.body_tags=this.tmp_tags;
				}else{
					throw new Exception("*** Doc part "+doc_part+"not allowed ***");
				}
				saveRemoved(s);
			}

			//saveRemoved();
			Node header = XMLutils.getNode(this.domDocument, "/TEI/teiHeader");
			//buildVrt
			if(this.prop.containsKey("buildVrt") && this.prop.get("buildVrt").equals("true")){
				System.out.println("Building VRT");
				this.createVrt.buildVrt(this.doc_id,this.domDocument, this.frontInfo, this.bodyInfo, this.frontNotesInfo, this.bodyNotesInfo);
				
				//this.createVrt.buildVrt(this.doc_id,this.domDocument, myFrontInfo, myBodyInfo, myFrontNotesInfo, myBodyNotesInfo);
			}
			
			
			if(this.prop.containsKey("buildWxml") && this.prop.get("buildWxml").equals("true") ){
				//buildXMLw
				System.out.println("Building XML");
				//Node header = XMLutils.getNode(this.domDocument, "/TEI/teiHeader");
				
				//this.buildXMLw.buildXML(this.doc_id,header, front_tokens, body_tokens, frontTagStart, frontTagEnd, bodyTagStart, bodyTagEnd);
				this.buildXMLw.buildXML(this.doc_id,header, this.frontInfo, this.bodyInfo, this.frontNotesInfo, this.bodyNotesInfo);
				
			}
			
			if(this.prop.containsKey("buildLGeRM") && this.prop.get("buildLGeRM").equals("true") ){
				//buildXMLw
				System.out.println("Building LGERM");
				//Node header = XMLutils.getNode(this.domDocument, "/TEI/teiHeader");
				
				//this.buildXMLw.buildXML(this.doc_id,header, front_tokens, body_tokens, frontTagStart, frontTagEnd, bodyTagStart, bodyTagEnd);
				this.buildLGeRM.buildXML(this.doc_id,header, this.frontInfo, this.bodyInfo, this.frontNotesInfo, this.bodyNotesInfo);
				
			}
			saveExtracted();




		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @throws Exception
	 */

	public void saveExtracted()throws Exception{

		if(debug){
			System.out.println("*** Front tags ***");
			for(TagInfo tf:this.front_tags){
				System.out.println(tf.toString());
			}
			System.out.println("*** Body tags ***");
			for(TagInfo bf:this.body_tags){
				System.out.println(bf.toString());
			}
			System.out.println("*** Front tokens ***");
			for(TokenInfo fT:this.front_tokens){
				System.out.println(fT.toString());
			}
			System.out.println("*** Body tokens ***");
			for(TokenInfo bT:this.body_tokens){
				System.out.println(bT.toString());
			}
		}else{

			System.out.println("*Writing to database*");

			// write to DB
//			connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
//					this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
//            
			 cleanTables(doc_id,connection);

			System.out.println("*Tags*");
			for(TagInfo tf:this.front_tags){
				writeHashToDB(createTagHash(tf),"tag_front",connection);
			}

			for(TagInfo bf:this.body_tags){
				writeHashToDB(createTagHash(bf),"tag_body",connection);
			}
			System.out.println("*Tokens*");
			for(TokenInfo fT:this.front_tokens){
				writeHashToDB(createTokenHash(fT),"token_front",connection);
			}

			for(TokenInfo bT:this.body_tokens){
				writeHashToDB(createTokenHash(bT),"token_body",connection);
			}

			//MySQLconnection.closeConnection(connection);
		}
		MySQLconnection.closeConnection(connection);
	}
	/**
	 * 
	 */

	public void saveRemoved(String part)throws Exception{
		
		 HashMap<Integer,LinkedList<RemovedTag>> removedNodes;
		 HashMap<Integer,FullPartInfo> notesPartInfo;
       
        	if(part.equals("front")){
        		removedNodes = this.removedNodesFront;
        		this.doc_part="front";
        		notesPartInfo = this.frontNotesInfo;
        	}else if(part.equals("body")){
        		removedNodes = this.removedNodesBody;
        		this.doc_part="body";
        		notesPartInfo = this.bodyNotesInfo;
        	}else{
        		throw new Exception("Part not correct: "+part);
        	} 
        	int tmp_id = 0;
    		for(Integer removedOffest: removedNodes.keySet() ){
    			LinkedList<RemovedTag> rOremoved = removedNodes.get(removedOffest);
    			
    			
				
    			for(RemovedTag rt: rOremoved){
    				
    				this.tmp_tags =  new LinkedList<TagInfo>();
    				this.tmp_tokens = new LinkedList<TokenInfo>();
    				this.tmpTagStart = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
    				this.tmpTagEnd = new HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>>();
    				
    				
    				clearAllGlobal();
    				this.sub_id =rt.getNode().getNodeName()+"_"+removedOffest+"_"+tmp_id;
    				HashMap<String,String> tmpFront = parseRemoved(removedOffest,rt); 
    				if(debug){
    					System.out.print("*** Removed tag ***  ");

    					System.out.println(this.sub_id+ ": "+tmpFront.toString());


    				}
    				
    				if(this.in_note){
    					FullPartInfo  tmpFPI = new FullPartInfo(this.tmp_tokens, this.tmpTagStart,this.tmpTagEnd);
    					notesPartInfo.put(removedOffest, tmpFPI);
    					this.in_note=false;
    				}

    				tmp_id++;

    			}
    			
    			if(this.doc_part.equals("front")){
    				this.front_tags.addAll(tmp_tags);
    			}else{
    				this.body_tags.addAll(tmp_tags);
    			}
    			

    		}
        	
        




	}

	/**
	 * 
	 * @param nodeToParse
	 */

	public void parsePartNode(Node nodeToParse)throws Exception{
	
		this.currentOffset=0;                            //offset starts at 0 for each part front or body
		this.tokenizerStartOffset=0;
		this.currentSentenceStartOffset = 0;
		this.sent_no=1;
		//this.sub_id =0;
		int startOffset = currentOffset;
		if(debug){
			System.out.println("Parsing part: "+ nodeToParse.getNodeName());
		}
		NodeList childrenDiv= nodeToParse.getChildNodes();

		for(int i=0;i< childrenDiv.getLength();i++){
			if(debug){
				System.out.println("Parsing part child: "+ childrenDiv.item(i).getNodeName());
			}
			if(childrenDiv.item(i).getNodeName().equals("div")){
				Node child=childrenDiv.item(i);
				parseDiv(child);

			}else if(childrenDiv.item(i).getNodeType() == Node.TEXT_NODE || childrenDiv.item(i).getNodeName().equals("#comment")){
				if(debug){
					System.out.println("Text node: "+childrenDiv.item(i).getTextContent());
				}
			}else{

				throw new Exception("***Basic part contains other than div***"+childrenDiv.item(i).getNodeName());
			}

		}
		if(this.currentSentenceStartOffset != -1){
			addSentenceTag(this.currentSentenceStartOffset, this.currentOffset);
		}
		int endOffset = this.currentOffset;
		addTag(nodeToParse,startOffset,endOffset);
		
	}

	/**
	 * 
	 *  - each div adds a part to a section_id
	 * @param divToParse
	 * 
	 */
	public void parseDiv(Node divToParse)throws Exception{

		int startOffset = this.currentOffset;
		String s_id = section_id;
		Node div= divToParse;
		NamedNodeMap attributes = div.getAttributes();
		String div_type=attributes.getNamedItem("type").getNodeValue();

		if(debug){
			System.out.println("Parsing div:" + div_type );
		}

		if(div_type.equals("sub")){
			sub_id = attributes.getNamedItem("n").getNodeValue();
		}else{
			sub_id="0";
			StringBuilder sb = new StringBuilder();
			sb.append(section_id).append("_").append(div_type);
			section_id = sb.toString();
		}


		if(debug){
			System.out.println("Section_Id: "+ section_id);
		}

		NodeList children= div.getChildNodes();
		for(int i=0;i< children.getLength();i++){
			Node child=children.item(i);
			if(child.getNodeType() != Node.TEXT_NODE){ // div can not have text nodes, they are empty formating text nodes
				parseNode(child);
			}
		}
		//tokenizeAndClean();
		int endOffset = this.currentOffset;
		section_id = s_id; 
		addTag(divToParse,startOffset,endOffset);
	}
	/**
	 * 
	 * @param nodeToParse
	 * @throws Exception
	 */

	public void parseNode(Node nodeToParse)throws Exception{

		String nodeName = nodeToParse.getNodeName();
		if(nodeName.equals("p")||nodeName.equals("head")){
			parseParagrapheOrTitle(nodeToParse);
		}else if(nodeName.equals("div")){
			parseDiv(nodeToParse);
		}else{
			parseOther(nodeToParse);
		}

	}


	/** 
	 * 
	 * @param nodeToParse
	 * @throws Exception
	 */
	public void parseParagrapheOrTitle(Node nodeToParse)throws Exception{
		//tokenizeAndClean();
		String pageTmp= this.page_id;
		int colTmp=this.col_no;
		
		this.inParHead=true;
		if(debug){
			System.out.println("Head or p! ");
		}
		int startOffset = this.currentOffset;
		String nodeName = nodeToParse.getNodeName();
		if(nodeName.equals("head")){
			this.paragraph_id = 0;
		}else{
			this.paragraph_id = Integer.parseInt(nodeToParse.getAttributes().getNamedItem("n").getNodeValue());
		}

		if(debug){
			//System.out.println("Text to tokenize: "+ textToTokenize.toString());
			System.out.println("Page out:"+ this.page_id);
		}

		NodeList children= nodeToParse.getChildNodes();
		for(int i=0;i< children.getLength();i++){
			parseNode(children.item(i));

		}

		int endOffset = this.currentOffset;
		this.inParHead=false;
		if(debug){

			System.out.println("Par: "+ this.paragraph_id+"; Page:"+ this.page_id);
			System.out.println("Text to tokenize: "+ textToTokenize.toString());
		}
		this.page_id = pageTmp;
		this.col_no = colTmp;
		tokenizeCleanAndTag();
		
		addTag(nodeToParse,startOffset,endOffset);
	}

	

	/**
	 *  tokenize and clean the paragraphe or title 
	 */
	public void tokenizeCleanAndTag(){
 
		LinkedList<String> tmpTokens;
		LinkedList<String> toTag = new LinkedList<String>();
		LinkedList<TtOutputLine> tagResult=new LinkedList<TtOutputLine>() ;
		LinkedList<TokenInfo> tmpTokensInfo= new LinkedList<TokenInfo>();
		int tmpOffset= this.tokenizerStartOffset;
		int in_bible_offset = this.tokenizerStartOffset;
		int in_bible_ln=0;
		int in_bible_col=0;
		int break_point=0;
		//this.sent_no=1;
		
		String in_bible_page="";
		boolean in_bible = false;
		StringBuilder in_bible_token = new StringBuilder(); 

	
		if(this.textToTokenize.length()>0){
			tmpTokens=this.tokenizer.tokenize(this.textToTokenize.toString());	
			for(String token:tmpTokens){
	
				int tokenStartOffset=tmpOffset; 
				break_point=0;
				int nextLn=0;
				int nextCol=0;
				String nextPage="";
				boolean nextLnB = false;
				boolean nextColB = false;
				boolean nextPageB = false;
				

				for(int i = 0; i<token.length();i++){

					if(this.column_change.containsKey(tmpOffset+i)){
						if(i==0){
							this.col_no = this.column_change.get(tmpOffset+i); 
						}else{
							nextCol=this.column_change.get(tmpOffset+i);
							nextColB=true;
							break_point=i;
						}
					}
					if(this.line_change.containsKey(tmpOffset+i)){
						if(i==0){
							this.ln_no = this.line_change.get(tmpOffset+i);
						}else{
							nextLn=this.line_change.get(tmpOffset+i);
							nextLnB =true;
							break_point=i;
						}
					}
					if(this.page_change.containsKey(tmpOffset+i)){
						if(i==0){
							this.page_id = this.page_change.get(tmpOffset+i);

						}else{
							nextPage=this.page_change.get(tmpOffset+i);
							nextPageB=true;
							break_point=i;
						}
					}

				}
				if(!in_bible && !this.bible_in.isEmpty() && tmpOffset >= this.bible_in.peek()){

					in_bible_offset = tmpOffset;
					in_bible_ln = this.ln_no;
					in_bible_col = this.col_no;
					in_bible_page = this.page_id;
					in_bible=true;
				}else{
					if(!this.bible_out.isEmpty() && tmpOffset > this.bible_out.peek()){
						this.bible_in.poll();
						this.bible_out.poll();
						in_bible=false;
						if(in_bible_token.toString().matches("\\s+")){
							tmpOffset += in_bible_token.toString().length();

						}else{
							
							TokenInfo tmp = addTokenTmp(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point,this.sent_no);
							if(tmp !=null){
								tmpTokensInfo.add(tmp);
								for(String ds:tmp.getFinalToken()){
									toTag.add(ds);
								}
							}
							tmpOffset += in_bible_token.toString().length();
						}
					in_bible_token.setLength(0);///!!!!!!!!!
					}
					if(!this.bible_in.isEmpty() && tmpOffset >= this.bible_in.peek()){
						in_bible=true;
					}

				}
				if(in_bible){
					if(in_bible_token.length()>0){
						//word followed by point but not in dictionary
							if(token.equals(".") && CharacterUtils.fullDictionary.containsKey(Character.toString(in_bible_token.toString().toLowerCase().charAt(0))) && !CharacterUtils.fullDictionary.get(Character.toString(in_bible_token.toString().toLowerCase().charAt(0))).containsKey(in_bible_token.toString().toLowerCase())){	
							
								in_bible_token.append(token);
							
						
						}else{ 
							
							TokenInfo tmp = addTokenTmp(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point,this.sent_no);
							if(tmp !=null){
								tmpTokensInfo.add(tmp);
								for(String ds:tmp.getFinalToken()){
									toTag.add(ds);
								}
							}
							tmpOffset += in_bible_token.toString().length();
							in_bible_token.setLength(0);
							if(token.matches("\\s+")){
								tmpOffset += token.length();
							}else{
								in_bible_token.append(token);
							}	
						}
					}else{
						if(token.matches("\\s+")){
							tmpOffset += token.length();
						}else{
							in_bible_token.append(token);
						}	
					}

					in_bible_offset = tmpOffset;
					in_bible_ln = this.ln_no;
					in_bible_col = this.col_no;
					in_bible_page = this.page_id;

				}else{
						if(token.matches("\\s+")){
							tmpOffset += token.length();
						}else{
							
						TokenInfo tmp = addTokenTmp(token, tmpOffset,this.ln_no,this.col_no,this.page_id,break_point,this.sent_no);
						if(tmp !=null){
							tmpTokensInfo.add(tmp);
							for(String ds:tmp.getFinalToken()){
								toTag.add(ds);
							}
						}
						tmpOffset += token.length();
					}
				}
				// if there has been a brake within the token change next values		
				if(nextColB){
					this.col_no=nextCol;
				}
				if(nextLnB){
					this.ln_no=nextLn;
				}

				if(nextPageB){
					this.page_id=nextPage;
				}

			}



			if(in_bible_token.length()>0){
				
				TokenInfo tmp = addTokenTmp(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point,sent_no);
				if(tmp !=null){
					tmpTokensInfo.add(tmp);
					for(String ds:tmp.getFinalToken()){
						toTag.add(ds);
					}
					
				}
				
			}
		}

		// perform tagging and add info to tmpTokens Info
		try{
         //   System.out.println("Tagging this part:"+toTag);
			tagResult = this.ttH.run(toTag);
			/// add pos and lemma to tokens info

			int lastSentEndOffset=-1;
			

			for(TokenInfo tf:tmpTokensInfo){
				
				lastSentEndOffset=tf.getEndOffset();
				if(this.currentSentenceStartOffset == -1){
					this.currentSentenceStartOffset= tf.getStartOffset();
				}
				
				StringBuilder pos= new StringBuilder();
				StringBuilder lemma= new StringBuilder();
				StringBuilder modern= new StringBuilder();
				/// get pos and lemma for each part of final token and join them with +
				for(String ftp : tf.getFinalToken()){
					TtOutputLine ttDL=tagResult.pop();
					if(pos.length()!=0){
						pos.append("+");
						lemma.append("+");
						modern.append("+");
					}
					pos.append(ttDL.getPos());
					lemma.append(ttDL.getLemma());
					
					modern.append(getModern(ftp,ttDL.getPos(),ttDL.getLemma()));
				}
				tf.setPos(pos.toString());
				tf.setLemma(lemma.toString());
				tf.setModern(modern.toString());

				//change sentence number after strong punctuation
				tf.setSentNo(sent_no);

				if(pos.toString().equals("Fs")){
					addSentenceTag(this.currentSentenceStartOffset, tf.getEndOffset());
					this.currentSentenceStartOffset=-1;
					this.sent_no++;
				}

			}
			
			//close sentence at the end of the paragraph even if it doesn't end in a Fs
			if(this.currentSentenceStartOffset != -1){
				addSentenceTag(this.currentSentenceStartOffset, lastSentEndOffset);
				this.currentSentenceStartOffset=-1;
				this.sent_no++;
			}

		}catch(Exception e){
			System.out.println("!!!Tagging problem!!!:"+ e.getMessage());
			e.printStackTrace();
		}
		
          this.tmp_tokens.addAll(tmpTokensInfo); 
		// add tmpTokensInfo to corresponding tokens  

		if(this.doc_part.equals("front")){

			this.front_tokens.addAll(tmpTokensInfo);

		}else{

			this.body_tokens.addAll(tmpTokensInfo);

		}		



		//reset globals
		this.textToTokenize=new StringBuilder();
		this.tokenizerStartOffset = this.currentOffset;
		this.line_change = new  HashMap<Integer,Integer>();
		this.page_change = new HashMap<Integer,String>();
		this.column_change =  new  HashMap<Integer,Integer>();
		this.bible_in = new LinkedList<Integer>();
		this.bible_out = new LinkedList<Integer>();
	}
	
	// add sentence tag to the tag list
	
	private void addSentenceTag(int start, int end)throws Exception{
		if(!this.in_note){  // no sentences within notes
			
		TagInfo tmp = new TagInfo();
		tmp.setName("s");
		tmp.setStartOffset(start);
		tmp.setEndOffset(end);
		
			Map<String,String> tmpA=new HashMap<String,String>();
			tmpA.put("n",Integer.toString(this.sent_no));
			tmp.setTagAttributes(tmpA);
				 
		tmp.setDoc_id(this.doc_id);
		tmp.setTag_id(this.tagNo);
		tmp.setSub_id(this.sub_id);
		
		tmp.setTextContent("");
		this.tagNo++;

			
			if(tmpTagStart.containsKey(start)){
				
				if(tmpTagStart.get(start).containsKey(end)){
					tmpTagStart.get(start).get(end).add(tmp);
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
			
									
			tmp_tags.add(tmp);
			
		}
		
		
	}
	
	

//	private HashMap<Integer, Integer> getSentencesIndex(String string, int offset) {
//
//		// TODO Auto-generated method stub
//		HashMap<Integer, Integer> sentenceIndex= new HashMap<Integer, Integer>();
//
//		LinkedList<SentenceInfo> sentences = this.sSplitter.getSentences(string);
//		for(SentenceInfo si:sentences){
//			System.out.println(si.toString());
//			sentenceIndex.put(si.getStartOffset()+offset, si.getSentenceId()+1);
//
//		}
//
//		return sentenceIndex;
//	}

	/**
	 * 
	 * @param token
	 * @param offset
	 * @param lineNo
	 * @param columnNo
	 * @param pageNo
	 * @param break_point
	 */

	public void addToken(String token, int offset,int lineNo,int columnNo,String pageNo, int break_point ){
		if(token.length() > 0){
			TokenInfo tokenInfo = new TokenInfo();

			tokenInfo.setDoc_id(this.doc_id);
			tokenInfo.setSection_id(section_id);
			tokenInfo.setSub_id(this.sub_id);
			tokenInfo.setPageNo(pageNo);
			tokenInfo.setColumnNo(columnNo);
			tokenInfo.setPar_id(this.paragraph_id);
			tokenInfo.setLineNo(lineNo);
			tokenInfo.setHead(paragraph_id == 0? 1 : 0);

			tokenInfo.setToken(token);

			tokenInfo.setToken_id(this.tokenNo);
			this.tokenNo++;

			tokenInfo.setStartOffset(offset);
			tokenInfo.setEndOffset(offset+token.length());
			tokenInfo.setBreak_point(break_point);

			tokenInfo.setFinalToken(CharacterUtils.decomposeString(CharacterUtils.normalize(token)));
           
			
			this.tmp_tokens.add(tokenInfo);
			
			if(this.doc_part.equals("front")){
				this.front_tokens.add(tokenInfo);
			}else{
				this.body_tokens.add(tokenInfo);
			}
		}

	}

	/**
	 * 
	 * @param token
	 * @param offset
	 * @param lineNo
	 * @param columnNo
	 * @param pageNo
	 * @param break_point
	 */

	public TokenInfo addTokenTmp(String token, int offset,int lineNo,int columnNo,String pageNo, int break_point, int sent_no ){
		if(token.length() > 0){
			TokenInfo tokenInfo = new TokenInfo();

			tokenInfo.setDoc_id(this.doc_id);
			tokenInfo.setSection_id(section_id);
			tokenInfo.setSub_id(this.sub_id);
			tokenInfo.setPageNo(pageNo);
			tokenInfo.setColumnNo(columnNo);
			tokenInfo.setPar_id(this.paragraph_id);
			tokenInfo.setLineNo(lineNo);
			tokenInfo.setHead(paragraph_id == 0? 1 : 0);

			tokenInfo.setToken(token);

			tokenInfo.setToken_id(this.tokenNo);
			this.tokenNo++;

			tokenInfo.setStartOffset(offset);
			tokenInfo.setEndOffset(offset+token.length());
			tokenInfo.setBreak_point(break_point);

			tokenInfo.setFinalToken(CharacterUtils.decomposeString(CharacterUtils.normalize(token)));

			tokenInfo.setSentNo(sent_no);

			return tokenInfo;

		}else{

			return null;
		}

	}

	/**
	 * 
	 * @param nodeToParse
	 * @throws Exception
	 */

	public void parseOther(Node nodeToParse)throws Exception{

		String nodeName = nodeToParse.getNodeName();

		// remove nodes not to be in the token list
		if(
				//	!section_id.equals("note")&&
				(nodeName.equals("note")||nodeName.equals("fw")||nodeName.equals("surplus")||nodeName.equals("del")
						||(nodeName.equals("ref") && ((Element)nodeToParse).hasAttribute("type")&& (!((Element)nodeToParse).getAttribute("type").equals("bible")&& !((Element)nodeToParse).getAttribute("type").equals("other")))
						||nodeName.equals("gap")	
						||((nodeName.equals("sic")||nodeName.equals("abbr")) && nodeToParse.getParentNode().getNodeName().equals("choice"))
						||(nodeName.equals("del") && nodeToParse.getParentNode().getNodeName().equals("subst")))){
			if(section_id.matches(".*_note")){

				String text = nodeToParse.getTextContent().replaceAll("(?s)\\s+", " ");


				String tmp_id = this.sub_id+"_"+nodeName+"_"+this.currentOffset;

				TagInfo tmpTag  = new TagInfo();
				tmpTag.setDoc_id(this.doc_id);
				tmpTag.setTag_id(0);
				//this.tagNo++;
				tmpTag.setName(nodeName);
				tmpTag.createAttributeMap(nodeToParse.getAttributes());
				
				tmpTag.setTextContent(text);
				tmpTag.setSub_id(tmp_id);
				if(text.startsWith("\\s")){
					tmpTag.setStartOffset(this.currentOffset+1); // start a tag after the space
				}else{
					tmpTag.setStartOffset(this.currentOffset);
				
				}
				tmpTag.setEndOffset(this.currentOffset+text.length()); 
				///////////
				tmp_tags.add(tmpTag);
				
				if(doc_part.equals("front")){
					front_tags.add(tmpTag);
				}else if(doc_part.equals("body")){
					body_tags.add(tmpTag);
				}else{
					throw new Exception("*** Doc part "+doc_part+"not allowed ***");
				}

			}else{

				if(debug){
					System.out.println("Removing node: "+ nodeName+" page: " + this.page_id);
				}
				HashMap<Integer,LinkedList<RemovedTag>> removedNodes = this.doc_part.equals("front")? removedNodesFront : removedNodesBody;
				RemovedTag removedTag = new RemovedTag(nodeToParse, this.page_id, this.ln_no);
				removedTag.setSection_id(section_id);
				removedTag.setSub_id(sub_id);

				if(!removedNodes.containsKey(this.currentOffset)){
					LinkedList<RemovedTag> tmp = new LinkedList<RemovedTag>();

					
					tmp.add(removedTag);
					removedNodes.put(this.currentOffset, tmp);
				}else{
					
					removedNodes.get(this.currentOffset).add(removedTag);
				}
			}


		}else if(nodeToParse.hasChildNodes()){
			int startOffset = this.currentOffset;
			NodeList children= nodeToParse.getChildNodes();
			for(int i=0;i< children.getLength();i++){
				Node child=children.item(i);
				parseNode(child);

			}

			int endOffset = this.currentOffset;
			if(nodeName.equals("ref")&& ((Element)nodeToParse).hasAttribute("type")&& ((Element)nodeToParse).getAttribute("type").equals("bible")){
				bible_in.add(startOffset);
				bible_out.add(endOffset);
			}
			addTag(nodeToParse,startOffset,endOffset);
		}else if(nodeToParse.getNodeType() == Node.TEXT_NODE){
			StringBuilder tmp=new StringBuilder();
			tmp.append(nodeToParse.getTextContent());//.append(" ");
			String tmpS = tmp.toString().replaceAll("\\s*\\n\\s+", " ");
			tmpS=tmpS.replaceAll("\\s+", " ");
			tmpS=tmpS.replaceAll("-\\s+", "-");
			tmpS=CharacterUtils.fixNonStandardCh(tmpS);
				if(inParHead){ //supplied can have just a space
					if(!tmpS.equals(" ")){
						//System.out.println("Adding text:"+ tmpS);
						//System.out.println("To text:"+ this.textToTokenize.toString());
						this.textToTokenize.append(tmpS);
						this.currentOffset += tmpS.length();
					}else{
						
						if(!this.textToTokenize.toString().matches(".*\\s")){
						//	System.out.println("Adding text:"+ tmpS);
						//	System.out.println("To text:"+ this.textToTokenize.toString());
							this.textToTokenize.append(tmpS);
							this.currentOffset += tmpS.length();
						}
					}
//				System.out.println("Adding text:"+ tmpS);
//				System.out.println("To text:"+ this.textToTokenize.toString());
//				this.textToTokenize.append(tmpS);
//				this.currentOffset += tmpS.length();
				}
		}else{
			switch(nodeName){
			case "lb":
				if(this.textToTokenize.length()>0 && !this.textToTokenize.toString().matches(".*-")&& !this.textToTokenize.toString().matches(".*\\s")){
					this.textToTokenize.append(" ");
					this.currentOffset++;
				}

				this.ln_no++;
				line_change.put(this.currentOffset, ln_no);

				break;
			case "cb":
				this.col_no++;
				column_change.put(this.currentOffset, col_no);
				break;
			case "pb":
				String pageName = nodeToParse.getAttributes().getNamedItem("n").getNodeValue();
				this.page_id=pageName;
				page_change.put(this.currentOffset, pageName);
				this.ln_no=0;
				this.col_no=0;
			}
			int startOffset = this.currentOffset;
			int endOffset = this.currentOffset;
			addTag(nodeToParse,startOffset,endOffset);
		}

	}

	/**
	 * 
	 * @param fileDesc
	 * @return doc_id
	 */

	public String getDocId(Node fileDesc){

		NamedNodeMap attributes = fileDesc.getAttributes();
		String id = attributes.getNamedItem("xml:id").getNodeValue();
		return id;
	}
	/**
	 * 
	 * @param node
	 * @param start
	 * @param end
	 * @throws Exception
	 */

	public void addTag(Node node,int start,int end)throws Exception{
		TagInfo tmp = new TagInfo();
		String tmpName = node.getNodeName();
		tmp.setName(node.getNodeName());
		tmp.setStartOffset(start);
		tmp.setEndOffset(end);
		if(node.hasAttributes()){
			tmp.createAttributeMap(node.getAttributes());
			 if(tmpName.equals("pb")){
					Map<String,String> tmpA=tmp.getTagAttributes();
					tmpA.put("xml:id", "page_"+this.page_id);
					tmpA.put("facs", this.page_id+".png");
					tmp.setTagAttributes(tmpA);
				}	
		}else if(node.getNodeType() == Node.COMMENT_NODE){
			Map<String,String> tmpA=new HashMap<String,String>();
			tmpA.put("comment",node.getTextContent());
			tmp.setTagAttributes(tmpA);
		}else if(tmpName.equals("lb")){
			Map<String,String> tmpA=new HashMap<String,String>();
			tmpA.put("n",Integer.toString(this.ln_no));
			tmp.setTagAttributes(tmpA);
			
		}else if(tmpName.equals("cb")){
			Map<String,String> tmpA=new HashMap<String,String>();
			tmpA.put("xml:id", "col_"+this.page_id+"_"+Integer.toString(this.col_no));
			tmpA.put("n",Integer.toString(this.col_no));
			tmp.setTagAttributes(tmpA);
		}
		tmp.setDoc_id(this.doc_id);
		tmp.setTag_id(this.tagNo);
		tmp.setSub_id(this.sub_id);
		String text = node.getTextContent().replaceAll("\\n\\s+", " ");
		tmp.setTextContent(text.length() < 500 ? text:text.substring(0, 499));
		this.tagNo++;

	
	if(!(tmp.getName().equals("hi")&& text.matches("\\s*"))){
	
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
		tmp_tags.add(tmp);
	}
	}

	/**
	 * 
	 * @param nodeR
	 */

	public HashMap <String,String> parseRemoved(Integer offset, RemovedTag tagR)throws Exception{

		section_id = tagR.getSection_id();
	
		HashMap <String,String> nodeResultMap= new HashMap <String,String>();
		int endOffset=offset;
		Node nodeR = tagR.getNode();
		String nodeName = nodeR.getNodeName();
		NamedNodeMap attributes = nodeR.getAttributes();
		HashMap<String,String> attr=new HashMap<String,String>();
		for(int j=0; j < attributes.getLength(); j++){
			attr.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());					 
		}
		nodeResultMap.put("name", nodeName);
		nodeResultMap.put("startOffset", offset.toString());
		nodeResultMap.put("attributes", attr.toString());


		String nodeTxt = nodeR.getTextContent().replaceAll("\\n\\s+", " ");
		if(nodeName.equals("surplus")||nodeName.equals("del")||nodeName.equals("sic")||nodeName.equals("abbr")||nodeName.equals("fw")){
			nodeResultMap.put("content", nodeTxt);
			nodeResultMap.put("endOffset", Integer.toString((offset+nodeTxt.length())));
			endOffset=offset+nodeTxt.length();
			addTag(nodeR,offset,endOffset);
		}else if(nodeName.equals("note")){
			in_note=true;
			
			endOffset = parseRemovedNote(nodeR,offset, tagR.getPage(),tagR.getLine());
			nodeResultMap.put("endOffset",Integer.toString(endOffset));
			addTag(nodeR,0,endOffset);
		}


		if(debug){
			System.out.println("Removed: " + offset+ "; "+nodeName+ "; "+ attr.toString() + "; "+ nodeTxt);
		}
		return nodeResultMap;
	}

	/**
	 * 
	 * @param note
	 * @param offset
	 * @param page
	 * @param line
	 * @return
	 */

	public int parseRemovedNote(Node note, Integer offset, String page, int line){
		//int endOffset = offset;
		int endOffset = 0;
		clearAllGlobal();


		this.page_id =page;
		if(debug){
			System.out.println("Note page:"+this.sub_id+" is "+this.page_id);
		}
		section_id += "_note";
		this.ln_no=line; 
		try{
			NodeList children= note.getChildNodes();
			for(int i=0;i< children.getLength();i++){

				parseOther(children.item(i));

			}
			tokenizeCleanAndTag();
		}catch(Exception e){ 
			System.out.println(e.getMessage());
		}
		endOffset += this.currentOffset;
		return endOffset;
	}

	/**
	 * 
	 */

	public void clearAllGlobal(){
		this.textToTokenize=new StringBuilder();
		this.tokenizerStartOffset = currentOffset= 0;
		this.line_change = new  HashMap<Integer,Integer>();
		this.page_change = new HashMap<Integer,String>();
		this.column_change =  new  HashMap<Integer,Integer>();
		this.bible_in = new LinkedList<Integer>();
		this.bible_out = new LinkedList<Integer>();

		this.inParHead=true; 

		this.tokenNo=0;
		this.tagNo=0;

		this.page_id ="0";

		this.paragraph_id = 0;
		this.ln_no=0;
		this.col_no=0; 
		
	}

	/**
	 *handleXMLdoc(String)
	 *@param path to the xml file to be treated
	 *
	 **/

	public void handleXMLdocBody(String filePath){
		//create a file
		File file  = new File(filePath);

		handleXMLFileBody(file);

	}

	/**
	 *handleXMLFileBody(File)
	 *@param path to the xml file to be treated
	 *@return 
	 *- LGeRM input as decided with Gilles Souvey
	 *- basic statistics if the parameter true
	 *
	 *
	 **/
	public  void handleXMLFileBody(File file){
		try{
			//create a file
			this.xmlFile = file;
			this.domDocument = XMLutils.getDoc(this.xmlFile);

			// build LGERM file
			final CreateLGeRMInput lGeRM  = new CreateLGeRMInput(this.tokenizer);;
			LinkedList<TagInfo> tags = XMLutils.getTagsInfo(this.domDocument, mainParts.get("body")); 
			if(debug){
				for(TagInfo tag: tags){

					System.out.println(tag.toString());
				}
			}
			HashMap <Integer,LinkedList<Integer>> inTags = new HashMap <Integer,LinkedList<Integer>>() ;
			HashMap<Integer,LinkedList<Integer>> outTags =new HashMap <Integer,LinkedList<Integer>>();
			XMLutils.buildTagIndex(tags,inTags,outTags);

			if(debug){
				System.out.println("***Getting paragraphs***");

			}			
			LinkedHashMap<Integer, String> pl= XMLutils.getParagraphs(this.domDocument,mainParts.get("body"),tags);
			if(debug){
				for(Integer key:  pl.keySet()){

					System.out.println(key+ " : " + pl.get(key));
				}
			}

			if(debug){
				System.out.println("***Building lgerm input***");
			}

			LinkedList<OutputLine> lGeRMInput = lGeRM.buildLGeRMOutputDOM(pl,tags,inTags,outTags);
			lGeRM.saveOutput(this.prop.getProperty("lgerm.out"), this.xmlFile.getName()+".dat", lGeRMInput);

			if(debug){
				for(TagInfo tag: tags){

					System.out.println(tag.toString());
				}
			}

			
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}


	/**
	 * creates data base record for the transcription
	 **/

	private HashMap<String,String> createTokenHash(TokenInfo token){
		HashMap <String, String> tmp = new HashMap<String, String>();

		tmp.put("token_id", Integer.toString(token.getToken_id()) );
		tmp.put("token",token.getToken() );
		tmp.put("final_token", token.getFinalToken().toString().replaceAll("^\\[|]$", ""));
		tmp.put("modern_token", token.getModern());
		tmp.put("start_offset",Integer.toString(token.getStartOffset()));
		tmp.put("end_offset", Integer.toString(token.getEndOffset()));
		tmp.put("doc_id", token.getDoc_id());
		tmp.put("section_id ", token.getSection_id());
		tmp.put("page_no ", token.getPageNo());
		tmp.put("col_no", Integer.toString(token.getColumnNo()));
		tmp.put("line_no", Integer.toString(token.getLineNo()));
		tmp.put("break_point", Integer.toString(token.getBreak_point()));
		tmp.put("par_id", Integer.toString(token.getPar_id()));
		tmp.put("sub_id ",token.getSub_id() );
		tmp.put("head", Integer.toString(token.getHead()));
		tmp.put("pos", token.getPos());
		tmp.put("lemma", token.getLemma());
		tmp.put("sent_no", Integer.toString(token.getSentNo()));


		return tmp;

	}
	/**
	 * creates data base record for the transcription
	 **/

	private HashMap<String,String> createTagHash(TagInfo tag){
		HashMap <String, String> tmp = new HashMap<String, String>();

		tmp.put("tag_id", Integer.toString(tag.getTag_id()) );
		tmp.put("doc_id", this.doc_id );
		tmp.put("sub_id", tag.getSub_id() );
		tmp.put("tag_name",tag.getName() );
		tmp.put("attributes", tag.getTagAttributes().toString());
		tmp.put("content",tag.getTextContent() );

		tmp.put("start_offset",Integer.toString(tag.getStartOffset()));
		tmp.put("end_offset", Integer.toString(tag.getEndOffset()));


		return tmp;

	}
	
	/*
	 * 
	 * 
	 *
	 */
	
	private String getModern(String word ,String pos,String lemma){
		//System.out.println("AM size: "+alreadyModernCache.size());
		
		String wKey= word+";"+pos+";"+lemma;
		//System.out.println("modernTo add"+ wKey);
		if(pos.equals("Fw")||pos.equals("Fs")){// it is modern
			return word;

		}else if(pos.equals("Np")||pos.equals("Nc")){
			if(pos.equals("Nc")){ // if the Nc is plural it will be different than lemma
				if(word.endsWith("s") && !lemma.toLowerCase().endsWith("s") ){
					return lemma.toLowerCase()+"s";
				}else if(word.endsWith("ux") && lemma.toLowerCase().endsWith("s")){
					String plural = lemma.toLowerCase().substring(0, lemma.toLowerCase().length()-2)+"ux";
					return plural;
				}	
			}

			return lemma.toLowerCase();     //in the case of the name  already represents the modern version of the word

		}else if(word.toLowerCase().equals(lemma.toLowerCase())){		
			return lemma.toLowerCase();
		}else if(modernCache.containsKey(wKey)){
			return modernCache.get(wKey);
		}else{ 
			String [] lemmaParts;
			String [] posParts;
			String glue="";
			if(lemma.split("\\|").length >1){
				lemmaParts = lemma.split("\\|"); 
				posParts = new String[1];
				posParts[0]=pos;
				glue="|";
			}else{
				lemmaParts= new String [1];
				lemmaParts[0]=lemma;
				posParts = new String[1];
				posParts[0]=pos;
			}

			StringBuilder modern = new StringBuilder();

			for(int i =0;i<lemmaParts.length;i++){
				String lp=lemmaParts[i];
				String pp = posParts.length > 1 ? posParts[i]:posParts[0];
				Set<String> modernCandidate;
				if(alreadyModernCache.containsKey(lp)){

					HashMap<String,Set<String>> tmpLemme = alreadyModernCache.get(lp); 
					if(tmpLemme.containsKey(pp)){
						modernCandidate = alreadyModernCache.get(lp).get(pp);

					}else{
						modernCandidate= GetFromDatabase.getModernCache(this.connection, lp,pp);
						tmpLemme.put(pp,  modernCandidate);
						alreadyModernCache.put(lp,tmpLemme);
					}

				}else{
					HashMap<String,Set<String>> tmpLemmeNew = new HashMap<String,Set<String>>();
					modernCandidate= GetFromDatabase.getModernCache(this.connection, lp,pp);
					tmpLemmeNew.put(pp,  modernCandidate);
					alreadyModernCache.put(lp,tmpLemmeNew);
				}

				String mKey= word+";"+pp+";"+lp;

				if(modernCandidate.contains(word)){
					if(i>0){modern.append(glue);}
					modern.append(word);
				}else if(modernCache.containsKey(mKey)){

					if(i>0){modern.append(glue);}
					modern.append(modernCache.get(mKey));
				}else{
					String modernTmp= GetFromDatabase.getModern(this.connection, word, pp, lp);
					if(modernTmp == null){
						if(i>0){modern.append(glue);}
						modern.append(word.toLowerCase());

					}else{
						modernCache.put(mKey, modernTmp);
						GetFromDatabase.writeModernCache(this.connection, word, pp, lp, modernTmp);
						if(i>0){modern.append(glue);}
						modern.append(modernTmp);
					}

				}
			}
			String modernString = modern.toString();
			if(modernString.equals("")){
				return word;
			}else{
				return modernString;
			}
			
		}
		//return null;
	}
	
	
	/**
	 *- writes data into the database
	 * Database info passed by a property file 
	 **/

	private void writeHashToDB(HashMap <String, String>  mapToWrite,String table,Connection connection ){

		try{
			MySQLconnection.insertHashRecordIntoTable(connection, table, mapToWrite);
		}catch(SQLException eSQL){
			eSQL.getMessage();
		}

	}

	/**
	 *- clean tables: token_body,token_front,tag_body,tag_front in the database
	 * Database info passed by a property file 
	 **/

	private void cleanTables(String doc_id , Connection connection ){

		try{
			String [] tables={"token_body","token_front","tag_body","tag_front"};
			for (String table:tables){
			MySQLconnection.cleanTable(connection, table,"doc_id" ,doc_id);
			}
		}catch(SQLException eSQL){
			eSQL.getMessage();
		}

	}

	/**
	 * 
	 * @param tokenizedString
	 * @return cleaned string, no space tokens tree tagger removes them any way
	 */
	public LinkedList<String> clearEmptyTokens(LinkedList<String> tokenizedString){
		LinkedList<String> cleanedString = new LinkedList<String>();
		for(String s : tokenizedString){
			if(!s.equals(" ")){
				cleanedString.add(s);

			}

		}

		return cleanedString; 
	}



	/**
	 * @param args
	 */    

	public static void main(String[] args) {

		try{

			XMLtoDbVrtXMLw dIFX = new XMLtoDbVrtXMLw("xml.properties");

			JFileChooser window= new JFileChooser("D:\\");
			window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int rv= window.showOpenDialog(null);

			if(rv == JFileChooser.APPROVE_OPTION){

				File window_file = window.getSelectedFile();
				if(window_file.isDirectory()){
					File[] files = window_file.listFiles();
					for(File f:files){
						
							dIFX.parseXMLFile(f);
						
					}
				}else{
					
						dIFX.parseXMLFile(window.getSelectedFile());;
					
				}

			}

		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}

	}

// utility class taking care of removed tags

	private class RemovedTag{
		private String section_id;
		private String sub_id;
		/**
		 * @return the section_id
		 */
		public String getSection_id() {
			return section_id;
		}

		/**
		 * @return the sub_id
		 */
		public String getSub_id() {
			return sub_id;
		}

		/**
		 * @param section_id the section_id to set
		 */
		public void setSection_id(String section_id) {
			this.section_id = section_id;
		}

		/**
		 * @param sub_id the sub_id to set
		 */
		public void setSub_id(String sub_id) {
			this.sub_id = sub_id;
		}

		private Node node;
		/**
		 * @return the node
		 */
		public Node getNode() {
			return node;
		}

		/**
		 * @return the page
		 */
		public String getPage() {
			return page;
		}

		/**
		 * @return the line
		 */
		public int getLine() {
			return line;
		}

		/**
		 * @param node the node to set
		 */
		public void setNode(Node node) {
			this.node = node;
		}

		/**
		 * @param page the page to set
		 */
		public void setPage(String page) {
			this.page = page;
		}

		/**
		 * @param line the line to set
		 */
		public void setLine(int line) {
			this.line = line;
		}

		private String page;
		private int line;

		RemovedTag(Node node, String page, int line){
			this.node = node;
			this.page = page;
			this.line = line;


		}

	}



}








