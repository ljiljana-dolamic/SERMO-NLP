package ch.unine.ILCF.SERMO.XML.Utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.w3c.dom.Document;

public class XMLutilsTest {

	@Test
	public void testGetAtributeValue() {
		String  nodePath ="/TEI/teiHeader/fileDesc";
		String attr= "xml:id";
		try {
			Document doc = XMLutils.getDoc("D:\\sermo_cwb\\input0610\\_1555_Jean_Calvin_RSL.xml") ;
			assertEquals("1", "_1555_Jean_Calvin_RSL", XMLutils.getAtributeValue(doc, nodePath, attr) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
