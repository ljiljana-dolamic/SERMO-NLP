/**
 * 
 */
package ch.unine.ILCF.SERMO;


/**
 * @author dolamicl
 * 
 * Reads the transcription word document, creates the test lgerm input and a first xml.
 * Uses properties file for database, tokenizer and output directories paths. 
 * 
 */
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

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
import ch.unine.ILCF.SERMO.LGeRM.*;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


   
public class TranscriptionHandler {
	
	/**
	 * tags to be used
	 * 
	 */
	private static final  Map<String, String> idLookupMap ;
	static{
		idLookupMap = new HashMap<String, String>();
		idLookupMap.put("Carine" ,"CSD");
		idLookupMap.put("Aurelie" , "ARE");
		idLookupMap.put("Aurélie" , "ARE");
		idLookupMap.put("Cinthia", "CiM");
		idLookupMap.put("Michaela" , "MBF");
		idLookupMap.put("Ruth", "RSL");
		idLookupMap.put("Magda", "MAG");
		idLookupMap.put("Ljiljana","LjD");

	}

	private static final Map<String, String> nameLookupMap ;
	static{
		nameLookupMap = new HashMap<String, String>();
		nameLookupMap.put("Carine" ,"Carine SKUPIEN DEKENS");
		nameLookupMap.put("Aurelie" , "Aurélie REUSSER-ELZINGRE");
		nameLookupMap.put("Aurélie" , "Aurélie REUSSER-ELZINGRE");
		nameLookupMap.put("Cinthia", "Cinthia MELI");
		nameLookupMap.put("Michaela" , "Michaela BJUGGFALT-CHÂTEAUX");
		nameLookupMap.put("Ruth", "Ruth STAWARZ-LUGINBUEHL");
		nameLookupMap.put("Magda", "Magdalena AUGUSTYN-GAULTIER");
		nameLookupMap.put("Ljiljana","Ljiljana DOLAMIC");

	}
	private static final Map<String, String> mailLookupMap ;
	static{
		mailLookupMap = new HashMap<String, String>();
		mailLookupMap.put("Carine" ,"carine.skupien-dekens@unine.ch");
		mailLookupMap.put("Aurelie" , "aurelie.elzingre@unine.ch");
		mailLookupMap.put("Aurélie" , "aurelie.elzingre@unine.ch");
		mailLookupMap.put("Cinthia", "cinthia.meli@unine.ch");
		mailLookupMap.put("Michaela" , "michaela.bjuggfalt@etu.univ-lille3.fr");
		mailLookupMap.put("Ruth", "ruth.luginbuhl@unine.ch");
		mailLookupMap.put("Magda", "magdalena.augustyn@unine.ch");
		mailLookupMap.put("Ljiljana","ljiljana.dolamic@unine.ch");

	}
	
	
	private TeiHeaderData headerData;
	
	private Document domDocument;
	private File transcriptionFile;
	
    private Map<String, LinkedList<String>> docParts;
    private Tokenizer tokenizer;
  //  private TreeTaggerHandler ttH; 
    private CreateLGeRMInput lGeRM;
    private Properties prop;
    
    
    public TranscriptionHandler(String propertiesFileName) throws Exception{
    	this.prop = SermoProperties.getProperties(propertiesFileName);
    	System.out.println(this.prop.toString());
    	 // get the tokenizer
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
    	
    		this.lGeRM = new CreateLGeRMInput(this.tokenizer);
    	}
    	
    	
    
	
//	public TranscriptionHandler(String tokenizerDictionaryPath, int window, String treeTaggerDictionaryPath){
//		this.tokenizer= new Tokenizer(tokenizerDictionaryPath, window);
//	}
	
    /**
	 *getDocParts(File)
	 *@param docx document to be read
	 *@return list of lines in both header and body parts 
	 * 
	 *
	 **/

	private Map<String, LinkedList<String>> getDocParts(File inputFile){
		// read the document
		ReadMSWordFile rMSW = new ReadMSWordFile();
        
		//split the document to body and header part
		Map<String, LinkedList<String>> parts=ParseDocParts.splitSentenceList(rMSW.readDocxFileXML(inputFile));
		 return parts;
	}
	
