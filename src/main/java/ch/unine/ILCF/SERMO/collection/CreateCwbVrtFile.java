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
public class CreateCwbVrtFile {
	
	
	private Properties prop;
	
	private Properties metaProps;
	private Set<Object> metaKeys;
	
	
	private Document doc;
	
	private boolean letterine=false;
	private Set<String> notFirst = new TreeSet<String>();
	//private LinkedList<Element> toReopen = new LinkedList<Element>();
	//private HashMap<String, Integer> lastOpenedEndOffset = new HashMap<String, Integer>();
	//private int toReopenAfter =0;
	public CreateCwbVrtFile(Properties props){
		this();
		this.prop=props;
		if(this.prop.containsKey("vrt.meta")){
			this.metaProps = SermoProperties.getProperties(this.prop.getProperty("vrt.meta"));
			this.metaKeys = metaProps.keySet();
		}
	}
	
	public CreateCwbVrtFile(){
		
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
	
	
	 
	 public void buildVrt(String doc_id,Document docOrg, FullPartInfo frontInfo,
			 FullPartInfo bodyInfo,
			 HashMap<Integer,FullPartInfo> frontNotesInfo,
			 HashMap<Integer,FullPartInfo> bodyNotesInfo){
		 
		 
		 try{
		    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
				this.doc = docBuilder.newDocument();
			
				Element text = doc.createElement("text");
				this.doc.appendChild(text);
				Connection connection;
				
				
					connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
								this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
					String doc_id_cqp = GetFromDatabase.getDocsCQPId(connection,doc_id);
					System.out.println("CQP id: "+ doc_id_cqp );
					connection.close();
				
				text.setAttribute("id", doc_id_cqp);
				String url="http://sermo.unine.ch/SERMO/collection/doc?docId="+doc_id;
				text.setAttribute("url", url);
				// if we have the original, we can get the meta data if not we work without it
				if(docOrg != null){
				   addMetaDataToVrt(doc_id_cqp ,text , docOrg , url);
				}
		    	
		   
		    	buildParts(text, bodyInfo, bodyNotesInfo);
		  
		    	saveDoc(doc_id_cqp);
		    	
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}
		 
	 }
	 
	 public void buildVrt(String doc_id, FullPartInfo bodyInfo, HashMap<Integer,FullPartInfo> bodyNotesInfo){
		 
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
	 
	 public void buildVrt(String id,String url_id, FullPartInfo bodyInfo, HashMap<Integer,FullPartInfo> bodyNotesInfo){
		 
	       System.out.println("Building vrt");
	 
	 try{
	    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
			this.doc = docBuilder.newDocument();
		
			
			Element text = doc.createElement("text");
			this.doc.appendChild(text);
			
			
			text.setAttribute("id", id);
			String url="http://sermo.unine.ch/SERMO/collection/doc?docId="+url_id;
			text.setAttribute("url", url);

	    	buildParts(text, bodyInfo, bodyNotesInfo);
	    	
	    	saveDoc(id);
	    	
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	 
}
	 
	 private void addMetaDataToVrt(String id,Node text, Document doc, String url){
		 
		 
		 StringBuilder dec=new StringBuilder();
		 StringBuilder metaLine= new StringBuilder();
		 metaLine.append(id);
		 metaLine.append("\t").append(url);
		 for(Object key:this.metaKeys){
			 try{
				 String path = metaProps.getProperty((String)key); 
				 XPathFactory factory = XPathFactory.newInstance();
				 XPath xpath = factory.newXPath();
				 Node attNode = (Node) xpath.evaluate(path, doc, XPathConstants.NODE);
				 String tmpS = attNode == null ? "" : (attNode.getTextContent().replaceAll("\\s+", " ")).trim(); // in case the xml file dosen't have the the info set 
				 if(tmpS.equals("")){
					// ((Element)text).setAttribute((String) key,"INCONNU" );
					 tmpS="INCONNU";
				 }else{
					 if(key.toString().matches("\\w+_date")){ // filters need to be stripped
						 tmpS=tmpS.replaceAll(",", "|");
						 if(key.toString().equals("pub_date")){
							 Integer start = ((int)(Integer.parseInt(tmpS)/10))*10;
							 Integer end = (start==1740? 1750:start+9);
							 dec.append(start.toString()).append("_").append(end.toString());
							// ((Element)text).setAttribute("decenie",dec.toString() );
							 metaLine.append("\t").append(dec.toString());
						 }
					 }else if(key.toString().equals("author")||
							 key.toString().equals("typologie")||
							 key.toString().equals("pub_place")
							 ){
						 tmpS=stripAccents(tmpS).replaceAll("[^\\w\\d_]", "_");
					 }
					 
					// ((Element)text).setAttribute((String) key,tmpS );
				 }
				 metaLine.append("\t").append(tmpS);
			 }catch(XPathExpressionException e){

				 System.out.println("problem reading path:");
				 e.printStackTrace();
			 }
		 }
		 
		 System.out.println("META:  "+metaLine.toString());
		 try{
			 
			 FileWriter fw = new FileWriter(this.prop.getProperty("cqp.meta"), true);
		
				    BufferedWriter bw = new BufferedWriter(fw);
				    PrintWriter out = new PrintWriter(bw);
				    
				    out.println(metaLine.toString());
				    
				   // out.println(metaLine.toString());
				    out.close();
				    
				} catch (IOException e) {
					System.out.println("META2");
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
			//toReopen= new LinkedList<Element>();
	    	//putting back the notes at their place
//			buildParts(partNode, tokens, tagStart, tagEnd);
//			if( partNotesInfo != null){
//			Node finalSibling=partNode.getLastChild();
//			while(finalSibling.getNodeName().equals("#text")){
//				finalSibling= finalSibling.getPreviousSibling();
//			}
//			System.out.println("Adding notes to :" +finalSibling.getNodeName());
//			System.out.println("No. notes: "+partNotesInfo.keySet().size());
//			for(int i: partNotesInfo.keySet()){
//				tokens =partNotesInfo.get(i).getTokens();
//		    	tagStart = partNotesInfo.get(i).getTagStart();
//				tagEnd = partNotesInfo.get(i).getTagEnd();
//				
//				buildParts(finalSibling, tokens, tagStart, tagEnd);
//				
//				
//				
//			}
//			}
			
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
		    		
//		    		

//		    		
		    		
		    		if(tagStart.containsKey(i)){
		   		//tags to open before the word
		            
		    		currentNode = openTags(currentNode, tagStart.get(i));
		    		}
		    		
//		    		while(!toReopen.isEmpty()){
//	    				Element next = toReopen.removeFirst();
//	    				
//	    			//	System.out.println("Reopening: "+next.getNodeName()+" On parent: "+ parent.getNodeName());
//	    				 
//	    				Node tmpNode=this.doc.importNode(next,true);
//	    				
//	    				currentNode.appendChild(tmpNode);
//	    				
//	    				currentNode=tmpNode;
//	    				
//	    				//System.out.println(" parent after reopening: "+ parent.getNodeName()+" with parent: "+parent.getParentNode().getNodeName());
//	    			}
		    		
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
				currentNode.appendChild(this.doc.createTextNode("\n"));
			}
	    	StringBuilder wI = new StringBuilder();

			wI.append(token.getToken().replaceAll("&amp;", "&").replaceAll("e\\u0303","\u1ebd").replaceAll("q̃","q͂")).append("\t");//q\u0303 by q\u0342

			wI.append(token.getModern()).append("\t");
			wI.append(token.getPos()).append("\t");
			wI.append(token.getLemma()).append("\t");
			wI.append(token.getPageNo()).append("\n");
			
			
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

//	    	    		
//	    	    		if(!toReopen.isEmpty()){
//	    	    		for(int i =0; i < toReopen.size();i++){
//		    				Element next = toReopen.get(i);
//		    				String name = next.getTagName();
//		    				int eO = lastOpenedEndOffset.get(name);
//		    				if(eO > endOffset){
//		    					Node tmpNode=this.doc.importNode(next,true);
//			    				
//			    				parent.appendChild(tmpNode);
//			    				
//			    				parent=tmpNode;
//			    				toReopen.remove(i);
//		    				}
//		    		    }
//	    	    		
//	    	    		}
	    	    	if(tmpName.equals("#comment")){
	    	    		//parent.appendChild(this.doc.createComment(tmp.getTagAttributes().get("comment")));
	    	    	}else if(tmpName.equals("c")){
	    	    		this.letterine=true;
	    	    	}else{	
	    	    		Element newE;
	    	    		//if(tmpName.equals("bibl") || tmpName.equals("quote")||tmpName.equals("q")){
    					//	newE = this.doc.createElement("seg");
    					//	newE.setAttribute("type", tmpName);
	    	    		//}else{
	    	    		System.out.println("node: "+tmpName);
	    	    			 newE = this.doc.createElement(tmpName);
	    	    		//}
	    	    		//Element newE = this.doc.createElement(tmpName);
	    	    		if(!tmp.getTagAttributes().isEmpty()){
	    	    			Map<String, String> attrs = tmp.getTagAttributes();
	    	    			for(String atr : attrs.keySet() ){
	    	    				System.out.println("attribute: "+atr+" : "+attrs.get(atr));
	    	    				newE.setAttribute(atr.trim(), attrs.get(atr));
	    	    				//System.out.println("attribute: "+atr+" : "+attrs.get(atr));
	    	    				//addTagId(newE,tmp);
	    	    			}
	    	    			
	    	    		}
	    	    		System.out.println("At ok");
	    	    	//	Nodes kept by the extraction but not added to vrt
//	    	    		
	    	    		if(!tmpName.equals("lb") 
	    	    				&& !tmpName.equals("pb") 
	    	    				&& !tmpName.equals("figure")
	    	    				&& !tmpName.equals("cb")
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
	    	    			parent.appendChild(newE);
	    	    			parent=newE;
	    	    			parent.appendChild(this.doc.createTextNode("\n"));
	    	    		   
	   	    		}
	  //  	    		toReopenAfter--;
  //                      if(toReopenAfter==0){
   //                     	while(!toReopen.isEmpty()){
  //      	    				Element next = toReopen.removeFirst();
//        	    				
//        	    			//	System.out.println("Reopening: "+next.getNodeName()+" On parent: "+ parent.getNodeName());
//        	    				 
  //      	    				Node tmpNode=this.doc.importNode(next,true);
//        	    				
 //     	    				    parent.appendChild(tmpNode);
//        	    				
 //       	    				parent=tmpNode;
//        	    				
//        	    				//System.out.println(" parent after reopening: "+ parent.getNodeName()+" with parent: "+parent.getParentNode().getNodeName());
     ///  	    			}
                        	
       //                 }
	    	    		
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
	    	
	    	//XMLutils.printtoFile(this.doc, this.prop.getProperty("vrt.home"), fileName,"vrt");
	    	
	    	File outDir = new File(this.prop.getProperty("vrt.home"));
			try{
				OutputStream outStream = new FileOutputStream (new File(outDir,fileName+".vrt"));	
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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
