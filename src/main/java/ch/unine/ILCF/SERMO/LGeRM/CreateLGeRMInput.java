/**
 * 
 */
package ch.unine.ILCF.SERMO.LGeRM;

/**
 * @author dolamicl
 *
 */


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.regex.*;
import javax.swing.JFileChooser;
import java.lang.StringBuilder;
import java.io.FileOutputStream;
import java.util.StringJoiner;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;



import ch.unine.ILCF.SERMO.File.Utils.ReadMSWordFile;
import ch.unine.ILCF.SERMO.LGeRM.OutputLine;
import ch.unine.ILCF.SERMO.Perl.Utils.ExecutePerlCommand;
import ch.unine.ILCF.SERMO.File.Utils.ParseDocParts; 
import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;
import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;



public class CreateLGeRMInput {

	/**
	 * @param args
	 */

	private String inputFileName;
	private LinkedList<String> headerPart;
	private LinkedList<String> bodyPart;
	private LinkedList<OutputLine> resultList =new LinkedList<OutputLine>();
	private Tokenizer tokenizer;
	boolean doJava=false; 
	private boolean inTitle=false;
	//	private Map<Integer,List<String>> tagOffset = new HashMap<Integer,List <String>>();

	public CreateLGeRMInput(){

	}

	public CreateLGeRMInput(String dictionaryPath){
		this(dictionaryPath, 10);
	}

	public CreateLGeRMInput(String dictionaryPath, int window ){
		this(new Tokenizer(dictionaryPath, window));

	}
	public CreateLGeRMInput(Tokenizer tokenizer ){
		this.tokenizer=tokenizer;
		this.doJava=true;
	}


	/**
	 * called from transcription handler to build the LGeRM input
	 * 
	 * **/

