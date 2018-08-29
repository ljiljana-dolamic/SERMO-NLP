package ch.unine.ILCF.SERMO.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author dolamicl
 *
 */

public class CharacterUtils {

	
	
	public static HashMap<String,HashMap<String,HashMap<String,String>>> fullDictionary= new HashMap<String,HashMap<String,HashMap<String,String>>>();
	
     public static HashMap<String,HashMap<String,HashMap<String,String>>> loadDictionary(String fileName){
		
	HashMap<String,HashMap<String,HashMap<String,String>>> postProcDict = new HashMap<String,HashMap<String,HashMap<String,String>>>();
		
		File dictFile = new File(fileName);
		 try{
	    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dictFile.getAbsolutePath()));
	    	 BufferedReader br = new BufferedReader(isr);
	    	 String line=null;
	         while ( (line = br.readLine()) != null)
	         {
	        	
	        	 String[] in = line.split("\\t");
	        	 String token = in[0];
	        	 String start = Character.toString(token.charAt(0));
	        	 HashMap<String,HashMap<String,String>> tmpDico;
	        	 if(postProcDict.containsKey(start)){
	        		 tmpDico=postProcDict.get(start);
	        	 }else{
	        		 tmpDico = new HashMap<String,HashMap<String,String>>();
	        		 
	        	 }
	        	 
	        	 HashMap<String,String> tmp;
	        	 if(tmpDico.containsKey(token)){
	        		 tmp = tmpDico.get(token);
	        	 }else{
	        		 tmp = new HashMap<String,String>();
	        	 }
	        	 for(int i = 1; i < in.length; i++){
	        	   String [] inParts=in[i].split("\\s");
	        	  
	        	   	 tmp.put(inParts[0],inParts[1]);	
	        	 }
	        	 tmpDico.put(token, tmp);
	        	 postProcDict.put(start, tmpDico);
	    	 }
		 }catch(Exception e){
			 
			 e.printStackTrace();
			 
		 }
		
		return postProcDict;
		
	}


public static String escapeUnicode(String input) {
  StringBuilder b = new StringBuilder(input.length());
	  Formatter f = new Formatter(b);
	  for (char c : input.toCharArray()) {
	    if (c < 128) {
	      b.append(c);
	    } else {
	      f.format("\\u%04x", (int) c);
	      f.close();
	    }
	    }
	  return b.toString();
	}

/**
 * 
 * normalize characters with tilde
 */
 static Map <String,String> tildeChV;
 	static{
 		tildeChV = new HashMap<String,String>();
 		tildeChV.put("\\u00e3","a");//ã
 		tildeChV.put("\\u00f5","o");//
 		tildeChV.put("\\u1ebd","e");
 		tildeChV.put("\\u0169","u");
 		tildeChV.put("\\u0129","i");
 		
 		
 	}
 	static Map <String,String> tildeChC;
 	static{
 		tildeChC = new HashMap<String,String>();
 		tildeChC.put("ñ","nn");//
 		tildeChC.put("m͂","mm");//
 		tildeChC.put("s͂","ss");
 		tildeChC.put("ß","ss");
 		tildeChC.put("œ","oe");
 		tildeChC.put("æ","ae");
 		tildeChC.put("q̃","que");
 		tildeChC.put("q͂","que");
 		tildeChC.put("e´","é");
 		tildeChC.put("Æ","AE");
 		tildeChC.put("Œ","OE");
 		tildeChC.put("E´","É");
 	}
   static List <String> labialCons;
   static{
	   labialCons= new ArrayList<String>();
	   labialCons.add("m");
	   labialCons.add("p");
	   labialCons.add("b");
	   
   } 
   /*
    * perform all normalizations of the text
    * 
    */
   
public static String normalize(String text){
	
	return normalizeTilde(normalize9(text.toLowerCase()));
} 


public static String normalize9(String text){
	
//replace exponent 9 by "us" (no9 -> nous)
	if(text.matches("\\w+9.*")){
		text=text.replaceAll("9", "us");
	}
	
	
       return text;
}

