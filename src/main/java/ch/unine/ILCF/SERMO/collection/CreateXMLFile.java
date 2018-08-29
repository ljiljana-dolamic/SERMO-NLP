


/**
 * 
 */
package ch.unine.ILCF.SERMO.collection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.Connection;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;
//import ch.unine.ILCF.SERMO.XMLtoDbVrtXMLw.FullPartInfo;
import ch.unine.ILCF.SERMO.collection.FullPartInfo;

/**
 * @author dolamicl
 *
 */
public class CreateXMLFile {


	
	
	private Properties prop;
	
	//private Properties metaProps;
	//private Set<Object> metaKeys;
	
	
	private Document doc;
	
	private static int lastOffset=0;
	private Set<String> notFirst = new TreeSet<String>();
	//private LinkedList<Element> toReopen = new LinkedList<Element>();
	//private HashMap<String, Integer> lastOpenedEndOffset = new HashMap<String, Integer>();
	//private int toReopenAfter =0;
	
	public  CreateXMLFile(Properties props){
		this();
		this.prop=props;
		//if(this.prop.containsKey("xml.meta")){
		//	this.metaProps = SermoProperties.getProperties(this.prop.getProperty("xml.meta"));
		//	this.metaKeys = metaProps.keySet();
		//}
	}
	
	public  CreateXMLFile(){
		
		notFirst.add("p");
		notFirst.add("s");
		notFirst.add("head");
		
	}
	
