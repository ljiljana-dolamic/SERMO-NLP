/**
 * 
 */
package ch.unine.ILCF.SERMO;

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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
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
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.XML.Utils.TeiHeaderData;
import ch.unine.ILCF.SERMO.propreties.*;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SSplit.SCNlpSSplit;
import ch.unine.ILCF.SERMO.SSplit.SentenceInfo;
import ch.unine.ILCF.SERMO.LGeRM.*;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
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

public class TokenInfoFromXML {


	private boolean debug = false;

	private String doc_id;


	//private TeiHeaderData headerData;
	private String docIdPath="/TEI/teiHeader/fileDesc";
	static HashMap<String,String> mainParts;
	static{
		mainParts=new HashMap<String,String>();
		mainParts.put("body","/TEI/text/body");
		mainParts.put("front","/TEI/text/front");

	}
	//	private String bodyPath="/TEI/text/body";
	//	private String frontPath="/TEI/text/front";

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
	private SCNlpSSplit sSplitter;
	//  private CreateLGeRMInput lGeRM;
	private Properties prop;
	//  private static List<String> dictionary = new ArrayList<String>();
	private LinkedList<TokenInfo> front_tokens;
	private LinkedList<TokenInfo> body_tokens;
	private LinkedList<TagInfo> front_tags;
	private LinkedList<TagInfo> body_tags;

	private int currentOffset=0;
	private int tokenizerStartOffset=0;

	private HashMap<Integer,LinkedList<RemovedTag>> removedNodesFront;
	private HashMap<Integer,LinkedList<RemovedTag>> removedNodesBody;
	private HashMap<String,RemovedTag> removedNodesNote;

	//private LinkedList<TokenInfo> note_tokens;
	// private LinkedList<TagInfo> note_tags;

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

	public TokenInfoFromXML(String propertiesFileName) throws Exception{
		this.prop = SermoProperties.getProperties(propertiesFileName);
		//System.out.println(this.prop.toString());
		// get the tokenizer
		System.out.println("Loading tokenizer");
		if(this.prop.containsKey("tokenizer.path")){
			if(this.prop.containsKey("tokenizer.window")){
				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"),Integer.parseInt(this.prop.getProperty("tokenizer.window")));
			}else{
				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"));// default tokenizer window
			}

		}else{
			//System.out.println("should throw");
			throw(new Exception("Error: 'tokenizer.path' propertiy  missing in "+ propertiesFileName));
		}

		// dictionary
		System.out.println("Loading dictionary");
		if(this.prop.containsKey("dictionary.lex")){

		//	CharacterUtils.loadDictionary(this.prop.getProperty("dictionary.lex"));
		}else{
			System.out.println("*** NO DICTIONARY LOADED ***");
		}
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
			System.out.println("Loading TreeTagger postDict");
			if(this.prop.containsKey("postProc.dico")){

				this.ttH.loadPostDictionary(this.prop.getProperty("postProc.dico"));
			}
			//			    			if(this.prop.containsKey("charUtils.dico")){
			//				    			CharacterUtils.loadDictionary(this.prop.getProperty("charUtils.dico"));
			//				    		}

		}else{
			throw(new Exception("Error: 'treetagger.home' or 'treetagger.model' propertiy  missing in "+ propertiesFileName));
		}
		//Building CWB vrt
		
		
		//Building w-xml
		
		//sSplitter= new SCNlpSSplit();
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

			this.removedNodesFront = new HashMap<Integer,LinkedList<RemovedTag>>();
			this.removedNodesBody = new HashMap<Integer,LinkedList<RemovedTag>>();
			this.removedNodesNote =new HashMap<String,RemovedTag>();

			this.textToTokenize=new StringBuilder();
			this.line_change = new  HashMap<Integer,Integer>();
			this.page_change = new HashMap<Integer,String>();
			this.column_change =  new  HashMap<Integer,Integer>();
			this.bible_in = new LinkedList<Integer>();
			this.bible_out = new LinkedList<Integer>();


			for(String s :  mainParts.keySet()){
				this.doc_part=s;
				section_id=s;
				this.tokenNo=0;
				this.tagNo=0;
				if(debug){
					System.out.println("Section_Id: "+ section_id);
				}
				parsePartNode(XMLutils.getNode(this.domDocument, mainParts.get(s)));
				// tokenizeAndClean();

			}

			saveRemoved();

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
			connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
            
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

