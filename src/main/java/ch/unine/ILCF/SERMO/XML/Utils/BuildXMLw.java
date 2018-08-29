/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

import java.util.Calendar;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//import ch.unine.ILCF.SERMO.XMLtoDbVrtXMLw.FullPartInfo;
import ch.unine.ILCF.SERMO.collection.FullPartInfo;
import ch.unine.ILCF.SERMO.collection.TokenInfo;

/**
 * @author dolamicl
 *
 */

import ch.unine.ILCF.SERMO.propreties.*;

public class BuildXMLw {
	private Document doc;
	private Properties props;
	private boolean letterine=false;
	private Set<String> notFirst = new TreeSet<String>();
	//private LinkedList<Element> toReopen= new LinkedList<Element>();
	private boolean note_token=false;
	private String note_id;
	
	static HashMap<String,String> tagReplace;
	static{
		tagReplace=new HashMap<String,String>();
		tagReplace.put("bibl","seg");
		tagReplace.put("quote","seg");
		tagReplace.put("q","seg");

	}
    public BuildXMLw (Properties prop){
    	this.props=prop;
    	notFirst.add("p");
		notFirst.add("s");
		notFirst.add("head");
    }
    

    public void buildXML(String doc_id,Node header, FullPartInfo frontInfo,
	 FullPartInfo bodyInfo,
	 HashMap<Integer,FullPartInfo> frontNotesInfo,
	HashMap<Integer,FullPartInfo> bodyNotesInfo){
    	
    	try{
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder() ;
		this.doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("TEI");
		doc.appendChild(rootElement);
		rootElement.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
		
		Node head = doc.importNode(header, true);
		rootElement.appendChild(head);
		Calendar rightNow = Calendar.getInstance();
		StringBuilder today = new StringBuilder();
		today.append(rightNow.get(Calendar.YEAR)).append("-").append(String.format("%02d", rightNow.get(Calendar.MONTH)+1)).append("-").append(String.format("%02d", rightNow.get(Calendar.DAY_OF_MONTH)));
		Element changeE = doc.createElement("change");
		changeE.setAttribute("who", "#LjD");
		changeE.setAttribute("when", today.toString());
		changeE.appendChild(doc.createTextNode("w, s tags added"));
		XMLutils.getNode(this.doc, "/TEI/teiHeader/revisionDesc").appendChild(changeE);
		
		Element text = doc.createElement("text");
		rootElement.appendChild(text);
    	
    	buildParts(text, frontInfo,  frontNotesInfo);
    	buildParts(text, bodyInfo, bodyNotesInfo);
    	
    	saveDoc(doc_id);
    	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    } 
    

    private void buildParts(Node partNode,  FullPartInfo partInfo, HashMap<Integer,FullPartInfo> partNotesInfo){
    	
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
    		
//    		

//    		
    		
    		if(tagStart.containsKey(i)){
   		//tags to open before the word
            
    		currentNode = openTags(currentNode, tagStart.get(i));
    		}
    		
    		if(partNotesInfo.containsKey(i)){
    			note_token=true;
    			note_id="n_"+i;
            	buildParts(currentNode, partNotesInfo.get(i), new HashMap<Integer,FullPartInfo>());
            	note_token=false;
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
		Element newW = this.doc.createElement("w");
		StringBuilder fT=new StringBuilder();
		for(String s: token.getFinalToken()){
			fT.append(s).append(" ");
			
		}
		addId(newW,token);
		//newW.setAttribute("ana", fT.toString().trim());
		newW.setAttribute("type", token.getPos());
		newW.setAttribute("lemma", token.getLemma());
		if(this.letterine){
			Element c = this.doc.createElement("c");
			newW.appendChild(c);
			c.appendChild(this.doc.createTextNode(token.getToken().substring(0,1)));
			newW.appendChild(this.doc.createTextNode(token.getToken().substring(1)));
			this.letterine=false;
		}else{
		newW.appendChild(this.doc.createTextNode(token.getToken()));
		}
		currentNode.appendChild(newW);
    	
    }
    

    private Node openTags(Node current,  HashMap<Integer, LinkedList<TagInfo>> tagsToOpen){
    	Node parent = current;
    	SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToOpen.keySet())).descendingSet();

    	System.out.println("open tags: "+ keys);
    	for(Integer key: keys){
    		LinkedList<TagInfo> tagList=tagsToOpen.get(key);
    		System.out.println(tagList);
    		try{
    			while(!tagList.isEmpty()){
    				TagInfo tmp = tagList.pollLast();
    				String tmpName = tmp.getName();
// not adding the correction or 
    				if(!tmpName.equals("choice")
    						&&!tmpName.equals("corr")
    						&&!tmpName.equals("expan")
    						&&!tmpName.equals("supplied")){
    				//    	    		// if it's not a simple tag lb,pb
    				if(tmpName.equals("#comment")){
    					parent.appendChild(this.doc.createComment(tmp.getTagAttributes().get("comment")));
    				}else if(tmpName.equals("c")){
    					this.letterine=true;
    				}else{	
    					Element newE;
    					if(tmpName.equals("bibl") || tmpName.equals("quote")||tmpName.equals("q")){
    						newE = this.doc.createElement("seg");
    						newE.setAttribute("type", tmpName);
    					}else{
    						newE = this.doc.createElement(tmpName);
    					}
    					//Element newE = this.doc.createElement(tmpName);
    					if(!tmp.getTagAttributes().isEmpty()){
    						Map<String, String> attrs = tmp.getTagAttributes();
    						for(String atr : attrs.keySet() ){
    							newE.setAttribute(atr, attrs.get(atr));
    							//addTagId(newE,tmp);
    						}

    					}
    					//	parent.appendChild(newE);
    					//  
    					if(tmpName.equals("pb")     					
    							|| tmpName.equals("cb")
    							|| tmpName.equals("milestone")){
    							Node addTo = parent;
    							Node before = parent;		
    							while(this.notFirst.contains(addTo.getNodeName()) && (!addTo.hasChildNodes() || addTo.getChildNodes().getLength()==1)){
    								before=addTo;
    								addTo=addTo.getParentNode();
    							}

    							if(addTo.hasChildNodes() && (addTo != before)){
    								addTo.insertBefore(newE,before);
    							}else{
    								addTo.appendChild(newE);
    							}

    						}else{
    							parent.appendChild(newE);
    							if(!tmpName.equals("lb") 
    	    							&& !tmpName.equals("pb") 
    	    							&& !tmpName.equals("figure")
    	    							&& !tmpName.equals("cb")
    	    							&& !tmpName.equals("milestone")
    	    							&& !tmpName.equals("g")
    	    							){
    	    						
    	    						parent=newE;

    	    					}
    						}
    					
    				}
    				}//if
    			}//while
    		}catch(Exception e){
    			System.out.println(e.getMessage());
    			e.printStackTrace();
    		}

    	}

    	return parent;


    } 

    
    private void addId(Element tag, TokenInfo token){
    	if(note_token){
    		tag.setAttribute("xml:id", "w_"+ token.getPageNo()+"_"+note_id+"_"+String.format("%06d", token.getToken_id()));
    	}else{
    	tag.setAttribute("xml:id", "w_"+ token.getPageNo()+"_"+String.format("%06d", token.getToken_id()));
    	}
    	
    }
    
