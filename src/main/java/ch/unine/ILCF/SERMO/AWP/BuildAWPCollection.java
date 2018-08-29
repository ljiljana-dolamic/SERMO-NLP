/**
 * 
 */
package ch.unine.ILCF.SERMO.AWP;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import ch.unine.ILCF.SERMO.XML.Utils.BuildTranscriptionXML;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

/**
 * @author dolamicl
 *
 */
public class BuildAWPCollection {
	 //copied from building header
	
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
//////
	
	private String input_dir;
	private String output_dir;
    private Properties prop;
    private Document B1;
    private Document B2;
    private Document B3_1;
    private Document B3_2;
    private Document B4;
    private Document B5_1;
    private Document B5_2;
    private Document B6;
    private Document AWPcollTMP;
    private Element teiHeaderTMP;
    
    private static final  Map<String, String> noSpaceCorrMap ;
	static{
		noSpaceCorrMap = new HashMap<String, String>();
		noSpaceCorrMap.put("pointdenousrendreimportuns,nousvousferionssouvenirqu'iln'yaplus" ,"point de nous rendre importuns, nous vous ferions souvenir qu' il n' y a plus");
		noSpaceCorrMap.put("négotiation,n'aianspasintentiondetoucheràlasommequiestdestinée" , "négotiation, n'aians pas intention de toucher à la somme qui est destinée");
		noSpaceCorrMap.put("riendedeçàpourlesnostresnypourlesRésidensquiserventencette" , "rien de deçà pour les nostres ny pour les Résidens qui servent en cette");
		noSpaceCorrMap.put("pourlespartiessecrèttes.LesImpériauxetEspagnolzquisonticyaians", "pour les parties secrèttes. Les Impériaux et Espagnolz qui sont icy aians");
		noSpaceCorrMap.put("onauraàvousmandersionvouloitlefairepartirparcet","on aura à vous mander si on vouloit le faire partir par cet");
		noSpaceCorrMap.put("depuispeudebeaucoupaugmentéleurdespencenenouspermettentpasde" , "depuis peu de beaucoup augmenté leur despence ne nous permettent pas de");
		noSpaceCorrMap.put("onauraàvousmandersionvouloitlefairepartirparcet", "on aura à vous mander si on vouloit le faire partir par cet");
		noSpaceCorrMap.put("depuispeudebeaucoupaugmentéleurdespencenenouspermettentpasde", "depuis peu de beaucoup augmenté leur despence ne nous permettent pas de");
		noSpaceCorrMap.put("voscollègues,maiscommej'ayveuquemalaisementpourois je","vos collègues, mais comme j'ay veu que malaisement pourois-je");
		noSpaceCorrMap.put("véritablesintérest","véritables intérest");
	}
	
	 private static String correctNoSpace(String text){
	 	  for(Entry<String,String> entry : noSpaceCorrMap.entrySet()){
	           text=text.replaceAll(entry.getKey(), entry.getValue());
	           
	       }
	       return text;
	 	  
	   }
	 static Map <String,String> nonStdCh;
	 	static{
	 		nonStdCh = new HashMap<String,String>();
	 		nonStdCh.put("œ","oe");
	 		nonStdCh.put("æ","ae");
	 		nonStdCh.put("«<","");
	 		nonStdCh.put("»>","");
	 		nonStdCh.put("”","");
	 		nonStdCh.put("“","");     
	 		nonStdCh.put("„","");
	 		nonStdCh.put("‘","");
	 		nonStdCh.put("½","");
	 		nonStdCh.put("‚","");
	 		nonStdCh.put("‹","\"");
	 		nonStdCh.put("›","\"");
	 		nonStdCh.put("-\n", "");
	 		nonStdCh.put("\n+","\u0020");
	 		nonStdCh.put("\\u2329","");
	 		nonStdCh.put("\\u232A","");
	 		nonStdCh.put("\\u2039","");
	 		nonStdCh.put("\\u203A","");
	 		nonStdCh.put("\\u005B\\u2026\\u005D","");
	 		nonStdCh.put("\\[Lücke\\]",". . .");
	 		nonStdCh.put("\\[unleserlich\\]",". . .");
	 		nonStdCh.put("\\u2026",". . .");
	 		nonStdCh.put("\\u005B. . .\\u005D",". . .");
	 		nonStdCh.put("Ludwig XIV.","Ludwig XIV");
	 		nonStdCh.put("Ludwigs XIV.","Ludwigs XIV");
	 		
	 	}
	   private static String fixCh(String text){
	 	  for(Entry<String,String> entry : nonStdCh.entrySet()){
	           text=text.replaceAll(entry.getKey(), entry.getValue());
	           
	       }
	       return text;
	 	  
	   }
	
