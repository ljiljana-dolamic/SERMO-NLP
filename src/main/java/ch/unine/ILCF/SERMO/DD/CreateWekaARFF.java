/**
 * 
 */
package ch.unine.ILCF.SERMO.DD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.LGeRM.OutputLine;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;



/**
 * @author dolamicl
 *
 */
public class CreateWekaARFF {
	
	
//	private File wordCountInput;
//	private File ddManuelInput;
	private Document domWordCount;
	private Document domDDManuel;
	private Document domDDTest;
	private HashMap<String,String> sentenceClass = new HashMap<String,String>();
	private HashMap<String,String> sentenceClassTest = new HashMap<String,String>();
	
	LinkedList<String> attributes = new LinkedList<String>(); 
	HashMap<String,HashMap<String,Integer>> attributesPerSentence = new HashMap<String,HashMap<String,Integer>> ();
	HashMap<String,HashMap<String,Integer>> attributesPerSentenceTest = new HashMap<String,HashMap<String,Integer>> ();

	public CreateWekaARFF  (File wCI, File ddM)throws Exception{
		
		this.domWordCount = XMLutils.getDoc(wCI); 
		this.domDDManuel =  XMLutils.getDoc(ddM);
	}
	
  public CreateWekaARFF  (File wCI, File ddM,File ddT)throws Exception{
		
	    this(wCI,ddM);
		this.domDDTest = XMLutils.getDoc(ddT);
	}
	
	public void buildSentenceClass(){
		
		NodeList sentences = XMLutils.getNodeList(this.domDDManuel, "/root/par/s");
		for (int i=0;i< sentences.getLength();i++){
			Node thisS= sentences.item(i);
			
			if(thisS.getAttributes().getNamedItem("DD")!=null){
				Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
				String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
				String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
				String sent_id = thisS.getAttributes().getNamedItem("n").getNodeValue();
				String ddClass = thisS.getAttributes().getNamedItem("DD").getNodeValue().equals("N") ? "noDD": "DD";
				this.sentenceClass.put(doc_id+"_"+par_id+"_"+sent_id, ddClass);
				//System.out.println(doc_id+" ; "+par_id+" ; "+sent_id);
			}
			
		}
		//if we have a dom for the test file build it's sentences test
		if(this.domDDTest != null){
			NodeList testSent = XMLutils.getNodeList(this.domDDTest, "/root/par/s");
			for (int i=0;i< testSent.getLength();i++){
				Node thisTS= testSent.item(i);
				
				if(thisTS.getAttributes().getNamedItem("DD")!=null){
					Node thisTSpar= thisTS.getParentNode();  // paragraph contains doc_id and par_no as attributes
					String doc_id = thisTSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
					String par_id = thisTSpar.getAttributes().getNamedItem("par_no").getNodeValue();
					String sent_id = thisTS.getAttributes().getNamedItem("n").getNodeValue();
					String ddClass = thisTS.getAttributes().getNamedItem("DD").getNodeValue().equals("N") ? "noDD": "DD";
					this.sentenceClassTest.put(doc_id+"_"+par_id+"_"+sent_id, ddClass);
					//System.out.println(doc_id+" ; "+par_id+" ; "+sent_id);
				}
				
			}
		}
		
	}
	
	public void buildAttributesList(){
		NodeList sentences = XMLutils.getNodeList(this.domWordCount, "/root/par/s");
		this.attributes.add("length");
		this.attributes.add("italic");
		this.attributes.add("maj_mid");
		//this.attributes.add("has_ins");
		for (int i=0;i< sentences.getLength();i++){
			Node thisS= sentences.item(i);
			Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
			String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
			String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
			String sent_id = doc_id + "_" + par_id + "_" + thisS.getAttributes().getNamedItem("n").getNodeValue();
			
			if(sentenceClass.containsKey(sent_id)){
				//System.out.println(sent_id);
				attributesPerSentence.put(sent_id, getAttributes(thisS));
				
			}
		}
		
		if(this.domDDTest != null){
			for (int i=0;i< sentences.getLength();i++){
				Node thisS= sentences.item(i);
				Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
				String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
				String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
				String sent_id = doc_id + "_" + par_id + "_" + thisS.getAttributes().getNamedItem("n").getNodeValue();
				
				if(sentenceClassTest.containsKey(sent_id)){
					//System.out.println(sent_id);
					attributesPerSentenceTest.put(sent_id, getAttributesTest(thisS));
					
				}
			}
		}
		}
	
	
	// check whether the sentence contains the insize of the type ",dit-il,"
	private int hasIncise(String [] words){
		int has_it=0;
		
		
		return has_it;
		
	}
	