    private Node closeTags(Node current, HashMap<Integer, LinkedList<TagInfo>> tagsToClose){
    	Node parent = current;
    	String currentName=current.getNodeName();
    	SortedSet<Integer> keys = (new TreeSet<Integer>(tagsToClose.keySet())).descendingSet();
    	System.out.println("end: "+keys);
    	for(Integer key: keys){
    		LinkedList<TagInfo> tagList=tagsToClose.get(key);
    		System.out.println(tagList);
    		SortedSet<String> tagsToCloseNames=  new TreeSet<String>();
    		for(TagInfo tag:tagList){
    			tagsToCloseNames.add(this.tagReplace.containsKey(tag.getName())?this.tagReplace.get(tag.getName()):tag.getName());
    		}
    		System.out.println("tags to close"+tagsToCloseNames.toString());
    		try{
    			LinkedList<Element> toReopen= new LinkedList<Element>();
    	    	while(!tagList.isEmpty()){
    	    		
    	    		TagInfo tmp = tagList.pollFirst();
    	    		String tmpName = this.tagReplace.containsKey(tmp.getName())?this.tagReplace.get(tmp.getName()):tmp.getName();
    	    		
    	    		if(!tmpName.equals("choice")
    						&&!tmpName.equals("corr")
    						&&!tmpName.equals("expan")
    						&&!tmpName.equals("supplied")){
    	    			//System.out.println("to keep "+ tmpName);
    	    			while(!parent.getNodeName().equals(tmpName) && !tagsToCloseNames.contains(parent.getNodeName())){
    	    			//	System.out.println("Not in to remove "+ tmpName);
    	    				Element tmpCopy =(Element) parent.cloneNode(false);
    	    				toReopen.add(tmpCopy);
   	    			 		parent = parent.getParentNode();
   	    			 	//	System.out.println("WARNiNG!!!!!! closing "+parent.getNodeName()+"with:  "+tmpName);
    	    			}
   	    			 parent = parent.getParentNode();
   	    			 
   	    		//System.out.println("Parent now "+parent.getNodeName());
    	    		}
    	    		
   	    			
    	    	}//while
    	    	
    	    	while(!toReopen.isEmpty()){
	    				Element next = toReopen.removeFirst();
	    				
	    				//System.out.println("Reopening: "+next.getNodeName()+" On parent: "+ parent.getNodeName());
	    				 
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
    private void saveDoc(String fileName){
    	
    	XMLutils.printXMLtoFile(this.doc, this.props.getProperty("xml.w"), fileName);
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
