/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

import javax.swing.JFileChooser;

/**
 * @author dolamicl
 *
 */

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



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.unine.ILCF.SERMO.TranscriptionHandler;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;

public class XMLutils {
	/**
	 * 
	 * 
	 * @param doc
	 * @param path
	 * @return
	 */

	public static NodeList getNodeList(Document doc, String path){


		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xPath = xPathfactory.newXPath();
		try{
			NodeList nodeList =  (NodeList)xPath.evaluate(path,
					doc.getDocumentElement(), XPathConstants.NODESET);

			return nodeList;
		}catch(XPathExpressionException xpe){
			System.out.println("Problem evaluating exspression " + path);
			return null;	
		}
	}

	/**
	 * 
	 * @param doc
	 * @param nodeName
	 * @param attr
	 * @return - list of nodes with given name and attributes
	 */
public static NodeList getNodesWithAttribute(Document doc, String nodeName, String attr, String attrValue){
	
	XPathFactory xPathfactory = XPathFactory.newInstance();
	XPath xpath = xPathfactory.newXPath();
	XPathExpression expr;
	try {
		expr = xpath.compile("//"+nodeName+"[@"+attr+"=\""+ attrValue +"\"]");
		NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		return nodeList;
	} catch (XPathExpressionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
	
} 	
  /**
   * 
   * @param doc
   * @param path
   * @return
   */
	public static Node getNode(Document doc, String path){


		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xPath = xPathfactory.newXPath();
		try{
			Node node =  (Node)xPath.evaluate(path,
					doc.getDocumentElement(), XPathConstants.NODE);

			return node;
		}catch(XPathExpressionException xpe){
			xpe.getMessage();
			System.out.println("Problem evaluating exspression " + path);
			return null;	
		}
	}
	/**
	 * 
	 * @param doc
	 * @param path
	 * @param attr
	 * @return  value of the given nodes attribute
	 */
	public static String getAtributeValue(Document doc, String path ,String attr){
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xPath = xPathfactory.newXPath();
		try{
			Node node =  (Node)xPath.evaluate(path,
					doc.getDocumentElement(), XPathConstants.NODE);

			return node.getAttributes().getNamedItem(attr).getTextContent();
		}catch(XPathExpressionException xpe){
			xpe.getMessage();
			System.out.println("Problem evaluating exspression " + path);
			return null;	
		}
	}
	
	
	
	/**
	 * 
	 * @param doc
	 * @param path
	 * @return
	 */

	public static String getNodeTextContent(Document doc, String path){


		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xPath = xPathfactory.newXPath();
		try{
			Node node = (Node) xPath.evaluate(path,
					doc.getDocumentElement(), XPathConstants.NODE);
			//	cleanEmptyNodes(node);
			//	return node.getTextContent();
			return node.getTextContent().replaceAll("\\n+","");
		}catch(XPathExpressionException xpe){
			System.out.println("Problem evaluating exspression " + path);
			return null;	
		}
	}
	/**
	 * Build dom document from a File
	 * @param xml file
	 * @return Document
	 * **/
	public static Document getDoc(File file) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(file);

		return document;
	}
	/**
	 * Create a dom document from a path
	 * @param file path (String)
	 * @return Document
	 */

	public static Document getDoc(String path) throws Exception{
		return getDoc(new File(path));
	}


