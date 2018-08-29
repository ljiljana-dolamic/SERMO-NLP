/**
 * 
 */
package ch.unine.ILCF.SERMO.LGeRM;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.unine.ILCF.SERMO.TranscriptionHandler;

/**
 * @author dolamicl
 *
 */
public class TransformLGeRMOutputToHtml {
	
	public void createHtmlFile(File file, String outDir){
		String fileName = file.getName();
		int line_no=1;
		boolean strong=false;
		boolean par=false;
		StringBuilder sb = new StringBuilder();
	//	StringBuilder tmp=new StringBuilder();
		sb.append("<!DOCTYPE html>").append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">");
		sb.append("<html>").append("<body>").append("<h1>").append(fileName).append("</h1>");
		try {
		  BufferedReader br = new BufferedReader(new FileReader(file)); 
		    String line;
		    boolean table=false;
		    while ((line = br.readLine()) != null) {
		    	String [] parts = line.split("\\t"); 
		    	parts[0]=parts[0].replaceAll("&", "&amp;");
		    	//System.out.println("page "+parts[0]);
		    	if(parts.length==1){ // a tag
		    		if(parts[0].matches("<lb.*/>")){
		    			if(line_no!=1 && !par){
		    			sb.append("</td>");
		    			sb.append("</tr>\n");
		    			}
		    			//tmp.setLength(0);
		    			sb.append("<tr>");
		    			sb.append("<td>").append(line_no).append("</td>");
		    			sb.append("<td>");
		    			par=false;
		    			//tmp.append("<tr>");
		    			//tmp.append("<td>").append(line_no).append("</td>");
		    			//tmp.append("<td>");
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			//sb.append("<td>").append(str).append("</td>");
		    			//sb.append("<br/>");
		    			line_no++;
		    		}else if(parts[0].matches("<p .*>")){
		    			//sb.append("<br/>").append("<br/>");
		    			//sb.append("</td></tr>\n<tr><th colspan=\"2\"> </th></tr>\n");
		    			sb.append("</td></tr>\n<tr><td>").append(" ").append("</td><td>").append(" ").append("</td></tr>\n");
		    			//line_no++;
		    			par=true;
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    		//}else if(parts[0].matches("<p .*>")){
		    			//sb.append("<br/>").append("<br/>");
		    		//	sb.append("<tr><td>");
		    		}else if(parts[0].matches("<pb .*/>")){
		    			//System.out.println("page "+parts[0]);
		    		    Pattern pattern = Pattern.compile("<pb.*n=\\\"(.*)?\\\".*/>");
		    			Matcher matcher = pattern.matcher(parts[0].trim());
		    			String page_no;
		    			if(matcher.find()){
		    		   page_no = matcher.group(1);
		    			}else{
		    			   page_no="problem";	
		    			}
		    			
		    			if(table){
		    				sb.append("</td>");
			    			sb.append("</tr>\n");
		    				sb.append("</table>\n").append("</br>\n");
		    			}
		    			sb.append("<table style=\"width:100%\">");
		    			sb.append("<tr><th colspan=\"2\">").append("NO. page: "+ page_no).append("</th></tr>\n");
		    			line_no=1;
		    			table=true;
		    		}else if(parts[0].matches("<head .*>") || parts[0].matches("<bibl .*>")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			//sb.append("<strong>");
		    			strong=true;
		    		}else if(parts[0].matches("</head>")|| parts[0].matches("</bibl>")){
		    			//sb.append(tmp.toString());
		    		//	tmp.setLength(0);
		    		//	sb.append("</strong>");
		    			strong=false;
		    		}else if(parts[0].matches("<fw.*>")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			sb.append("</td></tr>\n<tr><td>").append(" ").append("</td><td>(");
		    			//line_no++;
		    		}else if(parts[0].matches("</fw>")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			sb.append(")");
		    		}else if(parts[0].matches("<note.*>")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			sb.append("[");
		    			//line_no++;
		    		}else if(parts[0].matches("</note>")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			sb.append("]");
		    		}else if(parts[0].matches("<c type=\"letterine\">.*</c>.*")){
		    			//sb.append(tmp.toString());
		    			//tmp.setLength(0);
		    			sb.append(parts[0].replaceAll("<c type=\"letterine\">", "<strong>").replaceAll("</c>", "</strong>"));
		    		}
		    	}else if(parts[1].equals("0")){
		    		 if(parts[0].matches("\\s")){
		    			// sb.append(tmp.toString());
			    		//	tmp.setLength(0);
			    		sb.append(parts[0]);
			    	}else{
			    		//sb.append(tmp.toString());
		    			//tmp.setLength(0);
			    		if(strong){
			    			sb.append("<strong>");
			    		}
			    		if(parts[0].matches(".*<lb.*/>.*")){
			    			
			    			Pattern partPattern= Pattern.compile("(.*)<lb.*/>(.*)");
			    			Matcher partMatcher = partPattern.matcher(parts[0]);
			    			if(partMatcher.find()){
			    			sb.append("<span style=\"background-color:yellow\">").append(partMatcher.group(1)).append("</span>");
			    			
			    			if(line_no!=1 && !par){
				    			sb.append("</td>");
				    			sb.append("</tr>\n");
				    			}
				    			//tmp.setLength(0);
				    			sb.append("<tr>");
				    			sb.append("<td>").append(line_no).append("</td>");
				    			sb.append("<td>");
				    			par=false;
				    			
				    			line_no++;
				    			sb.append("<span style=\"background-color:yellow\">").append(partMatcher.group(2)).append("</span>");
			    			}
			    		
			    		}else{
			    		sb.append("<span style=\"background-color:yellow\">").append(parts[0]).append("</span>");
			    		}
			    		if(strong){
			    			sb.append("</strong>");
			    		}
			    	}
		    	}else if(parts[1].equals("1")){
		    		//sb.append(tmp.toString());
	    			//tmp.setLength(0);
		    		if(strong){
		    			sb.append("<strong>");
		    		}
		    		sb.append("<span style=\"background-color:red\">").append(parts[0]).append("</span>");
		    		if(strong){
		    			sb.append("</strong>");
		    		}
		    	} else{
		    		//sb.append(tmp.toString());
	    			//tmp.setLength(0);
		    		if(strong){
		    			sb.append("<strong>");
		    		}
		    		sb.append(parts[0]);
		    		if(strong){
		    			sb.append("</strong>");
		    		}
		    	}
		       // process the line.
		    }
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		sb.append("</td></tr>\n");
		sb.append("</table>\n");
		sb.append("</body>").append("</html>");
		
		System.out.println(sb.toString());
		printHtml(fileName, outDir, sb.toString());
	}
	
	private void printHtml(String fileName, String outDir, String text){
		
		try{
			File fout = new File(outDir, fileName+".htm");
		
		
		
		FileWriter fw = new FileWriter(fout);
	 
		fw.write(text);
		fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			TransformLGeRMOutputToHtml tH = new TransformLGeRMOutputToHtml();
			JFileChooser window= new JFileChooser();
			window.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int rv= window.showOpenDialog(null);


			if(rv == JFileChooser.APPROVE_OPTION){
				
				File window_file = window.getSelectedFile();
				if(window_file.isDirectory()){
					File[] files = window_file.listFiles();
					for(File f:files){
						tH.createHtmlFile(f, args[0]);
					}
				}else{
					tH.createHtmlFile(window.getSelectedFile(), args[0]);
				}
			}
			}catch(Exception e){
				System.out.println(e.getMessage());
				//e.getMessage();
			}
			
	}
	

}
