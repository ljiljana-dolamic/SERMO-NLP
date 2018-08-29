/**
 * 
 */
package ch.unine.ILCF.SERMO.TT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;

import java.util.LinkedList;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import ch.unine.ILCF.SERMO.File.Utils.ReadMSWordFile;
import ch.unine.ILCF.SERMO.PRESTO.Tokenizer;
import ch.unine.ILCF.SERMO.TT.TtOutputLine;

/**
 * @author dolamicl
 *
 */
public class TreeTaggerHandler {
	
	private TreeTaggerWrapper<String> tt;
	public HashMap<String,HashMap<String,HashMap<String,String>>> postProcDict;  // dictionary used for post processing
	
	public TreeTaggerHandler(String treeTaggerHome, String model){
		System.setProperty("treetagger.home", treeTaggerHome);
		tt = new TreeTaggerWrapper<String>();
		try{
			tt.setModel(model);
			//String [] args = {"-sgml","-no-unknown" ,"-token", "-lemma", "-lex", "src/main/resources/TTdico.csv"};
		    //tt.setArguments(args);
			}catch(Exception e){
				e.printStackTrace();
			}
		postProcDict = new HashMap<String,HashMap<String,HashMap<String,String>>>();
		
	}
	public TreeTaggerHandler(String treeTaggerHome, String model,String lex){
		System.setProperty("treetagger.home", treeTaggerHome);
		tt = new TreeTaggerWrapper<String>();
		try{
			tt.setModel(model);
			String [] args = {"-sgml","-no-unknown" ,"-token", "-lemma", "-lex", lex};
		    tt.setArguments(args);
			}catch(Exception e){
				e.printStackTrace();
			}
		
		postProcDict = new HashMap<String,HashMap<String,HashMap<String,String>>>();
	}
	public LinkedList<TtOutputLine> run(final String... aTokens)
	throws IOException, TreeTaggerException
	{
	//	try {
	       final  LinkedList<TtOutputLine> output = new LinkedList<TtOutputLine>();
			
			tt.setHandler(new TokenHandler<String>()
			{
			  //  private String token;
			    
				public void token(String aToken, String aPos, String aLemma)
				{
					    if(aToken.equals(aLemma)){
					    	if(aToken.matches(".+\\.")){
					    		 output.add(new TtOutputLine(aToken , "Xe" , aLemma.replaceAll("\\u008c", "\u0152").toUpperCase()));
					    	}else{
					    		String newLemma = getLEMMA(aToken, aPos);
					    		if(!newLemma.equals("")){
					    			output.add(new TtOutputLine(aToken , aPos , newLemma));
					    		}
					    	}
					    }else if(aLemma.equals("@ord@")){	// unify ordinals
							 output.add(new TtOutputLine(aToken , "Mo" , aLemma));
						}else if(aPos.equals("Mc")){	// unify cardinals
							 output.add(new TtOutputLine(aToken , aPos , "@card@"));
					    }else if(aPos.equals("Mo")){	// unify ordinals
							 output.add(new TtOutputLine(aToken , aPos , "@ord@"));
					    }else{
					    		 output.add(new TtOutputLine(aToken , aPos , aLemma.replaceAll("\\u008c", "\u0152")));
					    	}
					    	
					   
				}
				
//				
			});
			tt.process(aTokens);
						
			return output;
		//}
		
	}
	public LinkedList<TtOutputLine> run(final LinkedList<String> listTokens)
	        throws IOException, TreeTaggerException
			{
		String [] aTokens = listTokens.toArray(new String [listTokens.size()]);
			//	try {
			       final  LinkedList<TtOutputLine> output = new LinkedList<TtOutputLine>();
					
					tt.setHandler(new TokenHandler<String>()
					{
					  //  private String token;
					    
						public void token(String aToken, String aPos, String aLemma)
						{
							
							if(aToken.equals(aLemma)){
								if(aToken.matches(".+\\.")){
						    		 output.add(new TtOutputLine(aToken , "Xa" , aLemma.replaceAll("\\u008c", "\u0152").toUpperCase()));
						    	}else if(aToken.matches("[^\\u0000-\\u024f]*")){
						    		output.add(new TtOutputLine(aToken , "Xe" , aLemma.toUpperCase()));
						    	}else{
						    		String newLemma = getLEMMA(aToken, aPos);
						    		if(!newLemma.equals("")){
						    			output.add(new TtOutputLine(aToken , aPos , newLemma));
						    		}else{
						    			String[] posLemma = getPosLemma(aToken);
						    			if(posLemma!= null){
						    				output.add(new TtOutputLine(aToken , posLemma[0] , posLemma[1]));
						    			}else{
						    				output.add(new TtOutputLine(aToken , aPos , aLemma.replaceAll("\\u008c", "\u0152")));
						    			}
						    		}
						    	}
							}else if(aLemma.equals("@ord@")){	// unify ordinals
								 output.add(new TtOutputLine(aToken , "Mo" , aLemma));
							}else if(aPos.equals("Mc")){	// unify cardinals
								 output.add(new TtOutputLine(aToken , aPos , "@card@"));
						    }else if(aPos.equals("Mo")){	// unify ordinals
								 output.add(new TtOutputLine(aToken , aPos , "@ord@"));
						    }else{
						    	output.add(new TtOutputLine(aToken , aPos , aLemma.replaceAll("\\u008c", "\u0152")));
						    }
						
			               //     output.add(new TtOutputLine(aToken , aPos , aLemma));
						   
						}
						
//						
					});
					tt.process(aTokens);
								
					return output;
				//}
				
			}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	
	public HashMap<String,HashMap<String,HashMap<String,String>>> loadPostDictionary(String fileName){
		
		
		//HashMap<String,HashMap<String,String>> postProcDict = new HashMap<String,HashMap<String,String>>();
		
		File dictFile = new File(fileName);
		 try{
	    	 InputStreamReader isr = new InputStreamReader(new FileInputStream(dictFile.getAbsolutePath()));
	    	 BufferedReader br = new BufferedReader(isr);
	    	 String line=null;
	         while ( (line = br.readLine()) != null)
	         {
	        	// System.out.println(line);
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
	
	/**
	 * 
	 * @param token
	 * @param pos
	 * @return
	 */
	
	public String getLEMMA(String token, String pos){
		String start = Character.toString(token.charAt(0));
		HashMap<String,HashMap<String,String>> tmpDico ;
		if(postProcDict.containsKey(start)){
   		 	tmpDico=postProcDict.get(start);
   	 	}else{
   	 		tmpDico = new HashMap<String,HashMap<String,String>>();
   		 
   	 	}
		if(tmpDico.containsKey(token)){
			if(tmpDico.get(token).containsKey(pos)){
				return tmpDico.get(token).get(pos);
			}else{
				return "";
			}
			
		}else{
			return "";
		}
		
	}
	
	/**
	 * 
	 * @param token
	 * 
	 * @return
	 */
	
	public String[] getPosLemma(String token){
		String start = Character.toString(token.charAt(0));
		HashMap<String,HashMap<String,String>> tmpDico ;
		if(postProcDict.containsKey(start)){
   		 	tmpDico=postProcDict.get(start);
   	 	}else{
   	 		tmpDico = new HashMap<String,HashMap<String,String>>();
   		 
   	 	}
		
		
		if(tmpDico.containsKey(token)){
			StringBuilder pos = new StringBuilder();
			StringBuilder lemma = new StringBuilder();
			Set<String> posKeys= tmpDico.get(token).keySet();
			for(String s : posKeys){
				if(pos.length()!=0){
					pos.append(";");
					lemma.append(";");
				}
				pos.append(s);
				lemma.append(tmpDico.get(token).get(s));
			}
			String [] result = new String[2];
			result[0] = pos.toString();
			result[1] = lemma.toString();
			return result;
		}else{
				return null;
			}
			
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedList<TtOutputLine> result=new LinkedList<TtOutputLine>() ;
		//Tokenizer tokenizer=new Tokenizer("src/main/resources/lex.csv", 5);
		TreeTaggerHandler ttH = new TreeTaggerHandler("C:\\Program Files\\TreeTagger", "C:\\Program Files\\TreeTagger\\lib\\presto.par:UTF8","src/main/resources/sermoTTdico.csv");
		//TreeTaggerHandler ttH = new TreeTaggerHandler("C:\\Program Files\\TreeTagger", "C:\\Program Files\\TreeTagger\\lib\\presto.par:UTF8");
		System.setProperty("treetagger.home", "C:\\Program Files\\TreeTagger");
		
		
		JFileChooser window= new JFileChooser();
		int rv= window.showOpenDialog(null);
		
		if(rv == JFileChooser.APPROVE_OPTION){
			try{
				File file=window.getSelectedFile();
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				LinkedList<String> tokenizedString= new LinkedList<String>(); 
				while ( (line = br.readLine()) != null)
				{
					result.clear();
					tokenizedString.add(line.trim());
					//tokenizedString = tokenizer.tokenize(line);
					//LinkedList<String> tokenizedString2 = new LinkedList<String>();
				//	String [] string2={"A","quoi","peut","estre","rapporté","ce","aui","est","dit","au","4. de l'Ecclesiaste","Malheur","à","celui","qui","est","seul",":"	,"d'","autant","qu'",		
					//"estant","tombé","il","n'","y","aura","personne","d'","autre","pour","le","releuer","."};	
					//for(String s :string2){
					//	tokenizedString2.add(s);
					//}
					
				//result = ttH.run(tokenizedString.toArray(new String [tokenizedString.size()]));
				//	System.out.println(result.toString());
				
				}  
				result = ttH.run(tokenizedString.toArray(new String [tokenizedString.size()]));
				System.out.println(result.toString());
				br.close();	
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
