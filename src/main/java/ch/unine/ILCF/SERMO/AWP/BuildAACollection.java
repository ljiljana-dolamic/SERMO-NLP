/**
 * 
 */
package ch.unine.ILCF.SERMO.AWP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

/**
 * @author dolamicl
 *
 */
public class BuildAACollection {
	
	private String input_file;
	private String output_dir;
    private Properties prop;
    private Document in_xml;
	
	public BuildAACollection(String propFileName)throws Exception{
		this.prop = SermoProperties.getProperties(propFileName);
		this.input_file = this.prop.getProperty("collection.in");
		this.output_dir = this.prop.getProperty("collection.out");
		// read fuul collection
		System.out.println(input_file);
		this.in_xml = XMLutils.getDoc(new File(this.input_file));
		
		 System.out.println("id,what,dest,date");
		//read tmp file
		
		
	
	
		NodeList listOfLetters = XMLutils.getNodeList(this.in_xml, "/body/text/div");
		
		for(int i=0; i < listOfLetters.getLength();i++){
			
			Element n = (Element)listOfLetters.item(i); 
			String  doc_id= n.getAttribute("what").replaceAll(" ","");
		   
		    
		    System.out.println(doc_id+",\""+n.getAttribute("what")+"\",\""+n.getAttribute("dest")+"\","+n.getAttribute("date"));
		    
		    
		    Document finalDoc = buildCollectionDocument(n);
			writeCollectionDoc(this.output_dir,doc_id,finalDoc);
		    }
		
		
		//System.out.println(listOfLetters.getLength());
		
		//System.out.println(this.teiHeaderTMP.toString());
		
	}
	
	public Document buildCollectionDocument( Element content)throws Exception{
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			BuildAACollection resultXML=new  BuildAACollection("aa.properties");

			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Couldn't build XML");
		}

	}

}