public static String normalizeTilde(String text){
	String start = Character.toString(text.charAt(0)).toLowerCase();
	HashMap<String,HashMap<String,String>> tmpDico ;
	if(fullDictionary.containsKey(start)){
		 	tmpDico=fullDictionary.get(start);
	 	}else{
	 		tmpDico = new HashMap<String,HashMap<String,String>>();
		 
	 	}
	for(Entry<String,String> entryC : tildeChC.entrySet()){ //normalize cons
		
		text=text.replaceAll(entryC.getKey(), entryC.getValue());
		

	}
     
	for(Entry<String,String> entry : tildeChV.entrySet()){ //normalize vowels
		String key=entry.getKey();
		

		Pattern tildePattern = Pattern.compile("^(.*)?("+key+")(.)(.*)");
		
		Matcher tildeMatcher =tildePattern.matcher(text);
		while(tildeMatcher.matches()){
			
			String follow = tildeMatcher.group(3);
			
			
			if(labialCons.contains(follow)){
				//text = text.replace(entry.getKey(),entry.getValue()+follow );
				text = tildeMatcher.group(1)+entry.getValue()+"m"+tildeMatcher.group(3)+tildeMatcher.group(4);
			}else{
				String mn =tildeMatcher.group(1)+entry.getValue()+"m"+tildeMatcher.group(3)+tildeMatcher.group(4);
				if(tmpDico.containsKey(mn)){
					text=mn;
				}else{
					text = tildeMatcher.group(1)+entry.getValue()+"n"+tildeMatcher.group(3)+tildeMatcher.group(4);
				}
				
				
			}
			tildeMatcher =tildePattern.matcher(text);
		}

		

	}
	for(Entry<String,String> entry : tildeChV.entrySet()){ //normalize vowels
		String key=entry.getKey();
		
		if(text.matches(".*"+key)){
			//	System.out.println("ends: "+text);
				String tmpN = text.replaceAll(key,entry.getValue()+"n" );
				
				String tmpM = text.replaceAll(key,entry.getValue()+"m" );
				if(tmpDico.containsKey(tmpN.toLowerCase())){
					text = tmpN;
				}else {
					text = tmpM;
				}
			}
	}
	
	//fix r'
	if(text.matches("r'.*")){
		
		text=text.replace("r'", "re");
	}
	
	
	//replace mt-> ment; dt->dent, mnt->ment; nnt-> nent
	  if(!tmpDico.containsKey(text)){
	    String candidate = text;
		if(text.endsWith("nnt")){
			candidate = text.replaceAll("t$", "ent");
			
		}else if(text.endsWith("mnt")){
			candidate = text.replaceAll("nt$", "ent");
			
		}else if(text.endsWith("dt")|| text.endsWith("mt")){
			candidate = text.replaceAll("t$", "ent");
			
		}else if(text.endsWith("et")){
			candidate = text.replaceAll("t$", "nt");
		}
		if(tmpDico.containsKey(candidate)){
			text=candidate;
		}
	  }
	return text; 
}