	static HashMap<String,String> tagReplace;
	static{
		tagReplace=new HashMap<String,String>();
		//tagReplace.put("bibl","seg");
		//tagReplace.put("quote","seg");
		//tagReplace.put("q","seg");

	}
	
	
	 
	 
	 public void buildXML(String doc_id, FullPartInfo bodyInfo, HashMap<Integer,FullPartInfo> bodyNotesInfo){
		 
		       System.out.println("Building vrt");
		 
		 try{
		    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
				this.doc = docBuilder.newDocument();
			
				
				Element text = doc.createElement("text");
				this.doc.appendChild(text);
				
				
				text.setAttribute("id", doc_id);
				String url="http://sermo.unine.ch/SERMO/collection/doc?docId="+doc_id;
				text.setAttribute("url", url);
 
		    	buildParts(text, bodyInfo, bodyNotesInfo);
		    	
		    	saveDoc(doc_id);
		    	
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
		 
	 }
	 
	 
	 
	 
	 
	 /**
	  * 
	  * @param partNode
	  * @param partInfo
	  * @param partNotesInfo
	  */
	 
	   
	    
	    private void buildParts(Node partNode,  FullPartInfo partInfo, HashMap<Integer,FullPartInfo> partNotesInfo){
	    	
	    	System.out.println("Building parts");
	    	Node currentNode = partNode;
	    	LinkedList<TokenInfo> tokens = partInfo.getTokens();
	    	HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagStart = partInfo.getTagStart();
			HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagEnd = partInfo.getTagEnd();
		
			if(!tokens.isEmpty()){
		    	int last = tokens.peekLast().getEndOffset();
		    	TokenInfo nextToken = null;
		    	Iterator<TokenInfo> tfi = tokens.iterator();
		    	if(tfi.hasNext()){
		    		nextToken = tfi.next();
		    		
		    	}
		    	
		    	for(int i = 0; i <= last; i++){
		    		
		    		
		    		// tags to close before the word
		    		if(tagEnd.containsKey(i)){
		    			currentNode = closeTags(currentNode, tagEnd.get(i));
		    		}
		    		
		    		
		    		if(tagStart.containsKey(i)){
		   		//tags to open before the word
		            
		    		currentNode = openTags(currentNode, tagStart.get(i));
		    		}
		    		
	    		
		    		if(partNotesInfo.containsKey(i)){
		    		//	note_token=true;
		    		//	note_id="n_"+i;
		            	buildParts(currentNode, partNotesInfo.get(i), new HashMap<Integer,FullPartInfo>());
		            //	note_token=false;
		            }
		    		
		    		if(nextToken!=null && nextToken.getStartOffset()==i){
		    			addWord(currentNode,nextToken);
		    			if(tfi.hasNext()){
		    	    		nextToken = tfi.next();
		    	    		
		    	    	}
		    		}
		    	}
		    	}
	    }
	    
	    
	    private void addWord(Node currentNode,TokenInfo token){
			if(!currentNode.hasChildNodes()||(currentNode.hasChildNodes()&&currentNode.getLastChild().getNodeType() != Node.TEXT_NODE)){
				//currentNode.appendChild(this.doc.createTextNode("\n"));
			}
	    	StringBuilder wI = new StringBuilder();
            if(token.getStartOffset()!= lastOffset){
            	wI.append(" ");
            } 
			wI.append(token.getToken().replaceAll("e\\u0303","\u1ebd").replaceAll("q̃","q͂"));//q\u0303 by q\u0342

			lastOffset = token.getEndOffset();
			
			currentNode.appendChild(this.doc.createTextNode(wI.toString()));
	    	
	    }
	    
	    
	    private Node openTags(Node current,  HashMap<Integer, LinkedList<TagInfo>> tagsToOpen){
	    	Node parent = current;
	    	SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToOpen.keySet())).descendingSet();
	    	//SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToOpen.keySet()));
	    	for(Integer key: keys){
	    		LinkedList<TagInfo> tagList= (LinkedList<TagInfo>) tagsToOpen.get(key).clone();
	    		try{
	    	    	while(!tagList.isEmpty()){
	    	    		TagInfo tmp = tagList.pollLast();
	    	    		//TagInfo tmp = tagList.pollFirst();
	    	    		String tmpName = tmp.getName();
	    	    	if(tmpName.equals("#comment")){
	    	    		//parent.appendChild(this.doc.createComment(tmp.getTagAttributes().get("comment")));
	    	    	}else if(tmpName.equals("c")){
	    	    	//	this.letterine=true;
	    	    	}else{	
	    	    		Element newE;
	    	    		System.out.println("node: "+tmpName);
	    	    			 newE = this.doc.createElement(tmpName);
	    	    		if(!tmp.getTagAttributes().isEmpty()){
	    	    			Map<String, String> attrs = tmp.getTagAttributes();
	    	    			for(String atr : attrs.keySet() ){
	    	    				System.out.println("attribute: "+atr+" : "+attrs.get(atr));
	    	    				newE.setAttribute(atr.trim(), attrs.get(atr));
	    	    				}
	    	    			
	    	    		}
	    	    		System.out.println("At ok");
	    	    	//	Nodes kept by the extraction but not added to xml
//	    	    		
	    	    		if(
	    	    				!tmpName.equals("figure")
	    	    				//&& !tmpName.equals("cb")
	    	    				&& !tmpName.equals("milestone")
	    	    				&& !tmpName.equals("letterine")
	    	    				&& !tmpName.equals("#comment")
	    	    				&& !tmpName.equals("supplied")
	    	    				&& !tmpName.equals("choice")
	    	    				&& !tmpName.equals("corr")
	    	    				&& !tmpName.equals("subst")
	    	    				&& !tmpName.equals("add")
	    						&& !tmpName.equals("expan")
	    						&& !tmpName.equals("unclear")
	    						&& !tmpName.equals("g")
	    	    				){
	    	    			parent.appendChild(this.doc.createTextNode("\n"));// start a tag on a new line
	    	    			parent.appendChild(newE);
	    	    			if(!tmpName.equals("lb")
		    	    				&& !tmpName.equals("pb")
		    	    				&& !tmpName.equals("cb")){
	    	    				parent=newE;
	    	    			}else{
	    	    				lastOffset++;
	    	    			}
	    	    			
	    	    		   
	   	    		}
	    	    		
	    	    	}
	    	    	}
	    	    	}catch(Exception e){
	    	    		System.out.println("Error CCV: "+ e.getMessage());
	    	    		e.printStackTrace();
	    	    		
	    	    		
	    	    	}
	    		
	    	}
	    	
