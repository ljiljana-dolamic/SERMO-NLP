package ch.unine.ILCF.SERMO.LGeRM;

import static org.junit.Assert.*;

import org.junit.Test;

public class RefSplitterTest {

	String dictionaryPath ="src/main/resources/lex.csv";
	@Test
	public void testSplitter() {
		
		String s = "33. <lb/><reclame>d'E</reclame> <lb/><pb n=\"19\"/>d'Ezechiel";
		
		String [] res = CreateLGeRMInput.splitRefTag(s);
		
		//String [] exc = {"33."," ","<lb/>","<reclame>","d'E","</reclame>"," ","<lb/>","<pb n=\"19\"/>","d'Ezechiel"};
		String [] exc = {"33. ","<lb/>","<reclame>","d'E","</reclame>"," ","<lb/>","<pb n=\"19\"/>","d'Ezechiel"};
		
		assertEquals("length1",9, res.length);
		assertEquals("l0",exc[0], res[0]);
		assertEquals("l1",exc[1], res[1]);
		assertEquals("l7",exc[7], res[7]);//<pb n=\"19\"/>
		
		String [] resText = CreateLGeRMInput.splitRefTag("33. d'Ezechiel");
		assertEquals("length2",1, resText.length);
	}

}
