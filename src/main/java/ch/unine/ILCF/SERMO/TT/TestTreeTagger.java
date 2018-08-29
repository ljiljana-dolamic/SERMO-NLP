/**
 * 
 */
package ch.unine.ILCF.SERMO.TT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;


import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;
import ch.unine.ILCF.SERMO.Utils.CharacterUtils;
import ch.unine.ILCF.SERMO.propreties.SermoProperties;

/**
 * @author dolamicl
 *
 */
public class TestTreeTagger {

	
	 private Tokenizer tokenizer;
	    private TreeTaggerHandler ttH; 
	    
	    private Properties prop;
	   // private static List<String> dictionary = new ArrayList<String>();
	    
	    public TestTreeTagger(String propertiesFileName) throws Exception{
	    	this.prop = SermoProperties.getProperties(propertiesFileName);
	    	System.out.println(this.prop.toString());
	    	 // get the tokenizer
	    		if(this.prop.containsKey("tokenizer.path")){
	    			if(this.prop.containsKey("tokenizer.window")){
	    				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"),Integer.parseInt(this.prop.getProperty("tokenizer.window")));
	    			}else{
	    				this.tokenizer= new Tokenizer(this.prop.getProperty("tokenizer.path"));// default tokenizer window
	    			}
	    			
	    		}else{
	    			//System.out.println("should throw");
	    			throw(new Exception("Error: 'tokenizer.path' propertiy  missing in "+ propertiesFileName));
	    		}
	    	
	    	 // instantiate the treeTagger
	    		if(this.prop.containsKey("treetagger.home") && this.prop.containsKey("treetagger.model")){
	    			if(this.prop.containsKey("treetagger.lex")){
	    				this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"), this.prop.getProperty("treetagger.lex") );
	    				//CharacterUtils.fullDictionary = this.ttH.postProcDict;
	    				//CharacterUtils.loadDictionary(this.prop.getProperty("treetagger.lex"));
	    			}else{
	    				this.ttH = new TreeTaggerHandler(this.prop.getProperty("treetagger.home"), this.prop.getProperty("treetagger.model"));
	    			}
	    			
	    		 
	    		}else{
	    			//throw(new Exception("Error: 'treetagger.home' or 'treetagger.model' propertiy  missing in "+ propertiesFileName));
	    		}
	    		
	    		if(this.prop.containsKey("postProc.dico")){
	    			
	    			this.ttH.loadPostDictionary(this.prop.getProperty("postProc.dico"));
	    			if(this.prop.containsKey("charUtils.dico")){
	    				CharacterUtils.fullDictionary = this.ttH.postProcDict;
		    		}
	    		}
	    		
	    		
	    		
	    	}
	    
	   
	    