public static ArrayList<String> decomposeString(String token){
	
	String start = Character.toString(token.charAt(0));
	HashMap<String,HashMap<String,String>> tmpDico ;
	if(fullDictionary.containsKey(start)){
		 	tmpDico=fullDictionary.get(start);
	 	}else{
	 		tmpDico = new HashMap<String,HashMap<String,String>>();
		 
	 	}
	//System.out.println(token);
	ArrayList<String> components = new ArrayList<String>();
	if(!tmpDico.containsKey(token) && token.length() > 1 && !token.matches(".+\\.")){ // not in dictionary and not an abbr.
	//	System.out.println("not in dict");
		for(int i=1;i < token.length();i++){
			String first = token.substring(0, i);
			String firstL = first.toLowerCase();
			String last = token.substring(i);
			
			String startF = Character.toString(first.charAt(0)).toLowerCase();
			HashMap<String,HashMap<String,String>> tmpDicoFirst ;
			if(fullDictionary.containsKey(startF)){
				tmpDicoFirst=fullDictionary.get(startF);
			 	}else{
			 		tmpDicoFirst = new HashMap<String,HashMap<String,String>>();
				 
			 	}
			
			String startL = Character.toString(last.charAt(0)).toLowerCase();
			HashMap<String,HashMap<String,String>> tmpDicoLast ;
			if(fullDictionary.containsKey(startL)){
				tmpDicoLast=fullDictionary.get(startL);
			 	}else{
			 		tmpDicoLast = new HashMap<String,HashMap<String,String>>();
				 
			 	}
			
			if(last.equals("l")||last.equals("ls")){
				if(first.matches("(p.+)?\\ua757")){
					components.add(first.replaceAll("\\ua757", "qu'"));
				
					if(last.equals("l")){
						components.add("il");
					}else{
						components.add("ils");
					}
				
				}
			}else if(tmpDicoLast.containsKey(last)){
				if((firstL.equals("l")||firstL.equals("d")||firstL.equals("s")||firstL.equals("c"))&& isVowel(last.charAt(0))){
					components.add(first+"'");
					components.add(last);
				}else if(firstL.equals("a")){
					components.add("à");
					components.add(last);
				}else if(firstL.matches("tr[e|é|è]s")||firstL.equals("au")||firstL.equals("pour")||firstL.equals("prou")||firstL.equals("par")){
					components.add(first);
					components.add(last);
			    }else if(last.matches("fo[i|y][s|z]") && tmpDicoFirst.containsKey(firstL)){
			    	components.add(first);
					components.add(last);
			    }
			}
		}
		//System.out.println(components.toString());
	}else{
	//	System.out.println("in dict");
		components.add(token);
	}
	if(components.isEmpty()){
		components.add(token);
	}
	
	return components;
	
}
public static boolean isVowel(char c) {
    /*
     * Check if input alphabet is member of set{a,e,i,o,u} using
     * switch statement
     */
    switch (c) {    
    case 'a':
    case 'e':
    case 'i':
    case 'o':
    case 'u':
        return true;
    default:
        return false;
    }
}
//public static void loadDictionary(String fileName){
//	 File dictFile = new File(fileName);
//	 try{
//    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dictFile.getAbsolutePath()));
//    	 BufferedReader br = new BufferedReader(isr);
//    	 String line=null;
//         while ( (line = br.readLine()) != null)
//         {
//        	 String in = line.split("\\t")[0];
//        	dictionary.add(in.toLowerCase());
//    	 }
//	 }catch(Exception e){
//		 e.printStackTrace();
//	 }
//	
//}


static Map <String,String> nonStdCh;
	static{
		nonStdCh = new HashMap<String,String>();
		nonStdCh.put("\\u02bc","'");
		nonStdCh.put("\\u2019","'");
		nonStdCh.put("\\u00a0"," ");
		nonStdCh.put("\\u201C","\"");
		nonStdCh.put("\\u201D","\"");
		nonStdCh.put("\\u201E","\"");
		nonStdCh.put("\\u201A",",");
		nonStdCh.put("e\\u0303","\u1ebd");
		
	}
	
public static String fixNonStandardCh(String text){
	  for(Entry<String,String> entry : nonStdCh.entrySet()){
       text=text.replaceAll(entry.getKey(), entry.getValue());
       
   }
   return text;
	  
}

// the utils for incesie detection within the sentence text


static LinkedList<String> insRegExp;

static{
	insRegExp=new LinkedList<String>();
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]"); //79
	
	
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");//130
	
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");//131
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");//133
	
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)"); //133
	
	
	///magda 29.05
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
	
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	
	
	
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
	
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_\\)");
	
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
	
	
	

	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_\\)");
	
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
	
	
	
	//start of the sentence
	
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]"); //79
	
	
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");//130
	
	
	
	
	///magda 29.05
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
	
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
	
	
	
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
	
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
	
	
	
	

}


/**Takes sentence in the format ...pos_lemma pos_lemma... to check weather it has an incise
 * 
 * 
 * @param sentence
 * @return
 */
public static  int hasIncise(String sentence){
	int has_it=0;
	//System.out.println(tmpSent);	 
	 for(String exp: insRegExp){
		// System.out.println(exp);
		 Pattern expPattern = Pattern.compile(exp);
		 Matcher expMacher = expPattern.matcher(sentence);
			if(expMacher.find()){

				has_it=1;
				
				//System.out.println("Matched:"+expMacher.group(0));
				
			}

		 
	 }
	
	
	return has_it;
	
}
}