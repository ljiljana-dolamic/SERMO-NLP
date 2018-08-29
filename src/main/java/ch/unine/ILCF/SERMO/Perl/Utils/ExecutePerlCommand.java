/**
 * 
 */
package ch.unine.ILCF.SERMO.Perl.Utils;

/**
 * @author dolamicl
 *
 */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;

import ch.unine.ILCF.SERMO.File.Utils.StreamGobbler;

public class ExecutePerlCommand {

	/**
	 * @param args
	 */
	private String COMMAND="perl";
	private String script;
	private String [] arguments;
	private Process process;
	private StreamGobbler errorGobbler;
	private StreamGobbler outputGobbler;
	
	
	public ExecutePerlCommand (String script,String ... args) throws IOException{
	    this.script=script;
		//this.arguments=args;
		String [] perlArgs1={COMMAND,this.script};
		//concatenate strings
		this.arguments = new String[ perlArgs1.length + args.length];

	      System.arraycopy(perlArgs1, 0, this.arguments, 0, perlArgs1.length);
	      System.arraycopy(args, 0, this.arguments, perlArgs1.length, args.length);

		
		ProcessBuilder builder = new ProcessBuilder(this.arguments);
		//builder.redirectErrorStream(true);
		this.process = builder.start();
		this.errorGobbler = new 
                StreamGobbler(process.getErrorStream(), "ERROR");
		this.outputGobbler = new 
                StreamGobbler(this.process.getInputStream(), "OUTPUT", true);
		// kick them off
		this.errorGobbler.start();
		this.outputGobbler.start();
	}
	
	 public LinkedList<String> stdOut(String line){
		 LinkedList<String> resultList= new LinkedList<String>();
		
		try{ 
		
		   OutputStream stdin = this.process.getOutputStream();
		   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
	      
	    	   
	    	   writer.write(line+"\n");
	    	   writer.flush(); 
	           writer.close();
	       
		  
	            int exitVal = this.process.waitFor();
	            System.out.println("ExitValue: " + exitVal);   
	            resultList=this.outputGobbler.getResult();
	        } catch (Exception e) {
	            System.out.println("error executing stdOut " );
	            e.printStackTrace();
	        }
		return resultList;
	 } 
	 
	 
	
	 public static void main(String[] args) {
		 String script=args[0];
		 String [] otherArgs= new String [args.length-1];
		 System.arraycopy(args, 1, otherArgs, 0,  otherArgs.length);

		   
		   try{
		   ExecutePerlCommand  executePerlCommand=new ExecutePerlCommand(script,otherArgs);
		   OutputStream stdin = executePerlCommand.process.getOutputStream();
		   BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
	       String [] lines={"Aujourd'hui, il fait beau","Demain peut-être pas."};
	       
	       for (String s :lines){
	    	   System.out.println("Writing line:"+s);
	    	   
	    	   writer.write(s+"\n");
	    	   System.out.println("results:");
	            for(String sR: executePerlCommand.outputGobbler.getResult()){
	            	System.out.println(sR);
	            } 
	    	   
	       }
	       writer.flush(); 
	       writer.close();
	       
	            int exitVal = executePerlCommand.process.waitFor();
	            System.out.println("ExitValue: " + exitVal);   
	            System.out.println("results:");
	            for(String s: executePerlCommand.outputGobbler.getResult()){
	            	System.out.println(s);
	            } 
	        
	        } catch (Exception e) {
	            System.out.println("error executing " + args[0]);
	        }
	    }
}