	HashMap<String,Integer> getAttributes(Node sent){
		HashMap<String,Integer> sentAttr = new HashMap<String,Integer>();
		boolean mid_maj=false;
		
		if(sent.getAttributes().getNamedItem("italic")!=null){
			
			 sentAttr.put("italic", Integer.parseInt(sent.getAttributes().getNamedItem("italic").getNodeValue()) );
		}
		String[] words = sent.getTextContent().trim().split("\\n");
		  sentAttr.put("length", words.length);
		  //sentAttr.put("has_ins", hasIncise(words));
		 for(int i = 0;i<words.length;i++){
			 String[] parts = words[i].split("\\t");
			 
			 
			 
//			 String token = "token_"+parts[0].trim();
//			 if(!this.attributes.contains(token)){
//				 this.attributes.add(token);
//			 }
//			 if(sentAttr.containsKey(token)){
//					sentAttr.put(token, sentAttr.get(token)+1);
//					
//				}else{
//					sentAttr.put(token, 1);
//				}
			 
			 String ft = "final_"+parts[1].trim();
			 if(!this.attributes.contains(ft)){
				 this.attributes.add(ft);
//				 this.attributes.add(ft+"_p");
			 }
			 if(sentAttr.containsKey(ft)){
					sentAttr.put(ft, sentAttr.get(ft)+1);
					
				}else{
//					sentAttr.put(ft+"_p", 1);
					sentAttr.put(ft, 1);
				}
			 
			 
			 String pos = "pos_"+parts[2].trim();
			 if(!this.attributes.contains(pos)){
				 this.attributes.add(pos);
//				 this.attributes.add(pos+"_p");
			 }
			 if(sentAttr.containsKey(pos)){
					sentAttr.put(pos, sentAttr.get(pos)+1);
					
				}else{
//					sentAttr.put(pos+"_p", 1);
					sentAttr.put(pos, 1);
				}
			 // mid upper case counts only if it is not Np 
			 if(i !=0 && Character.isUpperCase(parts[0].trim().charAt(0)) && !pos.equals("pos_Np") && !pos.equals("pos_Nc")){
				 
				 //System.out.println("Warrning: "+ pos);
				 mid_maj=true;
			 }
			 
			 String lemma = "lemma_"+parts[3].trim();
			 if(!this.attributes.contains(lemma)){
				 this.attributes.add(lemma);
//				 this.attributes.add(lemma+"_p");
			 }
			 if(sentAttr.containsKey(lemma)){
					sentAttr.put(lemma, sentAttr.get(lemma)+1);
					
				}else{
//					sentAttr.put(lemma+"_p", 1);
					sentAttr.put(lemma, 1);
				}
			 
			 if(mid_maj){
					sentAttr.put("maj_mid", 1);
					
				}else{
					sentAttr.put("maj_mid", 0);;
				}
			 
		 }
		
		
		return sentAttr;
		
	}
	
	HashMap<String,Integer> getAttributesTest(Node sent){
		HashMap<String,Integer> sentAttr = new HashMap<String,Integer>();
		boolean mid_maj=false;
		
		if(sent.getAttributes().getNamedItem("italic")!=null){
			
			 sentAttr.put("italic", Integer.parseInt(sent.getAttributes().getNamedItem("italic").getNodeValue()) );
		}
		String[] words = sent.getTextContent().trim().split("\\n");
		  sentAttr.put("length", words.length);
		  sentAttr.put("has_ins", hasIncise(words));
		 for(int i = 0;i<words.length;i++){
			 String[] parts = words[i].split("\\t");
			 
			
			 
//			 String token = "token_"+parts[0].trim();
//			 if(!this.attributes.contains(token)){
//				 this.attributes.add(token);
//			 }
//			 if(sentAttr.containsKey(token)){
//					sentAttr.put(token, sentAttr.get(token)+1);
//					
//				}else{
//					sentAttr.put(token, 1);
//				}
			 
			 String ft = "final_"+parts[1].trim();
			
			 if(sentAttr.containsKey(ft)){
					sentAttr.put(ft, sentAttr.get(ft)+1);
					
				}else{
//					sentAttr.put(ft+"_p", 1);
					sentAttr.put(ft, 1);
				}
			 
			 
			 String pos = "pos_"+parts[2].trim();
			
			 if(sentAttr.containsKey(pos)){
					sentAttr.put(pos, sentAttr.get(pos)+1);
					
				}else{
//					sentAttr.put(pos+"_p", 1);
					sentAttr.put(pos, 1);
				}
			 
			 if(i !=0 && Character.isUpperCase(parts[0].trim().charAt(0)) && !pos.equals("pos_Np") && !pos.equals("pos_Nc")){
				 
				// System.out.println("Warrning: "+ pos);
				 mid_maj=true;
			 }
			 
			 String lemma = "lemma_"+parts[3].trim();
			 
			 if(sentAttr.containsKey(lemma)){
					sentAttr.put(lemma, sentAttr.get(lemma)+1);
					
				}else{
//					sentAttr.put(lemma+"_p", 1);
					sentAttr.put(lemma, 1);
				}
			 
			 if(mid_maj){
					sentAttr.put("maj_mid", 1);
					
				}else{
					sentAttr.put("maj_mid", 0);;
				}
			 
		 }
		
		
		return sentAttr;
		
	}
	

	
	
