package ch.unine.ILCF.SERMO.PRESTO;


/**
 * @author dolamicl
 *
 */

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.*;
import java.io.IOException;

public class Tokenizer {
	
	private static boolean buildTT=true;
	// private boolean config_verbose = false;
	 //private String config_fileDico = ""; //dictionary file name
	 private int config_window;  // Fenêtre de tokens à considérer si on utilise un dictionnaire pour fusionner les tokens (option -d)
	// private int config_nbTokens = 0;  // Afficher le nb de tokens, suivi d'une tabulation, suivi de la liste des tokens séparés par des tabulations
	 private boolean config_unk = false;
	 private String config_ocr = "";
	// private String config_pass1 = "";//pass1 file name
	// private int config_fallbackRmhyphens = 0;  // Si token non trouvé, regarder si il s'agit d'un mot contenant un tiret de fin de ligne (hyphen). Req pass1.
	 //private String config_fallbackSuperseg = "";  // Si token non trouvé, regarder si on peut le sur-segmenter. Rq un argument (fichier: liste des préfixes). Req pass1.
	 //private String config_fallbackSupersegSufix = "";  // Si token non trouvé, regarder si on peut le sur-segmenter. Rq un argument (fichier: liste des sufixes). Req pass1.
	 private boolean config_fallbackSupersegNP = false;  // Si token non trouvé, regarder si on peut le sur-segmenter et trouver un nom propre. Req pass1.
	// private int config_fallbackMergeseg = 0; //Si token non trouvé, regarder si on peut le fusionner avec ses voisins. Req pass1 et fallbackRmhyphens.
	 private boolean config_fallbackMergeseg = false;
	 //private String config_fallbackRmhyphens = "";
	 private boolean config_fallbackRmhyphens = false;
	 private boolean config_fallbackSuperseg = false;
	 private boolean config_fallbackSupersegSufix = false;
	 
	 private LinkedHashMap<String, String> dico ;
	 private LinkedHashMap<String, Integer> pass1;
	 private LinkedHashMap<String, Integer> unk = new LinkedHashMap<String, Integer>() ;
	 private List<String> prefixes;	
	 private List<String> sufixes;
	 
	 boolean config_fileDico	=	true; //using dictionary by default
	 
	 public Tokenizer(String config_fileDico){
    	 
    	 this(config_fileDico,10); // window of 10 by default
    	 
     }
     public Tokenizer(String config_fileDico, int window){
    	 File dictionary = new File(config_fileDico);
    	 this.dico = loadDictionary(dictionary);
    	 this.config_window=window;
    	 if(buildTT){
        	 buildTTdict(this.dico,dictionary.getParentFile());
        	 buildSermoLex(this.dico,dictionary.getParentFile());
         }
    	 
     }
     public Tokenizer(String config_fileDico, int window, String config_pass1, 
    		 String config_fallbackSuperseg, String config_fallbackSupersegSufix, 
    		 String  config_ocr){
    	 
    	 File dictionary = new File(config_fileDico);
    	 File dict_pass1 = new File(config_pass1);
    	 File fallbackSuperseg = new File(config_fallbackSuperseg);
    	 File fallbackSupersegSufix = new File(config_fallbackSupersegSufix);
    	 
    	 this.dico 			=	loadDictionary(dictionary);
    	 this.config_window	=	window;
    	 this.pass1			= 	loadPass1(dict_pass1);
    	 this.prefixes		=	loadPrefixes(fallbackSuperseg);
    	 this.sufixes 		=	loadSufixes(fallbackSupersegSufix);
    	 this.config_ocr	= config_ocr;
    	 this.config_fallbackSuperseg=true;
     }
     
  // Chargement du lexique

