/**
 * 
 */
package ch.unine.ILCF.SERMO.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFileChooser;

/**
 * @author dolamicl
 *
 */
public class FixTTdictionary {
	
	private HashMap<String,LinkedList<String>> dictionary ;
	
	public void loadDictionary(File[] dictFiles,File dir){

		this.dictionary = new HashMap<String,LinkedList<String>>();
		try{
			for(File f :dictFiles){
				InputStreamReader isr = new InputStreamReader(new FileInputStream(f.getAbsolutePath()));
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
				{
				System.out.println(line);
				String [] in = line.split("\\t");
				LinkedList<String>  tmp;
				if(dictionary.containsKey(in[0])){
					tmp = dictionary.get(in[0]);
					
				}else{
					tmp = new LinkedList<String>();
					
				}
				for(int i =1;i<in.length;i++){
					if(!tmp.contains(in[i])){
						tmp.add(in[i]);
					}
				}
				dictionary.put(in[0],tmp);
			}
			}
			writeDictionary(dir);
		}catch(Exception e){
			e.printStackTrace();
			e.getMessage();
		}

	}
	
	public void writeDictionary(File dir){
		
		//System.out.println("writing to : "+ outDir);
		try{
	    	 File fout = new File(dir,"sermoTTdico16.11.17.csv");
	 		
	 		FileOutputStream fos = new FileOutputStream(fout);
	 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	 		SortedSet<String> keys = new TreeSet<String>(dictionary.keySet());
	    	// for(String key : dictionary.keySet().){
	    		 for(String key : keys){
	    		 bw.write(key);
	    		 LinkedList<String> tmp = dictionary.get(key);
	    		 for(String s: tmp){
	    			 bw.write("\t");
	    			 bw.write(s);
	    		 }
	    		 bw.newLine();
	    	 }
	    	 bw.close();
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFileChooser window= new JFileChooser();
		window.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int rv= window.showOpenDialog(null);
		FixTTdictionary fTTd = new FixTTdictionary();
		if(rv == JFileChooser.APPROVE_OPTION){
			File dir = window.getSelectedFile();
			File[] files = dir.listFiles();
				
				fTTd.loadDictionary(files,dir);
					
				
		}	

		
	}

}