	public void buildARFF(){
		buildSentenceClass();
		buildAttributesList();
		writeARFF();
		
	}
	
	public void writeARFF(){
		StringBuilder sbTest = new StringBuilder();
		//header
		System.out.println("@relation SERMO-DD");
		sbTest.append("@relation SERMO-DD").append("\n");
		for(String s :attributes){
			//System.out.println("@attribute '"+s.replaceAll("\\s","_")+"' numeric");
			if(s.matches(".*_p")){
		  
			System.out.println("@attribute '"+s.replaceAll("'","£")+"' {0,1}");
			sbTest.append("@attribute '"+s.replaceAll("'","£")+"' {0,1}").append("\n");
			}
			else{
			
			  System.out.println("@attribute '"+s.replaceAll("'","£")+"' numeric");
			  sbTest.append("@attribute '"+s.replaceAll("'","£")+"' numeric").append("\n");
			}
		}
		System.out.println("@attribute class {noDD,DD}");	
		 sbTest.append("@attribute class {noDD,DD}").append("\n");
		
		System.out.println("@data");
		 sbTest.append("@data").append("\n");
		//training
		Set<String> sentences= sentenceClass.keySet();
		
		for(String sent_id: sentences){
			
			HashMap<String,Integer> sentAttr= this.attributesPerSentence.get(sent_id);
			StringBuilder line= new StringBuilder();
			//line.append(sent_id);
			for(String s :attributes){
				if(line.length()!=0){
					line.append(",");
				}
				if(sentAttr.containsKey(s)){
					line.append(sentAttr.get(s));
				}else{
					line.append(0);
				}
				
			}
			line.append(",").append(this.sentenceClass.get(sent_id));
			  //System.out.println(sent_id);
		   System.out.println(line);	
		}
		//test
		Set<String> sentencesTest= sentenceClassTest.keySet();
		StringBuilder testIDsb= new StringBuilder(); 
		for(String sent_id: sentencesTest){
			testIDsb.append(sent_id).append("\n");
			
			HashMap<String,Integer> sentAttrTest= this.attributesPerSentenceTest.get(sent_id);
			StringBuilder lineTest= new StringBuilder();
			//lineTest.append(sent_id);
			for(String s :attributes){
				if(lineTest.length()!=0){
					lineTest.append(",");
				}
				if(sentAttrTest.containsKey(s)){
					lineTest.append(sentAttrTest.get(s));
				}else{
					lineTest.append(0);
				}
				
			}
			lineTest.append(",").append(this.sentenceClassTest.get(sent_id)).append("\n");
			  //System.out.println(sent_id);
		  sbTest.append(lineTest);	
		}
		
		writeTestARFF(sbTest.toString(),testIDsb.toString());
	}
	
	public void writeTestARFF(String s ,String ids){
		try{
			File fout = new File("D:\\DD_test\\testDD_19.01.arff");

			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			
				bw.write(s);
				bw.newLine();
			

			bw.close();
			
			File idsOout = new File("D:\\DD_test\\testIds.txt");

			fos = new FileOutputStream(idsOout);

			 bw = new BufferedWriter(new OutputStreamWriter(fos));

			
				bw.write(ids);
				bw.newLine();
			

			bw.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{

			JFileChooser window= new JFileChooser("D:\\DD_test");
			int rv= window.showOpenDialog(null);
			File infoFile = window.getSelectedFile();
			
			JFileChooser wM= new JFileChooser("D:\\DD_test");
			int rM= wM.showOpenDialog(null);
			File manual = wM.getSelectedFile();
			
			JFileChooser wT= new JFileChooser("D:\\DD_test");
			int rT= wT.showOpenDialog(null);
			File test = wT.getSelectedFile();
			CreateWekaARFF cWArff;
			if(test!=null){
				 cWArff = new CreateWekaARFF(infoFile,manual,test);
			}else{
			 cWArff = new CreateWekaARFF(infoFile,manual);
			}
			cWArff.buildARFF();
		
		}catch(Exception e){
			System.out.println("Error:"+e.getMessage());
			e.printStackTrace();
			//e.getMessage();
		}
	}

}
