/**
 * 
 */
package ch.unine.ILCF.SERMO.DD;

/**
 * @author dolamicl
 *
 */

import weka.core.Instances;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.classifiers.functions.Logistic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ch.unine.ILCF.SERMO.SQL.MySQLconnection;
import ch.unine.ILCF.SERMO.SQL.Utils.GetFromDatabase;
import ch.unine.ILCF.SERMO.SQL.Utils.PutToDatabase;
import ch.unine.ILCF.SERMO.propreties.SermoProperties; 

public class Classify {
	//path to the model
	private String model; 
	// model to be loaded
	private Logistic logistic;
	
	private Properties prop;
	private Connection connection;
	
	private boolean fromDB = true; 
	
	// the attribute map is static because it is blocked by the model
	static ArrayList<Attribute> attributes ;
	static{
		attributes = new ArrayList<Attribute>();

		attributes.add(new Attribute("italic"));
		attributes.add(new Attribute("comma_maj_mid"));
		attributes.add(new Attribute("has_ins"));
		attributes.add(new Attribute("final_temps"));
		attributes.add(new Attribute("final_mon"));
		attributes.add(new Attribute("lemma_MON"));
		attributes.add(new Attribute("lemma_PAROLE"));
		attributes.add(new Attribute("final_es"));
		attributes.add(new Attribute("final_eternel"));
		attributes.add(new Attribute("lemma_JE"));
		attributes.add(new Attribute("final_me"));
		attributes.add(new Attribute("final_m£"));
		attributes.add(new Attribute("final_suis"));
		attributes.add(new Attribute("final_tu"));
		attributes.add(new Attribute("lemma_TU"));
		attributes.add(new Attribute("final_te"));
		attributes.add(new Attribute("final_toi"));
		attributes.add(new Attribute("lemma_DAVID"));
		attributes.add(new Attribute("final_ma"));
		attributes.add(new Attribute("final_ta"));
		attributes.add(new Attribute("lemma_TON"));
		attributes.add(new Attribute("final_tes"));
		attributes.add(new Attribute("final_mes"));
		attributes.add(new Attribute("lemma_TERME"));
		attributes.add(new Attribute("final_ton"));
		attributes.add(new Attribute("final_moy"));
		attributes.add(new Attribute("lemma_MOI"));
		attributes.add(new Attribute("final_dauid"));
		attributes.add(new Attribute("final_toy"));
		attributes.add(new Attribute("lemma_TOI"));
		attributes.add(new Attribute("final_t£"));
		attributes.add(new Attribute("final_as"));
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("noDD");
		labels.add("DD");
		attributes.add(new Attribute("class",labels));

	}

