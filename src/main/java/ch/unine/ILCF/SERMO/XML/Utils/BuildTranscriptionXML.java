/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

/**
 * @author dolamicl
 *
 */

import java.util.List;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashMap;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.*;
import javax.swing.JFileChooser;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.OutputKeys;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import ch.unine.ILCF.SERMO.File.Utils.ReadMSWordFile;
import ch.unine.ILCF.SERMO.File.Utils.ParseDocParts;
import ch.unine.ILCF.SERMO.LGeRM.OutputLine;
import ch.unine.ILCF.SERMO.XML.Utils.TeiHeaderData;
import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;

public class BuildTranscriptionXML {

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
	private HashMap<String,Integer> tagCode=new HashMap<String,Integer>();
	//	private String idCarine ="CSD";
	//	private String idAurelie = "ARE";
	//	private String idCinthia ="CiM";
	//	private String idMikaela = "MBF";
	//	private String idRuth = "RSL";
	//	private String idMagda = "MAC";
	//	private String idLjiljana ="LjD";
	//	
	private TeiHeaderData headerData= new TeiHeaderData();

	//TEI HEADER
	private String ROOT ="TEI";

	private String HEADER = "teiHeader";
	private String FILEDESC = "fileDesc";

	private String TITLESTMT = "titleStmt";
	private String TITLE = "title";
	private String EDITOR = "editor";
	private String AUTHOR = "author";

	private String EDITIONSTMT = "editionStmt";
	private String EDITION ="edition";
	private String RESPSTMT = "respStmt";
	private String RESP = "resp";
	private String PERSNAME = "persName";
	private String NAME = "name";
	private String ORGNAME = "orgName";
	private String AFF ="affiliation";
	private String MAIL = "email";
	private String XMLID = "xml:id";

	private String UNINE = "Université de Neuchâtel";
	private String SERMO ="Projet SERMO";
	private String SERMOURL = "http:\\\\sermo.unine.ch";
	private String TRANSELECT = "Transcription électronique";
	private String RESPONSABLE ="Responsable du projet";
	private String TRANSCRIPTION = "Transcription word";
	private String ENCODING = "Encodage XML TEI";
	private String ANNOTATION = "Annotation";

	private String PUBLICATIONSTMT ="publicationStmt";
	private String DISTRIBUTOR = "distributor";
	private String ADDRESS = "address";
	private String ADDRLINE = "addrLine";

	private String DATE = "date";

	private String SOURCEDESC = "sourceDesc";
	private String CHANGE = "change";
	private String WHO = "who";
	private String WHEN = "when";

	private String FIGURE = "figure";
	private String BODY = "body";

	private String PARAGRAPH = "p";
	private String HEADING = "head";

	private String T1 = "main";
	private String T2 = "sub";

	private String PAGEBREAK = "pb";
	private String LINEBREAK = "lb";
	private String COLUMNBREAK = "cb";
	private String NUMBER = "n";

	private String REFERENCE = "ref";
	private String RECLAME = "fw";

	private String NOTE = "note";
	private String CHARACTER = "c";
	private String GLYPH = "g";
	private String TYPE = "type";
	private String PLACE = "place";
	
	private String BOOKTITLE="book";
	private String SERMONBODY="sermon";
	
	



	private String BIBL = "bibl";
	private String CITATION = "quote";
	private String SITUATION = "seg";
	private String DD ="q";

	// standard
	private String HIGHLIGHT="hi";
	//highlite attributes
//	private String BOLD = "G";
//	private String ITALIC = "I";
//	private String UNDERLINE = "S";
//	private String SUPERSCRIPT = "E";
//	private String SUBSCRIPT = "i";
//	private String BOLDITALIC = "GI";
//	private String BOLDSS = "GE";

    
    private int currentDivLevel=0;
   // private Node currentDiv;

	private String inputFileName;
	private String outputFileName;
	private String editionYear;
	private LinkedList<String> headerPart;
	private LinkedList<String> bodyPart;

	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder ;
	private Document doc ;
	Element rootElement;

	public BuildTranscriptionXML()throws ParserConfigurationException{
		this.docFactory = DocumentBuilderFactory.newInstance();
		this.docBuilder = docFactory.newDocumentBuilder();
		this.doc = docBuilder.newDocument();
		this.tagCode.put("source_num", 0);
		this.tagCode.put("pb", 1);
		this.tagCode.put("sc", 2);
		this.tagCode.put("lb", 3);
		this.tagCode.put("reclame", 4);
		this.tagCode.put("t", 5);
		this.tagCode.put("t1", 6);
		this.tagCode.put("t2", 6);
		this.tagCode.put("t3", 6);
		this.tagCode.put("t4", 6);
		this.tagCode.put("date_sermon", 7);
		this.tagCode.put("footnote", 8);
		this.tagCode.put("manchette", 9);
		this.tagCode.put("lettrine", 10);
		this.tagCode.put("bibl", 11);
		this.tagCode.put("ref", 12);
		this.tagCode.put("citation", 13);
		this.tagCode.put("discours_direct", 14);
		this.tagCode.put("situation", 15);
		this.tagCode.put("hi", 16);
		this.tagCode.put("pdm",17);
		this.tagCode.put("image",18);
		this.tagCode.put("par",19);
		this.tagCode.put("note", 20);

	}

//	//////////////
//	public void buildSimpleTranscriptionXML(File file){
//		this.inputFileName= (file.getName().split("\\."))[0]; //keeping only filename without the extention
//		LinkedList<String> sentenceList = new LinkedList<String>() ;
//		ReadMSWordFile rMSW = new ReadMSWordFile();
//
//		sentenceList = rMSW.readDocxFileXML(file);
//		Map<String,LinkedList<String>> docParts=ParseDocParts.splitSentenceList(sentenceList);
//		this.headerPart = docParts.get("header");
//		this.bodyPart = docParts.get("body");
//		//splitSentenceList(sentenceList);
//		//buildRoot();
//		//buildHeader();
//		buildSimpleBody();
//
//
//	} 
///////////////
	
