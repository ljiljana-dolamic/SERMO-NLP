package ch.unine.ILCF.SERMO.LGeRM;

import static org.junit.Assert.*;

import org.junit.Test;

public class GraphicInfoTest {

	@Test
	public void test() {
		// GetInfoFromGraphicsPresto gIGP = new  GetInfoFromGraphicsPresto("lilyann.properties");
		
		 String in="$1;_NOM_PROPRE;nom propre;OUTILS;0;-1,-1;";
		 
		 String [] in_split =in.split(";");
		 
		 assertEquals("outils","", GetInfoFromGraphicsPresto.getModern(in_split[0],in_split[3]));
		 
	}

}
