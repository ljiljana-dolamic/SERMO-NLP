/**
 * 
 */
package ch.unine.ILCF.SERMO.collection.Utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ch.unine.ILCF.SERMO.TranscriptionHandler;
import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.Utils.CharacterUtils;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;
// /TEI/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date/@type
/**
 * @author dolamicl
 *
 */
public class UpdateMetaData {

	private Connection connection;
	private Properties prop;
	private String id_path ="/TEI/teiHeader/fileDesc";
	private Document domDocument;
	private String [] nodesToUpdate;
	private String [] columnsToUpdate;
	
	public UpdateMetaData(String propertiesFileName) {
		// TODO Auto-generated constructor stub
		
		this.prop = SermoProperties.getProperties(propertiesFileName);
		
		try {
			connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("user"), this.prop.getProperty("password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nodesToUpdate = this.prop.getProperty("nodesToUpdate").split(";");
		this.columnsToUpdate = this.prop.getProperty("columnsToUpdate").split(";");
	}

	
	private void updateData(File f) {
		// TODO Auto-generated method stub
		try {
			this.domDocument = XMLutils.getDoc(f);
			Node fileDesc= XMLutils.getNode(this.domDocument, this.id_path);
			NamedNodeMap attributes = fileDesc.getAttributes();
			String id = attributes.getNamedItem("xml:id").getNodeValue();
			
			for(int i =0; i < nodesToUpdate.length;i++){
				
				Node nodeToUpdate = XMLutils.getNode(this.domDocument, this.nodesToUpdate[i]);
				String nodeValue = CharacterUtils.fixNonStandardCh(nodeToUpdate.getTextContent().replaceAll("\\n", " ").replaceAll("\\s+", " "));
				
				
				
				writeMetaToDb(id,this.columnsToUpdate[i].trim(),nodeValue);
				
			}
			
			
//			Node nodeToUpdate = XMLutils.getNode(this.domDocument, this.prop.getProperty("nodeToUpdate"));
//			String nodeValue = CharacterUtils.fixNonStandardCh(nodeToUpdate.getTextContent().replaceAll("\\n", " ").replaceAll("\\s+", " "));
//			
//			
//			
//			writeMetaToDb(id,this.prop.getProperty("columnToUpdate"),nodeValue);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(f.getName());
			e.printStackTrace();
		}
	}
	
	private void writeMetaToDb(String id, String where,String what){
		StringBuilder sql = new StringBuilder("UPDATE ").append(this.prop.getProperty("tableToUpdate")).append(" set "); 
		sql.append(where).append(" = \"").append(what).append("\" WHERE ").append(this.prop.getProperty("tableKey")).append(" = '").append(id).append("'");
		
		 try {
			PreparedStatement preparedStatement =  connection.prepareStatement(sql.toString());
			
			int affectedRows = preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
			System.out.println(sql.toString());
			e.printStackTrace();
		}
		 
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		UpdateMetaData uMD = new UpdateMetaData("db.properties");
		JFileChooser window= new JFileChooser("M:\\sermo_xml");
		window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int rv= window.showOpenDialog(null);
		
		if(rv == JFileChooser.APPROVE_OPTION){
			///resultXML.buildTranscriptionXML(window.getSelectedFile());  
			//String fileName=window.getSelectedFile().getName();
		  //  String [] fileNameParts= fileName.split("_");
			File window_file = window.getSelectedFile();
			if(window_file.isDirectory()){
				File[] files = window_file.listFiles();
				for(File f:files){
					uMD.updateData(f);
				}
			}else{
				uMD.updateData(window.getSelectedFile());
			}
	//	JFileChooser window= new JFileChooser();
	//	int rv= window.showOpenDialog(null);

		//if(rv == JFileChooser.APPROVE_OPTION){
			
	//		tH.handleTranscriptionFile(window.getSelectedFile());
			
		}

	}

	

}