	public Document getDocument(){
		return this.doc;
	}
	
	
	public void buildTranscriptionXML(File file){
		this.inputFileName= (file.getName().split("\\."))[0]; //keeping only filename without the extention
		Pattern edYearPattern = Pattern.compile("^((\\d{4})_\\p{L}+(?:[^\\p{L}]+\\p{L}+)?).*");
		Matcher edYearMacher = edYearPattern.matcher(this.inputFileName);
		if(edYearMacher.find()){

			editionYear=edYearMacher.group(2);
			outputFileName = edYearMacher.group(1);
			outputFileName = outputFileName.replaceAll("[^\\p{L}|\\d]+", "_");
		}else{
			editionYear="unknown";
		}

		LinkedList<String> sentenceList = new LinkedList<String>() ;
		ReadMSWordFile rMSW = new ReadMSWordFile();

		sentenceList = rMSW.readDocxFileXML(file);
		Map<String,LinkedList<String>> docParts=ParseDocParts.splitSentenceList(sentenceList);
		this.headerPart = docParts.get("header");
		this.bodyPart = docParts.get("body");
		getHeaderElementsFromTranscription();
		//System.out.println(this.headerData.getTitreDuRecueil());
		//for(String s:this.headerPart){
		//	System.out.println("s: " + s);
		//} 
		
		buildRoot();
		buildHeader();
		buildBody3();


	} 

	
	public Document buildTranscriptionDOM(LinkedList<String> header, LinkedList<String> body, String editionYear){
		
		this.headerPart = header;
		this.bodyPart = body;
		this.editionYear=editionYear;
		getHeaderElementsFromTranscription();
		buildRoot();
		buildHeader();
		buildBody3();

		return this.doc;
	} 
	public void buildTranscriptionDOM(TeiHeaderData headerData, LinkedList<String> body){
		
		this.headerPart = null;
		this.bodyPart = body;
		this.editionYear=headerData.getEditionYear();
		this.headerData=headerData;
		
		buildRoot();
		buildHeader();
		buildBody3();

		//return this.doc;
	} 
	
	private void buildRoot(){
		rootElement = doc.createElement(ROOT);
		doc.appendChild(rootElement);
		rootElement.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
	    System.out.println(headerData.toString());
	}
	
	private void buildHeader(){
		Element headerRoot = doc.createElement(HEADER);
		rootElement.appendChild(headerRoot);



		Element fd = doc.createElement(FILEDESC);
		headerRoot.appendChild(fd);

		Element tStmt = doc.createElement(TITLESTMT);

		Element eStmt = doc.createElement(EDITIONSTMT);
		Element rStmtR =  doc.createElement(RESPSTMT);//responsable
		Element rStmtT =  doc.createElement(RESPSTMT);//transcription
		Element rStmtE =  doc.createElement(RESPSTMT);//encoding
		Element rStmtA =  doc.createElement(RESPSTMT);//annotation

		Element pStmt =  doc.createElement(PUBLICATIONSTMT);
		Element dist =  doc.createElement(DISTRIBUTOR);
		Element date =  doc.createElement(DATE);

		Element sDesc =  doc.createElement(SOURCEDESC);
		Element pDesc =  doc.createElement("profileDesc");
		Element rDesc =  doc.createElement("revisionDesc");

		/**
		 * title statement
		 **/
		//title statement
		fd.appendChild(tStmt);
		
		//title
		Element titleMain=doc.createElement(TITLE);
		titleMain.setAttribute(TYPE, BOOKTITLE);
		
		Element titleSub=doc.createElement(TITLE);
		titleSub.setAttribute(TYPE, SERMONBODY);
		//first add sermon title than book
		tStmt.appendChild(titleSub);
		tStmt.appendChild(titleMain);
		
		String titre = this.headerData.getTitreDuRecueil();
		
		// transcription handler removes formating data from title(Not important?)
//		Pattern titlePattern = Pattern.compile("<hi rend=\"(.)\">(.*)</hi>(.*)");
//		Matcher titleMatcher = titlePattern.matcher(titre);
//		if(titleMatcher.find()){
//			
//			System.out.println("matches");
//			Element hh=doc.createElement(HIGHLIGHT);
//			hh.setAttribute("rend", titleMatcher.group(1));
//			title.appendChild(hh);
//			hh.appendChild(doc.createTextNode(titleMatcher.group(2)));
//			title.appendChild(doc.createTextNode(titleMatcher.group(3)));
//
//		}else{
//			title.appendChild(doc.createTextNode(titre));
//		}
		
//		Pattern titlePattern = Pattern.compile("(.*?)(\\w{2,}\\.)(.*)");
//		Matcher titleMatcher = titlePattern.matcher(titre);
//		if(titleMatcher.find()){
//			
//			System.out.println("matches");
//			
//			titleMain.appendChild(doc.createTextNode(titleMatcher.group(1)+titleMatcher.group(2)));
//			titleSub.appendChild(doc.createTextNode(titleMatcher.group(3)));
//
//		}else{
//			titleMain.appendChild(doc.createTextNode(titre));
//		}
		titleMain.appendChild(doc.createTextNode(titre));
//		titleSub.appendChild(doc.createTextNode(titleMatcher.group(3))); Add title sermon later in the body
		
		
		//author
		Element titleAuthor = doc.createElement(AUTHOR);
		titleAuthor.setAttribute(REFERENCE, "...");
		titleAuthor.appendChild(doc.createTextNode(headerData.getAuteurFirstName()+" "+headerData.getAuteurLastName()));
		tStmt.appendChild(titleAuthor);
		
		//project(editor(name, orgName))
		Element titleEditor1 = doc.createElement(EDITOR); 
		tStmt.appendChild(titleEditor1);
		Element edName = doc.createElement(NAME);
		Element edOrgName = doc.createElement(ORGNAME);
		
		edName.appendChild(doc.createTextNode(this.headerData.getProjectName()));

	//	Element titleEditor2 = doc.createElement(EDITOR);
	//	tStmt.appendChild(titleEditor2);
		edOrgName.appendChild(doc.createTextNode(UNINE));
		titleEditor1.appendChild(edName);
		titleEditor1.appendChild(edOrgName);
		

//		//edition year !!! not as edition!!!!
//		Element titleEditor3 = doc.createElement(EDITOR);
//		tStmt.appendChild(titleEditor3);
//		titleEditor3.appendChild(doc.createTextNode(this.editionYear));

		/**
		 * edition statement
		 **/		 
		fd.appendChild(eStmt);
		Element edition= doc.createElement(EDITION);
		eStmt.appendChild(edition);
		edition.appendChild(doc.createTextNode(TRANSELECT));

		//responsable (Carine)
		eStmt.appendChild(rStmtR);
		
		Element resp= doc.createElement(RESP);
		rStmtR.appendChild(resp);
		resp.appendChild(doc.createTextNode(RESPONSABLE));
		
		Element rNameR= doc.createElement(NAME);
		rStmtR.appendChild(rNameR);
		
		Element rPersNameR= doc.createElement(PERSNAME);
		rPersNameR.setAttribute(XMLID, idLookupMap.get("Carine"));
		rPersNameR.appendChild(doc.createTextNode(nameLookupMap.get("Carine")));
		rNameR.appendChild( rPersNameR);
		
		Element rMailR= doc.createElement(MAIL);
		rMailR.appendChild(doc.createTextNode(mailLookupMap.get("Carine")));
		rNameR.appendChild(rMailR);
		
		Element rAffR= doc.createElement(AFF);
		rAffR.setAttribute(XMLID, "ILCF");
		rNameR.appendChild(rAffR);
		
		Element rAffName = doc.createElement(NAME);
		rAffName.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[0]));
		Element rAffOrgName = doc.createElement(ORGNAME);
		rAffOrgName.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[1]));
		
		Element rAffAddr = doc.createElement(ADDRESS);
		Element raffAddrL1 = doc.createElement(ADDRLINE);
		raffAddrL1.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[2]));
		Element raffAddrL2 = doc.createElement(ADDRLINE);
		raffAddrL2.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[3]));
		rAffAddr.appendChild(raffAddrL1);
		rAffAddr.appendChild(raffAddrL2);
		
		rAffR.appendChild(rAffName);
		rAffR.appendChild(rAffOrgName);
		rAffR.appendChild(rAffAddr);
		
		//Element respOrg = doc.createElement(ORGNAME);
		//rStmtR.appendChild(respOrg);
       // rAffR.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[0]));
		//for(String adr: this.headerData.getILCFaddresse() ){
		//	respOrg.appendChild(doc.createTextNode(adr+"\n"));
		//}
		// end Carinw

		// transcription

		eStmt.appendChild(rStmtT);
		Element respT= doc.createElement(RESP);
		rStmtT.appendChild(respT);
		//respT.appendChild(doc.createTextNode(TRANSCRIPTION +", " +this.headerData.getTranscriptionDate()));
		respT.appendChild(doc.createTextNode(TRANSCRIPTION));
		Element rNameT= doc.createElement(NAME);
		rStmtT.appendChild(rNameT);
		
		Element rPersNameT= doc.createElement(PERSNAME);
		rPersNameT.setAttribute(XMLID, this.headerData.getTranscriptionWordID());
		rPersNameT.appendChild(doc.createTextNode(this.headerData.getTranscriptionWord()));
		rNameT.appendChild( rPersNameT);
		
		Element rMailT= doc.createElement(MAIL);
		rMailT.appendChild(doc.createTextNode(this.headerData.getTranscriptionWordMail()));
		rNameT.appendChild(rMailT);
		
		Element rAffT= doc.createElement(AFF);
		Element tAffOrgNameT = doc.createElement(ORGNAME);
		tAffOrgNameT.appendChild(doc.createTextNode(UNINE));
		rAffT.appendChild(tAffOrgNameT);
		rNameT.appendChild(rAffT);


		//encoding
		eStmt.appendChild(rStmtE);
		Element respE= doc.createElement(RESP);
		rStmtE.appendChild(respE);
		//respT.appendChild(doc.createTextNode(TRANSCRIPTION +", " +this.headerData.getTranscriptionDate()));
		respE.appendChild(doc.createTextNode(ENCODING));
		Element rNameE= doc.createElement(NAME);
		rStmtE.appendChild(rNameE);
		
		Element rPersNameE= doc.createElement(PERSNAME);
		rPersNameE.setAttribute(XMLID, idLookupMap.get("Ljiljana"));
		rPersNameE.appendChild(doc.createTextNode(nameLookupMap.get("Ljiljana")));
		rNameE.appendChild( rPersNameE);
		
		Element rMailE= doc.createElement(MAIL);
		rMailE.appendChild(doc.createTextNode(mailLookupMap.get("Ljiljana")));
		rNameE.appendChild(rMailE);
		
		Element rAffE= doc.createElement(AFF);
		Element tAffOrgNameE = doc.createElement(ORGNAME);
		tAffOrgNameE.appendChild(doc.createTextNode(UNINE));
		rAffE.appendChild(tAffOrgNameE);
		rNameE.appendChild(rAffE);
