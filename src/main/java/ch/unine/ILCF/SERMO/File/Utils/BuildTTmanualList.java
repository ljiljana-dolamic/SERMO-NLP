/**
 * 
 */
package ch.unine.ILCF.SERMO.File.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerWrapper;

import ch.unine.ILCF.SERMO.LGeRM.CreateLGeRMInput;
import ch.unine.ILCF.SERMO.LGeRM.OutputLine;
import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;
import ch.unine.ILCF.SERMO.Perl.Utils.ExecutePerlCommand;
import ch.unine.ILCF.SERMO.TT.*;

/**
 * @author dolamicl
 *
 */
public class BuildTTmanualList {

	/**
	 * @param args
	 */
	
	private String inputFileName;
	private LinkedList<String> headerPart;
	private LinkedList<String> bodyPart;
	private LinkedList<String> sentenceList = new LinkedList<String>() ;
	private LinkedList<TtOutputLine> ttOut = new LinkedList<TtOutputLine>();
	private Tokenizer tokenizer;
	private TreeTaggerHandler ttH = new TreeTaggerHandler("C:\\Program Files\\TreeTagger","C:\\Program Files\\TreeTagger\\lib\\presto.par:UTF8");
	boolean doJava=false; 
	private boolean inTitle=false;
//	private Map<Integer,List<String>> tagOffset = new HashMap<Integer,List <String>>();
	
	public BuildTTmanualList(){
		
	}
	
	public BuildTTmanualList(String dictionaryPath){
		this(dictionaryPath, 10);
	}
	
	public BuildTTmanualList(String dictionaryPath, int window ){
		this.tokenizer=new Tokenizer(dictionaryPath, window);
		
	}
	
 private void buildManualList(File inputFile){
		String [] fileNameParts= inputFile.getName().split("_"); //keeping only filename without the extention
		 System.out.println("Doc: "+new StringJoiner("_").add(fileNameParts[0]).add(fileNameParts[1]));

		 System.out.println();
		 
		 StringBuilder paragraphBuilder;
		 StringBuilder titleBuilder=new StringBuilder();;
		 
		ReadMSWordFile rMSW = new ReadMSWordFile();
		
		sentenceList = rMSW.readDocxFileXML(inputFile);
		
		Map<String,LinkedList<String>> docParts=ParseDocParts.splitSentenceList(sentenceList);
		this.headerPart = docParts.get("header");
		this.bodyPart = docParts.get("body") ;
		//System.out.println(this.bodyPart.size());
		
		paragraphBuilder = new StringBuilder(); 
		
		for(String line: bodyPart){   //start the rest of the body
			
			if(isTitle(line)){
			
			parseParagraph(paragraphBuilder.toString());
			paragraphBuilder=new StringBuilder();
			
			titleBuilder.append(line);
			titleBuilder.append("\n");
			//System.out.println("Title Part: "+titleBuilder.toString());
			if(isTitleEnd(line)){
			    parseParagraph(titleBuilder.toString());
			}else{
				inTitle=true;
			} 
		 }else if(inTitle){
			 titleBuilder.append(line);
			 if(isTitleEnd(line)){
				    parseParagraph(titleBuilder.toString());
				    inTitle=false;
				}else{
					titleBuilder.append("\n");
				} 
			
		 }else if(isTitleEnd(line)){
			 titleBuilder.append(line);
			
			parseParagraph(titleBuilder.toString());
			 inTitle=false;
		 }else if(line.matches("<par/>.?")){
			
				 if(paragraphBuilder.length()!=0){
						 
			parseParagraph(paragraphBuilder.toString());

			 }
			 paragraphBuilder=new StringBuilder();
			
		 }else{
			
				 if(paragraphBuilder.length()!=0){
					 
				 paragraphBuilder.append("\n");
				 }
			 paragraphBuilder.append(line);
			
		 }
			

		}
		if(paragraphBuilder.length()!=0){
			 
			parseParagraph(paragraphBuilder.toString());

			 }
		
	}
	
	private boolean isTitle(String line){
		boolean is_title=false;
		 Pattern p = Pattern.compile(".*<t>.*");
		 Matcher m = p.matcher(line);
		 
		if( m.matches()){
			is_title=true;
			//System.out.println("Title line:"+line);
			//inTitle=true;
		}
		
		return is_title;
		
		
	}
	private boolean isTitleEnd(String line){
		boolean is_title_end=false;
		 Pattern p = Pattern.compile(".*</t>.*");
		 Matcher m = p.matcher(line);
	//	 System.out.println(line);
		if( m.matches()){
			is_title_end=true;
			
			//System.out.println("titleEnds");
		//	inTitle=false;
		}
		
		return is_title_end;
		
		
	}
	