	public LinkedList<OutputLine> buildLGeRMOutputDOM(LinkedHashMap<Integer, String> paragraphList, LinkedList<TagInfo> tags, HashMap <Integer,LinkedList<Integer>> inTags, HashMap<Integer,LinkedList<Integer>> outTags){
		LinkedList<OutputLine> tmpRes = new LinkedList<OutputLine>();
		try{
			for(Integer offset : paragraphList.keySet()){
				String paragraphText = paragraphList.get(offset);
				LinkedList<OutputLine> tmp=parseText(paragraphText);
				tmpRes.addAll(reconstructTags(offset,tmp,tags,inTags,outTags));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return tmpRes;
	}

	private LinkedList<OutputLine> reconstructTags(Integer startOffset,LinkedList<OutputLine> paragraphOut, LinkedList<TagInfo> tags , HashMap <Integer,LinkedList<Integer>> inTags, HashMap<Integer,LinkedList<Integer>> outTags)throws Exception{

		LinkedList<OutputLine> tmpRes = new LinkedList<OutputLine>();
		LinkedList<Integer> inTagsList = inTags.get(startOffset);
		LinkedList<Integer> outTagsList =null;
		LinkedList<OutputLine> inTagsOff = new LinkedList<OutputLine>();
		LinkedList<OutputLine> outTagsOff = new LinkedList<OutputLine>();

		int currentOffset = startOffset;
		boolean inBible = false;
		StringBuilder biblRef=new StringBuilder();

		for(OutputLine oLine : paragraphOut){

			String token = oLine.getWord();
			char [] tokenChar = token.toCharArray();
			StringBuilder tokenOut=new StringBuilder();

			if (outTagsList != null) {
				Iterator<Integer> outTagIter = outTagsList.iterator();

				while (outTagIter.hasNext()) {
					Integer tagIndex = outTagIter.next();
					if (tags.get(tagIndex).getName().equals("bibl")) {
						inBible = false;
						tmpRes.add(addLine(biblRef.toString(), "", ""));
						biblRef.setLength(0);
					}
					if (inTagsList == null || (inTagsList != null && !inTagsList.contains(tagIndex))) {
						StringBuilder tag = new StringBuilder();
						tag.append("</").append(tags.get(tagIndex).getName()).append(">");
						outTagIter.remove();
						outTagsOff.add(addLine(tag.toString(), tag.toString(), "tag"));
					}
				}
			}
			if (inTagsList != null) {
				Iterator<Integer> inTagIter = inTagsList.iterator();

				while (inTagIter.hasNext()) {
					Integer tagIndex = inTagIter.next();
					String tagName=tags.get(tagIndex).getName();

					if (tags.get(tagIndex).getName().equals("bibl")) {
						inBible = true;
					}

					StringBuilder tag = new StringBuilder();
					tag.append("<");
					Map<String, String> attr = tags.get(tagIndex).getTagAttributes();

					if(tagName.equals("#comment")){
						//tag.append("!--").append(attr.get("comment")).append("-->");
						tag.append("!--").append("comment").append("-->");
						if (outTagsList != null && outTagsList.contains(tagIndex)){
							outTagsList.remove(outTagsList.indexOf(tagIndex));
						}
					}else{
						tag.append(tagName);
						for (String an : attr.keySet()) {
							tag.append(" ").append(an).append("=").append("\"").append(attr.get(an)).append("\"");
						}
						if ((outTagsList != null && outTagsList.contains(tagIndex))||tagName.equals("lb")||tagName.equals("pb")||tagName.equals("figure")||tagName.equals("cb")) {
							tag.append("/>");
							if (outTagsList != null && outTagsList.contains(tagIndex)){
								outTagsList.remove(outTagsList.indexOf(tagIndex));
							}
						} else {
							tag.append(">");
						}
					}
					if (tags.get(tagIndex).getName().equals("c")) {
						tokenOut.append(tag);

					} else {
						inTagsOff.add(addLine(tag.toString(), tag.toString(), "tag"));
					}
					inTagIter.remove();


				}
			}
			//check if there are tags within words
			if(token.length()>0){
				tokenOut.append(tokenChar[0]);
				for(int i= 1 ;i < token.length() ; i++){
					if(outTags.containsKey(currentOffset+i)||inTags.containsKey(currentOffset+i)){
						LinkedList<Integer> inTagsListW = inTags.get(currentOffset+i);
						LinkedList<Integer> outTagsListW = outTags.get(currentOffset+i);
						if( inTagsListW != null){
							for(Integer inTag : inTagsListW ){
								
								if(!tags.get(inTag).getName().equals("#comment")){
									if(tags.get(inTag).getName().equals("lb")||tags.get(inTag).getName().equals("pb")||tags.get(inTag).getName().equals("figure")||tags.get(inTag).getName().equals("cb")){
										tokenOut.append("<").append(tags.get(inTag).getName()).append("/>");
									}else{
										tokenOut.append(buildInTag(tags.get(inTag)));
									}
									}
								if(tags.get(inTag).getName().equals("bibl")){
									inBible=true;
								}
							}
						}
						
						if(outTagsListW != null){
							for(Integer outTag : outTagsListW){
								if(!tags.get(outTag).getName().equals("#comment")
										&&!(tags.get(outTag).getName().equals("lb")
												||tags.get(outTag).getName().equals("pb")
												||tags.get(outTag).getName().equals("figure")
												||tags.get(outTag).getName().equals("cb"))){
									tokenOut.append("</").append(tags.get(outTag).getName()).append(">");
									if(tags.get(outTag).getName().equals("bibl")){
										inBible=false;
									}
								}
							}
						}


					}
					tokenOut.append(tokenChar[i]);
				}
			}
			oLine.setWord(tokenOut.toString());


			if(inBible){


				if(token.equals(".")){
					//append first the tags before the token
					for(OutputLine ol:outTagsOff){

						biblRef.append(ol.getWord());
					}
					for(OutputLine il:inTagsOff){

						biblRef.append(il.getWord());
					}
				}else {
					if( biblRef.length()!=0){
						tmpRes.add(addLine(biblRef.toString(), "", ""));
						biblRef.setLength(0);
					}
					tmpRes.addAll(outTagsOff);
					tmpRes.addAll(inTagsOff);

				}
				biblRef.append(tokenOut);
							}else{
				
				tmpRes.addAll(outTagsOff);
				tmpRes.addAll(inTagsOff);
				tmpRes.add(oLine);
			}
			tokenOut.setLength(0);
			if (inTagsList != null) {
				if(inTagsList.isEmpty()){
					inTags.remove(currentOffset);
				}else{
					inTags.put(currentOffset, inTagsList);
				}
			}
			if (outTagsList != null) {
				if(outTagsList.isEmpty()){
					outTags.remove(currentOffset);
				}else{
					outTags.put(currentOffset, outTagsList);
				}
			}
			currentOffset += token.length();
			inTagsList = inTags.get(currentOffset);
			outTagsList =outTags.get(currentOffset);
			inTagsOff.clear();
			outTagsOff.clear();
		}
        
		if(inBible && biblRef.length()!=0){
			tmpRes.add(addLine(biblRef.toString(), "", ""));
			biblRef.setLength(0);
			inBible=false;
		}

		if(outTags.containsKey(currentOffset)){
		
			LinkedList<Integer> toRemove = new LinkedList<Integer>();
			if(inTagsList != null){
				Iterator<Integer> inTagIter = inTagsList.iterator();
				while(inTagIter.hasNext()){
					Integer inTagIndex = inTagIter.next();
					if(outTagsList.contains(inTagIndex)){
						toRemove.add(inTagIndex);
						inTagIter.remove();
					}else{
						break;
					}
				}
			}
			Iterator<Integer> outTagIter = outTagsList.iterator();
			while (outTagIter.hasNext()) {
				Integer tagIndex = outTagIter.next();
				String tagName=tags.get(tagIndex).getName();
				StringBuilder tag = new StringBuilder();
				if (toRemove != null && toRemove.contains(tagIndex)) {
					tag.append("<");
					Map<String, String> attr = tags.get(tagIndex).getTagAttributes();
					if(tagName.equals("#comment")){
					//	tag.append("!--").append(attr.get("comment")).append("-->");
						tag.append("!--").append("comment").append("-->");
					}else{
						tag.append(tagName);
						for (String an : attr.keySet()) {
							tag.append(" ").append(an).append("=").append("\"").append(attr.get(an)).append("\"");
						}
						tag.append("/>");
					}
					tmpRes.add(addLine(tag.toString(), tag.toString(), "tag"));
				} else if (inTagsList==null||(inTagsList != null && !inTagsList.contains(tagIndex))){
					tag.append("</").append(tagName).append(">");
					tmpRes.add(addLine(tag.toString(), tag.toString(), "tag"));
				}
				outTagIter.remove();
			}
			if (inTagsList != null) {
				if(inTagsList.isEmpty()){
					inTags.remove(currentOffset);
				}else{
					inTags.put(currentOffset, inTagsList);
				}
			}
			if(outTagsList.isEmpty()){
				outTags.remove(currentOffset);
			}else{
				outTags.put(currentOffset, outTagsList);
			}
		}


		return tmpRes;

	}

	private String buildInTag(TagInfo tag){
		StringBuilder inTag =new StringBuilder();
		String name = tag.getName();
		inTag.append("<");
		Map<String, String> attr = tag.getTagAttributes();

		if(name.equals("#comment")){
			//inTag.append("!--").append(attr.get("comment")).append("-->");
			inTag.append("!--").append("comment").append("-->");
		}else{
			inTag.append(name);
			for (String an : attr.keySet()) {
				inTag.append(" ").append(an).append("=").append("\"").append(attr.get(an)).append("\"");
			}
			if (name.equals("lb")||name.equals("pb")||name.equals("figure")||name.equals("cb")) {
				inTag.append("/>");

			} else {
				inTag.append(">");
			}
		}
		return inTag.toString();


	}

	private LinkedList<OutputLine> parseText(String text){
		LinkedList<OutputLine> tmp=new LinkedList<OutputLine>();

		if(this.doJava){
			tmp=parseTextPRESTOjava(text);
		}else{
			tmp=parseTextPRESTOperl(text);
		}
		return tmp;
	}

	private LinkedList<OutputLine> parseTextPRESTOperl(String text){
		LinkedList<OutputLine> tmp=new LinkedList<OutputLine>();
		LinkedList<String> stdOutRes=new LinkedList<String>();
		String script="src/main/resources/perl/tokenise.pl";
		String [] otherArgs={"-d", "src/main/resources/lex.csv", "-w", "5"};
		try{
			ExecutePerlCommand  executePerlCommand=new ExecutePerlCommand(script,otherArgs);
			stdOutRes=executePerlCommand.stdOut(text);
		}catch(Exception e){
			e.printStackTrace();
		}


		for(String s: stdOutRes){
			if(!s.trim().isEmpty()){
				tmp.add(addLine(s,"",""));
			} 
		}
		return tmp;
	}

	/**
	 * Tokenizing using java ported presto tokenizer
	 * @param text to be parsed
	 * @return list of LGeRM outputLines
	 * @see ch.unine.ILCF.SERMO.LGeRM.OutputLine
	 * **/
	private LinkedList<OutputLine> parseTextPRESTOjava(String text){
		LinkedList<OutputLine> tmp=new LinkedList<OutputLine>();
		LinkedList<String> stdOutRes=new LinkedList<String>();

		stdOutRes= tokenizer.tokenize(text);

		for(String s: stdOutRes){

			if(!s.equals("_") && !s.equals("#")&& !s.equals("@")&&!s.equals("")){
				tmp.add(addLine(s,"",""));
			}

		}
		return tmp;
	}

	private LinkedList<OutputLine> parseTextRefBibl(String text){
		LinkedList<OutputLine> tmp=new LinkedList<OutputLine>();
		LinkedList<String> stdOutRes=new LinkedList<String>();

		stdOutRes= tokenizer.tokenize(text);

		for(String s: stdOutRes){

			if(!s.isEmpty()){
				tmp.add(addLine(s,s,"ref_bibl"));
			}
		}
		return tmp;
	}


	// from here code not really used

	private void buildLGeRMOutputList(File inputFile){
		this.inputFileName= (inputFile.getName().split("\\."))[0]; //keeping only filename without the extention
		LinkedList<String> sentenceList = new LinkedList<String>() ;
		// boolean newParagraf= true;
		// boolean started=false;
		StringBuilder paragraphBuilder;
		StringBuilder titleBuilder=new StringBuilder();;
		//OutputLine tmpLine;
		ReadMSWordFile rMSW = new ReadMSWordFile();

		sentenceList = rMSW.readDocxFileXML(inputFile);

		Map<String,LinkedList<String>> docParts=ParseDocParts.splitSentenceList(sentenceList);
		this.headerPart = docParts.get("header");
		this.bodyPart = docParts.get("body") ;
		paragraphBuilder = new StringBuilder(); 
		resultList.add(addLine("<div>","<div>","tag"));
		resultList.add(addLine("<pb n=\"titre\"/>","<pb n=\"titre\"/>","tag"));
for(String line: bodyPart){   //start the rest of the body

			if(isTitle(line)){
	resultList.add(addLine("</div>","</div>","tag"));

resultList.add(addLine("<div>","<div>","tag"));
				resultList.addAll(parseParagraph(paragraphBuilder.toString()));
				paragraphBuilder=new StringBuilder();

				titleBuilder.append(line);
				titleBuilder.append("<lb/>");
				if(isTitleEnd(line)){
					resultList.addAll(parseParagraph(titleBuilder.toString()));
				}else{
					inTitle=true;
				} 
			}else if(inTitle){
				titleBuilder.append(line);
				if(isTitleEnd(line)){
					resultList.addAll(parseParagraph(titleBuilder.toString()));
					inTitle=false;
				}else{
					titleBuilder.append("<lb/>");
				} 

			}else if(isTitleEnd(line)){
				titleBuilder.append(line);
				
				resultList.addAll(parseParagraph(titleBuilder.toString()));
				inTitle=false;
			}else if(line.matches("<par/>")){
				if(paragraphBuilder.length()!=0){
					paragraphBuilder.insert(0, "<p>");
					paragraphBuilder.append("</p>");
				
					resultList.addAll(parseParagraph(paragraphBuilder.toString()));
					
				}
				paragraphBuilder=new StringBuilder();
				
			}else{
				
				if(paragraphBuilder.length()!=0){
					
					paragraphBuilder.append("<lb/>");
				}
				paragraphBuilder.append(line);

			}


		}

		resultList.add(addLine("</div>","</div>","tag"));
	}

	private void buildLGeRMOutputListOffset(File inputFile){
		this.inputFileName= (inputFile.getName().split("\\."))[0]; //keeping only filename without the extention
		LinkedList<String> sentenceList = new LinkedList<String>() ;
		ReadMSWordFile rMSW = new ReadMSWordFile();

		sentenceList = rMSW.readDocxFileXML(inputFile);

		Map<String,LinkedList<String>> docParts=ParseDocParts.splitSentenceList(sentenceList);
		this.headerPart = docParts.get("header");
		this.bodyPart = docParts.get("body") ;

		resultList = buildLGeRMOutputListOffset(this.bodyPart);

	}

	public LinkedList<OutputLine> buildLGeRMOutputListOffset(LinkedList<String> bodyPart){
		LinkedList<OutputLine> tmpRes = new LinkedList<OutputLine>();
		this.bodyPart = bodyPart;
		int parNO=1;
		StringBuilder paragraphBuilder=new StringBuilder();
		StringBuilder titleBuilder=new StringBuilder();

		tmpRes.add(addLine("<div type=\"titre\">","<div type=\"titre\">","tag"));
		tmpRes.add(addLine("<pb n=\"titre\"/>","<pb n=\"titre\"/>","tag"));


		for(String line: bodyPart){   //start the rest of the body
			
			if(isTitle(line)){

				tmpRes.add(addLine("</div>","</div>","tag"));
				tmpRes.add(addLine("<div type=\"sermon\">","<div type=\"sermon\">","tag"));
				parNO=1;
				tmpRes.addAll(parseParagraphOffset(paragraphBuilder.toString()));
				paragraphBuilder=new StringBuilder();
				titleBuilder.append(line);

				titleBuilder.append("</lb>");

				if(isTitleEnd(line)){
					tmpRes.addAll(parseParagraphOffset(titleBuilder.toString()));
				}else{
					inTitle=true;
				} 
			}else if(inTitle){
				titleBuilder.append(line);
				if(isTitleEnd(line)){
					tmpRes.addAll(parseParagraphOffset(titleBuilder.toString()));
					inTitle=false;
				}else{
					titleBuilder.append("</lb>");
				} 
			}else if(isTitleEnd(line)){
				titleBuilder.append(line);
				tmpRes.addAll(parseParagraphOffset(titleBuilder.toString()));
				inTitle=false;
			}else if(line.matches("<par/>.?")){
				if(paragraphBuilder.length()!=0){
					paragraphBuilder.insert(0, "<p n=\""+parNO+"\">");
					paragraphBuilder.append("</p>");
					tmpRes.addAll(parseParagraphOffset(paragraphBuilder.toString()));
					parNO++;
				}

				paragraphBuilder=new StringBuilder();
			}else{
				if(paragraphBuilder.length()!=0 && !paragraphBuilder.toString().matches("\\s*") ){
					paragraphBuilder.append("<lb/>");
				}
				if(!line.matches("\\s*")){
					paragraphBuilder.append(line);
				}
			}


		}
		//flash-out the paragraphBuilder
		if(paragraphBuilder.length()!=0 || !paragraphBuilder.toString().equals(" ")){
			paragraphBuilder.insert(0, "<p n=\""+parNO+"\">");
			paragraphBuilder.append("</p>");	 
			tmpRes.addAll(parseParagraphOffset(paragraphBuilder.toString()));
		}
		tmpRes.add(addLine("</div>","</div>","tag"));

		return tmpRes;
	}

	private boolean isTitle(String line){
		boolean is_title=false;
		Pattern p = Pattern.compile(".*<t>.*");
		Matcher m = p.matcher(line);

		if( m.matches()){
			is_title=true;
		}

		return is_title;


	}
	private boolean isTitleEnd(String line){
		boolean is_title_end=false;
		Pattern p = Pattern.compile(".*</t>.*");
		Matcher m = p.matcher(line);
		
		if( m.matches()){
			is_title_end=true;

		}

		return is_title_end;


	}

	// biblical reference returned by lGeRM parsed in format token : offset 
	public LinkedList<OutputLine> parseBiblRef(String ref){
		LinkedList<OutputLine> parsedRef = new LinkedList<OutputLine>();
		String [] refArray = splitRefTag(ref);

		for (String s: refArray){
			if(s.matches("<.*>")){
				parsedRef.add(addLine(s,s,"tag"));
			}else if(!s.isEmpty()){

				parsedRef.addAll(this.parseTextRefBibl(s));
			}
		}
		return parsedRef;

	}






	private LinkedList<OutputLine> parseParagraph(String paragraph){

		LinkedList<OutputLine> lineList = new LinkedList<OutputLine>(); 

		Pattern extractionPattern= Pattern.compile("(<.*?>)\\s*([^<]*)?");
		Matcher exMatcher = extractionPattern.matcher(paragraph);

		boolean is_bibl=false;
		StringBuilder refBibl=new StringBuilder();


		OutputLine tmpLine = new OutputLine();

		while(exMatcher.find()){
			String tag= exMatcher.group(1);
			String text= exMatcher.group(2);

			if(tag.matches("<bibl>")){

				lineList.add(addLine(tag,tag,"tag"));
				is_bibl=true;
				refBibl.setLength(0);
				refBibl.append(text);
			}else if(tag.matches("</bibl>")){
				is_bibl=false;
				String ref_bibl=refBibl.toString();

				lineList.add(addLine(ref_bibl,ref_bibl,"ref_bibl"));
				lineList.add(addLine(tag,tag,"tag"));

				if(!text.trim().equals("")){
					lineList.addAll(parseText(text));

				}
			}else{
				if(is_bibl){
					refBibl.append(tag);
					refBibl.append(text);
				}else{
					lineList.add(addLine(tag,tag,"tag"));

					if(!text.trim().equals("")){
						lineList.addAll(parseText(text));

					}
				}
			}


		}

		return lineList;


	}
	private LinkedList<OutputLine> parseParagraphOffset(String paragraph){

		LinkedList<OutputLine> lineList = new LinkedList<OutputLine>();
		Map<Integer,List<String>> tagOffset = new HashMap<Integer,List <String>>();
		Integer currentStringOffest=0;

		StringBuilder paragraphText = new StringBuilder();
		Pattern extractionPattern= Pattern.compile("(<.*?>)([^<]*)?");
		Matcher exMatcher = extractionPattern.matcher(paragraph);

		boolean is_bibl=false;
		StringBuilder refBibl=new StringBuilder();
		while(exMatcher.find()){
			String tag= exMatcher.group(1);
			String text= exMatcher.group(2);

			if(tag.matches("<bibl>")){

				if(!tagOffset.containsKey(currentStringOffest)){
					LinkedList<String> tmpList=new LinkedList<String>();
					tmpList.add(tag);
					tagOffset.put(currentStringOffest, tmpList);
				}else{
					tagOffset.get(currentStringOffest).add(tag);
				}

				is_bibl=true;
				refBibl.setLength(0);
				refBibl.append(text);
			}else if(tag.matches("</bibl>")){
				is_bibl=false;
				String ref_bibl=refBibl.toString();

				tagOffset.get(currentStringOffest).add(ref_bibl);
				tagOffset.get(currentStringOffest).add(tag);

				paragraphText.append(text);
				currentStringOffest +=text.length();

			}else{
				if(is_bibl){
					refBibl.append(tag);
					refBibl.append(text);
				}else{
					if(!tagOffset.containsKey(currentStringOffest)){
						LinkedList<String> tmpList=new LinkedList<String>();
						tmpList.add(tag);
						tagOffset.put(currentStringOffest, tmpList);
					}else{
						tagOffset.get(currentStringOffest).add(tag);
					}

					paragraphText.append(text);

					currentStringOffest +=text.length();

				}
			}


		}
		lineList = 	reconstructTags(paragraphText.toString(), tagOffset);	
		return lineList;


	}
	private  LinkedList<OutputLine> reconstructTags (String paragraphText, Map<Integer,List<String>> tagOffset ){


		LinkedList<OutputLine> tmp=parseText(paragraphText);

		LinkedList<OutputLine> out = new LinkedList<OutputLine>();
		int currentOffset = 0;
		boolean skipTag = false;
		for(OutputLine oLine : tmp){
			String token = oLine.getWord();
			
			char [] tokenChar = token.toCharArray();
			StringBuilder tokenOut=new StringBuilder();

			if(!skipTag && tagOffset.containsKey(currentOffset)){
				List<String> tagsList = tagOffset.get(currentOffset);
				for(String tag: tagsList ){
					if(tag.matches("<.*>")){
						if(tag.matches("<lettrine>")){
							tokenOut.append(tag);
						}else{
							out.add(addLine(tag, tag ,"tag"));
						}
					}else{
						out.addAll(parseBiblRef(tag));
						//out.add(addLine(tag,"bibl_ref","bibl_ref"));
					}
				}
			}

			//add tag that is within the token
			if(token.length()>0){
				tokenOut.append(tokenChar[0]);
				for(int i= 1 ;i < token.length() ; i++){
					if(tagOffset.containsKey(currentOffset+i)){
						List<String> tagsList = tagOffset.get(currentOffset+i);
						for(String tag: tagsList ){
							tokenOut.append(tag);
						}
					}
					tokenOut.append(tokenChar[i]);
				}
				currentOffset += token.length();
				out.add(addLine(tokenOut.toString(),"",""));
				skipTag=false;
			}else{
				skipTag = true;
			}	
		}
		//if there is tags at the end of paragaraph add them
		if( tagOffset.containsKey(currentOffset)){
			List<String> tagsList = tagOffset.get(currentOffset);
			for(String tag: tagsList ){
				if(tag.matches("<.*>")){

					out.add(addLine(tag, tag ,"tag"));
				}else{

					out.addAll(parseBiblRef(tag));
					//out.add(addLine(tag,"bibl_ref","bibl_ref"));
				}
			}

		}


		return out;

	}
	private OutputLine addLine(String w,String l,String t){
		OutputLine tmpLine=new OutputLine(w,l,t);
		return tmpLine;

	}
	public static String [] splitRefTag(String s){
		String [] tmp = s.split("((?<=>)|(?=<))");
		return tmp;

	}
	public static String [] splitRefText(String s){
		String [] tmp = s.split("((?<=\\s)|(?=\\s))");
		return tmp;

	}



	public void saveOutput(File dir, String fileName, LinkedList<OutputLine> output ){
		
		try{
			File fout = new File(dir, fileName);

			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for(OutputLine s: output){
				bw.write(s.getWord()+"\t"+s.getLemma()  +"\t"+s.getTag());
				bw.newLine();
			}

			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private void saveOutput(String filePath){
		
		try{
			File fout = new File(filePath);

			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for(OutputLine s: this.resultList){
				bw.write(s.getWord()+"\t"+s.getLemma()  +"\t"+s.getTag());
				bw.newLine();
			}

			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void saveOutput(String dirName, String fileName, LinkedList<OutputLine> output){

		File outDir = new File(dirName);

		if(!outDir.isDirectory()){

			outDir.mkdirs();

		}
		saveOutput(outDir,fileName,output);

	}





	public static void main(String[] args) {


		CreateLGeRMInput createLGeRMInput = new CreateLGeRMInput("src/main/resources/lex.csv", 5);
		String resultsDir = args[0];
		JFileChooser window= new JFileChooser();
		int rv= window.showOpenDialog(null);

		if(rv == JFileChooser.APPROVE_OPTION){
			///resultXML.buildTranscriptionXML(window.getSelectedFile());  
			String fileName=window.getSelectedFile().getName();
			String [] fileNameParts= fileName.split("_");
			createLGeRMInput.buildLGeRMOutputListOffset(window.getSelectedFile());  
			//createLGeRMInput.printListConsole();
			createLGeRMInput.saveOutput(resultsDir+"\\"+new StringJoiner("_").add(fileNameParts[0]).add(fileNameParts[1])+".out");
			//createLGeRMInput.saveOutput(resultsDir+"\\"+new StringJoiner("_").add(fileNameParts[0]).add(fileNameParts[1])+".out");
		}


	}

}
