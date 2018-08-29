package ch.unine.ILCF.SERMO.SSplit;

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.XML.Utils.BuildCleanXML;
import ch.unine.ILCF.SERMO.XML.Utils.BuildTranscriptionXML;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class SCNlpSSplit {
	
	private LinkedList<SentenceInfo> sentences;
	Properties props;
	StanfordCoreNLP pipeline;
	private Annotation annotation;
	
	public SCNlpSSplit() {
		this.props = new Properties();
	    
		this.props.setProperty("annotators", "tokenize, ssplit");
	    
		this.props.put("tokenize.language","French");
	    this.pipeline = new StanfordCoreNLP(props);
	}
	
	
	public LinkedList<SentenceInfo> getSentences(String textToAnnotate){
	
		sentences= new LinkedList<SentenceInfo>();
    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
    annotation = new Annotation(textToAnnotate);;
    

    // run all the selected Annotators on this text
    pipeline.annotate(annotation);
//    pipeline.prettyPrint(annotation, new PrintWriter(System.out));
   
    // An Annotation is a Map with Class keys for the linguistic analysis types.
    // You can get and use the various analyses individually.
    // For instance, this gets the parse tree of the first sentence in the text.
    
    List<CoreMap> sentencesAnnotation = annotation.get(CoreAnnotations.SentencesAnnotation.class);
    for(CoreMap sa: sentencesAnnotation) {
    	
    	SentenceInfo si= new SentenceInfo();
    	si.setSentenceId(sa.get(CoreAnnotations.SentenceIndexAnnotation.class));
        si.setStartOffset(sa.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
        si.setEndOffset(sa.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
 	   	si.setNoToken(sa.get(CoreAnnotations.TokensAnnotation.class).size());
 	   	si.setSentence(sa.get(CoreAnnotations.TextAnnotation.class));
 	   
 	   sentences.add(si);
   }

    return sentences; 
      
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
		    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element root=doc.createElement("root");
			doc.appendChild(root);
	
			SCNlpSSplit sentenceSplit=new SCNlpSSplit();

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
			BuildCleanXML.printXMLtoFile(doc, outDir, "SsplitSCnlp2.xml");
		}catch(Exception e){
			e.printStackTrace();
			//System.out.println();
//			
		}

	}

}
