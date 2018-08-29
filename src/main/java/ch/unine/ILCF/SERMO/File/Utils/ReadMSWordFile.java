/**
 * 
 */
package ch.unine.ILCF.SERMO.File.Utils;

/**
 * @author dolamicl
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import java.lang.StringBuilder;
import java.util.regex.*;


import org.apache.poi.*;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.*;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;



public class ReadMSWordFile {
	
	private String itOpen	=	"<hi rend=\"I\">";
	private String blOpen	=	"<hi rend=\"G\">";
	private String ulOpen	=	"<hi rend=\"S\">";
	private String exOpen	=	"<hi rend=\"E\">";
	private String inOpen	=	"<hi rend=\"i\">";
	private String itblOpen	=	"<hi rend=\"IG\">";
	private String blexOpen	=	"<hi rend=\"GE\">";
	private String stOpen 	= 	"<hi rend=\"ST\">";
	private String hiClose 	=	"</hi>";
	
	
	
	public  void readDocFile(String fileName) {

		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			HWPFDocument doc = new HWPFDocument(fis);

			WordExtractor we = new WordExtractor(doc);

			String[] paragraphs = we.getParagraphText();
			
			System.out.println("Total no of paragraph "+paragraphs.length);
			for (String para : paragraphs) {
				System.out.println(para.toString());
			}
			fis.close();
			we.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void readDocxFile(File file) {
		
		

		try {
			
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			
			XWPFDocument document = new XWPFDocument(fis);
			
          

			List<XWPFParagraph> paragraphs = document.getParagraphs();
			
			for (XWPFParagraph para : paragraphs) {
				System.out.println(para.getText());
				//para.getIRuns()
				
				
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * readDocxFileXML
	 * @param file to read
	 * @return sentences in the LinkedList<String>
	 * the output contains formating information in the form of XML tags. 
	 * The extracted information is:
	 *  - bold, italic, underline
	 *  - boldItalic
	 *  - superscript, subscript
	 *  - boldSuperscript
	 *  The tags added by transcriptors are also taken into account
	 * */
	
	

	//public void readDocxFileXML(File file) {
	public LinkedList<String> readDocxFileXML(File file) {
		
		LinkedList<String> result= new LinkedList<String>();
	//	Pattern tagPattern = Pattern.compile("\\s*<.*>\\s*");
		
		try {			
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());

			XWPFDocument document = new XWPFDocument(fis);
			

			List<XWPFParagraph> paragraphs = document.getParagraphs();		
			
			
			int n=0;
			boolean startedItalic = false;
			boolean startedBold = false;
			boolean startedItalicBold = false;
			boolean startedUnderline = false;
			boolean startedSuperscript = false;
			boolean startedSubscript = false;
			boolean startedBoldSuperscript = false;
			boolean startedStrikeThrough = false; 
			boolean superS = false;
			boolean subS = false;
			boolean underline = false;
			boolean newLine = false;
			boolean newParagraph = false;
			boolean smallCaps=false;
			//String previous="";
			StringBuilder sb = new StringBuilder();
			for (XWPFParagraph para : paragraphs) {
				newLine=true;
				if(para.getText().equals("")){
					newParagraph=true;
					if(startedItalic || startedBold || startedItalicBold || 
							startedUnderline || startedSuperscript ||  startedSubscript || startedBoldSuperscript||startedStrikeThrough){
						sb.append(hiClose);
						startedItalic	=	false;
						startedBold	=	false;
						startedItalicBold	=	false;
						startedUnderline	=	false;
						startedSuperscript	=	false;
						startedSubscript	=	false;
						startedBoldSuperscript	=	false;
						startedStrikeThrough	=	false;
						
					}
				}else{
				
				List<XWPFRun> runs =para.getRuns();
				
				for(XWPFRun run : runs){
					// smallCaps=false;
				//	Matcher tagMatcher = tagPattern.matcher(run.toString());
					// get information on super or sub scripts
					VerticalAlign va = run.getSubscript(); 
					switch(va){
					case BASELINE: 
						superS=false;
						subS=false;
						break;
					case SUBSCRIPT:
						subS=true;
						break;
					case SUPERSCRIPT:
						superS=true;
						break;
					}
				//	if(run.isSmallCaps()){
				//		smallCaps=true;
				//	}
					// find out whether it is underlined
					UnderlinePatterns up = run.getUnderline(); 
					if(!up.name().toString().equals( "NONE") ){
						underline = true;
					}else{
						underline = false;
					}
//					System.out.println(run.text());
//					System.out.println(va);
//					System.out.println(up.name().toString());
//					System.out.println(run.isBold());
					//if starting a new line check whether the hi needs to be closed on the previous one
					if(newLine ){ 						
						if((!run.isItalic() && !run.isBold() && !run.isStrikeThrough() &&
								!underline && !superS && !subS  
								//&& !tagMatcher.find()
								)
								&& 
								(startedItalic || startedBold || startedItalicBold || 
								startedUnderline || startedSuperscript ||  startedSubscript || startedBoldSuperscript||startedStrikeThrough)){
							sb.append(hiClose);
							startedItalic	=	false;
							startedBold	=	false;
							startedItalicBold	=	false;
							startedUnderline	=	false;
							startedSuperscript	=	false;
							startedSubscript	=	false;
							startedBoldSuperscript	=	false;
							startedStrikeThrough	=	false;
							
							
						}
						if(n>0){
//						System.out.println(n);
//						System.out.println(previous);
//						System.out.println(sb.toString());
						result.add(sb.toString());
						if(newParagraph) {
							//System.out.println("<par/>");
							result.add("<par/>");
							newParagraph = false;
							}
						sb=new StringBuilder();
						}
						newLine=false;
						n++;
					//	previous= para.getText();
					}
					
					if(run.isItalic()){
						if(run.isBold()){
							if(!startedItalicBold){
								startedItalicBold = true;
								sb.append(itblOpen);
							}
						}else if(!startedItalic){
							startedItalic=true;
							sb.append(itOpen);
							
						}
						
					}else if(run.isBold()){
						if(superS){
							if(!startedBoldSuperscript){
								startedBoldSuperscript = true;
								sb.append(blexOpen);
							}
						}else if(!startedBold){
							startedBold=true;
							sb.append(blOpen);
						}
						
					}else if(underline){
						if(!startedUnderline){
							startedUnderline=true;
							sb.append(ulOpen);
						}
					}else if(superS){
						if(!startedSuperscript){
							startedSuperscript=true;
							sb.append(exOpen);
						}
					}else if(subS){
						if(!startedSubscript){
							startedSubscript=true;
							sb.append(inOpen);
						}
						
					}else if(run.isStrikeThrough()){
						if(!startedStrikeThrough){
							startedStrikeThrough=true;
							sb.append(stOpen);
						}
					}else{
						if((startedItalic || startedBold || startedItalicBold || 
								startedUnderline || startedSuperscript ||  startedSubscript || startedBoldSuperscript||startedStrikeThrough) 
								//&& !tagMatcher.find()
								){
							sb.append(hiClose);
							startedItalic	=	false;
							startedBold	=	false;
							startedItalicBold	=	false;
							startedUnderline	=	false;
							startedSuperscript	=	false;
							startedSubscript	=	false;
							startedBoldSuperscript	=	false;
							startedStrikeThrough	=	false;
						}
					}
					
					//if(smallCaps){
					//	sb.append(run.toString().toUpperCase());
				//	}else{
						sb.append(run.toString());
				//	}
					
					
				
					
					
				}
			
			}
			}
//			System.out.println(n);
//			System.out.println(previous);
//			System.out.println(sb.toString());
			result.add(sb.toString());
			
//			for(String r:result){
//				System.out.println(r);
//			}
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList<String> result=new LinkedList<String>() ;
		ReadMSWordFile rMSW=new ReadMSWordFile();
		JFileChooser window= new JFileChooser();
		int rv= window.showOpenDialog(null);
		
		if(rv == JFileChooser.APPROVE_OPTION){
			result=rMSW.readDocxFileXML(window.getSelectedFile());
		}
		for(String r:result){
			System.out.println(r);
		}
		// TODO Auto-generated method stub
		//readDocxFile("D:\\ljiljana.dolamic\\test_data\\Calvin.docx");

		//readDocFile("C:\\Test.doc");

	}

}
