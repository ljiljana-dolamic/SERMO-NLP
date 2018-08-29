/**
 * 
 */
package ch.unine.ILCF.SERMO.TT;

import java.io.File;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;

/**
 * @author dolamicl
 *
 */
public class EvaluateTT {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{

			EvaluateTT eTT = new EvaluateTT();
		// TODO Auto-generated method stub
		JFileChooser testWindow= new JFileChooser("D:\\ljiljana.dolamic\\test_data\\TT\\test_results\\outputs");
		testWindow.setDialogTitle("File to test");
		JFileChooser correctWindow= new JFileChooser("D:\\ljiljana.dolamic\\test_data\\TT\\test_results\\gold");
		correctWindow.setDialogTitle("Correct file");
		
		File fileToTest=null;
		File correctPOS=null;
		int tv= testWindow.showOpenDialog(null);
		if(tv == JFileChooser.APPROVE_OPTION){
			
			fileToTest = testWindow.getSelectedFile();
			
				
			}
		int ew= correctWindow.showOpenDialog(null);
		if(ew == JFileChooser.APPROVE_OPTION){
			
			correctPOS = correctWindow.getSelectedFile();
		}

		if(fileToTest != null && correctPOS != null ){
			
			eTT.evaluate(fileToTest,correctPOS);
		}
		
	}catch(Exception e){
		System.out.println(e.getMessage());
		//e.getMessage();
	}

	}

	private void evaluate(File fileToTest, File correctPOS) {
		
		System.out.println ("Testing file: " + fileToTest.getPath());
		System.out.println ("Correct file: " + correctPOS.getPath());
		System.out.println("année\tw\tPOS\tLEMMA");
		String an= fileToTest.getName().substring(0, fileToTest.getName().indexOf(".") );
		int total=0;
		int incorrectPOS=0;
		int incorrectLEMMA=0;
		int incorrectPOSLEMMA=0;
		
		LinkedList<String> notKnown = new LinkedList<String>();
		
		  try {
			Document testDoc = XMLutils.getDoc(fileToTest);
			Document correctDoc = XMLutils.getDoc(correctPOS);
			
			XMLutils.removeEmptyTextNodex(testDoc);
			XMLutils.removeEmptyTextNodex(correctDoc);
			
			NodeList divToTest = XMLutils.getNode(testDoc, "/test").getChildNodes();
			NodeList divCorrect = XMLutils.getNode(correctDoc, "/test").getChildNodes();
			
			if(divToTest.getLength() != divCorrect.getLength()){
				throw new Exception("***length missmatch***");
			}
			
			for(int i=0;i< divToTest.getLength();i++){
				Node divTT = divToTest.item(i);
				Node divC = divCorrect.item(i);
				
				
				if(!divTT.getAttributes().getNamedItem("n").getNodeValue().equals(divC.getAttributes().getNamedItem("n").getNodeValue())){
					
					throw new Exception("***not same div *** "+divTT.getAttributes().getNamedItem("n").getNodeValue()+" vs. "+divC.getAttributes().getNamedItem("n").getNodeValue());
				}
				
				NodeList wordsTT = divTT.getChildNodes();
				NodeList wordsC = divC.getChildNodes();
				
				for(int j=0,k=0;j< wordsTT.getLength();j++,k++){
					
					Node wordTT = wordsTT.item(j);
					Node wordC = wordsC.item(k);
					if(wordC.getNodeName().equals("#comment")){
						wordC = wordsC.item(k+1);
						k++;
					}
					
					if(!wordTT.getAttributes().getNamedItem("token").getNodeValue().equals(wordC.getAttributes().getNamedItem("token").getNodeValue())){
						
						throw new Exception("***not same words *** "+wordTT.getAttributes().getNamedItem("token").getNodeValue()+" vs. "+ wordC.getAttributes().getNamedItem("token").getNodeValue());
					}
					total ++;
					
					
					
					if(wordTT.getAttributes().getNamedItem("token").getNodeValue().equals(wordTT.getAttributes().getNamedItem("lemma").getNodeValue())&&
							!wordTT.getAttributes().getNamedItem("pos").getNodeValue().matches("F[sw]")){
						
						//System.out.println(wordTT.getTextContent());
						//System.out.println("Unknown: "+wordTT.getAttributes().getNamedItem("token").getNodeValue());
						
						notKnown.add(wordTT.getAttributes().getNamedItem("token").getNodeValue());
					}
					
					
					if(!wordTT.getAttributes().getNamedItem("pos").getNodeValue().equals(wordC.getAttributes().getNamedItem("pos").getNodeValue())
							&&
							!wordTT.getAttributes().getNamedItem("lemma").getNodeValue().equals(wordC.getAttributes().getNamedItem("lemma").getNodeValue())){
						System.out.print(an);
						System.out.print("\t");
						System.out.print(wordTT.getTextContent());
						System.out.print("\t");
						System.out.print(wordTT.getAttributes().getNamedItem("pos").getNodeValue()+" : "+wordC.getAttributes().getNamedItem("pos").getNodeValue());
						System.out.print("\t");
						System.out.println(wordTT.getAttributes().getNamedItem("lemma").getNodeValue()+" : "+wordC.getAttributes().getNamedItem("lemma").getNodeValue());
						incorrectPOSLEMMA++;
						
					}else if (!wordTT.getAttributes().getNamedItem("pos").getNodeValue().equals(wordC.getAttributes().getNamedItem("pos").getNodeValue())){
						System.out.print(an);
						System.out.print("\t");
						System.out.print(wordTT.getTextContent());
						System.out.print("\t");
						System.out.print(wordTT.getAttributes().getNamedItem("pos").getNodeValue()+" : "+wordC.getAttributes().getNamedItem("pos").getNodeValue());
						System.out.print("\t");
						System.out.println(wordTT.getAttributes().getNamedItem("lemma").getNodeValue());
						incorrectPOS++;
						
					}else if(!wordTT.getAttributes().getNamedItem("lemma").getNodeValue().equals(wordC.getAttributes().getNamedItem("lemma").getNodeValue())){
						
						System.out.print(an);
						System.out.print("\t");
						System.out.print(wordTT.getTextContent());
						System.out.print("\t");
						System.out.print(wordTT.getAttributes().getNamedItem("pos").getNodeValue());
						System.out.print("\t");
						System.out.println(wordTT.getAttributes().getNamedItem("lemma").getNodeValue()+" : "+wordC.getAttributes().getNamedItem("lemma").getNodeValue());
						incorrectLEMMA++;
						
					}
					
				}
				
				
			}
			double prec = ( incorrectPOS + incorrectPOSLEMMA)!=0 ? ((total-incorrectPOS - incorrectPOSLEMMA)/(double)total)*100 : 100;
			System.out.println("P: "+ prec + "%");
			
			System.out.println("total\tiPOS\tiLEMMA\tiPOSLEMMA");
			System.out.println(total+"\t"+incorrectPOS+"\t"+incorrectLEMMA+"\t"+incorrectPOSLEMMA+"\n\n");
			
			//System.out.println("Unknown: "+ notKnown);
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}

}
