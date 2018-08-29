package ch.unine.ILCF.SERMO.File.Utils;
/**
 * @author dolamicl
 *
 */
import java.util.*;
import java.io.*;

public class StreamGobbler extends Thread {
	
	InputStream is;
    String type;
    OutputStream os;
    LinkedList<String> result;
    boolean getResult=false;
    
    public StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
//    public StreamGobbler(InputStream is, String type, OutputStream redirect)
//    {
//        this.is = is;
//        this.type = type;
//        this.os = redirect;
//    }
    
    public StreamGobbler(InputStream is, String type, boolean getResult)
  {
      this.is = is;
      this.type = type;
      this.getResult = getResult;
      this.result=new LinkedList<String>();
  }
    public LinkedList<String> getResult(){
    	return this.result;
    }
  
 
    public void run()
    {
        try
        {
//            PrintWriter pw = null;
//            if (os != null)
//                pw = new PrintWriter(os);
        	
                
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                //if (pw != null)
            	if (this.getResult && !line.trim().equals(""))
                   // pw.println(line);
                //pw.append(line);
            		this.result.add(line.trim());
                System.out.println(type + ">" + line);    
            }
            //if (pw != null)
            //    pw.flush();
        } catch (IOException ioe)
            {
            ioe.printStackTrace();  
            }
    }
    

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		

	}

}
