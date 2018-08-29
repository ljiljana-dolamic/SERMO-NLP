/**
 * 
 */
package ch.unine.ILCF.SERMO.collection.Utils;

/**
 * @author dolamicl
 *
 */
/**
 * 
 */


import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class InsertMetaData {

	private Connection connection;
	private Properties prop;
	private String id_path ="/TEI/teiHeader/fileDesc";
	private Document domDocument;
	private String [] nodesToInsertFrom;
	private String  textColumnsToInsert;
	private String [] attsColumnsToInsert;
	private String [] attsToInsert;
	
	public InsertMetaData(String propertiesFileName) {
		// TODO Auto-generated constructor stub
		
		this.prop = SermoProperties.getProperties(propertiesFileName);
		
		try {
			connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("user"), this.prop.getProperty("password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.nodesToInsertFrom = this.prop.getProperty("nodesToInsertFrom").split(";");
		this.textColumnsToInsert = this.prop.getProperty("textColumnsToInsert"); // in what column to insert text
		this.attsColumnsToInsert = this.prop.containsKey("attsColumnsToInsert") ? this.prop.getProperty("attsColumnsToInsert").split(";"):null;
		this.attsToInsert = this.prop.containsKey("attsColumnsToInsert") ? this.prop.getProperty("attsToInsert").split(";") : null;
	}

	
	private void insertData(File f) {
		// TODO Auto-generated method stub
		try {
			this.domDocument = XMLutils.getDoc(f);
			Node fileDesc= XMLutils.getNode(this.domDocument, this.id_path);
			NamedNodeMap attributes = fileDesc.getAttributes();
			String id = attributes.getNamedItem("xml:id").getNodeValue();
			
			for(int i =0; i < nodesToInsertFrom.length;i++){
				
				NodeList nodesToinsert = XMLutils.getNodeList(this.domDocument, this.nodesToInsertFrom[i]);
				for (int j=0;j< nodesToinsert.getLength();j++){
					StringBuilder insertTo = new StringBuilder();
					//StringBuilder insertWhat = new StringBuilder();
					LinkedList<String>  insertWhat = new LinkedList<String>();
					insertTo.append("doc_id");// text content
					//insertWhat.append("'").append(id).append("'");
					insertWhat.add(id);
					insertTo.append(',').append(this.textColumnsToInsert);// text content
					//insertWhat.add(this.textColumnsToInsert);
					//insertWhat.append(',').append("'").append(CharacterUtils.fixNonStandardCh(nodesToinsert.item(j).getTextContent().replaceAll("\\n", " ").replaceAll("\\s+", " "))).append("'");
					insertWhat.add(CharacterUtils.fixNonStandardCh(nodesToinsert.item(j).getTextContent().replaceAll("\\n", " ").replaceAll("\\s+", " ")));
					if(this.attsColumnsToInsert != null){
						for (int k=0;k< this.attsColumnsToInsert.length;k++){
							insertTo.append(',').append(this.attsColumnsToInsert[k]);// text content
					//		//insertWhat.append(',').append("'").append(nodesToinsert.item(j).getAttributes().getNamedItem(this.attsToInsert[k]).getNodeValue()).append("'");
							insertWhat.add(nodesToinsert.item(j).getAttributes().getNamedItem(this.attsToInsert[k]).getNodeValue());
							
						}
					}
				
				    writeMetaToDb(id,insertTo.toString(),insertWhat);
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(f.getName());
			e.printStackTrace();
		}
	}
	
	private void writeMetaToDb(String doc_id, String where,LinkedList what){
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(this.prop.getProperty("tableToUpdate")).append("("); 
		sql.append(where);
		sql.append(")values(");
		for(int i =0;i<what.size();i++){
			if(i!=0){
				sql.append(',');
			}
			sql.append('?');
		}
		sql.append(")");
		
		 try {
			PreparedStatement preparedStatement =  connection.prepareStatement(sql.toString());
			for(int i =0;i<what.size();i++){
				preparedStatement.setString(i+1, what.get(i).toString());
			}
			
			
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
		
		InsertMetaData uMD = new InsertMetaData("imd.properties");
		JFileChooser window= new JFileChooser("M:\\sermo_xml");
		window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int rv= window.showOpenDialog(null);
		
		if(rv == JFileChooser.APPROVE_OPTION){
			File window_file = window.getSelectedFile();
			if(window_file.isDirectory()){
				File[] files = window_file.listFiles();
				for(File f:files){
					uMD.insertData(f);
				}
			}else{
				uMD.insertData(window.getSelectedFile());
			}
			
		}

	}

	

}