	/**
	 *handleTranscriptionFile(String)
	 *@param path to the doc file transcription to be treated
	 *@return transcription XML containing all formating information and tags added during the transcription etc...
	 *-builds a file from the given path and calls the method handleTranscriptionFile(String filePath)
	 *
	 **/
	
	public void handleTranscriptionFile(String filePath){
		//create a file
		File file  = new File(filePath);
		
		handleTranscriptionFile(file);
			
	}
	
	/**
	 *handleTranscriptionFile(File)
	 *@param  doc file transcription to be treated
	 *@return transcription XML containing all formating information and tags added during the transcription 
	 *-LGeRM input as decided with Gilles Souvey
	 *-creates data base record for the file if it doesn't exist
	 *
	 *
	 **/
	public void handleTranscriptionFile(File file){
		//create a file
		this.transcriptionFile = file;
		System.out.println("Treating file:" + this.transcriptionFile.getName());
		//get documents part
		this.docParts = getDocParts(this.transcriptionFile);
		// get header data
		this.headerData = getHeaderElementsFromTranscription(this.docParts.get("header"), this.transcriptionFile.getName());
		// write doc info data to database
		String fileName = writeDocInfoDataToDB(this.headerData);
		//writeDocInfoDataToDB(this.headerData);
		//StringBuilder outFileNameBuilder = new StringBuilder();
		//outFileNameBuilder.append(this.headerData.getEditionYear()).append("_").append(this.headerData.getAuteurFirstName()).append("_").append(this.headerData.getAuteurLastName()).append("_").append(this.headerData.getTranscriptionWordID());
		//String fileName = outFileNameBuilder.toString().replaceAll("\\s+", "_");  
		//build transcription XML file
		
		try{
		System.out.println("***Building xml***");	
		BuildTranscriptionXML transcriptionXML=new BuildTranscriptionXML();
		transcriptionXML.buildTranscriptionDOM(this.headerData, this.docParts.get("body"));
		this.domDocument = transcriptionXML.getDocument();
		// add docId to fileDesc 
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node = (Node) xPath.evaluate("/TEI/teiHeader/fileDesc",
				this.domDocument.getDocumentElement(), XPathConstants.NODE);
		((Element)node).setAttribute("xml:id", fileName);
	
		transcriptionXML.printXMLtoFile(this.prop.getProperty("xml.out"),fileName );
		}catch(ParserConfigurationException e){
			System.out.println("Couldn't build XML");
			e.printStackTrace();
		}catch(Exception e){
			System.out.println("Couldn't build XML 2");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		// build LGERM file
		System.out.println("***Building tag index***");
		//LinkedList<TagInfo> tags = XMLutils.getTagsInfo(this.domDocument,"/TEI/text/body/div[@type='sermon']" );
		LinkedList<TagInfo> tags = XMLutils.getTagsInfo(this.domDocument,"/TEI/text" ); 
		for(TagInfo tag: tags){
			
			System.out.println(tag.toString());
		}
		HashMap <Integer,LinkedList<Integer>> inTags = new HashMap <Integer,LinkedList<Integer>>() ;
		HashMap<Integer,LinkedList<Integer>> outTags =new HashMap <Integer,LinkedList<Integer>>();
		XMLutils.buildTagIndex(tags,inTags,outTags);
		
	////	for(Integer key:  inTags.keySet()){
			
	//		System.out.println(key+ " : " + inTags.get(key));
	//	}
        
     //   for(Integer key:  outTags.keySet()){
			
	//		System.out.println(key+ " : " + outTags.get(key));
	//	}
		System.out.println("***Getting paragraphs***");
		//LinkedHashMap<Integer, String> pl= XMLutils.getParagraphs(this.domDocument,"/TEI/text/body/div[@type='sermon']",tags);
		//LinkedHashMap<Integer, String> plFront= XMLutils.getParagraphs(this.domDocument,"/TEI/text/front",tags);
		//LinkedHashMap<Integer, String> plBody= XMLutils.getParagraphs(this.domDocument,"/TEI/text/body",tags);
		LinkedHashMap<Integer, String> pl= XMLutils.getParagraphs(this.domDocument,"/TEI/text",tags);
		//for(Integer key:  pl.keySet()){
			
		//	System.out.println(key+ " : " + pl.get(key));
	//	}
		//LinkedList<OutputLine> lGeRMInput = lGeRM.buildLGeRMOutputListOffset(this.docParts.get("body"));
		
		System.out.println("***Building lgerm input***");
		//LinkedList<OutputLine> lGeRMInputFront = lGeRM.buildLGeRMOutputDOM(plFront,tags,inTags,outTags);
		//LinkedList<OutputLine> lGeRMInputBody = lGeRM.buildLGeRMOutputDOM(plBody,tags,inTags,outTags);
		LinkedList<OutputLine> lGeRMInput=lGeRM.buildLGeRMOutputDOM(pl,tags,inTags,outTags);;
		//lGeRMInput.addAll(lGeRMInputBody);
		lGeRM.saveOutput(this.prop.getProperty("lgerm.out"), fileName, lGeRMInput);
		
	//	LinkedList<TagInfo> tags = XMLutils.getTagsInfo(this.domDocument,"/TEI/text/body" ); 
		
	//	for(TagInfo tag: tags){
			
		//	System.out.println(tag.toString());
		//}
		
	}
	
	/**
	 * reads header information from the transcription
	 * **/
	private TeiHeaderData getHeaderElementsFromTranscription( LinkedList<String> headerPart, String fileName){
		TeiHeaderData teiHeaderData = new TeiHeaderData(); 
		Pattern patternDC = Pattern.compile("(.*?):(.*)");
		Pattern source_num = Pattern.compile("<source_num>(.*)</source_num>");
		for(String s: headerPart){
			// System.out.println(s);
			Matcher matcherDC = patternDC.matcher(s);
			Matcher matcher_sc = source_num.matcher(s);
			if(matcher_sc.find()){
				String source = matcher_sc.group(1);
				teiHeaderData.setSourceNumerique(source);
			} else if(matcherDC.find()){
				String left = matcherDC.group(1);
				String right = matcherDC.group(2);	
				// System.out.println("left: "+left);
				// System.out.println("right: "+right);

				if(left.matches(".*Titre.*")){
					teiHeaderData.setTitreDuRecueil(right.trim().replaceAll("<.*?>", ""));
					 System.out.println(right.trim());
				}else if(left.trim().matches(".*Auteur.*")){
					String [] nameParts=right.trim().split("\\s");
					
					if (nameParts.length>1){
						if(nameParts.length == 2){
							teiHeaderData.setAuteurFirstName(nameParts[0]);
							teiHeaderData.setAuteurLastName(nameParts[1]);
						}else{
							if(nameParts[1].length() > 3){
								StringBuilder firstName=new StringBuilder();
								firstName.append(nameParts[0]).append(" ").append(nameParts[1]);
								teiHeaderData.setAuteurFirstName(firstName.toString().trim());
								StringBuilder lastName=new StringBuilder();
								for(int i=2;i<nameParts.length;i++){
									lastName.append(nameParts[i]).append(" ");
								}
								teiHeaderData.setAuteurLastName(lastName.toString().trim());
							}else{
								teiHeaderData.setAuteurFirstName(nameParts[0]);
								StringBuilder lastName=new StringBuilder();
								for(int i=1;i<nameParts.length;i++){
									lastName.append(nameParts[i]).append(" ");
								}
								teiHeaderData.setAuteurLastName(lastName.toString().trim());
							}
						}

					}else{
						teiHeaderData.setAuteurFirstName("");
						teiHeaderData.setAuteurLastName(nameParts[0]);
					}
					//teiHeaderData.setAuteur(right.trim());
				}else if(left.trim().matches(".*Transcription.*")){
					String [] parts = right.split(",");
					if(parts.length == 3){
						String name="";
						teiHeaderData.setTranscriptionDate(parts[2]);
						if(nameLookupMap.containsKey(parts[1].trim())){
							name=parts[1].trim();
						}else if(nameLookupMap.containsKey(parts[0].trim())){
							name=parts[0].trim();
						} 
						teiHeaderData.setTranscriptionWord(nameLookupMap.get(name));
						teiHeaderData.setTranscriptionWordID(idLookupMap.get(name));
						teiHeaderData.setTranscriptionWordMail(mailLookupMap.get(name));
					}else{
						teiHeaderData.setTranscriptionDate(parts[1]);
						String [] nameParts= parts[0].split("\\s+");
						for(String name:nameParts){
							name=name.trim();

							if(nameLookupMap.containsKey(name)){

								teiHeaderData.setTranscriptionWord(nameLookupMap.get(name));
								teiHeaderData.setTranscriptionWordID(idLookupMap.get(name));
								teiHeaderData.setTranscriptionWordMail(mailLookupMap.get(name));

							} 
						}

					}
				} 
			}  

		}
		
		if (fileName.indexOf(".") > 0) {
		    teiHeaderData.setFileName( fileName.substring(0, fileName.lastIndexOf(".")));
		}
		Pattern edYearPattern = Pattern.compile("^((\\d{4})_\\p{L}+(?:[^\\p{L}]+\\p{L}+)?).*");
		Matcher edYearMacher = edYearPattern.matcher(fileName);
		if(edYearMacher.find()){

			teiHeaderData.setEditionYear(edYearMacher.group(2));
			
		}else{
			teiHeaderData.setEditionYear("unknown");
		}

		return teiHeaderData;

	}
	
	/**
	 * creates data base record for the transcription
	 **/
	private HashMap<String,String> createDocInfoMapForSql(TeiHeaderData headerData){
		HashMap <String, String> tmp = new HashMap<String, String>();
		// docId editionYear+authorName+authorLastName+transcriptorID
		StringBuilder docId =new StringBuilder();
		docId.append("_");
		docId.append(headerData.getEditionYear()).append("_");
		docId.append(headerData.getAuteurFirstName()).append("_").append(headerData.getAuteurLastName()).append("_").append(headerData.getTranscriptionWordID());
		
		
		tmp.put("doc_id", docId.toString().replaceAll("\\s", "_"));
		tmp.put("doc_name", headerData.getTitreDuRecueil());
		tmp.put("author_first_name", headerData.getAuteurFirstName());
		tmp.put("author_last_name", headerData.getAuteurLastName());
		tmp.put("edition_year",headerData.getEditionYear());
		
		
		return tmp;
		
	}
	/**
	 *- writes data into the database
	 * Database info passed by a property file 
	 **/
	
	@SuppressWarnings("finally")
	private String writeDocInfoDataToDB(TeiHeaderData headerData){
		HashMap <String, String> mapToWrite = createDocInfoMapForSql(headerData);
		
		try{
			if(this.prop.getProperty("writeToDB").equals("true")){
		Connection connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
				this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
		MySQLconnection.insertHashRecordIntoTable(connection, "documents_info", mapToWrite);
		}
		}catch(SQLException eSQL){
			eSQL.getMessage();
		}finally{
			return mapToWrite.get("doc_id");
		}
		
	}
	
    
	
	public static void main(String[] args) {
		try{
		TranscriptionHandler tH = new TranscriptionHandler("lilyann.properties");
		JFileChooser window= new JFileChooser("D:\\ljiljana.dolamic\\test_data");
		window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int rv= window.showOpenDialog(null);
		
		if(rv == JFileChooser.APPROVE_OPTION){
			///resultXML.buildTranscriptionXML(window.getSelectedFile());  
			//String fileName=window.getSelectedFile().getName();
		  //  String [] fileNameParts= fileName.split("_");
			File window_file = window.getSelectedFile();
			if(window_file.isDirectory()){
				File[] files = window_file.listFiles();
				for(File f:files){
					tH.handleTranscriptionFile(f);
				}
			}else{
				tH.handleTranscriptionFile(window.getSelectedFile());
			}
	//	JFileChooser window= new JFileChooser();
	//	int rv= window.showOpenDialog(null);

		//if(rv == JFileChooser.APPROVE_OPTION){
			
	//		tH.handleTranscriptionFile(window.getSelectedFile());
			
		}
		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}
		
	}


}

