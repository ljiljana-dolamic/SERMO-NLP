package ch.unine.ILCF.SERMO.Utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class NormalizerTest {

	

	@Test
	public void testNormalizeTilde() {
		try{
			CharacterUtils.fullDictionary = CharacterUtils.loadDictionary("src/main/resources/sermoTTdico04.04.18.csv");
		
		String [] testStrings ={"estoyẽt","quãt","ordõnez","moyẽ","abrahã","adã","possessiõ","accõplissemẽt","celebratiõ","cõmencemẽt","hõmes","cõbiẽ", "viennt","seulemt","regardt","excellemnt","doivet"};
		String [] assertString ={"estoyent","quant","ordonnez","moyen","abrahan","adan","possession","accomplissement","celebration","commencement","hommes","combien","viennent","seulement","regardent","excellement","doivent"};
		
		for(int i=0; i < testStrings.length;i++){
			System.out.println(testStrings[i]);
			assertEquals("test "+i,assertString[i], CharacterUtils.normalizeTilde(testStrings[i]));
		}
		
		}catch(Exception e){
			
		}
	}

}