	public BuildAWPCollection(String propFileName)throws Exception{
		this.prop = SermoProperties.getProperties(propFileName);
		this.input_dir = this.prop.getProperty("collection.in");
		this.output_dir = this.prop.getProperty("collection.out");
		// read fuul collection
		System.out.println(input_dir);
		this.B1 = XMLutils.getDoc(new File(this.input_dir,"B1.xml"));
		this.B2= XMLutils.getDoc(new File(this.input_dir,"B2.xml"));
		this.B3_1 = XMLutils.getDoc(new File(this.input_dir,"B3_1.xml"));
		this.B3_2 = XMLutils.getDoc(new File(this.input_dir,"B3_2.xml"));
		this.B4 = XMLutils.getDoc(new File(this.input_dir,"B4.xml"));
		this.B5_1 = XMLutils.getDoc(new File(this.input_dir,"B5_1.xml"));
		this.B5_2 = XMLutils.getDoc(new File(this.input_dir,"B5_2.xml"));
		this.B6 = XMLutils.getDoc(new File(this.input_dir,"B6.xml"));
		 System.out.println("id,title,place,date,dateCorr");
		//read tmp file
		this.AWPcollTMP = XMLutils.getDoc(new File(this.input_dir,"cor_fr_text.xml"));
		
	
		this.teiHeaderTMP =(Element) XMLutils.getNode(this.AWPcollTMP,"/TEI/teiHeader");
		NodeList listOfLetters = XMLutils.getNodeList(this.AWPcollTMP, "/TEI/text/body/div");
		
		for(int i=0; i < listOfLetters.getLength();i++){
			
			Element n = (Element)listOfLetters.item(i); 
		    Map <String,String> attr = new HashMap <String,String>();
		    attr.put("id", n.getAttribute("id"));
		    attr.put("vol", n.getAttribute("vol"));
		    attr.put("nr", n.getAttribute("nr"));
		    attr.put("type", n.getAttribute("type"));
		    attr.put("aut", n.getAttribute("aut"));
		    attr.put("loc", n.getAttribute("loc"));
		    attr.put("dat", n.getAttribute("dat"));
		    
		    String id= attr.get("vol")+"_"+attr.get("nr")+"_"+attr.get("id");
		    Document inWhich=null;
		    if(attr.get("vol").equals("APWIIB1")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B1;
		    }else  if(attr.get("vol").equals("APWIIB2")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B2;
		    }else  if(attr.get("vol").equals("APWIIB3-1")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B3_1;
		    }else  if(attr.get("vol").equals("APWIIB3-2")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B3_2;
		    }else  if(attr.get("vol").equals("APWIIB4")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B4;
		    }else  if(attr.get("vol").equals("APWIIB5-1")){
		    	//System.out.println("got the doc");
		    	inWhich=this.B5_1;
		    }else  if(attr.get("vol").equals("APWIIB5-2")){
		    	inWhich=this.B5_2;
		    }else  if(attr.get("vol").equals("APWIIB6")){
		    	inWhich=this.B6;
		    }
		    
		    Element bookTeiHeader = (Element) XMLutils.getNode(inWhich, "/TEI/teiHeader");
		    
			StringBuilder div_id= new StringBuilder();
			div_id.append("/TEI/text/body/div/div[@type='correspondency']/div[@id='").append(attr.get("vol")).append(" ").append(attr.get("nr")).append("']");
			
			
		    String textPath=div_id.toString()+"/div[@type='text']";
		    
		    String titlePath=div_id.toString()+"/div[@type='title']/p";
		    
		    Element titleNode  = ((Element) XMLutils.getNode(inWhich, titlePath));
		    for(Node childNode = titleNode.getFirstChild();
				    childNode!=null;){
				  Node nextChild = childNode.getNextSibling();
				  // Do something with childNode,
				  //   including move or delete...
				//  System.out.println(childNode.getNodeName());
				  if(childNode.getNodeName().equals("note")){
					  titleNode.removeChild(childNode);
				  }
				  childNode = nextChild;
				}
		    String title = titleNode.getTextContent().replaceAll("\n", " ").trim();
		    
		    String placePath = div_id.toString()+"/div[@type='placedate']/p/rs[@type='placename']";
		    Element placeNode = (Element) XMLutils.getNode(inWhich, placePath);
		    for(Node childNode = placeNode.getFirstChild();
				    childNode!=null;){
				  Node nextChild = childNode.getNextSibling();
				  // Do something with childNode,
				  //   including move or delete...
				  if(childNode.getNodeName().equals("note")){
					  placeNode.removeChild(childNode);
				  }
				  childNode = nextChild;
				}
		    String place = placeNode.getTextContent();
		    String datePath = div_id.toString()+"/div[@type='placedate']/p/date";
		    Element dateNode  = ((Element) XMLutils.getNode(inWhich, datePath));
		    for(Node childNode = dateNode.getFirstChild();
				    childNode!=null;){
				  Node nextChild = childNode.getNextSibling();
				  // Do something with childNode,
				  //   including move or delete...
				  if(childNode.getNodeName().equals("note")){
					  dateNode.removeChild(childNode);
				  }
				  childNode = nextChild;
				}
		    
		    String date = dateNode.getTextContent();
		    String dateCorr=((Element) XMLutils.getNode(inWhich, datePath)).getAttribute("when");
		    System.out.println(id+",\""+title.trim()+"\","+place+","+date+","+dateCorr);
		   
		    Element content =(Element) XMLutils.getNode(inWhich, textPath);
		    if(content == null){System.out.println("no content");
		    }else{
		    
		    	
		    Element header=buildHeader( bookTeiHeader, this.teiHeaderTMP,id,title,dateCorr,date,place,attr);
		   // Document finalDoc = buildCollectionDocument(id,this.teiHeaderTMP,cleanContent(content));
		    Document finalDoc = buildCollectionDocument(header,cleanContent(content,title));
			writeCollectionDoc(this.output_dir,id,finalDoc);
		    }
		}
		
		//System.out.println(listOfLetters.getLength());
		
		//System.out.println(this.teiHeaderTMP.toString());
		
	}
	
