/**
 * 
 */
package ch.unine.ILCF.SERMO.File.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dolamicl
 *
 */
public class ParseDocParts {
	//public static parseBodyPart() {
		
//	}
	public static Map<String,LinkedList<String>> splitSentenceList(LinkedList<String> list){
		
		LinkedList<String> headerPart = new LinkedList<String>();
		LinkedList<String> bodyPart = new LinkedList<String>();
		
		boolean is_header = true;

		for(String sentence:list){
			
			if(is_header){
				headerPart.add(sentence);
			}else{
				if(!sentence.matches(".*\\s")){

					StringBuilder tmp=new StringBuilder();
					tmp.append(sentence).append(" ");
					
					bodyPart.add(tmp.toString());
				}else{
					bodyPart.add(sentence);
				}
			
				
			}
			Pattern p = Pattern.compile(".*<source_num>.*");
			Matcher m = p.matcher(sentence);

			if( m.matches()){
				is_header=false;
			}
		}
		// clean the start of body part
		String start=bodyPart.pop();
		
		while(start.trim().equals("") || start.matches("<par/>")){  // remove leading empty spaces and paragraph starts
			
			start=bodyPart.pop();
		}
		bodyPart.push(start);
		
		Map<String,LinkedList<String>> tmp = new HashMap<String,LinkedList<String>>();
		tmp.put("header", headerPart);
		tmp.put("body", bodyPart);
		//tmp.add(headerPart);
		//tmp.add(bodyPart);
		return tmp;
	}

}