//		//del
//		eStmt.appendChild(rStmtE);
//		Element respE= doc.createElement(RESP);
//		rStmtE.appendChild(respE);
//		respE.appendChild(doc.createTextNode(ENCODING +", " + (new Date()).toString()));
//
//		Element respName1E= doc.createElement(PERSNAME);
//		rStmtE.appendChild(respName1E);
//		respName1E.appendChild(doc.createTextNode(nameLookupMap.get("Ljiljana")));
//
//		Element respName2E= doc.createElement(NAME);
//		rStmtE.appendChild(respName2E);
//		respName2E.appendChild(doc.createTextNode(mailLookupMap.get("Ljiljana")));
//
//		Element respOrgE = doc.createElement(ORGNAME);
//		rStmtE.appendChild(respOrgE); 
//		respOrgE.appendChild(doc.createTextNode(UNINE));

		//annotation
		
		eStmt.appendChild(rStmtA);
		Element respA= doc.createElement(RESP);
		rStmtA.appendChild(respA);
		//respT.appendChild(doc.createTextNode(TRANSCRIPTION +", " +this.headerData.getTranscriptionDate()));
		respA.appendChild(doc.createTextNode(ANNOTATION));
		Element rNameA= doc.createElement(NAME);
		rStmtA.appendChild(rNameA);
		
		Element rPersNameA= doc.createElement(PERSNAME);
		rPersNameA.setAttribute(XMLID, idLookupMap.get("Magda"));
		rPersNameA.appendChild(doc.createTextNode(nameLookupMap.get("Magda")));
		rNameA.appendChild( rPersNameA);
		
		Element rMailA= doc.createElement(MAIL);
		rMailA.appendChild(doc.createTextNode(mailLookupMap.get("Magda")));
		rNameA.appendChild(rMailA);
		
		Element rAffA= doc.createElement(AFF);
		Element tAffOrgNameA = doc.createElement(ORGNAME);
		tAffOrgNameA.appendChild(doc.createTextNode(UNINE));
		rAffA.appendChild(tAffOrgNameA);
		rNameA.appendChild(rAffA);
		
		//lematization
		//	 eStmt.appendChild(rStmtL);
      
		/**
		 * publication statement
		 **/	
		//publication statement
		fd.appendChild(pStmt);
		//distributor
		pStmt.appendChild(dist);

		Element respName2P= doc.createElement(NAME);
		
		dist.appendChild(respName2P);
		respName2P.setAttribute(TYPE, "projet");
		respName2P.appendChild(doc.createTextNode(SERMO));

		Element addr = doc.createElement(ADDRESS);
		dist.appendChild(addr);

		for(int i=0;i< this.headerData.getILCFaddresse().length;i++ ){
			Element addrLine= doc.createElement(ADDRLINE);
			addr.appendChild(addrLine);
			addrLine.appendChild(doc.createTextNode(this.headerData.getILCFaddresse()[i]));
		}
		Element addrLine= doc.createElement(ADDRLINE);
		addr.appendChild(addrLine);
		addrLine.appendChild(doc.createTextNode(SERMOURL));

	//	pStmt.appendChild(date);
		
		/**
		 * sourceDesc 
		 **/	
		fd.appendChild(sDesc);