	    	return parent;
	    	
	    	
	    } 
	    

	    
	    private Node closeTags(Node current, HashMap<Integer, LinkedList<TagInfo>> tagsToClose){
	    	Node parent = current;
	    	//String currentName=current.getNodeName();
	    	SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToClose.keySet())).descendingSet();
	    	//SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToClose.keySet()));
	    	//System.out.println("Curent name: "+ currentName +" end: "+keys);
	    	for(Integer key: keys){
	    		LinkedList<TagInfo> tagList=tagsToClose.get(key);
	    		//System.out.println("tags to close: "+ tagList+" at "+ key);
	    		SortedSet<String> tagsToCloseNames=  new TreeSet<String>();
	    		for(TagInfo tag:tagList){
	    			tagsToCloseNames.add(tagReplace.containsKey(tag.getName())?tagReplace.get(tag.getName()):tag.getName());
	    		}
	    		//System.out.println("tags to close"+tagsToCloseNames.toString());
	    		try{
	    			//toReopen= new LinkedList<Element>();
	    		//	toReopenAfter=0; 
	    			
	    			LinkedList<Element> toReopen= new LinkedList<Element>();
	    	    	while(!tagList.isEmpty()){
	    	    		
	    	    		TagInfo tmp = tagList.pollFirst();
	    	    		String tmpName = tagReplace.containsKey(tmp.getName())?tagReplace.get(tmp.getName()):tmp.getName();
	    	    		
	    	    		//	Nodes kept by the extraction but not added to vrt
	    	    		if(!tmpName.equals("choice")
	    	    				&&!tmpName.equals("subst")
	    	    				&&!tmpName.equals("add")
	    						&&!tmpName.equals("corr")
	    						&&!tmpName.equals("expan")
	    						&&!tmpName.equals("supplied")
	    						&&!tmpName.equals("unclear")
	    						&&!tmpName.equals("letterine")){
	    	    			//System.out.println("to keep "+ tmpName);
	    	    			while(!parent.getNodeName().equals(tmpName) && !tagsToCloseNames.contains(parent.getNodeName())){
	    	    				System.out.println("Not in to remove "+ tmpName +" parent:  "+ parent.getNodeName());
	    	    				Element tmpCopy =(Element) parent.cloneNode(false);
	    	    				toReopen.add(tmpCopy);
	    	    				//toReopenAfter++;
	    	    				if(!parent.hasChildNodes()||parent.getTextContent().matches("\\s+")){   // to avoid having accidently reopened empty nodes
		    	    				Node tmpCurrent=parent;
		    	    				parent = parent.getParentNode();
		    	    				parent.removeChild(tmpCurrent); 
		    	    			}else{
		    	    				 parent = parent.getParentNode();
		    	    			}
	   	    			 		//parent = parent.getParentNode();
	   	    			 	//	System.out.println("WARNiNG!!!!!! closing "+parent.getNodeName()+"with:  "+tmpName);
	    	    			}
	    	    			if(!parent.hasChildNodes()||parent.getTextContent().matches("\\s+")){   // to avoid having accidently reopened empty nodes
	    	    				Node tmpCurrent=parent;
	    	    				parent = parent.getParentNode();
	    	    				parent.removeChild(tmpCurrent); 
	    	    			}else{
	    	    				 parent = parent.getParentNode();
	    	    			}
	   	    			
	   	    			 
	   	    		//System.out.println("Parent now "+parent.getNodeName());
	    	    		}
	    	    		
	    	    		//parent.appendChild(this.doc.createTextNode("\n"));// to avoid having first word glued to the last closed tag 
	    	    	}//while
	    	    	
	    	    	while(!toReopen.isEmpty()){
		    				Element next = toReopen.removeFirst();
		    				
		    			//	System.out.println("Reopening: "+next.getNodeName()+" On parent: "+ parent.getNodeName());
		    				 
		    				Node tmpNode=this.doc.importNode(next,true);
		    				
		    				parent.appendChild(tmpNode);
		    				
		    				parent=tmpNode;
		    				
		    				//System.out.println(" parent after reopening: "+ parent.getNodeName()+" with parent: "+parent.getParentNode().getNodeName());
		    			}
	    	    	
	    	    	}catch(Exception e){
	    	    		
	    	    		e.printStackTrace();
	    	    	}
	    		
	    	}
	    	
	    	return parent;
	    	
	    	
	    } 
	    
	 
	    public static String stripAccents(String s) 
	    {
	        s = Normalizer.normalize(s, Normalizer.Form.NFD);
	        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
	        return s;
	    }

	    private void saveDoc(String fileName){
	    	
	    	
	    	File outDir = new File(this.prop.getProperty("xml.home"));
			try{
				OutputStream outStream = new FileOutputStream (new File(outDir,fileName+".xml"));	
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				//transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

				DOMSource source = new DOMSource(this.doc);

				StreamResult result = new StreamResult(outStream);

				transformer.transform(source, result);

			}catch(TransformerException tfe){

			}catch(IOException e){

			}
	    }
	 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
