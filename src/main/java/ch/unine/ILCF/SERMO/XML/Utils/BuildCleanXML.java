/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Calendar;
import java.util.Date;

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
public class BuildCleanXML {
	
	private static String bodyPath = "/TEI/text/body";
	private static String frontPath = "/TEI/text/front";
	private static String headerPath = "/TEI/teiHeader";
	
	
	public static Document handleXMLFile(File file){
		try{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		File xmlFile = file;
		System.out.println("Treating file:" + xmlFile.getName());
		//get documents part
		Document domDocument = XMLutils.getDoc(xmlFile);
		Element root = doc.createElement("TEI");
		root.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
		doc.appendChild(root);
		
		Node headerNode = XMLutils.getNode(domDocument, headerPath);
		Node frontNode = XMLutils.getNode(domDocument, frontPath);
		Node bodyNode = XMLutils.getNode(domDocument, bodyPath);
		
		//Node bodyCleaned = 
		       XMLutils.cleanFormatNode2 (bodyNode);
		//Node frontCleaned = 
				XMLutils.cleanFormatNode2(frontNode);
		Calendar rightNow = Calendar.getInstance();
		StringBuilder today = new StringBuilder();
		today.append(rightNow.get(Calendar.YEAR)).append("-").append(String.format("%02d", rightNow.get(Calendar.MONTH)+1)).append("-").append(String.format("%02d", rightNow.get(Calendar.DAY_OF_MONTH)));
		Node headerUpdate =  XMLutils.updateChangeHeader(headerNode,today.toString(), "#LjD", "cleaned XML");
		
		
		
		root.appendChild(doc.importNode(headerUpdate, true));
		Element textE = doc.createElement("text");
		root.appendChild(textE);
		
		textE.appendChild(doc.importNode(frontNode, true));
		textE.appendChild(doc.importNode(bodyNode, true));
		return doc;
		
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static void printXMLtoFile(Document doc, File outDir, String fileName){
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
		
				String outDirName = args[1];
				try{
				File outDir = new File(outDirName);
				if(!outDir.isDirectory()){
					outDir.mkdirs();
				}
				
			

				   
					JFileChooser window= new JFileChooser(args[0]);
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
				
						
					}
					}catch(Exception e){
						System.out.println(e.getMessage());
						//e.getMessage();
					}

			}
}


