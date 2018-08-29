/**
 * 
 */
package ch.unine.ILCF.SERMO.Utils;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Attr;

import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.XML.Utils.BuildCleanXML;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

import java.util.Properties;

/**
 * @author dolamicl
 *
 */
public class CreateTestSet {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
			Properties prop = SermoProperties.getProperties("db.properties");
			GetFromDatabase databaseReader = new GetFromDatabase("db.properties");
			
			Connection connection = MySQLconnection.createConnection(databaseReader.getHost(),databaseReader.getDatabase(),databaseReader.getUser() , databaseReader.getPassword());
			
			LinkedList<String> docs;
			if(prop.containsKey("doc_id")){
				docs=new LinkedList<String>();
				docs.add(prop.getProperty("doc_id"));
				
			  
			}else{
				
				docs= databaseReader.getDocsId(connection);
				
				System.out.println("Got docs");
			}
			HashMap<String,String> paragraphsText = new HashMap<String,String>();
			
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument(); 
			Element root=doc.createElement("root");
			doc.appendChild(root);
			
			
			for(String doc_id :docs){
				System.out.println(doc_id);
			
				LinkedList<Integer> parIdList = databaseReader.getParagraphs(doc_id, connection); 
				if(parIdList.size() == 0){continue;}
				int middle= databaseReader.getParagraphs(doc_id, connection).size(); 
				middle=(int)(middle/4)*3;
				int middleId = parIdList.get(middle);
				Element par=doc.createElement( "par");
				par.setAttribute("doc_id", doc_id);
				par.setAttribute("par_no", Integer.toString(middleId));
				root.appendChild(par);
				
				
				if(prop.getProperty("sent").equals("false")){
					par.appendChild(doc.createTextNode(databaseReader.getParagraphText(doc_id, middleId, connection)));
				}else{
					//LinkedList<HashMap<String,String>> sentences = databaseReader.getSentencesInfo(doc_id, middleId, connection);
					LinkedList<HashMap<String,String>> sentences = databaseReader.getSentencesInfoFull(doc_id, middleId, connection);
					System.out.println("NoSent: "+ sentences.size());
					for(HashMap<String,String> tmpS: sentences){
						//System.out.println(tmpS);
						Element sent= doc.createElement( "s");
						sent.setAttribute("start", tmpS.get("start"));
						sent.setAttribute("end", tmpS.get("end"));
						sent.setAttribute("n", tmpS.get("sent_no"));
						sent.setAttribute("italic", tmpS.get("italic"));
						par.appendChild(sent);
						sent.appendChild(doc.createTextNode(tmpS.get("text")));
					}
				}
				
				//String par =  databaseReader.getParagraphText(doc_id, 2, connection);
				//paragraphs.put(doc_id, par);
			}
			
			File outDir = new File(prop.getProperty("dir"));
			if(!outDir.isDirectory()){
				outDir.mkdirs();
			}
			BuildCleanXML.printXMLtoFile(doc, outDir, prop.getProperty("file"));
			//for()
			
		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}
	}

}