	private void printListConsole(){
		
		
		
		for(String s:sentenceList){
			
			System.out.println(s);
		}
		
		
		
	}
	
	private void saveOutput(String filePath){
		System.out.println("writing to :"+ filePath);
		try{
			File fout = new File(filePath);
		
		FileOutputStream fos = new FileOutputStream(fout);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 
		for(String s: this.sentenceList){
			bw.write(s);
			bw.newLine();
		}
	 
		bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	private void parseParagraph(String paragraph){
			
		   
			paragraph=paragraph.replaceAll("<.*?>", "");
			
			//System.out.println("paragraph "+paragraph+"\n end par*****************");
			LinkedList<String> tokenizedText = parseTextPRESTOjava(paragraph); 
			LinkedList<TtOutputLine> ttOut = new LinkedList<TtOutputLine>();
		//	String [] tT = tokenizedText.toArray();
			try{
			 ttOut = ttH.run(tokenizedText.toArray(new String [tokenizedText.size()]));
			}catch(Exception e){
				System.out.println("TTerror");
			}
			
			getLines(ttOut);
//			System.setProperty("treetagger.home", "C:\\Program Files\\TreeTagger");
//			
//			TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
//			try {
//				//tt.setModel("/opt/treetagger/models/english.par:iso8859-1");
//				tt.setModel("C:\\Program Files\\TreeTagger\\lib\\presto.par:UTF8");
//				//tt.setArguments(args);
//				tt.setHandler(new TokenHandler<String>()
//				{
//					public void token(String token, String pos, String lemma)
//					{
//						System.out.println(token + "\t" + pos + "\t" + lemma);
//					}
//				});
//				
//				
//				tt.process(str3);
//			}
//			finally {
//				tt.destroy();
//			}
			


		}
	private void getLines(LinkedList<TtOutputLine> ttOut){
		
		//System.out.println("TToutléines; ");
		for(int i = 1; i<ttOut.size();i++){
			//System.out.println(ttOut.toString());
			TtOutputLine ttLine = ttOut.get(i);
			//System.out.println(ttLine.toString());
			if(ttLine.getLemma().equals("QUE") && ttLine.getPos().equals("Cs") && !ttOut.get(i-1).getLemma().equals(",") && !ttOut.get(i-1).getLemma().equals(".") ){
				//System.out.println(ttOut.get(i-1).toString());
			    
				//System.out.println(ttLine.toString()+ i);
				int start = (i>5)? (i-5) : 0;
				int end = (i < (ttOut.size()-5) )? i+5 : ttOut.size()-1;
				StringBuilder line = new StringBuilder();
				for(int j = start; j<=end;j++){
					line.append(ttOut.get(j).getToken()).append(" ");
				}
				System.out.println(line);
				System.out.println();
			}
			
			
		}
	}
	
	private OutputLine addLine(String w,String l,String t){
		OutputLine tmpLine=new OutputLine(w,l,t);
		return tmpLine;
		
	}
	
	
	private LinkedList<String> parseTextPRESTOjava(String text){
		
		LinkedList<OutputLine> tmp=new LinkedList<OutputLine>();
		
		LinkedList<String> stdOutRes=new LinkedList<String>();
		
		
		stdOutRes= tokenizer.tokenize(text);
		
		
		return stdOutRes;
	}
//	 private void splitSentenceList(LinkedList<String> list){
//    	 headerPart = new LinkedList<String>();
//    	 bodyPart = new LinkedList<String>();
//    	 boolean is_header = true;
//    	
//    	 for(String sentence:list){
//    		 
//    		 if(is_header){
//    			 headerPart.add(sentence);
//    		 }else{
//    			 bodyPart.add(sentence);
//    		 }
//    		 Pattern p = Pattern.compile(".*<source_num>.*");
//    		 Matcher m = p.matcher(sentence);
//    		
//    		if( m.matches()){
//    			is_header=false;
//    		}
//    	 }
//    	 
//     }
	
	public static void main(String[] args) {
		
	
		BuildTTmanualList bTTML = new BuildTTmanualList("src/main/resources/lex.csv", 5);
				//String resultsDir = args[0];
				JFileChooser window= new JFileChooser();
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
							bTTML.buildManualList(f);
						}
					}else{
						bTTML.buildManualList(window_file);  
					}
					//createLGeRMInput.printListConsole();
					//createLGeRMInput.saveOutput(resultsDir+"\\"+new StringJoiner("_").add(fileNameParts[0]).add(fileNameParts[1]).add(fileNameParts[3])+".out");
				}
			

	}


}
