/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ch.unine.ILCF.SERMO.TranscriptionHandler;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;

import javax.swing.JFileChooser;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author dolamicl
 *
 */


public class BuildTXMxml {
	
	
	private static String sermoPath="/TEI/text/body";
	
	
    
	public static Document handleXMLFile(File file){
		try{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		File xmlFile = file;
		System.out.println("Treating file:" + xmlFile.getName());
		//get documents part
		Document domDocument = XMLutils.getDoc(xmlFile);
		
		Node bodyNode = XMLutils.getNode(domDocument, sermoPath);
		
		Node bodyCleaned =  XMLutils.removeMargineReclame(bodyNode);
		
		Node body = doc.importNode(bodyCleaned, true);
		doc.appendChild(body);
		return doc;
		
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	public static void printXMLtoFile(Document doc, File outDir,String fileName){
		try{
			OutputStream outStream = new FileOutputStream (new File(outDir,fileName));	
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
		
		String outDirName = args[0];
		try{
		File outDir = new File(outDirName);
		if(!outDir.isDirectory()){
			outDir.mkdirs();
		}
		
	

		   
			JFileChooser window= new JFileChooser();
			window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int rv= window.showOpenDialog(null);
			
			if(rv == JFileChooser.APPROVE_OPTION){
				
				File window_file = window.getSelectedFile();
				if(window_file.isDirectory()){
					File[] files = window_file.listFiles();
					for(File f:files){
						Document doc= handleXMLFile(f);
						printXMLtoFile(doc,outDir,f.getName());
					}
				}else{
					File f= window.getSelectedFile();
					Document doc= handleXMLFile(f);
					printXMLtoFile(doc,outDir,f.getName());
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
