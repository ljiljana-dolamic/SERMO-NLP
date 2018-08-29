/**
 * 
 */
package ch.unine.ILCF.SERMO.SSplit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.XML.Utils.BuildCleanXML;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import edu.stanford.nlp.io.IOUtils;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

/**
 * @author dolamicl
 *
 */
public class OpenNlpSSplit {

	private SentenceModel sentenceModel;
	private SentenceDetectorME sentenceDetector;
	private LinkedList<SentenceInfo> sentences;

	public OpenNlpSSplit(String sentenceModelpath) throws FileNotFoundException {
		super();


		InputStream modelIn = new FileInputStream(sentenceModelpath);

		try {
			this.sentenceModel = new SentenceModel(modelIn);
			this.sentenceDetector = new SentenceDetectorME(this.sentenceModel);
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
		
	}
	public LinkedList<SentenceInfo> getSentences (String textToAnnotate){
		sentences = new LinkedList<SentenceInfo>();
			
	String sent[] = this.sentenceDetector.sentDetect(textToAnnotate);
					
	Span spans[] = this.sentenceDetector.sentPosDetect(textToAnnotate);
		
		for(int i = 0; i<spans.length;i++){
				
			SentenceInfo tmp = new SentenceInfo();
			tmp.setSentence(sent[i]);
			tmp.setSentenceId(i);
			tmp.setStartOffset(spans[i].getStart());
			tmp.setEndOffset(spans[i].getEnd());
			sentences.add(tmp);
		}
		
		return sentences;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		LinkedList<SentenceInfo> result;
//		try{
//			OpenNlpSSplit sentenceSplit=new OpenNlpSSplit("src/main/resources/fr-sent.bin");
//
//				JFileChooser window= new JFileChooser();
//				int rv= window.showOpenDialog(null);
//                
//				if(rv == JFileChooser.APPROVE_OPTION){
//					result=sentenceSplit.getSentences(IOUtils.slurpFileNoExceptions(window.getSelectedFile()));
//					
//					for(SentenceInfo s:result){
//						System.out.println(s.toString());
//					}
//					
//				}
//				
//				
//			}catch(Exception e){
//				System.out.println("Couldn't build XML");
//				
//			}

		// TODO Auto-generated method stub
				try{
				    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element root=doc.createElement("root");
					doc.appendChild(root);
			
					OpenNlpSSplit sentenceSplit=new OpenNlpSSplit("src/main/resources/fr-sent.bin");

					JFileChooser window= new JFileChooser();
					int rv= window.showOpenDialog(null);
					if(rv == JFileChooser.APPROVE_OPTION){
					//get documents part
					Document domDocument = XMLutils.getDoc(window.getSelectedFile()); 
					 
					NodeList parList=  XMLutils.getNode(domDocument, "/root").getChildNodes();
					
					for(int i=0; i<parList.getLength();i++){
						
						Node parNode=parList.item(i);
						if(parNode.getNodeName().equals("par")){
						String paragraf = parNode.getTextContent(); 
						
						NamedNodeMap attributes = parNode.getAttributes();
						Element par=doc.createElement("par");
					    for(int j =0;j < attributes.getLength();j++ ){
					    	Node tmp = attributes.item(j);
					    	par.setAttribute(tmp.getNodeName(), tmp.getNodeValue());
					    	
					    } 
						root.appendChild(par);
						
						
						LinkedList<SentenceInfo> result;
					
						result=sentenceSplit.getSentences(paragraf);
						int n=1;
						for(SentenceInfo s:result){
							Element sent=doc.createElement("s");
							sent.setAttribute("n", Integer.toString(n));
							sent.setAttribute("start",Integer.toString( s.getStartOffset()));
							sent.setAttribute("end",Integer.toString( s.getEndOffset()));
							sent.appendChild(doc.createTextNode(s.getSentence()));
							par.appendChild(sent);
							n++;
						}
						
					}
					}
					}
					
					File outDir = new File("D:\\ljiljana.dolamic\\SSplit");
					BuildCleanXML.printXMLtoFile(doc, outDir, "SsplitOnlp.xml");
				}catch(Exception e){
					e.printStackTrace();
					//System.out.println();
//					
				}


	}

}