	/**
	 * 
	 * @param doc (Document)
	 * @param path (String)
	 * @return Tags info (LinkList<TagInfo>)
	 * problem: end offset taken before the tag is cleaned should be taken at the end
	 */
	public static LinkedList<TagInfo> getTagsInfo(Document doc, String path){
		LinkedList<TagInfo> tagList = new LinkedList<TagInfo>();
		int currentOffset = 0;

		NodeList nodes = getNodeList(doc,path);
		for (int i=0;i< nodes.getLength();i++){
			Node thisNode= nodes.item(i);
			cleanEmptyNodes(thisNode); // remove empty text nodes
			TagInfo tagInfo=new TagInfo();
			tagInfo.setName(thisNode.getNodeName());
			tagInfo.setStartOffset(currentOffset);
			//String tmpNC=thisNode.getTextContent().replaceAll("\\n\\s*", "");
			tagInfo.setEndOffset(currentOffset+thisNode.getTextContent().replaceAll("\\n+","").length());
			// tagInfo.setEndOffset(currentOffset+thisNode.getTextContent().length());

			if(thisNode.hasAttributes()){
				NamedNodeMap attributes = thisNode.getAttributes();
				HashMap<String,String> attr=new HashMap<String,String>();
				for(int j=0; j <attributes.getLength(); j++){
					attr.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());					 
				}
				tagInfo.setTagAttributes(attr);
			} 

			tagList.add(tagInfo);
			if(thisNode.hasChildNodes()){
				NodeList children = thisNode.getChildNodes();
				//System.out.println("start off: "+ currentOffset );
				tagList.addAll(addTags(currentOffset,children));
			}
			currentOffset=tagInfo.getEndOffset();
		}	
		return tagList;

	}


	/**
	 * 
	 * @param offset (int)
	 * @param children (NodeList)
	 * @return
	 */

	private static LinkedList<TagInfo> addTags (int offset, NodeList children){
		LinkedList<TagInfo> tmpList = new LinkedList<TagInfo>();
		int currentOffset=offset;

		//System.out.println("start ch off: "+ currentOffset );

		for(int i =0 ; i < children.getLength(); i++){
			Node current = children.item(i);
			if(current.getNodeType() == Node.TEXT_NODE){
				// if(current.getTextContent().trim().length()!=0){
				if(current.getTextContent().length()!=0){
					//currentOffset += current.getTextContent().length();
					currentOffset += current.getTextContent().replaceAll("\\n+","").length();
				}
			}else{

				TagInfo tagInfo=new TagInfo();
				tagInfo.setName(current.getNodeName());
				tagInfo.setStartOffset(currentOffset);
				if(current.getNodeType() == Node.COMMENT_NODE){
					tagInfo.setEndOffset( currentOffset);
					HashMap<String,String> cc=new HashMap<String,String>();
					cc.put("comment",current.getTextContent());
					tagInfo.setTagAttributes(cc);
				}else{
					tagInfo.setEndOffset(currentOffset+current.getTextContent().replaceAll("\\n+","").length());
					//tagInfo.setEndOffset(currentOffset+current.getTextContent().length());
				}
				if(current.hasAttributes()){
					NamedNodeMap attributes = current.getAttributes();
					HashMap<String,String> attr=new HashMap<String,String>();
					for(int j=0; j <attributes.getLength(); j++){
						attr.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());					 
					}
					tagInfo.setTagAttributes(attr);
				} 
				tmpList.add(tagInfo);
				if(current.hasChildNodes()){
					NodeList cc = current.getChildNodes();
					tmpList.addAll(addTags(currentOffset,cc));
				}
				currentOffset=tagInfo.getEndOffset();
			}
		}
		return tmpList;

	}

	/**
	 * 
	 * @param doc
	 * @param path
	 * @param tags
	 * @return
	 */

	public static LinkedHashMap<Integer, String> getParagraphs(Document doc, String path, LinkedList<TagInfo> tags){
		LinkedHashMap<Integer,String> paragraphList = new  LinkedHashMap<Integer,String> ();
		String nodeContent = getNodeTextContent(doc,path);
		// System.out.println("TextLength: "+ nodeContent.length());
		//get the line breaks and recalmes to add a new separator
		HashMap<Integer,String> underscoreOffset = new HashMap<Integer,String>();
		for(TagInfo tag:tags){
			// if(tag.getName().equals("lb") ||tag.getName().equals("fw")){
			if(tag.getName().equals("fw")){
				underscoreOffset.put(tag.getStartOffset(), tag.getName());
			}else if(tag.getName().equals("note")&& tag.getTagAttributes().containsKey("type")&& tag.getTagAttributes().get("type").equals("margin")){
				underscoreOffset.put(tag.getStartOffset(), tag.getName());
				underscoreOffset.put(tag.getEndOffset(), tag.getName());
			}

		}
		for(TagInfo tag:tags){
			if(tag.getName().equals("p")||tag.getName().equals("head")){
				StringBuilder pText = new StringBuilder();
				int start = tag.getStartOffset();
				int end = tag.getEndOffset();
				pText.append(nodeContent.substring(start, end));
				//number of added underscores
				int added=0;
				for(int i = start+1; i<end ;i++){
					if(underscoreOffset.containsKey(i)){
						//						 if(underscoreOffset.get(i).equals("lb")){
						//							 pText.insert(i-start+added, '_');
						//							// pText.insert(i-start+added, ' ');
						//							 added++;
						//						 }else 
						if(underscoreOffset.get(i).equals("fw")){
							pText.insert(i-start+added, '#');  
							added++;
						}else if(underscoreOffset.get(i).equals("note")){
							pText.insert(i-start+added, '@');
							added++;
						}

						//added++;
					}

				}

				paragraphList.put(tag.getStartOffset(), pText.toString() );

			}

		}

		return paragraphList;
	}

	/**
	 * 
	 * @param node
	 *  remove empty text node added for formating purposes
	 */

	private static void cleanEmptyNodes(Node node){
		//	 List<Node> toRemove = new ArrayList<Node>();

		if(node.hasChildNodes()){
			NodeList cc = node.getChildNodes();
			// System.out.println(cc.getLength());
			for(int i =0 ; i < cc.getLength(); i++){
				// System.out.println("i:"+i);
				Node current = cc.item(i);
				// System.out.println("Node c:"+ current.getNodeName());
				if(current.getNodeType() == Node.TEXT_NODE ){
					if( current.getTextContent().matches("\\n+")){
						//		 if( current.getTextContent().trim().length()==0){
						current.setNodeValue("");
						//System.out.println("removing");
						//toRemove.add(current);
					}
				}else{
					cleanEmptyNodes(current);
				}


			}
		}
		// for (Node exile: toRemove) {
		//       exile.getParentNode().removeChild(exile);
		//   }
		// return node;
	}

	/**
	 * 
	 * @param tags
	 * @param inTags
	 * @param outTags
	 */

	public static void buildTagIndex(LinkedList<TagInfo> tags, HashMap <Integer,LinkedList<Integer>> inTags, HashMap<Integer,LinkedList<Integer>> outTags){

		for(TagInfo tag:tags){
			Integer in = tag.getStartOffset();
			Integer out = tag.getEndOffset();
			Integer index = tags.indexOf(tag);
			if(inTags.containsKey(in)){
				inTags.get(in).add(index);
			}else{
				LinkedList<Integer> tmp= new LinkedList<Integer>();
				tmp.add(index);
				inTags.put(in, tmp);
			}
			if(outTags.containsKey(out)){
				outTags.get(out).addFirst(index);
			}else{
				LinkedList<Integer> tmp= new LinkedList<Integer>();
				tmp.add(index);
				outTags.put(out, tmp);
			}
		} 
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	public static Node removeMargineReclame(Node node){

		for(Node childNode = node.getFirstChild();
				childNode!=null;){
			Node nextChild = childNode.getNextSibling();
			if(childNode.getNodeName().equals("note") && ((Element)childNode).hasAttribute("type") && (((Element)childNode).getAttribute("type").equals("margin")||((Element)childNode).getAttribute("type").equals("foot"))){
				node.removeChild(childNode);
			}else if (childNode.getNodeName().equals("fw")){
				node.removeChild(childNode);
			}else{
				removeMargineReclame(childNode);
			}
			childNode = nextChild;
		}		 
		return node;

	}

	/**
	 * 
	 * @param doc (Document)
	 * @param path (String)
	 * @return Tags info (LinkList<TagInfo>)
	 * 
	 */


	public static LinkedList<TagInfo> getCleanedTagsInfo(Document doc, String path){
		LinkedList<TagInfo> tagList = new LinkedList<TagInfo>();
		int currentOffset = 0;

		NodeList nodes = getNodeList(doc,path); // just in case we are talking about more than one node 
		for (int i=0;i< nodes.getLength();i++){
			Node thisNode= nodes.item(i);
			cleanEmptyNodes(thisNode); // remove empty text nodes
			TagInfo tagInfo=new TagInfo();
			tagInfo.setName(thisNode.getNodeName());
			tagInfo.setStartOffset(currentOffset);
			//String tmpNC=thisNode.getTextContent().replaceAll("\\n\\s*", "");
			tagInfo.setEndOffset(currentOffset+thisNode.getTextContent().replaceAll("\\n+","").length());
			// tagInfo.setEndOffset(currentOffset+thisNode.getTextContent().length());

			if(thisNode.hasAttributes()){
				NamedNodeMap attributes = thisNode.getAttributes();
				HashMap<String,String> attr=new HashMap<String,String>();
				for(int j=0; j <attributes.getLength(); j++){
					attr.put(attributes.item(j).getNodeName(), attributes.item(j).getNodeValue());					 
				}
				tagInfo.setTagAttributes(attr);
			} 

			tagList.add(tagInfo);
			if(thisNode.hasChildNodes()){
				NodeList children = thisNode.getChildNodes();
				//System.out.println("start off: "+ currentOffset );
				tagList.addAll(addTags(currentOffset,children));
			}
			currentOffset=tagInfo.getEndOffset();
		}	
		return tagList;

	}

	/**
	 * 
	 * @param node
	 * @return Node
	 * 
	 * cleaning all but div, haed, p, lb, pb, cb, note, comment
	 * removing completely  fw, sic, keeping text content for others
	 */
	public static void cleanFormatNode(Node node){
		String name = node.getNodeName();
		System.out.println("Node name: "+ name);
		Node parent = node.getParentNode();
		
		
	//	if(name.equals("sic")||name.equals("fw")||name.equals("figure")||name.equals("#comment")||(name.equals("#text")&&node.getTextContent().matches("(?s)\\s+"))){
		if(name.equals("sic")||name.equals("abbr")||name.equals("fw")||name.equals("figure")||(name.equals("#text")&&node.getTextContent().matches("(?s)\\s+"))){
			parent.removeChild(node);
			parent.normalize();
			System.out.println("Removed: "+ name);
		}else if(node.hasChildNodes()){
				System.out.println("Has children "+ name);
				NodeList nodeList = node.getChildNodes();
				System.out.println("before: "+nodeList.getLength());
				for(int i = 0; i < nodeList.getLength(); i++){ 
					Node child = nodeList.item(i);
					String childName = child.getNodeName();
					System.out.println("Child: "+ childName);
					if (child.hasChildNodes() && !childName.equals("front") && !childName.equals("body") 
							&& !childName.equals("div") && !childName.equals("head") 
							&& !childName.equals("p") && !childName.equals("note")
							&& 
							!(childName.equals("sic")||name.equals("abbr")||childName.equals("fw")||childName.equals("figure")||childName.equals("#comment")
									)
							){
						NodeList childMoveList = child.getChildNodes();
						Node last = child;
						for(int j =  childMoveList.getLength() -1 ; j >= 0; j--){
							Node next  = childMoveList.item(j);
							System.out.println("Inserting: "+ next.getNodeName());
							node.insertBefore(next, last);	
							last=next;
							
						}
						System.out.println("Removing Child: "+ childName);
						node.removeChild(child);
					}
				}
				node.normalize();
				NodeList nodeListAfter = node.getChildNodes();	
				System.out.println("after: "+nodeListAfter.getLength());
				for(int k = nodeListAfter.getLength() - 1; k >= 0; k--){

					Node current = nodeListAfter.item(k);
					if(current!= null){
						System.out.println("Treat: "+current.getNodeName() + k );
						cleanFormatNode(current);
					}
				}
				
				
			}else if(name.equals("#text")){
				node.setTextContent(node.getTextContent().replaceAll("\\n\\s+", ""));
				
			}
			

		




	}  

	/**
	 * 
	 * @param node
	 * @return Node
	 * 
	 * cleaning all but div, haed, p, lb, pb, cb, note, comment
	 * removing completely  fw, sic, keeping text content for others
	 */
	public static void cleanFormatNode2(Node node){
		String name = node.getNodeName();
		System.out.println("Node name: "+ name);
		Node parent = node.getParentNode();
		
	if(node!=null && parent!=null){	
	//	if(name.equals("sic")||name.equals("fw")||name.equals("figure")||name.equals("#comment")||(name.equals("#text")&&node.getTextContent().matches("(?s)\\s+"))){
		if(name.equals("sic")||name.equals("abbr")||name.equals("del")||name.equals("surplus")||name.equals("fw")||name.equals("figure")||(name.equals("#text")&&node.getTextContent().matches("(?s)\\s+"))){
			parent.removeChild(node);
			parent.normalize();
			System.out.println("Removed: "+ name);
		}else if(node.hasChildNodes()){
			NodeList childrenList = node.getChildNodes();   
			LinkedList<Node> newChildrenList = new LinkedList<Node>();
			if (!name.equals("front") && !name.equals("body") 
					&& !name.equals("div") && !name.equals("head") 
					&& !name.equals("p") && !name.equals("note")&& !name.equals("ref")
					){	
				System.out.println("Children list length: "+ childrenList.getLength());
				Node last = node;
				for(int j =  childrenList.getLength() -1 ; j >= 0; j--){
					Node next  = childrenList.item(j);
					System.out.println("Inserting: "+ next.getNodeName());
					parent.insertBefore(next, last);
					newChildrenList.add(next);
					last=next;
					
				}
				System.out.println("Removing Node: "+ node);
				parent.removeChild(node);
				parent.normalize();
			   }else{
				   for(int l =  childrenList.getLength() -1 ; l >= 0; l--){
						
						newChildrenList.add( childrenList.item(l));
						
					}
				   
			   }
			System.out.println("Children list length 2: "+ newChildrenList.size());
				for(int k =0;  k < newChildrenList.size() ; k++){

					Node current = newChildrenList.get(k);
					if(current!= null){
						System.out.println("Treat: "+current.getNodeName() + k );
						cleanFormatNode2(current);
					}
				}
				
				
			}else if(name.equals("#text")){
				node.setTextContent(node.getTextContent().replaceAll("\\s*\\n\\s+", " "));
				
			}
			

		
	}



	}  


	/**
	 * 
	 * @param node
	 * @param when
	 * @param who
	 * @param what
	 * @return
	 */
	public static Node updateChangeHeader(Node node, String when, String who,String what){
		NodeList  chNodes = node.getChildNodes();
		int i=0;
		Node next = chNodes.item(0);
		System.out.println(next.getNodeName());
		while(!next.getNodeName().equals("revisionDesc") && i < chNodes.getLength() ){
			i++;
			next = chNodes.item(i);
			System.out.println(next.getNodeName());

		}
		Element change = next.getOwnerDocument().createElement("change");
		change.setAttribute("when", when);
		change.setAttribute("who", who);
		change.setAttribute("when", when);
		Node text = next.getOwnerDocument().createTextNode(what);

		change.appendChild(text);
		next.insertBefore(change,next.getFirstChild());
		return node;

	}
	
	/**
	 * 
	 * @param doc
	 * @throws Exception
	 */
	public static void removeEmptyTextNodex(Document doc) throws Exception{
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
		NodeList emptyTextNodes = (NodeList) 
		        xpathExp.evaluate(doc, XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
		    Node emptyTextNode = emptyTextNodes.item(i);
		    emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}
	
	
	public static void printXMLtoFile(Document doc, String dir, String fileName){
		
		XMLutils.printtoFile(doc, dir, fileName,"xml");
//		File outDir = new File(dir);
//		try{
//			OutputStream outStream = new FileOutputStream (new File(outDir,fileName+".xml"));	
//			TransformerFactory transformerFactory = TransformerFactory.newInstance();
//			Transformer transformer = transformerFactory.newTransformer();
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
//
//			DOMSource source = new DOMSource(doc);
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
	
public static void printtoFile(Document doc, String dir, String fileName, String ext){
		
		File outDir = new File(dir);
		try{
			OutputStream outStream = new FileOutputStream (new File(outDir,fileName+"."+ext));	
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

	/**
	 * 
	 * @param args
	 */

	public static void main(String [] args){
		try{

			JFileChooser window= new JFileChooser();
			int rv= window.showOpenDialog(null);

			if(rv == JFileChooser.APPROVE_OPTION){

				Document doc = getDoc(window.getSelectedFile());
				LinkedList<TagInfo> tags = getTagsInfo(doc,args[0] ); 

				for(TagInfo tag: tags){

					System.out.println(tag.toString());
				}

				HashMap <Integer,LinkedList<Integer>> inTags = new HashMap <Integer,LinkedList<Integer>>() ;
				HashMap<Integer,LinkedList<Integer>> outTags =new HashMap <Integer,LinkedList<Integer>>();

				buildTagIndex(tags,inTags,outTags);
				for(Integer key:  inTags.keySet()){

					System.out.println(key+ " : " + inTags.get(key));
				}

				for(Integer key:  outTags.keySet()){

					System.out.println(key+ " : " + outTags.get(key));
				}
				LinkedHashMap<Integer, String> pl= getParagraphs(doc,args[0],tags);

				for(Integer key:  pl.keySet()){

					System.out.println(key+ " : " + pl.get(key));
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}

	}


}