     private static LinkedHashMap<String, String> loadDictionary(File dictionary){
    	 //System.out.println("Loading dictionary...");
    	 LinkedHashMap<String, String> tmpDico = new LinkedHashMap<String, String>();
    	 int nbOfTokens = 0;
    	 try{
    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dictionary.getAbsolutePath()));
    	 BufferedReader br = new BufferedReader(isr);
    	 String line=null;
         while ( (line = br.readLine()) != null)
         {
        	// System.out.println(line);
        	 Pattern pattern= Pattern.compile("^(.+?)\\t(.+?)\\t(.+?)(?:$|\\t|\\n)");
        	// Pattern pattern= Pattern.compile("^(.+?)\\t(.+?)\\t(.+?)");
        	 
    	     Matcher matcher = pattern.matcher(line);
    	    if(matcher.find()){
    	    	// System.out.println(matcher.group(1));
    	    	// System.out.println(matcher.group(2));
    	    	// System.out.println(matcher.group(3));
    	     
    	    String form=matcher.group(1); 
    	    
    	   // if(form.contains("'")||form.contains("-")||form.contains("_")||form.contains("°")||form.contains(".")){
        	//	 System.out.println(line);
        	//	 }
    	  //  System.out.println(form);
    	    String POSlemma=matcher.group(2)+" "+matcher.group(3);
    	    //String lemma=matcher.group(3);
    	    if(!POSlemma.contains("S+Di")&&!POSlemma.contains("S+Dp")&&!POSlemma.contains("S+Dn")){//presto TT problem with S+
    	    if(tmpDico.containsKey(form)){
    	    	if(!tmpDico.get(form).contains(POSlemma)){// avoid duplicates
    	    		StringBuilder  tmpPos = new StringBuilder();
    	    		tmpPos.append(tmpDico.get(form)).append("\t").append(POSlemma);
    	    		tmpDico.put(form, tmpPos.toString());
    	    	}
    	    }else{
    	    	tmpDico.put(form, POSlemma);
    	    }
    	    }
    	    nbOfTokens++; 
    	    }  
         }
       //  System.out.println("Total number of token in dictionary: "+ nbOfTokens);//transform thin into logger
         br.close();
         
        
    	 }catch(IOException e){
    		 System.out.println("Problem loading dictionary");
    		 e.printStackTrace();
    	 }
    	 return tmpDico;
     }
     
     private static void buildTTdict(LinkedHashMap<String, String> dico, File outDir){
    	try{
    	 File fout = new File(outDir,"TTdicoLatest.csv");
 		
 		FileOutputStream fos = new FileOutputStream(fout);
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
    	 for(String key : dico.keySet()){
    		 bw.write(key+"\t"+dico.get(key));
    		 bw.newLine();
    	 }
    	 bw.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
     }
	 
     private static void buildSermoLex(LinkedHashMap<String, String> dico, File outDir){
     	try{
     	 File fout = new File(outDir,"lexSermoLatest.csv");
  		
  		FileOutputStream fos = new FileOutputStream(fout);
  		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
     	 for(String key : dico.keySet()){
     		 if(key.contains("'")||key.contains("-")||key.contains("_")||key.contains("°")||key.contains(".")){
     		 bw.write(key+"\t"+dico.get(key).replaceAll(" ", "@"));
     		 bw.newLine();
     		 }
     	 }
     	 bw.close();
     	}catch(Exception e){
     		e.printStackTrace();
     	}
      }
 	 
     private static LinkedHashMap<String, Integer> loadPass1(File dicoPass1){
    	 System.out.println("Loading pass 1 ...");
    	 
    	 LinkedHashMap<String, Integer> tmpPass1 = new LinkedHashMap<String, Integer>();
    	 
    	 try{
    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dicoPass1.getAbsolutePath()));
    	 BufferedReader br = new BufferedReader(isr);
    	 String line=null;
         while ( (line = br.readLine()) != null)
         {
        	 Pattern pattern= Pattern.compile("([0-9]+)\\s+(.+?)\\t");
    	     Matcher matcher = pattern.matcher(line);
    	     String key=matcher.group(1);  
    	     String value=matcher.group(2);
    	     tmpPass1.put(key, Integer.parseInt(value));
    	     
         }
         System.out.println("Finished pass 1! ");//transform thin into logger
         br.close();
    	 }catch(IOException e){
    		 System.out.println("Problem loading pass1");
    		 e.printStackTrace();
    	 }
    	 return tmpPass1;
     }

     private static LinkedList<String> loadPrefixes(File prefixFile){ 
    	 System.out.println("Loading prefixes....");
    	 LinkedList<String> tmpPrefixes=new LinkedList<String>(); 
    	 try{
    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(prefixFile.getAbsolutePath()));
    	 BufferedReader br = new BufferedReader(isr);
    	 String line=null;
    	 Pattern pattern1= Pattern.compile("^\\s*#");
    	 Pattern pattern2= Pattern.compile("^\\s*$");
    	 while ( (line = br.readLine()) != null)
    	 {
    		 line=line.trim();
    		 Matcher matcher1 = pattern1.matcher(line);
    		 Matcher matcher2 = pattern2.matcher(line);
    		 if(!matcher1.matches() && !matcher2.matches()) {
    			 tmpPrefixes.add(line);
    		 }
    	 }
    	 
    	 System.out.println("Prefixes loaded!");
    	 br.close();
     }catch(IOException e){
		 System.out.println("Problem loading prefixes");
		 e.printStackTrace();
	 }
    	
    	 return tmpPrefixes;
	 }
     
     private LinkedList<String> loadSufixes(File sufixFile){ 
    	 System.out.println("Loading sufixes....");
    	 LinkedList<String> tmpSufixes=new LinkedList<String>(); 
    	 try{
    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(sufixFile.getAbsolutePath()));
    	 BufferedReader br = new BufferedReader(isr);
    	 String line=null;
    	 Pattern pattern1= Pattern.compile("^\\s*#");
    	 Pattern pattern2= Pattern.compile("^\\s*$");
    	 while ( (line = br.readLine()) != null)
    	 {
    		 line=line.trim();
    		 Matcher matcher1 = pattern1.matcher(line);
    		 Matcher matcher2 = pattern2.matcher(line);
    		 if(!matcher1.matches() && !matcher2.matches()) {
    			 tmpSufixes.add(line);
    		 }
    	 }
    	 System.out.println("sufixes loaded!");
    	 br.close();
    	 }catch(IOException e){
    		 System.out.println("Problem loading sufixes");
    		 e.printStackTrace();
    	 }
    	 this.config_fallbackSupersegSufix=true;
    	 return tmpSufixes;
	 }
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
     public LinkedList<String> tokenize(String text){
    	 LinkedList<String> tokens=new LinkedList<String>();
    	
    	 String stream = fixNonStandardCh(text);
    	// System.out.println("Stream 1: "+stream);
    	
    	 //  stream	=	stream.replaceAll("(?s)([ ,;:\\.'\\?!()'\"\\-«»\\(\\)\\[\\]])", "\n$1\n");
    	   stream	=	stream.replaceAll("(?s)([ ,;:\\.'\\?!()'\"\\-«»\\(\\)\\[\\]\\t&_#@*\\*†‡])", "\n$1\n"); // Ljiljana: keeping also tab and treating the & as separator, problem like &pour 
    	   
    	 //  ***** Ljiljana small lettre followed by capital : ex. bAmos****
    	   stream	=	stream.replaceAll("(?s)([a-zàâäæéèêëîïôöœùûüÿç])([A-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ])", "$1\n$2"); // 
    	   stream	=	stream.replaceAll("(?s)(\\d+)\\n(\\.)", "$1$2"); //ordinal numbers
    	   //System.out.println(stream);
    	   stream	=	stream.replaceAll("(?s)(\\n[mMcCdDlLxXvViIlJ]+)\\n(\\.)", "$1$2");//roman digits
    	   //System.out.println(stream);
    	   stream	=	stream.replaceAll("(?s)(\\d+)([a-zàâäæéèêëîïôöœùûüÿçA-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ])", "$1\n$2"); // Ljiljana: separating the digit from lettre 
    	   stream	=	stream.replaceAll("(?s)([a-zàâäæéèêëîïôöœùûüÿçA-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ])(\\d+)", "$1\n$2"); // Ljiljana: separating the lettre from digit 
    	   
    	   //$stream=~s/([ ,;:\.'\?!()'"\-«»\(\)\[\]])/\n$1\n/gs;  # Séparateurs qu'on conserve
    	   stream	=	stream.replaceAll("(?m)^((「[^」]+?」\\s*)+)", "\n$1\n");
    	   //$stream=~s/^((「[^」]+?」\s*)+)/\n$1\n/gm;  # Décaller les balises en début de token
    	   stream	=	stream.replaceAll("(?m)((「[^」]+?」\\s*)+)$", "\n$1\n");
    	  // $stream=~s/((「[^」]+?」\s*)+)$/\n$1\n/gm;  # Décaller les balises en fin de token
    	   stream	=	stream.replaceAll("(?m) +", " ");
    	   //$stream=~s/ +/ /gs;  # Supprimer espaces doubles
    	   stream	=	stream.replaceAll("(?s)\\n+", "\n");
    	  // $stream=~s/\n+/\n/gs;  # Supprimer lignes vides
    	//   System.out.println("Stream 2: "+stream);
    	   // Nettoyage "lourd" des erreurs d'OCR
    	   //Ljiljana: nettoyage omit 
//    	   if(this.config_ocr.matches("Rmhyphens1")) {
//    		   stream	=	stream.replaceAll("(?i)\\s+-\\s+[*1;,]\\s+", ""); 
//    	     //$stream=~s/\s+-\s+[*1;,]\s+//gi;
//    		   stream	=	stream.replaceAll("(?i)([a-zàâäæéèêëîïôöœùûüÿç])-[1*;,�'\"](\\s)", "$1$2");
//    	    // $stream=~s/([a-zàâäæéèêëîïôöœùûüÿç])-[1*;,�'"](\s)/$1$2/gi;
//    		   stream	=	stream.replaceAll("(?i)([a-zàâäæéèêëîïôöœùûüÿç])[1*�•](\\s)", "$1$2");
//    	    // $stream=~s/([a-zàâäæéèêëîïôöœùûüÿç])[1*�•](\s)/$1$2/gi;
//    		   stream	=	 stream.replaceAll("<\\/p>\\s*<p[^>]*>\\s*[^A-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ]", "");
//    	    // $stream=~s/<\/p>\s*<p[^>]*>\s*[^A-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ]//g;  # Un § qui ne commence pas par une majuscule ? Impossible !
//    	   }
    	  // Pattern patternRN = Pattern.compile("(?es)<(.+?)>");
    	   Pattern patternRN = Pattern.compile("(?s)<(.+?)>");
    	   Matcher matcherRN = patternRN.matcher(stream);
    	   StringBuffer sb = new StringBuffer(); 
    	   while(matcherRN.find()){
    		   String grFound= matcherRN.group(1);
    		   grFound = removeN(grFound);
    		  matcherRN.appendReplacement(sb,"<"+ grFound +">");
    	   }
    	   matcherRN.appendTail(sb);
    	   stream=sb.toString();
    	   //stream	=	stream.replaceAll("(?es)<(.+?)>", "<"+removeN($1)+">");
    	  // $stream=~s/<(.+?)>/'<'.rmN($1).'>'/egs;  # Recoller le contenu des balises
    	//   System.out.println("Stream : "+stream);
    	  String [] tokensArray= stream.split("\\n");
    	  
    	  for(String as:tokensArray){
    		   tokens.add(as);
    	  }
    	//  System.out.println("NO tokens 1: "+ tokens.size());
    	   // Resoudage ciblé des tokens en fonction du lexique  
    	  // # Attention, un espace = un token
    	   if(this.config_fileDico) {
    	     int nbFusions = 0;
    	     List<String> newTokens = new LinkedList<String>();
    	     for(int i=0; i < tokens.size(); ++i) {  //# Pour chaque token
    	       for( int j = this.config_window; j>0; --j) {  // Taille de la fenêtre à tester
    	         String concat = tokens.get(i);
    	         for( int k=1; k < j; ++k) {
    	           if((i + k) < tokens.size()) {
    	             concat += tokens.get(i + k);
    	           }
    	         }  
    	         String concatNorm = concat; 
    	         concatNorm = concatNorm.replaceAll("「[^」]+?」", "");
    	         if(j==3 && concat.matches("r'.+")){
    	        	 concatNorm="r'plus"; 
    	         }
//    	         if(j==2 && concat.matches("\\w+9")){
//    	        	 concatNorm="plus9"; 
//    	         }
    	        // $concatNorm=~s/「[^」]+?」//g; 
    	       //  concatNorm = concatNorm.replaceAll("\\*", "");
    	        //	$concatNorm=~s/\*//g;    -- OCR not corrected
    	        // # Si token dans le dico ou bien impossible d'y trouver un token plus petit (j==1)
    	         if( j==1 || dico.containsKey(concat) || dico.containsKey(concat.toLowerCase()) || dico.containsKey(concatNorm.toLowerCase()) ) {  
    	         //# Pour les tokens qui ne sont pas des espaces ni des balises ni des nombres et ne sont pas dans le dictionnaire
    	           if( config_unk && !concat.matches("^\\s+$") && !concat.matches("「") && !concat.matches("^<") && 
    	        		   !(dico.containsKey(concat) || dico.containsKey(concat.toLowerCase()) || dico.containsKey(concatNorm.toLowerCase())) ) {  
    	                       
    	             //# Erreurs d'OCR systématiques
    	             if(config_ocr.matches("ies_les") && concat.equals("ies")) {
    	               concat = "les\tCORRIGÉ";
    	             }
    	             else if(config_ocr.matches("ee_de") && concat.equals("ee")) {
    	               concat = "de\tCORRIGÉ";
    	             }
    	             
    	             //# Bien formé
    	             else if( concat.matches("^[A-ZÀÂÄÆÉÈÊËÎÏÔÖŒÙÛÜŸÇ]*[a-zàâäæéèêëîïôöœùûüÿç]*$") && !concat.matches("(.)\\1\\1") && !concat.matches("^(.)\\1")) {  
    	            	 // # Un mot inconnu, mais bien formé et qu'on retrouve plusieurs fois            
    	               int threshold = 1;
    	               int unkTmp = unk.get(concat);
    	               unk.replace(concat, unkTmp++);
    	               if(unkTmp > threshold || (pass1.containsKey(concat) && pass1.get(concat) > threshold)) { 
    	                 concat += "\tUNK_-1";
    	               }
    	               else {
    	                 concat += "\tUNK_0";
    	               }
    	             }
    	             
    	            // # Trois fois la même lettre... essayer avec deux
    	             else if(concat.matches("(.*)(.)\\2\\2(.*)") && dico.containsKey(concat.replaceAll("(.*)(.)\\2\\2(.*)","$1$2$2$3")) || 
    	        		   dico.containsKey((concat.replaceAll("(.*)(.)\2\2(.*)","$1$2$2$3").toLowerCase()))) {
    	               concat = concat.replaceAll("(.*)(.)\\2\\2(.*)","$1$2$2$3") + "\tCORRIGÉ";
    	             }
    	             
    	            // # Mal formé, introuvable
    	             else {  
    	               concat += "\tUNK_1";
    	             }
    	             
    	           }
    	         newTokens.add(concat);
    	        
    	          // push(@newTokens, $concat);
    	           if(j>1) {
    	             ++nbFusions;
    	           }
    	           i+=(j-1);
    	           break;
    	         }
    	       }
     		}
    	     tokens.clear();
    	     tokens.addAll(newTokens);  // # Tokens de la ligne
    	    
    	 }
             
    	//   System.out.println("NO tokens 2: "+ tokens.size());


    	//   # Essayer de recoller les tokens séparés par un hyphen
//    	   # Exemple:
//    	   #  assu	UNK_-1
//    	   #  -
//    	   #
//    	   #  jettissait	UNK_0
  	   if(this.config_fallbackRmhyphens) {
  		    HashMap<Character,Character> seps = new HashMap<Character,Character>();
  		    seps.put('-', null);
  		    
    	     if(config_ocr.matches("Rmhyphens1")) {
    	     char [] emptySepsTmp= {'\'', '"', '.', '�', '«', '»'};
    	     for (char c: emptySepsTmp){
    	    	 seps.put(c, null);
    	     }
    	     seps.put('Ç', 'C');
    	     seps.put('!','l' );
    	     seps.put( '\'', 'l');
    	     seps.put('"', 'u');
    	     seps.put('"', 'c');
    	     }
    	     if(config_fallbackMergeseg) {
    	    	
    	       seps.put(' ',null);
    	     }
    	     
    	     for(Character sep : seps.keySet()) {	      
    	       char rep = seps.get(sep);
    	       int tokens_size=tokens.size();
    	       for(int i=1; i < tokens_size; ++i) {
    	    	   String token_i = tokens.get(i);
    	    	   String token_i_m = tokens.get(i-1);
    	    	   String token_i_p = tokens.get(i+1);
    	    	  
    	         if(token_i == sep.toString() || (token_i = token_i.replaceAll("(?r)\\t.*", "")) == sep.toString()) {
    	        		 //# Cas 1: sans espace après l'hyphen
    	           if((i+1) < tokens_size && !token_i_m.matches("^\\s*$") && 
    	        		   !token_i_p.matches("^\\s*$") && (token_i_m.matches("\\tUNK_") || token_i_p.matches("\\tUNK_")) ) { 
    	             String tokA = tokens.get(i-1);//=~s/\t.*//r;
    	             tokA = tokA.replaceAll("(?r)\\t.*", "");	
    	             String tokB = tokens.get(i+1);//=~s/\t.*//r;
    	             tokB = tokB.replaceAll("(?r)\\t.*", "");	
    	             String conc = tokA+rep+tokB;
    	             if(dico.containsKey(conc) || dico.containsKey(conc.toLowerCase())) { // # Si la concaténation donne un mot connu
    	            	 tokens.set(i-1, null);
    	                 tokens.set(i, conc+"\tCORRIGÉ");  //# Remplacer l'hyphen par la concaténation des deux tokens
    	                 tokens.set(i+1, null);
    	             }
    	           }
    	           else if((i+2) < tokens_size && !token_i_m.matches("^\\s*$") && 
    	        		   !token_i_p.matches("^\\s*$") && !tokens.get(i+2).matches("^\\s*$") 
    	        		   && (token_i_m.matches("\\tUNK_") || tokens.get(i+2).matches("\\tUNK_")) ) { // # Cas 2: avec espace après l'hyphen
    	            
    	        	   	String tokA = tokens.get(i-1);//=~s/\t.*//r;
    	        	   	tokA = tokA.replaceAll("(?r)\\t.*", "");
    	        	   	String tokB = tokens.get(i+2);//=~s/\t.*//r;
    	        	   	tokB = tokB.replaceAll("(?r)\\t.*", "");
    	        	   	String conc = tokA+rep+tokB;
    	            
    	             if(dico.containsKey(conc) || dico.containsKey(conc.toLowerCase())) {  //# Si la concaténation donne un mot connu
    	            	 tokens.set(i-1, null);
    	                 tokens.set(i, conc+"\tCORRIGÉ");  //# Remplacer l'hyphen par la concaténation des deux tokens
    	                 tokens.set(i+2, null);
    	               
    	             }
    	           }
    	         }
    	       }
    	     }
    	   }

    	   
    	  // # Essayer de sursegmenter les tokens inconnus (préfixes)
    	   if(this.config_fallbackSuperseg) {
    		   int tokens_size=tokens.size();  
    	     for( String prefix : this.prefixes) {  //# Pour chaque préfixe
    	       for(int i=0;  i< tokens_size; ++i) {  //# Pour chaque token
    	         String token = tokens.get(i);//=~s/\t.*//r;
    	         token = token.replaceAll("(?r)\\t.*", "");
    	         Pattern pattern= Pattern.compile("(?i)^($prefix)(.+)$");
        	     Matcher matcher = pattern.matcher(token);
        	    
    	         if(tokens.get(i).matches("\\tUNK_") && matcher.matches()) { // # Token inconnu et commence par un préfixe
    	           String tokenA = matcher.group(1);;
    	           String tokenB = matcher.group(2);;
    	           if(dico.containsKey(tokenB) || dico.containsKey(tokenB.toLowerCase())) {
    	             tokens.set(i, tokenA + "\tCORRIGÉ\n" + tokenB + "\tCORRIGÉ"); // # ATTENTION, ça ne marche que parce que on fait un join sur @tokens juste après !
    	           }
    	         }
    	       }
    	     }
    	   } 
    	   
    	  // # Essayer de sursegmenter les tokens inconnus (suffixes)
    	   if(this.config_fallbackSupersegSufix) {
    		   int tokens_size=tokens.size();
    	     for(String sufix : this.sufixes) {  //# Pour chaque préfixe
    	       for(int i=0; i< tokens_size; ++i) { // # Pour chaque token
    	         String token = tokens.get(i);//=~s/\t.*//r;
    	         token = token.replaceAll("(?r)\\t.*", "");
    	         Pattern pattern= Pattern.compile("(?i)^(.+)($sufix)$");
        	     Matcher matcher = pattern.matcher(token);
    	         if(tokens.get(i).matches("\\tUNK_") && matcher.matches()) { // # Token inconnu et commence par un préfixe
    	        	String tokenA = matcher.group(1);
      	           	String tokenB = matcher.group(2);
      	           if(dico.containsKey(tokenB) || dico.containsKey(tokenB.toLowerCase())) {
      	             tokens.set(i, tokenA + "\tCORRIGÉ\n" + tokenB + "\tCORRIGÉ"); // # ATTENTION, ça ne marche que parce que on fait un join sur @tokens juste après !
      	           }
    	         }
    	       }
    	     }
    	   } 
    	   
    	  // # Essayer de sursegmenter les tokens inconnus (NP)
    	   if(this.config_fallbackSupersegNP) { 
    		   int tokens_size=tokens.size();
    	     for(int i=0; i< tokens_size; ++i) { // # Pour chaque token
    	    	 String token = tokens.get(i);//=~s/\t.*//r;
    	         token = token.replaceAll("(?r)\\t.*", "");
    	         Pattern pattern= Pattern.compile("^([A-Za-z]+)([A-Z][a-z]+)$");
        	     Matcher matcher = pattern.matcher(token);
    	       if(tokens.get(i).matches("\\tUNK_") && matcher.matches()) { // # xxxxxYyyyy
    	    	   String tokenA = matcher.group(1);
     	           String tokenB = matcher.group(2);
    	         if((dico.containsKey(tokenA) || dico.containsKey(tokenA.toLowerCase())) && (dico.containsKey(tokenB) || dico.containsKey(tokenB.toLowerCase()))) {
    	        	 tokens.set(i, tokenA + "\tCORRIGÉ\n" + tokenB + "\tCORRIGÉ");  // # ATTENTION, ça ne marche que parce que on fait un join sur @tokens juste après !
    	         }
    	       }
    	     }
    	   } 

//
//    	   StringBuilder outStream = new StringBuilder();
//    	   for(String s : tokens){
//    		   if(s!= null){
//    			   outStream.append(s).append("\n");
//    		   }
//    	   } 
//    	   stream=outStream.toString();
//    	   stream.replaceAll("(?m)^((「[^」]+?」\\s*)+)", "\n$1\n");
//    	//   $stream=~s/^((「[^」]+?」\s*)+)/\n$1\n/gm;  //# Décaller les balises en début de token
//    	   stream.replaceAll("(?m)((「[^」]+?」\\s*)+)$","\n$1\n");
//    	  // $stream=~s/((「[^」]+?」\s*)+)$/\n$1\n/gm;  //# Décaller les balises en fin de token
//    	   stream.replaceAll("(?s) +", " ");
//    	  // $stream=~s/ +/ /gs; // # Supprimer espaces doubles
//    	   stream.replaceAll("(?s)\\n+","\n");
//    	 //  $stream=~s/\n+/\n/gs; // # Supprimer lignes vides  
//
//    	  stream.trim();
//    	     
//    	   if($config_nbTokens) {
//    	     print split(/\n/, $stream) + 0;
//    	     print "\t" . ($stream=~s/\n/\t/rg);
//    	   }
//    	   else {
//    	     print "$stream";
//    	   }
//    	   print "\n";

    	  
    	   
    	 

    	 
    	 return tokens;
     }
	

	 private String removeN(String str) {
	   String rep = str;
	   rep.replaceAll("\\n","");
	   //$str=~s/\n//gs;
	   return rep;
	 }

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String dictionaryPath=args[0];
		int window = Integer.parseInt(args[1]);
		String stringToTest= args[2];
		
		Tokenizer tokenizer = new Tokenizer(dictionaryPath , window);
		
		LinkedList<String> tokensList = tokenizer.tokenize(stringToTest);
		//System.out.println(tokensList.size());
		for ( String token : tokensList){
			System.out.println(token);
		} 
		
	}

}