	public Document buildCollectionDocument(String id, Node tmpHeader, Element content)throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
		Document doc = docBuilder.newDocument();
		
		//Node header = doc.importNode(tmpHeader, true);
		
		
		//Element rootElement = doc.createElement("TEI");
		//doc.appendChild(rootElement);
		//rootElement.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
		
		//NodeList fileDescList= ((Element)header).getElementsByTagName("fileDesc");
		//Element fileDesc =(Element) fileDescList.item(0);  
		//fileDesc.setAttribute("xml:id", id);
		//rootElement.appendChild(header);
		Element text = doc.createElement("text"); 
		Element body = doc.createElement("body");
		//rootElement.appendChild(text);
		doc.appendChild(text);
		text.appendChild(body);
		Node cont = doc.importNode(content, true);
		body.appendChild(cont);
		
		return doc;
		
	}
	
	public Document buildCollectionDocument( Node teiHeader, Element content)throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
		Document doc = docBuilder.newDocument();
		
		//Node header = doc.importNode(teiHeader, true);
		
		
		//Element rootElement = doc.createElement("TEI");
		//doc.appendChild(rootElement);
		//rootElement.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
		
		
		//rootElement.appendChild(header);
		Element text = doc.createElement("text"); 
		Element body = doc.createElement("body");
		//rootElement.appendChild(text);
		doc.appendChild(text);
		text.appendChild(body);
		Node cont = doc.importNode(content, true);
		body.appendChild(cont);
		
		return doc;
		
	}
	
	public void writeCollectionDoc(String outDirName,String docId, Document doc){
		File outDir = new File(outDirName);
		try{
			OutputStream outStream = new FileOutputStream (new File(outDir,docId+".xml"));	
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(outStream);

			transformer.transform(source, result);

		}catch(TransformerException tfe){

		}catch(IOException e){

		}
		
		
	}
	
	public Element buildHeader(Element bookHeader, Element tmpHeader,String doc_id, String title, String dateC,String date,String place, Map<String,String>attr)throws Exception{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
		Document doc = docBuilder.newDocument();
		
		
		Element headerRoot = doc.createElement(HEADER);
		doc.appendChild(headerRoot);



		Element fd = doc.createElement(FILEDESC);
		fd.setAttribute("xml:id", doc_id);
		headerRoot.appendChild(fd);
		
		NodeList tStmtList= bookHeader.getElementsByTagName(TITLESTMT); 
		Node tStmt = doc.importNode(tStmtList.item(0), true);
		
		/**
		 * title statement
		 **/
		//title statement
		fd.appendChild(tStmt);
		
		//title
		
		Element titleMain=doc.createElement(TITLE);
		titleMain.setAttribute(TYPE, "letter");
	
		Node first =tStmt.getFirstChild();
		while(first.getNodeType() != Node.ELEMENT_NODE){
			first = first.getNextSibling();
		}
		((Element)first).setAttribute(TYPE, BOOKTITLE);
		tStmt.insertBefore(titleMain, first);
		

		titleMain.appendChild(doc.createTextNode(title));
		Element to= doc.createElement("name");
		to.setAttribute(TYPE, "to");
		Element from = doc.createElement("name");
		from.setAttribute(TYPE, "from");
		String[] fromTo= title.split("an");
		//System.out.println(fromTo);
		titleMain.appendChild(to);
		if(fromTo.length>1){
		to.appendChild(doc.createTextNode(fromTo[1]));
		}
		titleMain.appendChild(from);
		from.appendChild(doc.createTextNode(fromTo[0]));
		Element where = doc.createElement("rs");
		where.setAttribute(TYPE, "placename");
		where.appendChild(doc.createTextNode(place));
		titleMain.appendChild(where);
		Element when = doc.createElement("date");
		titleMain.appendChild(when);
		when.setAttribute("when", dateC);
		when.appendChild(doc.createTextNode(date));
		
		
		
		NodeList editoeFD= tmpHeader.getElementsByTagName("editor"); 
		Node editor = doc.importNode(editoeFD.item(0), true);
		tStmt.appendChild(editor);
		
		
		/**
//		 * edition statement
//		 **/		 
//		fd.appendChild(eStmt);
		//Element eStmt = doc.createElement(EDITIONSTMT);
		Element eStmt = (Element) doc.importNode(tmpHeader.getElementsByTagName(EDITIONSTMT).item(0),true);
		fd.appendChild(eStmt);
		
		//encoding
		Element rStmtE =  doc.createElement(RESPSTMT);//encoding
				eStmt.appendChild(rStmtE);
				Element respE= doc.createElement(RESP);
				rStmtE.appendChild(respE);
				//respT.appendChild(doc.createTextNode(TRANSCRIPTION +", " +this.headerData.getTranscriptionDate()));
				respE.appendChild(doc.createTextNode(ENCODING));
				Element rNameE= doc.createElement(NAME);
				rStmtE.appendChild(rNameE);
				
				Element rPersNameE= doc.createElement(PERSNAME);
				rPersNameE.setAttribute(XMLID,"SERMO");
				rPersNameE.appendChild(doc.createTextNode("Projet SERMO"));
				rNameE.appendChild( rPersNameE);
				

				Element rAffR= doc.createElement(AFF);
				rAffR.setAttribute(XMLID, "ILCF");
				rNameE.appendChild(rAffR);
				
				Element rAffName = doc.createElement(NAME);
				rAffName.appendChild(doc.createTextNode("Institut de langue et civilisation françaises"));
				Element rAffOrgName = doc.createElement(ORGNAME);
				rAffOrgName.appendChild(doc.createTextNode("Université de Neuchâtel"));
				
				Element rAffAddr = doc.createElement(ADDRESS);
				Element raffAddrL1 = doc.createElement(ADDRLINE);
				raffAddrL1.appendChild(doc.createTextNode("Fbg de l’Hôpital 61-63"));
				Element raffAddrL2 = doc.createElement(ADDRLINE);
				raffAddrL2.appendChild(doc.createTextNode("CH-2000 Neuchâtel"));
				rAffAddr.appendChild(raffAddrL1);
				rAffAddr.appendChild(raffAddrL2);
				Element raffAddrL3 = doc.createElement(ADDRLINE);
				raffAddrL3.appendChild(doc.createTextNode(SERMOURL));
				rAffAddr.appendChild(raffAddrL3);
				rAffR.appendChild(rAffName);
				rAffR.appendChild(rAffOrgName);
				rAffR.appendChild(rAffAddr);
		
		/**
		 * publication statement
		 **/	
		//Element pStmt =  doc.createElement(PUBLICATIONSTMT);
		Element pStmt =(Element) doc.importNode(bookHeader.getElementsByTagName(PUBLICATIONSTMT).item(0),true);
		fd.appendChild(pStmt);
		
		
		Element dist =  doc.createElement(DISTRIBUTOR);
		
		Element dateElemet =  doc.createElement(DATE);

		Element sDesc =  doc.createElement(SOURCEDESC);
		
			fd.appendChild(sDesc);
			
			Element biblStruct = doc.createElement("biblStruct");
			sDesc.appendChild(biblStruct);
			
			Element monogr = doc.createElement("monogr");
			biblStruct.appendChild(monogr);
			Element idno = doc.createElement("idno");
			idno.setAttribute(TYPE, "...");
			//idno.appendChild(doc.createTextNode(this.headerData.getSourceNumerique()));
			biblStruct.appendChild(idno);
			
			//within monogr author, title, imprint
			Element monAuthor = doc.createElement(AUTHOR);
			Element monTitle = doc.createElement(TITLE);
			
			Element monImprint = doc.createElement("imprint");
			monogr.appendChild(monAuthor);
			monogr.appendChild(monTitle);
			monTitle.appendChild(doc.createTextNode(title));
			monogr.appendChild(monImprint);
			
			//imprint: pubPlce, publisher, date
			Element impPlace = doc.createElement("pubPlace");
			Element impPub = doc.createElement("publisher");
			Element sDbook = (Element)bookHeader.getElementsByTagName(SOURCEDESC).item(0);
			NodeList sDbookContent = sDbook.getChildNodes();
			for (int i =0; i<sDbookContent.getLength();i++){
				if(sDbookContent.item(i).getNodeType()==Node.ELEMENT_NODE){
					impPub.appendChild(doc.createTextNode(sDbookContent.item(i).getTextContent()));
				}
			}
			Element impDate = doc.createElement(DATE);
			monImprint.appendChild(impPlace);
			monImprint.appendChild(impPub);
			monImprint.appendChild(impDate);
			
			/**
			 * profileDesc 
			 **/
			
			Element pDesc =  doc.createElement("profileDesc");
			headerRoot.appendChild(pDesc);
			Element textClass = doc.createElement("textClass");
			pDesc.appendChild(textClass);
			Element kw = doc.createElement("keywords");
			textClass.appendChild(kw);
			Element term = doc.createElement("term");
			term.setAttribute(TYPE, "genre");
			kw.appendChild(term);

		

		NodeList revD= tmpHeader.getElementsByTagName("revisionDesc"); 
		Element rDesc = (Element)doc.importNode(revD.item(0), true);
		
		Node firstR =rDesc.getFirstChild();
		while(firstR.getNodeType() != Node.ELEMENT_NODE){
			firstR = firstR.getNextSibling();
		}
		((Element)firstR).setAttribute(WHO, "#AG");


		/**
		 * revisionDesc 
		 **/
		headerRoot.appendChild(rDesc);
		Calendar rightNow = Calendar.getInstance();
		StringBuilder today = new StringBuilder();
		today.append(rightNow.get(Calendar.YEAR)).append("-").append(String.format("%02d", rightNow.get(Calendar.MONTH)+1)).append("-").append(String.format("%02d", rightNow.get(Calendar.DAY_OF_MONTH)));
		Element changeE = doc.createElement(CHANGE);
	    changeE.setAttribute(WHO, "#SERMO");
		changeE.setAttribute(WHEN, today.toString());
		changeE.appendChild(doc.createTextNode("transformation  XML TEI"));
		rDesc.appendChild(changeE);

		return headerRoot;
	}
	public Element cleanContent(Element content, String title)throws Exception{
		DocumentBuilderFactory tmpFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder tmpBuilder = tmpFactory.newDocumentBuilder() ;
		Document tmp = tmpBuilder.newDocument();
		Element root = tmp.createElement("div");
		Element titleE = tmp.createElement("head");
		titleE.appendChild(tmp.createTextNode(title));
		tmp.appendChild(root);
		root.appendChild(titleE);
		int n=1;
		NodeList children= content.getChildNodes();
		 for (int i=0;i< children.getLength();i++){
			 Node thisPar= children.item(i);
			// System.out.println(thisPar.getNodeName());
			 if(thisPar.getNodeName().equals("p")){
				// NodeList parChildren= thisPar.getChildNodes();
				 //remove notes
				 for(Node childNode = thisPar.getFirstChild();
						    childNode!=null;){
						  Node nextChild = childNode.getNextSibling();
						  // Do something with childNode,
						  //   including move or delete...
						  if(childNode.getNodeName().equals("note")){
							  thisPar.removeChild(childNode);
						  }else if(childNode.getNodeName().equals("choice")){ //keep only correction in the choice
							  for(Node choiceChild = childNode.getFirstChild();
									  choiceChild!=null;){
								  Node nextChoiceChild = choiceChild.getNextSibling();
								  if(choiceChild.getNodeName().equals("sic")){
									  childNode.removeChild(choiceChild);
								  }
								  choiceChild=nextChoiceChild;
							  }
						  }else if(childNode.getNodeName().equals("choice")){
						  	  
						  }else if(childNode.getNodeName().equals("lb")){
							  
							  childNode.setTextContent(" ");
							  
						  }
						  childNode = nextChild;
						}
				 String parText = fixCh(correctNoSpace(thisPar.getTextContent()));
				 Element par = tmp.createElement("p");
				 par.setAttribute("n",Integer.toString(n));
				 n++;
				 root.appendChild(par);
				par.appendChild(tmp.createTextNode(parText.replaceAll("- ", "")));
				 			 
			 }
		 }
		return root;
	}
	         
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//LinkedList<String> result=new LinkedList<String>() ;
				try{
					 BuildAWPCollection resultXML=new  BuildAWPCollection("awp.properties");

					
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("Couldn't build XML");
				}
		
		
		
	}

}
