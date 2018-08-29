/**
 * 
 */
package ch.unine.ILCF.SERMO.collection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import ch.unine.ILCF.SERMO.XMLtoDbVrtXMLw;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;


/**
 * @author dolamicl
 *
 */
public class AddNewTag {
	
	private String docId;
	private String tagName; 
	private String attr; 
	private String attrValue;
	private int startOffset=0;
	private int endOffset=0;

	private String brokenToken="";
	
	private Document xmlDocument;
	
	private String tagToAdd;
	
	private int nextTagId;
	
	private NodeList nodeList;
	
	
	private LinkedList<TokenInfo> tokens;
	private LinkedList<TagInfo> tags;
	
	private Properties prop;
	
	private Connection connection;
	/**
	 * 
	 * 
	 */
	public AddNewTag(String propertiesFilePath){
		this.prop = SermoProperties.getProperties(propertiesFilePath);
		if(this.prop.containsKey("tagName")
				&&this.prop.containsKey("attribute")
				&&this.prop.containsKey("attributeValue")
				&&this.prop.containsKey("host")
				&&this.prop.containsKey("dbuser")
				&&this.prop.containsKey("dbpassword")){
			this.tagName = this.prop.getProperty("tagName");
			this.attr = this.prop.getProperty("attribute");
			this.attrValue = this.prop.getProperty("attributeValue");
	
		
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
	 * 
	 * @param fileName
	 */
	public void getNewTags(String fileName){
		
		System.out.println("Starting");
		File file = new File(fileName);
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File f:files){
				this.brokenToken="";
				System.out.println("Starting file:" + f.getName());
				buildXmlDoc(f);
				getDocId();
				getDocTokens();
				getFirstTagId();
				buildTags(f);
				
			}
		}else{
			buildXmlDoc(file);
			getDocId();
			getDocTokens();
			getFirstTagId();
			buildTags(file);
		}

		
		
		
	}
	/**
	 * 
	 * @param path
	 */
	private void buildXmlDoc(File file){
		
	
			try {
				this.xmlDocument = XMLutils.getDoc(file);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
			
	}
	/**
	 * 
	 * @return
	 */
	private void getDocId(){
		String  nodePath ="/TEI/teiHeader/fileDesc";
		String attr= "xml:id";
		//String  nodePath ="/text";
		//String attr= "id";
		
		this.docId= XMLutils.getAtributeValue(this.xmlDocument, nodePath, attr);
		System.out.println("DI: "+ this.docId);
	}
	/**
	 * 
	 * @param nodeName
	 * @param attr
	 */
	private void getNodesToAdd(String nodeName,String attr,String attrValue){
		
		this.nodeList = XMLutils.getNodesWithAttribute(this.xmlDocument, nodeName, attr, attrValue);
		
		System.out.println("number of segments: "+ this.nodeList.getLength());
	}
	/**
	 * 
	 */
	private void getDocTokens(){
		
		this.tokens = GetFromDatabase.getNoNoteTokenInfoList(this.connection, this.docId);
		System.out.println("Tokens no.:"+this.tokens.size());
	}
	/**
	 * 
	 */
	private void getFirstTagId(){
		
		this.nextTagId= GetFromDatabase.getNextTagId(this.connection, this.docId);		
		
	}
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	private void buildTags(File docFile){
		
		System.out.println("Building tags");
	
		
		this.tags=new LinkedList<TagInfo>();
		Node bodyNode= XMLutils.getNode(this.xmlDocument, "/TEI/text/body");
		//Node bodyNode= XMLutils.getNode(this.xmlDocument, "/text/body");
		System.out.println("Body node "+bodyNode.getNodeName());
		parseNode(XMLutils.getNode(this.xmlDocument, "/TEI/text/body"));
		//parseNode(XMLutils.getNode(this.xmlDocument, "/text/body"));
		
		saveTags();
	}
	
	private void parseNode(Node nodeToParse){
		System.out.println("Parsing node: "+ nodeToParse.getNodeName());
		if(nodeToParse.getNodeName().equals("note")){
			System.out.println("skiping note" + nodeToParse.getTextContent());
		}else if(nodeToParse.getNodeName().equals(this.tagName)&& ((Element)nodeToParse).hasAttribute(this.attr)&& ((Element)nodeToParse).getAttribute(this.attr).equals(this.attrValue)){//if the node is what we are looking for add the tag
			System.out.println("FOUND");
			TagInfo tmpTag= new TagInfo();
			
			tmpTag.setDoc_id(this.docId);
			
			tmpTag.setName(nodeToParse.getNodeName());
			
			tmpTag.setStartOffset(this.startOffset);
			String nodeText = nodeToParse.getTextContent().replaceAll("\\s+", " "); 
			
			
			
			tmpTag.setTextContent(nodeText);
			
			tmpTag.setTag_id(this.nextTagId++);
			
			tmpTag.createAttributeMap(nodeToParse.getAttributes());
			
			tmpTag.setSub_id("add");
			
			
			
			if(nodeToParse.hasChildNodes()){
				NodeList children= nodeToParse.getChildNodes();
				for(int i=0;i< children.getLength();i++){
					Node child=children.item(i);
					parseNode(child);

				}
			}
			
			//add end offset here
			tmpTag.setEndOffset(this.endOffset);
			//System.out.println("adding tag: "+ tmpTag.toString());
			this.tags.add(tmpTag);
			
		}else if(nodeToParse.hasChildNodes()){
			NodeList children= nodeToParse.getChildNodes();
			for(int i=0;i< children.getLength();i++){
				Node child=children.item(i);
				parseNode(child);

			}
		}else if(nodeToParse.getNodeType() == Node.TEXT_NODE){
			
			String nodeText = nodeToParse.getTextContent().replaceAll("\\n+","").replaceAll("\\s+", " ").replaceAll("\\u0342", "\u0303").trim();
			getStartOffset(nodeText);
		}
		
	}
	
	
	
	/**
	 * 
	 * 
	 */
	private void getStartOffset(String text){
		if(!this.tokens.isEmpty()){
			this.startOffset = this.tokens.peekFirst().getStartOffset();
		}
		
		
		if(text.equals("")){
			if(!this.tokens.isEmpty()){
				this.startOffset = this.tokens.peekFirst().getStartOffset();
			}
		}else{
			String textNoSpace = text.replaceAll("\\s+", "");
			System.out.println("textNoSpace: "+ textNoSpace);
			if(this.brokenToken.length()>0){
				System.out.println("Broken token: " +this.brokenToken);
				textNoSpace = textNoSpace.substring(this.brokenToken.length());
				this.brokenToken="";
				
			}
			while(textNoSpace.length() > 0){
				if(!this.tokens.isEmpty()){
				TokenInfo token =  this.tokens.pollFirst();
				this.endOffset = token.getEndOffset();
				if(!this.tokens.isEmpty()){
					this.startOffset = this.tokens.peekFirst().getStartOffset();
				}
				String tokenText = token.getToken().replaceAll("\\s+", "").replaceAll("\\u0342", "\u0303"); // to deal with problem of missing spaces
				System.out.println("token: " + tokenText + " text to match: "+ textNoSpace);
				if(textNoSpace.startsWith(tokenText)){
					System.out.println("starts with");
					textNoSpace = textNoSpace.substring(tokenText.length());
				}else if(textNoSpace.length() < tokenText.length()){
					this.brokenToken = tokenText.substring(textNoSpace.length());
					textNoSpace = "";
				}else{ // in case there has been the corrections in the original file tokens might not match exactly
					System.out.println("doesn't start with");
					if(tokenText.length()>= textNoSpace.length()){
						textNoSpace="";
					}else{
						String secondToken = this.tokens.peekFirst().getToken().replaceAll("\\s+", "").replaceAll("\\u0342", "\u0303"); 
						System.out.println("second token: "+secondToken + ";token: "+tokenText );
						int nextWordIndex = textNoSpace.indexOf(secondToken);
					
						if( textNoSpace.startsWith(secondToken)){
					
							nextWordIndex = textNoSpace.substring(secondToken.length()).indexOf(secondToken);
						
						}
					      if(nextWordIndex == -1){
					    	  textNoSpace="";
					      }else{
					
							textNoSpace = textNoSpace.substring(nextWordIndex);
					      }
					}
				}
				
				
			}
			}
			
		}
	    
	} 
	
	/**
	 * 
	 */
	private void saveTags(){
		for(int i=0;i< this.tags.size();i++){
			System.out.println(this.tags.get(i).toString());
			GetFromDatabase.writeHashToDB(this.tags.get(i).asHashmap(), "tag_body", this.connection);
			
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
 
		try{
			if (args.length < 2)
		        showUsage();
			AddNewTag aNT = new AddNewTag(args[0]);
			
			
			aNT.getNewTags(args[1]);
			
			

		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}


	}
	
	 private static void showUsage() {
		    System.err.println("\nUsage: AddNewTag <properties file path> <file/directory to be treated>\n");
		    System.err.println("Properties file needs to contain following: ");
		    System.err.println("  host   database host");
		    System.err.println("  dbuser  database user");
		    System.err.println("  dbpassword   database password");
		    System.err.println("  tagName      name of the tag to be added (ex. seg)");
		    System.err.println("  attribute    name of the attribute (ex. type)");
		    System.err.println("  attributeValue          value of the attribute (ex. metadiscours)");
		    
		    
		    System.err.println("  docPath          path to the file or directory to be treated");
		    
		    System.exit(0);
		  }

}

