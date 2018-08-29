/**
 * 
 */
package ch.unine.ILCF.SERMO.propreties;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
/**
 * @author dolamicl
 *
 */
public class SermoProperties {
	
	//private static Properties prop;
	
	public static void setProperties(String propertiesFileName){
		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream(propertiesFileName);

			// set the properties value
			prop.setProperty("host", "localhost");
			prop.setProperty("database", "sermo");
			prop.setProperty("dbuser", "root");
			prop.setProperty("dbpassword", "v1707n3010D!");
			
			prop.setProperty("tokenizer.path", "src/main/resources/lex.csv");
			prop.setProperty("tokenizer.window", "5");
			prop.setProperty("treetagger.home", "C:\\Program Files\\TreeTagger");
			prop.setProperty("treetagger.model", "C:\\Program Files\\TreeTagger\\lib\\presto.par:UTF8");
			prop.setProperty("treetagger.lex", "src/main/resources/TTdico.csv");
			

			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		
	}
	
	public static Properties getProperties(String propertiesFileName){
		Properties prop = new Properties();
		InputStream input = null;

		try {

			//input = new FileInputStream("config.properties");
			input = new FileInputStream(propertiesFileName);

			// load a properties file
			prop.load(input);
			  
			//System.out.println(prop.toString());

			// get the property value and print it out
			//System.out.println(prop.getProperty("database"));
			//System.out.println(prop.getProperty("dbuser"));
			//System.out.println(prop.getProperty("dbpassword"));
			return prop;

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return prop;
	}

	/**
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//SermoProperties.setProperties("lilyann.properties");
		//SermoProperties.getProperties("config.properties");
		SermoProperties.getProperties("lilyann.properties");
	}

}