			MySQLconnection.closeConnection(connection);
		}


	}
	/**
	 * 
	 */

	public void saveRemoved()throws Exception{



		int tmp_id =0;
		for(Integer removedOffest: this.removedNodesFront.keySet() ){
			LinkedList<RemovedTag> rOremoved = this.removedNodesFront.get(removedOffest);
			this.doc_part="front";
			//this.sub_id ="0";

			for(RemovedTag rt: rOremoved){
				clearAllGlobal();
				this.sub_id =rt.getNode().getNodeName()+"_"+removedOffest+"_"+tmp_id;
				HashMap<String,String> tmpFront = parseRemoved(removedOffest,rt); 
				if(debug){
					System.out.print("*** Removed tag ***  ");

					System.out.println(this.sub_id+ ": "+tmpFront.toString());


				}else{


				}


				tmp_id++;

			}

		}
		tmp_id =0;
		for(Integer removedOffest: this.removedNodesBody.keySet() ){
			LinkedList<RemovedTag> rOremoved = this.removedNodesBody.get(removedOffest);
			this.doc_part="body";
			//this.sub_id ="0";
			for(RemovedTag rt: rOremoved){
				clearAllGlobal();
				this.sub_id =rt.getSub_id()+"_"+rt.getNode().getNodeName()+"_"+removedOffest+"_"+tmp_id;
				HashMap<String,String> tmpBody = parseRemoved(removedOffest,rt); 
				if(debug){
					System.out.print("*** Removed tag ***  ");

					System.out.println(this.sub_id+ ": "+tmpBody.toString());


				}else{



				}


				tmp_id++;

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
		addTag(nodeToParse,startOffset,endOffset);
		this.inParHead=false;
		if(debug){
			//System.out.println("Text to tokenize: "+ textToTokenize.toString());
			System.out.println("Par: "+ this.paragraph_id+"; Page:"+ this.page_id);
		}
		this.page_id = pageTmp;
		this.col_no = colTmp;
		//tokenizeAndClean();
		tokenizeCleanAndTag();
		
		if(debug){
			//System.out.println("Text to tokenize: "+ textToTokenize.toString());
			System.out.println("Page out:"+ this.page_id);
		}
	}

	/**
	 *  tokenize and clean the paragraphe or title 
	 */
	public void tokenizeAndClean(){
		if(debug){
			//			System.out.println("Srrfgtdjesrhdf: "+ this.textToTokenize);
			//			System.out.println(this.bible_in.toString());
			//			System.out.println(this.bible_out.toString());
		}
		LinkedList<String> tmpTokens;
		int tmpOffset= this.tokenizerStartOffset;
		int in_bible_offset = this.tokenizerStartOffset;
		int in_bible_ln=0;
		int in_bible_col=0;
		int break_point=0;
		String in_bible_page="";
		boolean in_bible = false;
		StringBuilder in_bible_token = new StringBuilder(); 
		if(this.textToTokenize.length()>0){
			tmpTokens=this.tokenizer.tokenize(this.textToTokenize.toString());	
			if(debug){	
				System.out.println("TOKENS: "+ tmpTokens.toString());
			}
			for(String token:tmpTokens){
				for(int i = 0; i<token.length();i++){	
					if(this.column_change.containsKey(tmpOffset+i)){this.col_no = this.column_change.get(tmpOffset+i); break_point=i;}
					if(this.line_change.containsKey(tmpOffset+i)){this.ln_no = this.line_change.get(tmpOffset+i);break_point=i;}
					if(this.page_change.containsKey(tmpOffset+i)){this.page_id = this.page_change.get(tmpOffset+i);break_point=i;}
				}
				if(!in_bible && !this.bible_in.isEmpty() && tmpOffset >= this.bible_in.peek()){
					if(debug){
						System.out.println("got it"+ tmpOffset);
					}

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
							addToken(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point);
							tmpOffset += in_bible_token.toString().length();
						}
						in_bible_token.setLength(0);
					}
					if(!this.bible_in.isEmpty() && tmpOffset >= this.bible_in.peek()){
						in_bible=true;
					}

				}
				if(in_bible){
					if(token.equals(".")){ 
						if(in_bible_token.length() >0 ){
							in_bible_token.append(token);
						}else{
							addToken(token, tmpOffset,this.ln_no,this.col_no,this.page_id,break_point);
							tmpOffset += token.length();

						}
					}else{ 
						if(in_bible_token.length()>0){
							addToken(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point);
							tmpOffset += in_bible_token.toString().length();
							in_bible_token.setLength(0);
						}


						in_bible_offset = tmpOffset;
						in_bible_ln = this.ln_no;
						in_bible_col = this.col_no;
						in_bible_page = this.page_id;
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
						addToken(token, tmpOffset,this.ln_no,this.col_no,this.page_id,break_point);
						tmpOffset += token.length();
					}
				}


			}
			if(in_bible_token.length()>0){

				addToken(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point);
			}
		}
		if(debug){
			System.out.println(this.bible_in.toString());
			System.out.println(this.bible_out.toString());
		}
		this.textToTokenize=new StringBuilder();
		this.tokenizerStartOffset = this.currentOffset;
		this.line_change = new  HashMap<Integer,Integer>();
		this.page_change = new HashMap<Integer,String>();
		this.column_change =  new  HashMap<Integer,Integer>();
		this.bible_in = new LinkedList<Integer>();
		this.bible_out = new LinkedList<Integer>();
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
		//int sent_no=1;
		String in_bible_page="";
		boolean in_bible = false;
		StringBuilder in_bible_token = new StringBuilder(); 

		// HashMap<Integer,Integer> sentenceIndex = getSentencesIndex(this.textToTokenize.toString(),tmpOffset);

		if(this.textToTokenize.length()>0){
			tmpTokens=this.tokenizer.tokenize(this.textToTokenize.toString());	
			if(debug){	
				System.out.println("TOKENS: "+ tmpTokens.toString());
			}
			for(String token:tmpTokens){
				int tokenStartOffset=tmpOffset; 
				break_point=0;
				int nextLn=0;
				int nextCol=0;
				String nextPage="";
				boolean nextLnB = false;
				boolean nextColB = false;
				boolean nextPageB = false;
				//				if(sentenceIndex.containsKey(tmpOffset)){
				//					sent_no = sentenceIndex.get(tmpOffset);
				//				} 

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
					if(debug){
						System.out.println("got it"+ tmpOffset);
					}

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
							TokenInfo tmp = addTokenTmp(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point,sent_no);
							if(tmp !=null){
								tmpTokensInfo.add(tmp);
								for(String ds:tmp.getFinalToken()){
									toTag.add(ds);
								}
								//toTag.add(in_bible_token.toString());
							}
							tmpOffset += in_bible_token.toString().length();
						}
						in_bible_token.setLength(0);
					}
					if(!this.bible_in.isEmpty() && tmpOffset >= this.bible_in.peek()){
						in_bible=true;
					}

				}
				if(in_bible){
					if(in_bible_token.length()>0){
						if(token.equals(".") ){
							//&& !CharacterUtils.dictionary.contains(in_bible_token.toString().toLowerCase())){ ////warning does'nt work
							System.out.println("in_bible_token before:"+in_bible_token);
							in_bible_token.append(token);
							
						System.out.println("in_bible_token after:"+in_bible_token);
						}else{ 
							System.out.println("ib:"+in_bible_token.toString());
							TokenInfo tmp = addTokenTmp(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point,this.sent_no);
							if(tmp !=null){
								tmpTokensInfo.add(tmp);
								for(String ds:tmp.getFinalToken()){
									toTag.add(ds);
								}
								//toTag.add(in_bible_token.toString());
							}
							//addToken(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point);
							tmpOffset += in_bible_token.toString().length();
							in_bible_token.setLength(0);
							if(token.matches("\\s+")){
								tmpOffset += token.length();
								//tmpOffset += 1;
							}else{
//							
								in_bible_token.append(token);
							}	
						}
					}else{
						if(token.matches("\\s+")){
							tmpOffset += token.length();
							//tmpOffset += 1;
						}else{
//						
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
						TokenInfo tmp = addTokenTmp(token, tmpOffset,this.ln_no,this.col_no,this.page_id,break_point,sent_no);
						if(tmp !=null){
							tmpTokensInfo.add(tmp);
							for(String ds:tmp.getFinalToken()){
								toTag.add(ds);
							}
							//toTag.add(token);
						}
						//addToken(token, tmpOffset,this.ln_no,this.col_no,this.page_id,break_point);
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
					//toTag.add(in_bible_token.toString());
				}
				//addToken(in_bible_token.toString(), in_bible_offset,in_bible_ln,in_bible_col,in_bible_page,break_point);
			}
		}

		// perform tagging and add info to tmpTokens Info

		//String[] ttArray = toTag.toArray(new String [toTag.size()]);
		try{

			tagResult = this.ttH.run(toTag);
			/// add pos and lemma to tokens info


			for(TokenInfo tf:tmpTokensInfo){

				StringBuilder pos= new StringBuilder();
				StringBuilder lemma= new StringBuilder();
				/// get pos and lemma for each part of final token and join them with +
				for(String ftp : tf.getFinalToken()){
					TtOutputLine ttDL=tagResult.pop();
					//System.out.println(ttDL.toString());
					if(pos.length()!=0){
						pos.append("+");
						lemma.append("+");
					}
					pos.append(ttDL.getPos());
					lemma.append(ttDL.getLemma());
				}
				tf.setPos(pos.toString());
				tf.setLemma(lemma.toString());

				//change sentence number after strong punctuation
				tf.setSentNo(sent_no);

				if(pos.toString().equals("Fs")){
					sent_no++;
				}

			}
		}catch(Exception e){
			System.out.println("!!!Tagging problem!!!:"+ e.getMessage());
		}	


		// add tmpTokensInfo to corresponding tokens  

		if(this.doc_part.equals("front")){

			this.front_tokens.addAll(tmpTokensInfo);

		}else{

			this.body_tokens.addAll(tmpTokensInfo);

		}		


		if(debug){
			System.out.println(this.bible_in.toString());
			System.out.println(this.bible_out.toString());
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

	private HashMap<Integer, Integer> getSentencesIndex(String string, int offset) {

		// TODO Auto-generated method stub
		HashMap<Integer, Integer> sentenceIndex= new HashMap<Integer, Integer>();

		LinkedList<SentenceInfo> sentences = this.sSplitter.getSentences(string);
		System.out.println("Par: "+string);
		System.out.println("No Sent: "+sentences.size());
		for(SentenceInfo si:sentences){
			System.out.println(si.toString());
			sentenceIndex.put(si.getStartOffset()+offset, si.getSentenceId()+1);

		}

		return sentenceIndex;
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

			if(this.doc_part.equals("front")){
				//if(section_id.equals("note")){
				//	this.note_tokens.add(tokenInfo);
				//	}else{
				this.front_tokens.add(tokenInfo);
				//	}
			}else{
				//if(section_id.equals("note")){
				//	this.note_tokens.add(tokenInfo);
				//}else{
				this.body_tokens.add(tokenInfo);
				//}
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

		// remove nodes not to be into the token list
		if(
				//	!section_id.equals("note")&&
				(nodeName.equals("note")||nodeName.equals("fw")||nodeName.equals("surplus")||nodeName.equals("del")||
						((nodeName.equals("sic")||nodeName.equals("abbr")) && nodeToParse.getParentNode().getNodeName().equals("choice")))){
			if(section_id.matches(".*_note")){

				String text = nodeToParse.getTextContent().replaceAll("(?s)\\s+", " ");


				String tmp_id = this.sub_id+"_"+nodeName+"_"+this.currentOffset;


				TagInfo tmpTag  = new TagInfo();
				tmpTag.setDoc_id(this.doc_id);
				tmpTag.setTag_id(0);
				//this.tagNo++;
				tmpTag.setName(nodeName);
				tmpTag.createAttributeMap(nodeToParse.getAttributes());;
				tmpTag.setStartOffset(this.currentOffset);
				tmpTag.setEndOffset(this.currentOffset+text.length());
				tmpTag.setTextContent(text);
				tmpTag.setSub_id(tmp_id);


				if(doc_part.equals("front")){
					//if(section_id.equals("note")){
					//	note_tags.add(tmp);
					//}else{
					front_tags.add(tmpTag);
					//front_tokens.add(tmp);
					//}
				}else if(doc_part.equals("body")){
					//if(section_id.equals("note")){
					//	note_tags.add(tmp);
					//}else{
					body_tags.add(tmpTag);
					//body_tokens.add(tmp);
					//}
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

					//tmp.add(nodeToParse);
					tmp.add(removedTag);
					removedNodes.put(this.currentOffset, tmp);
				}else{
					//removedNodes.get(currentOffset).add(nodeToParse);
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
			if(nodeName.equals("bibl")){
				bible_in.add(startOffset);
				bible_out.add(endOffset);
			}
			addTag(nodeToParse,startOffset,endOffset);
		}else if(nodeToParse.getNodeType() == Node.TEXT_NODE){
			StringBuilder tmp=new StringBuilder();
			tmp.append(nodeToParse.getTextContent());//.append(" ");
			if(debug){
				System.out.println("Text before: "+ tmp);
			}
			String tmpS = tmp.toString().replaceAll("\\n\\s+", "");
			tmpS=tmpS.replaceAll("\\s+", " ");
			tmpS=tmpS.replaceAll("-\\s", "-");
			tmpS=CharacterUtils.fixNonStandardCh(tmpS);
			if(debug){
				System.out.println("Text after: "+ tmpS);
			}
			if(inParHead && !tmpS.matches("\\s+")){

				this.textToTokenize.append(tmpS);
				this.currentOffset += tmpS.length();
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
		tmp.setName(node.getNodeName());
		tmp.setStartOffset(start);
		tmp.setEndOffset(end);
		if(node.hasAttributes()){
			tmp.createAttributeMap(node.getAttributes());
		}else if(node.getNodeType() == Node.COMMENT_NODE){
			Map<String,String> tmpA=new HashMap<String,String>();
			tmpA.put("comment",node.getTextContent());
			tmp.setTagAttributes(tmpA);
		}			 
		tmp.setDoc_id(this.doc_id);
		tmp.setTag_id(this.tagNo);
		tmp.setSub_id(this.sub_id);
		String text = node.getTextContent().replaceAll("\\n\\s+", " ");
		tmp.setTextContent(text.length() < 500 ? text:text.substring(0, 499));
		this.tagNo++;
		if(!(tmp.getName().equals("hi")&& text.equals("\\s+"))){
		if(doc_part.equals("front")){
			//if(section_id.equals("note")){
			//	note_tags.add(tmp);
			//}else{
			front_tags.add(tmp);
			//}
		}else if(doc_part.equals("body")){
			//if(section_id.equals("note")){
			//	note_tags.add(tmp);
			//}else{
			body_tags.add(tmp);
			//}
		}else{
			throw new Exception("*** Doc part "+doc_part+"not allowed ***");
		}
		}
	}

	/**
	 * 
	 * @param nodeR
	 */

	public HashMap <String,String> parseRemoved(Integer offset, RemovedTag tagR)throws Exception{

		section_id = tagR.getSection_id();
		//this.sub_id = tagR.getSub_id();

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
		}else if(nodeName.equals("note")){
			endOffset = parseRemovedNote(nodeR,offset, tagR.getPage(),tagR.getLine());
			nodeResultMap.put("endOffset",Integer.toString(endOffset));
		}

		addTag(nodeR,offset,endOffset); 

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
		int endOffset = offset;
		clearAllGlobal();


		this.page_id =page;

		System.out.println("Note page:"+this.sub_id+" is "+this.page_id);

		section_id += "_note";
		this.ln_no=line; 
		try{
			NodeList children= note.getChildNodes();
			for(int i=0;i< children.getLength();i++){

				parseOther(children.item(i));

			}
			tokenizeCleanAndTag();
			//tokenizeAndClean();
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

			if(this.prop.containsKey("baseStat")||this.prop.getProperty("baseStat").equals("true")){
				getBaseStat(lGeRMInput,this.xmlFile.getName());
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



	public void getBaseStat(LinkedList<OutputLine> lGeRMInput, String name){
		int noToken=0;
		int noPunct=0;
		int tokenLength=0;

		String year=name.split("_")[0];
		for(OutputLine ol:lGeRMInput){
			if(!ol.getTag().equals("tag")){
				if(!ol.getWord().equals(" ") && !ol.getWord().equals("	")){

					String w=(ol.getWord()).replaceAll("<.*?>", "");
					if(w.equals(".")||w.equals(",")||w.equals("?")||w.equals("!")||w.equals(":")||w.equals(";")||w.equals("-")||w.equals("(")||w.equals(")")||w.equals("*")){
						noPunct++;
					}else{
						noToken++;
						if(debug){
							System.out.println(w);
						}
						tokenLength+=w.length();
					}
				}

			}
		}
		Double avg = (double)tokenLength/noToken;
		if(debug){
			System.out.println(name+"\t"+year+"\t"+noToken+"\t"+tokenLength+"\t"+avg+"\t"+noPunct);
		}
	}

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

			TokenInfoFromXML dIFX = new TokenInfoFromXML("xml.properties");

			JFileChooser window= new JFileChooser("D:\\ljiljana.dolamic\\test_data");
			window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int rv= window.showOpenDialog(null);

			if(rv == JFileChooser.APPROVE_OPTION){

				File window_file = window.getSelectedFile();
				if(window_file.isDirectory()){
					File[] files = window_file.listFiles();
					for(File f:files){
						if(dIFX.prop.getProperty("parse").equals("true")){
							dIFX.parseXMLFile(f);;
						}else{
							dIFX.handleXMLFileBody(f);
						}
					}
				}else{
					if(dIFX.prop.getProperty("parse").equals("true")){
						dIFX.parseXMLFile(window.getSelectedFile());;
					}else{
						dIFX.handleXMLFileBody(window.getSelectedFile());
					}
				}

			}

		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}

	}



}