	private Classify(String propFile){
		this.prop = SermoProperties.getProperties(propFile);
			
		if(this.prop.containsKey("model")){

			this.model= this.prop.getProperty("model");


			try {

				this.logistic = (Logistic) weka.core.SerializationHelper.read(this.model);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			showUsage1();

		}
		// if the model is read than read the sentences
		if(this.prop.containsKey("host")
				&&this.prop.containsKey("dbuser")
				&&this.prop.containsKey("dbpassword")
				){
			
		
			try {
			
				this.connection = MySQLconnection.createConnection(this.prop.getProperty("host"), this.prop.getProperty("database"),
					this.prop.getProperty("dbuser"), this.prop.getProperty("dbpassword"));
			} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			}else{
				showUsage();
			
			}
		

	}
	/*
	 * perform the classification itself 
	 * get the documents and sentences from the properties file
	 */
	
	public void doClassification(){
		
		ArrayList<HashMap<String,String>> results = new ArrayList<HashMap<String,String>>();
		
		String [] doc_ids ;
		
		if(this.prop.containsKey("doc_id")){
			doc_ids= this.prop.getProperty("doc_id").split(",");
			
		}else{
			Object [] tmp =GetFromDatabase.getDocsId(this.connection).toArray();
			doc_ids =Arrays.copyOf(tmp, tmp.length, String[].class); 
		}
		
		String [] sent_no;
		
		if(this.prop.containsKey("sent_no")){
			sent_no= this.prop.getProperty("sent_no").split(",");
			
		}else{
			sent_no = new String [1];
			sent_no[0]= "all";
		}
		// create sentences instances to be classified
		Instances sentencesData = new Instances("Sentence-test", attributes,0);
		
		for(String doc:doc_ids){
			System.out.println("Doc: "+ doc);
			String []sentences;
			if(sent_no[0].equals("all")){
				Object[] tmpA = GetFromDatabase.getDistinctSentenceNumber(this.connection,doc).toArray();
				System.out.println("S dg:"+ tmpA.toString());
				sentences=Arrays.copyOf(tmpA, tmpA.length, String[].class);
			}else{
				sentences=sent_no;
			}
			//for(String s:sentences){
				//System.out.println("S dg:"+ s);
			//}
			
			for(String s:sentences){
				//get all sentence attributes
				HashMap<String,Integer> attributesPerSentence = GetFromDatabase.getDDAttributes(this.connection,doc,s);
				System.out.println("S:"+ s);
				System.out.println("ATTS:"+ attributesPerSentence.toString());
				HashMap<String,String> result = new HashMap<String,String>();
				result.put("doc_id", doc);
				result.put("sent_no", s);
				
				result.put("start_offset",attributesPerSentence.remove("start_offset").toString());
			    result.put("end_offset",attributesPerSentence.remove("end_offset").toString());
				
				results.add(result);
				// attributes values holder
				double[] values = new double[sentencesData.numAttributes()];
				
				for (int i=0;i<sentencesData.numAttributes();i++){
					System.out.println(i+" : "+ sentencesData.attribute(i).name());
					if(attributesPerSentence.containsKey(sentencesData.attribute(i).name())){
						values[i] = attributesPerSentence.get(sentencesData.attribute(i).name());
						System.out.println(i+" : "+ values[i]);
					}else{
						if(sentencesData.attribute(i).name().equals("class")){
							values[i]= sentencesData.attribute(i).addStringValue("?");
							System.out.println("class : "+ values[i]);
						}else{
							values[i] = 0;
							System.out.println("null : "+ values[i]);
						}
					}
				}
				
				Instance inst = new DenseInstance(1.0, values);
				sentencesData.add(inst);
				
			}
		
		sentencesData.setClassIndex(sentencesData.numAttributes()-1);
		
		for(int j= 0; j<sentencesData.numInstances();j++){
			try{
			double clsLabel = this.logistic.classifyInstance(sentencesData.instance(j));
			
			System.out.println("Class:"+ clsLabel);
				results.get(j).put("dd", Double.toString(clsLabel));
				
				
			}catch(Exception e){
				System.out.println("Ex.:"+e.getMessage());
				
			}
		}
		}
		System.out.println("Result:"+results.toString());
		
		for(HashMap<String, String> result:results){
			writeResult(result,this.connection);
		}
		
		
	}
	/**
	 * 
	 * @param result
	 * @param connection
	 */
	public void writeResult(HashMap<String, String> result, Connection connection){
		
		//GetFromDatabase.writeHashToDB(result,"dd", connection);
		if(result.get("dd").equals("1.0")){
			
			HashMap<String, String> tag_hash=new HashMap<String, String>();
			tag_hash.put("doc_id", result.get("doc_id"));
			tag_hash.put("sub_id", "0");
			tag_hash.put("tag_name", "q");
			tag_hash.put("start_offset", result.get("start_offset"));
			tag_hash.put("end_offset", result.get("end_offset"));
			tag_hash.put("attributes", "{resp=#AUT}");
			tag_hash.put("content", "");
			System.out.println(tag_hash.toString());
			PutToDatabase.addTagToDb(tag_hash, connection);
			
			
		}
		
		
		
	}
	 /**
	  * 
	  * @param result
	  * @param connection
	  */
	public void addTagToDb(String doc_id,HashMap<String, String> result, Connection connection){
		
		
		
	}
	
	public void getModelCapabilities(){
		System.out.println("coefficients: ");
	    System.out.println(this.logistic.coefficients().length);
	    System.out.println(this.logistic.coefficients()[0][0]);
	    System.out.println(this.logistic.coefficients()[1].length);   // provjeriti koje su sve informacije koje se mogu izvuci odavdje
	    System.out.println(this.logistic.toString());
	    System.out.println(attributes.get(0).name());
	   
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			if (args.length < 1)
		        showUsage();
				Classify classifier = new Classify(args[0]);
				//classifier.getModelCapabilities();
				classifier.doClassification();
		}catch(Exception e){
			System.out.println(e.getMessage());
			//e.getMessage();
		}

		
	}

	private static void showUsage() {
	    System.err.println("\nUsage: Classify <properties file path>\n");

	    System.exit(0);
	  }
	
	private static void showUsage1() {
	    System.err.println("\nUsage: Classify <properties file path> \n");
	    System.err.println("Properties file needs to contain following: ");
	    System.err.println("  model   weka model to be used");
	    
	    
	    System.exit(0);
	  }
}