//		Element sourcePar = doc.createElement(PARAGRAPH);
//		sDesc.appendChild(sourcePar);
//		Element sourceName = doc.createElement(NAME);
//		sourcePar.appendChild(sourceName);
//		sourceName.appendChild(doc.createTextNode("sourceName"));
//		Element sourceURL = doc.createElement(NAME);
//		sourcePar.appendChild(sourceURL);
//		sourceURL.appendChild(doc.createTextNode(this.headerData.getSourceNumerique()));
		Element biblStruct = doc.createElement("biblStruct");
		sDesc.appendChild(biblStruct);
		
		Element monogr = doc.createElement("monogr");
		biblStruct.appendChild(monogr);
		Element idno = doc.createElement("idno");
		idno.setAttribute(TYPE, "...");
		idno.appendChild(doc.createTextNode(this.headerData.getSourceNumerique()));
		biblStruct.appendChild(idno);
		
		//within monogr author, title, imprint
		Element monAuthor = doc.createElement(AUTHOR);
		Element monTitle = doc.createElement(TITLE);
		Element monImprint = doc.createElement("imprint");
		monogr.appendChild(monAuthor);
		monogr.appendChild(monTitle);
		monTitle.appendChild(doc.createTextNode(titre));
		monogr.appendChild(monImprint);
		
		//imprint: pubPlce, publisher, date
		Element impPlace = doc.createElement("pubPlace");
		Element impPub = doc.createElement("publisher");
		Element impDate = doc.createElement(DATE);
		monImprint.appendChild(impPlace);
		monImprint.appendChild(impPub);
		monImprint.appendChild(impDate);
		
		/**
		 * profileDesc 
		 **/
		headerRoot.appendChild(pDesc);
		Element textClass = doc.createElement("textClass");
		pDesc.appendChild(textClass);
		Element kw = doc.createElement("keywords");
		textClass.appendChild(kw);
		Element term = doc.createElement("term");
		term.setAttribute(TYPE, "genre");
		kw.appendChild(term);

		
		/**
		 * revisionDesc 
		 **/
		headerRoot.appendChild(rDesc);
		Calendar rightNow = Calendar.getInstance();
		StringBuilder today = new StringBuilder();
		today.append(rightNow.get(Calendar.YEAR)).append("-").append(String.format("%02d", rightNow.get(Calendar.MONTH)+1)).append("-").append(String.format("%02d", rightNow.get(Calendar.DAY_OF_MONTH)));
		Element changeE = doc.createElement(CHANGE);
		changeE.setAttribute(WHO, "#"+idLookupMap.get("Ljiljana"));
		changeE.setAttribute(WHEN, today.toString());
		changeE.appendChild(doc.createTextNode("transformation word - XML"));
		rDesc.appendChild(changeE);
		Element changeA = doc.createElement(CHANGE);
		changeA.setAttribute(WHO, "#"+idLookupMap.get("Magda"));
		changeA.setAttribute(WHEN, today.toString());
		changeA.appendChild(doc.createTextNode("Verification, citations"));
		rDesc.appendChild(changeA);
		Element changeT = doc.createElement(CHANGE);
		changeT.setAttribute(WHO, "#"+this.headerData.getTranscriptionWordID());
		//changeT.setAttribute(WHEN, this.headerData.getTranscriptionDate());
		changeT.setAttribute(WHEN, fixDate(this.headerData.getTranscriptionDate()));
		changeT.appendChild(doc.createTextNode(TRANSCRIPTION));
		rDesc.appendChild(changeT);
		// System.out.println(fixDate(this.headerData.getTranscriptionDate()));
	//	 <change who="#RSL" when="2014-04-30">transcription</change>


	}
	
	private String fixDate(String date){
		StringBuilder dateOut= new StringBuilder();
		Pattern datePattern = Pattern.compile("(\\d{1,2})(.*?)(\\d{4})");
		
		Matcher dateMatcher = datePattern.matcher(date);
	
		 if(dateMatcher.find()){
			
			//System.out.println("matches");
//			
//			titleMain.appendChild(doc.createTextNode(titleMatcher.group(1)+titleMatcher.group(2)));
//			titleSub.appendChild(doc.createTextNode(titleMatcher.group(3)));
//
//		}else{
//			titleMain.appendChild(doc.createTextNode(titre));
			dateOut.append(dateMatcher.group(3)).append("-").append(dateMatcher.group(2)).append("-").append(dateMatcher.group(1));
			return dateOut.toString();
		}else{
		
		return date;
		}
	}

	private void getHeaderElementsFromTranscription(){
		Pattern patternDC = Pattern.compile("(.*?):(.*)");
		Pattern source_num = Pattern.compile("<source_num>(.*)</source_num>");
		for(String s: this.headerPart){
			// System.out.println(s);
			Matcher matcherDC = patternDC.matcher(s);
			Matcher matcher_sc = source_num.matcher(s);
			if(matcher_sc.find()){
				String source = matcher_sc.group(1);
				headerData.setSourceNumerique(source);
			} else if(matcherDC.find()){
				String left = matcherDC.group(1);
				String right = matcherDC.group(2);	
				// System.out.println("left: "+left);
				// System.out.println("right: "+right);

				if(left.matches(".*Titre.*")){
					System.out.println("Title:" +right.trim());
					headerData.setTitreDuRecueil(right.trim().replaceAll("<.*?>", ""));
					// System.out.println(right.trim());
				}else if(left.trim().matches(".*Auteur.*")){
					String [] nameParts=right.trim().split("\\s");
					
					if (nameParts.length>1){
						headerData.setAuteurFirstName(nameParts[0]);
						
						StringBuilder lastName=new StringBuilder();
						for(int i=1;i<nameParts.length;i++){
							lastName.append(nameParts[i]).append(" ");
						}
						headerData.setAuteurLastName(lastName.toString().trim());
						
					}else{
						headerData.setAuteurFirstName("");
						headerData.setAuteurLastName(nameParts[0]);
					}
					//headerData.setAuteur(right.trim());
				}else if(left.trim().matches(".*Transcription.*")){
					String [] parts = right.split(",");
					if(parts.length == 3){
						String name="";
						headerData.setTranscriptionDate(parts[2]);
						if(nameLookupMap.containsKey(parts[1].trim())){
							name=parts[1].trim();
						}else if(nameLookupMap.containsKey(parts[0].trim())){
							name=parts[0].trim();
						} 
						headerData.setTranscriptionWord(nameLookupMap.get(name));
						headerData.setTranscriptionWordID(idLookupMap.get(name));
						headerData.setTranscriptionWordMail(mailLookupMap.get(name));
					}else{
						headerData.setTranscriptionDate(parts[1]);
						String [] nameParts= parts[0].split("\\s+");
						for(String name:nameParts){
							name=name.trim();

							if(nameLookupMap.containsKey(name)){

								headerData.setTranscriptionWord(nameLookupMap.get(name));
								headerData.setTranscriptionWordID(idLookupMap.get(name));
								headerData.setTranscriptionWordMail(mailLookupMap.get(name));

							} 
						}

					}
				} 
			}  

		}

	}
