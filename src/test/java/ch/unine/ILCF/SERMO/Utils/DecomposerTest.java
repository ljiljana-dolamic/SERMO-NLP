package ch.unine.ILCF.SERMO.Utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class DecomposerTest {

	@Test
	public void testDecomposeString() {
		try{
			//CharacterUtils.loadDictionary("src/main/resources/sermoTTdico2206.csv");
			String [] testStrings ={"desbahissement","pouraultans","tresestroite","plusieursfoys","aumoings","lillustrissime","leuangile","dccv."
					//,"dune","limage"
					};
			String [][] assertString={{"d'","esbahissement"},{"pour","aultans"},{"tres","estroite"},{"plusieurs","foys"},{"au","moings"},
					{"l'","illustrissime"},{"l'","euangile"},{"dccv."}
					//,{"d'","une"},{"l'","image"}
					};
			
			ArrayList<String> [] assertList = new ArrayList[testStrings.length];
			for(int j=0; j < assertList.length;j++){
				assertList[j]= new ArrayList<String>();
				assertList[j].addAll(Arrays.asList(assertString[j]));
				
			}
			
			for(int i=0; i < testStrings.length;i++){
				System.out.println("List: "+assertList[i]);
				System.out.println("Res: "+CharacterUtils.decomposeString(testStrings[i]));
				assertEquals("test"+i,assertList[i], CharacterUtils.decomposeString(testStrings[i]));
			}
			
			}catch(Exception e){
				e.printStackTrace();
			}
	}

}