	    private void doTest(File inputFile){
			LinkedList<TtOutputLine> result=new LinkedList<TtOutputLine>() ;
			LinkedList<TtOutputLine> resultDec=new LinkedList<TtOutputLine>() ;
			try{
				int n=0;
				int nDiv=1;
				InputStreamReader isr = new InputStreamReader(new FileInputStream(inputFile));
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				
				LinkedList<String> tokenizedString= new LinkedList<String>(); 
				LinkedList<ArrayList<String>> normalizedString = new LinkedList<ArrayList<String>>();
				LinkedList<String> TTString= new LinkedList<String>();
				
				
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				
				Element root=doc.createElement("test");
				doc.appendChild(root);
				ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"test.xsl\"");
				doc.insertBefore(pi, root);
				while ( (line = br.readLine()) != null)
				{
					
					result.clear();
					tokenizedString.clear();
					
					normalizedString.clear();
					TTString.clear();
					resultDec.clear();
					
					Element currentDiv = doc.createElement("div");
					currentDiv.setAttribute("n", Integer.toString(nDiv));
					nDiv++;
					root.appendChild(currentDiv);
					
					tokenizedString=clearEmptyTokens(this.tokenizer.tokenize(line));
					
					
					
					String[] ttArray = tokenizedString.toArray(new String [tokenizedString.size()]);
					
					if(this.prop.getProperty("to.lower").equals("true") || this.prop.getProperty("normalize").equals("true")){
						//String [] normalizedTTA= new String [tokenizedString.size()];
						for(int ttI=0 ; ttI<ttArray.length ;ttI++){
							StringBuilder sb=new StringBuilder();
							sb.append(ttArray[ttI]);
							if(this.prop.getProperty("to.lower").equals("true")){
								sb.replace(0,ttArray[ttI].length(),ttArray[ttI].toLowerCase());
							}
							if(this.prop.getProperty("normalize").equals("true")){
								String norm = normalize(sb.toString());
								//normalizedTTA[ttI]=norm;
								
								ArrayList<String> decomp = CharacterUtils.decomposeString(norm);
								for(String ds:decomp){
									TTString.add(ds);
								}
								normalizedString.add(decomp);
								
								//normalizedTTA[ttI]=norm;
							}else{
								ArrayList<String> tmp = new ArrayList<String>();
								tmp.add(sb.toString());
								normalizedString.add(tmp);
							//	normalizedTTA[ttI]=sb.toString();
								TTString.add(sb.toString());
							}
							
						}
					//	result = ttH.run(normalizedTTA);
						resultDec = ttH.run(TTString.toArray(new String [TTString.size()]));
					}else{
							
					result = ttH.run(ttArray);
					resultDec = ttH.run(ttArray);
					}
					
					for(ArrayList<String> as: normalizedString){
						StringBuilder token = new StringBuilder();
						StringBuilder lemma = new StringBuilder();
						StringBuilder pos = new StringBuilder();
						
						for(String s : as){
							TtOutputLine ttDL=resultDec.pop();
							if(token.length()>0){
								token.append("+");
								lemma.append("+");
								pos.append("+");
							}
							token.append(ttDL.getToken());
							lemma.append(ttDL.getLemma());
							pos.append(ttDL.getPos());
						}
						Element tokenElement= doc.createElement("w");
						tokenElement.setAttribute("token", token.toString());
						tokenElement.setAttribute("lemma", lemma.toString());
						tokenElement.setAttribute("pos", pos.toString());
						
						currentDiv.appendChild(tokenElement);
						tokenElement.appendChild(doc.createTextNode(tokenizedString.pop()));
						n++;
					}	
					
					
				}  
				System.out.println(n);
				br.close();	
				
				//write xml
				
			//	OutputStream outStream = new FileOutputStream (new File(this.prop.getProperty("test.result.dir"),this.prop.getProperty("test.file.name")+".xml"));
				OutputStream outStream = new FileOutputStream (new File(this.prop.getProperty("test.result.dir"),inputFile.getName()+".xml"));
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

				DOMSource source = new DOMSource(doc);

				StreamResult resultStream = new StreamResult(outStream);

				transformer.transform(source, resultStream);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		/**
		 * 
		 * @param tokenizedString
		 * @return cleaned string, no space tokens tree tagger removes them any way
		 */
		public LinkedList<String> clearEmptyTokens(LinkedList<String> tokenizedString){
			LinkedList<String> cleanedString = new LinkedList<String>();
			for(String s : tokenizedString){
				if(!s.equals(" ")){
					cleanedString.add(s);
					
				}
				
			}
			
			return cleanedString; 
		}
		
		

		   
		public String normalize(String text){
			
			return CharacterUtils.normalizeTilde(CharacterUtils.normalize9(text));
		} 
		
		

		
	    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{

			TestTreeTagger tTT = new TestTreeTagger("tTT.properties");
		// TODO Auto-generated method stub
		JFileChooser testWindow= new JFileChooser("D:\\ljiljana.dolamic\\test_data\\TT\\test_results");
		testWindow.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int rv= testWindow.showOpenDialog(null);

	
		if(rv == JFileChooser.APPROVE_OPTION){
			
			File testFile = testWindow.getSelectedFile();
			if(testFile.isDirectory()){
				File[] files = testFile.listFiles();
				for(File f:files){
					tTT.doTest(f);
				}
			}else{
				tTT.doTest(testFile);
			}

		}
		
	}catch(Exception e){
		System.out.println(e.getMessage());
		//e.getMessage();
	}

}
}