//	private void buildSimpleBody(){
//		Element root = doc.createElement("text");
//		rootElement.appendChild(root);
//		for(String s:bodyPart){ 
//			s.replaceAll("&", "&amp;");
//			System.out.println(s);
//			s.replaceAll("([<>])","\\$1");
//			System.out.println(s);
//			//System.out.println(s);
//			root.appendChild(doc.createTextNode(s));
//			Element lb=doc.createElement(LINEBREAK);
//			root.appendChild(lb);
//		}
//	}
//
//	private void buildBody2(){
//		Element textRoot = this.doc.createElement("text");
//		this.rootElement.appendChild(textRoot);
//		Element bodyRoot = this.doc.createElement(BODY);
//		textRoot.appendChild(bodyRoot);
//		
//		Node current;
//		int parNo = 1;
//		boolean titleDiv=true;
//		Element div1 = this.doc.createElement("div");
//		div1.setAttribute(TYPE, BOOKTITLE);
//		bodyRoot.appendChild(div1);
//
//		Element div2 = this.doc.createElement("div");
//		div2.setAttribute(TYPE, SERMONBODY);
//		bodyRoot.appendChild(div2);
//
//		current=div1;
//		Element titrePB = this.doc.createElement(PAGEBREAK);
//		titrePB.setAttribute(NUMBER, "titre");
//		current.appendChild(titrePB);
//		Element p1 = this.doc.createElement(PARAGRAPH);
//		p1.setAttribute(NUMBER, Integer.toString(parNo));
//		
//		div1.appendChild(p1);
//		current=p1;
//		
//		boolean newParagraphe=false;
//		boolean addedLB=false;
//		boolean reclame=false;
//		boolean heading=false;
//		
//		for(String s:bodyPart){ 
//			addedLB=false;
//			if(s.trim().matches("<par/>") && !newParagraphe){//if a line is just a new paragraph get the next one
//				newParagraphe=true;
//				
//				Node parent = current.getParentNode();
//				
//					if(current.getTextContent().matches("\\s*")){
//					parNo = (parNo > 1) ? --parNo : 1;
//					parent.removeChild(current);
//				}
//
//				current = parent;
//			}else {
//				
//				LinkedList<String> stringList = splitLine(s.trim());
//				Iterator<String> stringListIter = stringList.iterator();
//				while (stringListIter.hasNext()) {
//					String lnPart = stringListIter.next();
//					if(!lnPart.trim().matches("<.*>")){//if it is just text
//						if(newParagraphe){ // if it is a new paragrapf add node
//							//if(!heading && !current.getNodeName().equals(NOTE)){
//								if(!heading ){
//								Element newP = this.doc.createElement(PARAGRAPH);
//								newP.setAttribute(NUMBER, Integer.toString(parNo));
//								parNo++;
//								current.appendChild(newP);
//								current=newP;
//								newParagraphe=false;
//							}
//						}
//						if(!addedLB && !reclame){
//							Element lb=this.doc.createElement(LINEBREAK);
//							current.appendChild(lb);
//							addedLB=true;
//						}
//						current.appendChild(doc.createTextNode(lnPart));//add a text node
//						
//					}else{ // if it is a tag
//						if(isInTag(lnPart)){
//							Element newIn = fixInTag(lnPart);
//							if(newIn.getNodeName().equals(HEADING)){//if it is a title main or sub no line break before
//								heading=true;
//								if(newIn.getAttribute(TYPE).equals(T2)){//subtitle - add new div
//									Element newDiv = this.doc.createElement("div");
//									newDiv.setAttribute(TYPE, T2);
//									div2.appendChild(newDiv);
//									current=newDiv;
//								}
//								//current.appendChild(newIn);
//								//current = newIn;
//							}else if(newIn.getNodeName().equals(RECLAME)){//no line break before reclame 
//								reclame=true;
//								//current.appendChild(newIn);
//								//current = newIn;	
//							}
//							//if not title, reclame or note add new paragraph and line break before
//							if(!newIn.getNodeName().equals(HEADING) && 
//									!newIn.getNodeName().equals(RECLAME)&&
//									!(newIn.getNodeName().equals(NOTE)&& newIn.getAttribute(RESP).equals("author"))){
//								if(newParagraphe){ // if it is a new paragrapf add node
//									//if(!heading && !current.getNodeName().equals(NOTE)){
//										if(!heading){
//									Element newP = this.doc.createElement(PARAGRAPH);
//									newP.setAttribute(NUMBER, Integer.toString(parNo));
//									parNo++;
//									current.appendChild(newP);
//									current=newP;
//									newParagraphe=false;
//									}
//								}
//								if(!addedLB){
//									Element lb=this.doc.createElement(LINEBREAK);
//									current.appendChild(lb);
//									addedLB=true;
//								}
//
//							}
//							current.appendChild(newIn);
//							current = newIn;	
//						}else if(isOutTag(lnPart)){
//							//if(!addedLB && !reclame){
//							//	Element lb=this.doc.createElement(LINEBREAK);
//							//	current.appendChild(lb);
//							//	addedLB=true;
//							//}
//							if(current.getNodeName().equals(HEADING)){
//								heading=false;
//							}
//							if(current.getNodeName().equals(RECLAME)){
//								reclame=false;
//							}
//							current = current.getParentNode();
//
//						}else if(isSimpleTag(lnPart)){
//							Element tmp = fixSimpleTag(lnPart);
//
//							if(tmp.getNodeName().equals("pb") && tmp.hasAttributes() && titleDiv) {
//								
//								titleDiv=false;
//								current = div2;
//								parNo=1;
//								//current.appendChild(tmp);
//							}//else{
//								current.appendChild(tmp); 
//							//}
//
//
//						}else if(isComment(lnPart)){
//							Comment comment = fixComment(lnPart);
//							current.appendChild(comment);
//						}
//
//					}
//					
//				}
//				
//				//newParagraphe=false;//stays at the end; just to be sure
//			}
//		}	
//	}

	private void buildBody3(){
		Element textRoot = this.doc.createElement("text");
		this.rootElement.appendChild(textRoot);
		Element frontRoot = this.doc.createElement("front");
		textRoot.appendChild(frontRoot);
		Element bodyRoot = this.doc.createElement(BODY);
		textRoot.appendChild(bodyRoot);

		//Node current;
		int parNo = 1;
		boolean titleDiv=true;
		Element div1 = this.doc.createElement("div");
		div1.setAttribute(TYPE, BOOKTITLE);
		frontRoot.appendChild(div1);

		Element div2 = this.doc.createElement("div");
		div2.setAttribute(TYPE, SERMONBODY);
		bodyRoot.appendChild(div2);

		Node current = div1;
		Stack<Node> currentDiv = new Stack<Node>();
		currentDiv.push(div1);
		currentDivLevel=1;
		Element titrePB = this.doc.createElement(PAGEBREAK);
		titrePB.setAttribute(NUMBER, "titre");
		current.appendChild(titrePB);
		Element p1 = this.doc.createElement(PARAGRAPH);
		p1.setAttribute(NUMBER, Integer.toString(parNo));

		div1.appendChild(p1);
		current=p1;

		boolean newParagraphe=false;
		boolean addedLB=false;
		boolean reclame=false;
		boolean heading=false;
		boolean note=false;
		boolean margin=false;
		boolean foot=false;

		for(String s:bodyPart){ 
			System.out.println(s);
			addedLB=false;
			if(s.trim().matches("<par/>") ){//if a line is just a new paragraph get the next one
				//System.out.println("1 "+current.getNodeName());
				if(heading || note){
					Element lb=this.doc.createElement(LINEBREAK);
					current.appendChild(lb);
				}else if(!newParagraphe){
					newParagraphe=true;
					if(current.getNodeName().equals("p")){
					Node parent = current.getParentNode();

					if( current.getTextContent().matches("\\s*")){
						parNo = (parNo > 1) ? --parNo : 1;
						
						parent.removeChild(current);
					}

					current = parent;
					}
				}
				//System.out.println("2 "+current.getNodeName());
				//newParagraphe=true;
			}else {

				LinkedList<String> stringList = splitLine(s);
				Iterator<String> stringListIter = stringList.iterator();
				while (stringListIter.hasNext()) {
					String lnPart = stringListIter.next();
					//System.out.println(lnPart);
					if(!lnPart.trim().matches("<.*>")){//if it is just text
						//System.out.println(1);
						if(newParagraphe){ // if it is a new paragrapf add node
							if(!heading && !note && !reclame && !margin && !foot){
								//System.out.println(current.getNodeName()+ "par no" + parNo);
								Element newP = this.doc.createElement(PARAGRAPH);
								newP.setAttribute(NUMBER, Integer.toString(parNo));
								parNo++;
								current.appendChild(newP);
								current=newP;
								newParagraphe=false;
							}
						}
						if(!addedLB && !reclame){
							Element lb=this.doc.createElement(LINEBREAK);
							current.appendChild(lb);
							addedLB=true;
						}
						current.appendChild(doc.createTextNode(Tokenizer.fixNonStandardCh(lnPart)));//add a text node
						//System.out.println(current.getNodeName());
					}else{ // if it is a tag
						if(isInTag(lnPart)){
							//System.out.println(2);
							Element newIn = fixInTag(lnPart);
							
							//if not title, reclame or note add new paragraph and line break before
							if(!newIn.getNodeName().equals(HEADING) && 
									!newIn.getNodeName().equals(RECLAME)&&
									!(newIn.getNodeName().equals(NOTE)&& newIn.getAttribute(RESP).equals("author"))&&
									//!(newIn.getNodeName().equals(NOTE)&& !newIn.getAttribute(TYPE).equals("margin"))&&
									!(newIn.getNodeName().equals(NOTE)&&newIn.getAttribute(TYPE).equals("foot"))){
								if(newParagraphe){ // if it is a new paragrapf add node except before title, note or reclame
									if(!heading && !note && !reclame && !margin && !foot){
										Element newP = this.doc.createElement(PARAGRAPH);
										newP.setAttribute(NUMBER, Integer.toString(parNo));
										parNo++;
										current.appendChild(newP);
										current=newP;
										newParagraphe=false;
									}
								}
								if(!addedLB && !reclame && !foot){
									Element lb=this.doc.createElement(LINEBREAK);
									current.appendChild(doc.createTextNode(" "));
									current.appendChild(lb);
									addedLB=true;
								}

							}
							if(newIn.getNodeName().equals(HEADING)){//if it is a title main or sub no line break before
								heading=true;
								if(current.getNodeName().equals(PARAGRAPH)){ // heading can not be a pare of paragraph
									Node parent = current.getParentNode();

									if( current.getTextContent().matches("\\s*")){
										parNo = (parNo > 1) ? --parNo : 1;
										
										parent.removeChild(current);
									}

									current = parent;
								}
//								if(newIn.getAttribute(TYPE).equals(T2)){//subtitle - add new div
//									Element newDiv = this.doc.createElement("div");
//									newDiv.setAttribute(TYPE, T2);
//									div2.appendChild(newDiv);
//									current=newDiv;
//								}
								if(newIn.getAttribute(TYPE).equals(T2)){//subtitle - add new div
									Element newDiv = this.doc.createElement("div");
									newDiv.setAttribute(TYPE, T2);
									Node previous=null;
									Node parent;
									while(currentDiv.size()>=currentDivLevel){
										previous = currentDiv.pop();
									}
									 parent = currentDiv.peek();
									
									
									if(previous!=null){
										String n = previous.getAttributes().getNamedItem(NUMBER).getNodeValue(); 
										String [] nParts = n.split("\\.");
										
										int last = Integer.parseInt(nParts[nParts.length-1]);
										last++;
										if(nParts.length > 1){
											StringBuilder nb=new StringBuilder();
											nb.append(n.substring(0, n.lastIndexOf('.')+1)).append(Integer.toString(last));
											newDiv.setAttribute(NUMBER, nb.toString());
										}else{
											newDiv.setAttribute(NUMBER, Integer.toString(last));
											}
										
									}else{
										if(parent.getAttributes().getNamedItem(NUMBER)!=null){
											StringBuilder nb=new StringBuilder();
											nb.append(parent.getAttributes().getNamedItem(NUMBER).getNodeValue()).append(".1");
											newDiv.setAttribute(NUMBER, nb.toString());
										}else{
											newDiv.setAttribute(NUMBER,"1");
										}
									}
									
									
									parent.appendChild(newDiv);
									current=newDiv;
									currentDiv.push(newDiv);
								}
								//current.appendChild(newIn);
								//current = newIn;
							}else if(newIn.getNodeName().equals(RECLAME)){//no line break before reclame 
								reclame=true;
								//current.appendChild(newIn);
								//current = newIn;	
							}else if(newIn.getNodeName().equals(NOTE)&& newIn.getAttribute(RESP).equals("author")){//no par break before authors note
							//}else if(newIn.getNodeName().equals(NOTE)){
							note=true;
							//current.appendChild(newIn);
							//current = newIn;	
						}else if(newIn.getNodeName().equals(NOTE)&& newIn.getAttribute(TYPE).equals("margin")){//no par break before authors note
							//}else if(newIn.getNodeName().equals(NOTE)){
							margin=true;
							//current.appendChild(newIn);
							//current = newIn;	
							}else if(newIn.getNodeName().equals(NOTE)&& newIn.getAttribute(TYPE).equals("foot")){//no par break before authors note
						//}else if(newIn.getNodeName().equals(NOTE)){
						   foot=true;
						//current.appendChild(newIn);
						//current = newIn;	
						}
							//System.out.println("New in "+newIn.getNodeName());
							current.appendChild(newIn);
							current = newIn;
							//System.out.println(current.getNodeName());
						}else if(isOutTag(lnPart)){
							//System.out.println(3);
							//if(!addedLB && !reclame){
							//	Element lb=this.doc.createElement(LINEBREAK);
							//	current.appendChild(lb);
							//	addedLB=true;
							//}
							if(current.getNodeName().equals(HEADING)){
								heading=false;
							}
							//if(current.getNodeName().equals(RECLAME)){
						//		reclame=false;
						//	}
							if(current.getNodeName().equals(NOTE)){
								if(note){
								note=false;
								}else if(margin){
								margin=false;
								}else if(foot){
								foot=false;
								}
							}
							//System.out.println(current.getNodeName());
							//System.out.println(current.getParentNode().getNodeName());
							//System.out.println("end tag:"+current.getNodeName()+":"+lnPart);
							current = current.getParentNode();
							//System.out.println(current.getNodeName());
							//System.out.println(newParagraphe);
							}else if(isSimpleTag(lnPart)){
							//System.out.println(4);
							Element tmp = fixSimpleTag(lnPart);
							if(tmp.getNodeName().equals("pb") && reclame){
								reclame=false;
							}
							if(tmp.getNodeName().equals("pb") && tmp.hasAttributes() && titleDiv) {
								
								titleDiv=false;
								current = div2;
								currentDivLevel=1;
								currentDiv.clear();
								currentDiv.push(div2);
								parNo=1;
								//System.out.println("going for div 2");
								//current.appendChild(tmp);
							}//else{
							
							current.appendChild(tmp); 
							//}
						//	System.out.println(current.getNodeName());

						}else if(isComment(lnPart)){
							//System.out.println(5);
						//	System.out.println(current.getNodeName());
							Comment comment = fixComment(lnPart);
							current.appendChild(comment);
						}

					}

				}

				//newParagraphe=false;//stays at the end; just to be sure
			}
		}	
	}



	private boolean isInTag(String tag){
		boolean isInTag=false;
		if(tag.trim().matches("<[^/!]*>")){
			// System.out.println(tag + "is in tag");
			isInTag=true;
		}
		return isInTag;
	}
	private boolean isOutTag(String tag){
		boolean isOutTag=false;
		if(tag.trim().matches("</.*>")){
			// System.out.println(tag + "is out tag");
			isOutTag=true;
		}
		return isOutTag;
	}
	private boolean isSimpleTag(String tag){
		boolean isSimpleTag=false;
		if(tag.trim().matches("<.*/>")){
			// System.out.println(tag + "is simple tag");
			isSimpleTag=true;
		}
		return isSimpleTag;
	}
	private boolean isComment(String tag){
		boolean isComment=false;
		if(tag.trim().matches("<!--.*-->")){
			// System.out.println(tag + "is comment tag");
			isComment=true;
		}
		return isComment;
	}

	private Element fixInTag(String inTagString){
		Element inTag = null;
      //  System.out.println(inTagString);
        Pattern tagPattern= Pattern.compile("<(.*?)>");
		Matcher tagMatcher = tagPattern.matcher(inTagString);
		if(tagMatcher.find()){
			String tagContent=tagMatcher.group(1);


			String [] tagParts = tagContent.split("\\s+");
			String tagName= tagParts[0].trim();
		//	System.out.println(tagName);
			//  for(String tp:tagParts){System.out.println(tp);}  

			if(tagCode.containsKey(tagName)){ 		
				switch(tagCode.get(tagName)){
				//transcription
				case 0://"source_num":
					inTag=this.doc.createElement(SOURCEDESC);//??????????
					break;
				case 1: //"pb":;
					inTag=this.doc.createElement(PAGEBREAK);
					if(tagParts.length >1){
						Pattern pbPattern= Pattern.compile("n=\\\"(.+)?\\\"");
						Matcher pbMatcher = pbPattern.matcher(tagParts[1].trim());
						if(pbMatcher.find()){
							String pn=tagMatcher.group(1);
							inTag.setAttribute(NUMBER, pn);
						}

					}
					break;
				case 2: //"cb":;
					inTag = this.doc.createElement(COLUMNBREAK);
					if(tagParts.length >1){
						Pattern pbPattern= Pattern.compile("n=\\\"(.+)?\\\"");
						Matcher pbMatcher = pbPattern.matcher(tagParts[1].trim());
						if(pbMatcher.find()){
							String pn=tagMatcher.group(1);
							inTag.setAttribute(NUMBER, pn);
						}

					}
					break;
				case 3: //"lb":;
					inTag = this.doc.createElement(LINEBREAK);
					break;
				case 4://"reclame":;
					inTag = this.doc.createElement(RECLAME);
					inTag.setAttribute(PLACE, "bot-right");
					inTag.setAttribute(TYPE, "catch");
					break;
				case 5://"t":;
					inTag = this.doc.createElement(HEADING);
					inTag.setAttribute(TYPE, T1);
					break;
				case 6://"t1,t2,t3,t4":;
					inTag = this.doc.createElement(HEADING);
					inTag.setAttribute(TYPE, T2);
					switch(tagName){
					case "t1":currentDivLevel=2;break;
					case "t2":currentDivLevel=3;break;
					case "t3":currentDivLevel=4;break;
					case "t4":currentDivLevel=5;break;
					
					}
					break;
				case 7://"date_sermon":;
					inTag = this.doc.createElement(DATE);
					break;
				case 8://"footnote":;
					inTag = this.doc.createElement(NOTE);
					inTag.setAttribute(TYPE, "foot");
					break;
				case 9://"manchette":;
					inTag = this.doc.createElement(NOTE);
					inTag.setAttribute(TYPE, "margin");
					if(tagParts.length >1){
						System.out.println(); 
						Pattern manchettePattern= Pattern.compile("place=\\\"(.+)?\\\"");
						Matcher manchetteMatcher = manchettePattern.matcher(tagParts[1].trim());
						if(manchetteMatcher.find()){
							String mP=manchetteMatcher.group(1);
							inTag.setAttribute(PLACE, mP);
						}
					}
					break;
				case 10://"lettrine":;
					inTag = this.doc.createElement(CHARACTER);
					inTag.setAttribute(TYPE, "lettrine");
					break;
				case 11://"bibl":;
					inTag = this.doc.createElement(BIBL);
					inTag.setAttribute(TYPE, "bible");
					break;
				case 12://"ref":;
					inTag = this.doc.createElement(BIBL);
					inTag.setAttribute(TYPE, "other");
					break;
				case 13://"citation":;
					inTag = this.doc.createElement(CITATION);
					inTag.setAttribute("source", "# ...");
					break;
				case 14://"discours_direct":;
					inTag = this.doc.createElement(DD);
					break;
				case 15://"situation":;
					inTag = this.doc.createElement(SITUATION);
					break;

					//parsing
				case 16://"hi":;
					inTag = this.doc.createElement(HIGHLIGHT);
					if(tagParts.length >1){
						Pattern hiPattern= Pattern.compile("(.+)?=\\\"(.+)?\\\"");
						Matcher hiMatcher = hiPattern.matcher(tagParts[1].trim());
						if(hiMatcher.find()){
							String an=hiMatcher.group(1);
							String av=hiMatcher.group(2);
							inTag.setAttribute(an, av);
						}

					}
					break;
				case 17://"pdm":;
					inTag = this.doc.createElement(GLYPH);
					inTag.setAttribute(REFERENCE, "#pdm");
					break;
				case 18:  //"image"
					inTag = this.doc.createElement(FIGURE);
					break;
				case 20://authors note
					inTag = this.doc.createElement(NOTE);
					inTag.setAttribute(RESP, "author");
					break;
				default:
					inTag = doc.createElement("error");
					break;
				}
			}
		}
		return inTag;
	}	

	//	 private Element fixOutTag(String outTagIn){
	//		 Pattern tagPattern= Pattern.compile("</(.*?)>");
	//	     Matcher tagMatcher = tagPattern.matcher(outTagIn);
	//	     String tagContent=tagMatcher.group(1);
	//			
	//				 
	//		 Element outTag=null;
	//		 
	//		 return outTag;
	//	 }	 

	private Element fixSimpleTag(String simpleTagIn){
		Pattern tagPattern= Pattern.compile("<(.*?)/>");
		Matcher tagMatcher = tagPattern.matcher(simpleTagIn);
		Element simpleTag=null;

		if(tagMatcher.find()){
			String tagContent=tagMatcher.group(1);
			tagContent = tagContent.replaceAll("\\s+=\\s+", "=");


			String [] tagParts = tagContent.split("\\s+");
			String tagName= tagParts[0].trim();


			if(tagCode.containsKey(tagName)){ 		
				switch(tagCode.get(tagName)){
				//transcription
				case 0://"source_num":
					simpleTag=this.doc.createElement(SOURCEDESC);//??????????
					break;
				case 1: //"pb":;
					// System.out.println("pageBreak ok");
					simpleTag=this.doc.createElement(PAGEBREAK);
					if(tagParts.length >1){
						Pattern pbPattern= Pattern.compile("n=\\\"(.+)?\\\"");
						Matcher pbMatcher = pbPattern.matcher(tagParts[1].trim());
						if(pbMatcher.find()){
							String pn=pbMatcher.group(1);
							simpleTag.setAttribute(NUMBER, pn);
						}
					}
					break;
				case 2: //"cb":;
					simpleTag = this.doc.createElement(COLUMNBREAK);
					break;
				case 3: //"lb":;
					simpleTag = this.doc.createElement(LINEBREAK);
					break;
				case 17://"pdm":;
					simpleTag = this.doc.createElement(GLYPH);
					simpleTag.setAttribute(REFERENCE, "#pdm");
					break;
				case 18:  //"image"
					simpleTag = this.doc.createElement(FIGURE);
					break;
				case 19:  //"par"
					simpleTag = this.doc.createElement(PARAGRAPH);
					break;
				default: 
					simpleTag = doc.createElement("error");
					break;
				}
			}



		} 
		return simpleTag;
	}	 

	private Comment fixComment(String commentTagIn){
		Pattern tagPattern= Pattern.compile("<!--(.*?)-->");
		Matcher tagMatcher = tagPattern.matcher(commentTagIn);
		Comment comment=null;
		if(tagMatcher.find()){
			String tagContent=tagMatcher.group(1);
			//System.out.println(tagContent);
			tagContent=tagContent.replaceAll("<.*?>", "");
			// System.out.println(tagContent);
			comment = doc.createComment(tagContent);

		}
		return comment;
	}	 

	private LinkedList<String> splitLine(String s){
		LinkedList<String> sL =new LinkedList<String>();
		String toSplit;
		if(s.replaceAll("\n", "").endsWith(" ")||s.replaceAll("\n", "").endsWith("-")){
			toSplit=s.replaceAll("\n", "");
		}else{
			toSplit=s.replaceAll("\n", "")+" ";
		}

		Pattern extractionPattern= Pattern.compile("(<!--.*?-->|<[^!]*?>)?(\\s*[^<]*)");
		Matcher exMatcher = extractionPattern.matcher(toSplit);

		while(exMatcher.find()){
			if(exMatcher.group(1)!=null && !exMatcher.group(1).equals("") ){sL.add(exMatcher.group(1));}
			if(exMatcher.group(2)!=null && !exMatcher.group(2).equals("")){sL.add(exMatcher.group(2));}
		}
		return sL;		 

	}


	public void printXMLconsole(){
		try{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

			DOMSource source = new DOMSource(this.doc);

			StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

		}catch(TransformerException tfe) {
			tfe.printStackTrace();
		}

	}

	public void printXMLtoFile(File outDir){
		
		printXMLtoFile(outDir,this.outputFileName);
//		try{
//			OutputStream outStream = new FileOutputStream (new File(outDir,this.outputFileName+".xml"));	
//			TransformerFactory transformerFactory = TransformerFactory.newInstance();
//			Transformer transformer = transformerFactory.newTransformer();
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//
//			DOMSource source = new DOMSource(this.doc);
//
//			StreamResult result = new StreamResult(outStream);
//
//			transformer.transform(source, result);
//
//		}catch(TransformerException tfe){
//
//		}catch(IOException e){
//
//		}

	}
	public void printXMLtoFile(String outDirName){
		File outDir = new File(outDirName);
		if(!outDir.isDirectory()){
			outDir.mkdirs();
		}
		printXMLtoFile(outDir);

	}

	public void printXMLtoFile(File outDir,String fileName){
		try{
			OutputStream outStream = new FileOutputStream (new File(outDir,fileName+".xml"));	
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

			DOMSource source = new DOMSource(this.doc);

			StreamResult result = new StreamResult(outStream);

			transformer.transform(source, result);

		}catch(TransformerException tfe){

		}catch(IOException e){

		}

	}
	public void printXMLtoFile(String outDirName, String fileName){
		File outDir = new File(outDirName);
		if(!outDir.isDirectory()){
			outDir.mkdirs();
		}
			printXMLtoFile(outDir,fileName);
	}
	
  
	/**
	 * @param argsread a file using the FileChooser, Write the result to XML file
	 */
	public static void main(String[] args) {
		//LinkedList<String> result=new LinkedList<String>() ;
		try{
			BuildTranscriptionXML resultXML=new BuildTranscriptionXML();

			JFileChooser window= new JFileChooser();
			int rv= window.showOpenDialog(null);

			if(rv == JFileChooser.APPROVE_OPTION){
				resultXML.buildTranscriptionXML(window.getSelectedFile());  
				//resultXML.buildSimpleTranscriptionXML(window.getSelectedFile());  
				//resultXML.printXMLconsole();
				resultXML.printXMLtoFile(args[0]);
			}
		}catch(ParserConfigurationException e){
			System.out.println("Couldn't build XML");
			
		}
	}

}
