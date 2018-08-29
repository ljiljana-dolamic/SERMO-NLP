/**
 * 
 */
package ch.unine.ILCF.SERMO.PRESTO;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.LinkedList;

/**
 * @author dolamicl
 *
 */
public class TokenizerTest {
	

	/**
	 * Test method for {@link ch.unine.ILCF.SERMO.PRESTO.Tokenizer#tokenize(java.lang.String)}.
	 */
	@Test
	public void testTokenize() {
		String dictionaryPath ="src/main/resources/sermoLex04.05.18.csv";
		int window = 10;
		Tokenizer tokenizer = new Tokenizer(dictionaryPath , window);
		
//		String s1 = "Aujourd'hui	il fait beau!";
//		LinkedList<String> tokensListS1 = tokenizer.tokenize(s1);
//		String s2 = "Puis qu'ainsi est qu'vne fois il a voulu "
//				+ "auoir fraternité auec nous, ne douton "
//				+ "point qu'en receuant noz pouretez, il "
//				+ "ait fait vn tel eschange que nous soyons "
//				+ "richez en luy. ";
//		LinkedList<String> tokensListS2 = tokenizer.tokenize(s2);
//		String s3 = "est là comme nous tendant les bras, à fin que "
//				+ "cognoissans l'amour qu'il nous porte"; //16, 9 = à fin que
//		LinkedList<String> tokensListS3 = tokenizer.tokenize(s3);
//		String s4 = "Quant aux fideles, combien que Dieu les fortiffie "
//				+ "si est ce que ceux-là sont assez "
//				+ "convaincuz de leur infirmité et la confessent";//5=combien que; 9 = si est ce que; 21
//		LinkedList<String> tokensListS4 = tokenizer.tokenize(s4);
//		String s5 = "l'aliance du peuple,	c'est à dire pour "
//				+ "la ratiffier et pour donner clarté à toutes nations du monde. ";//
//		LinkedList<String> tokensListS5 = tokenizer.tokenize(s5);
//		String s6 = "nous tiendrons le chemin que nous monstre le prophete,"
//				+ " c'est à savoir que nous venions à nostre Seigneur Jesus Christ ";//
//		LinkedList<String> tokensListS6 = tokenizer.tokenize(s6);
//		String s7 = "et il n'y a message qui nous doive estre si amiable que "
//				+ "cestui là, d'autant qu'il apporte repos à noz consciences, ";//
//		LinkedList<String> tokensListS7 = tokenizer.tokenize(s7);
//		String s8 = "Ilz bastissent avec grande dificulté, on les moleste,"
//				+ " on les tourmente, en sorte qu'au lieu de s'esjouir ilz plorent "
//				+ "et font de grandes lamentations :";//
//		LinkedList<String> tokensListS8 = tokenizer.tokenize(s8);
//		String s9 = "mais nostre foy est beaucoup mieux aidée quand nous sommes asseurez que Dieu nous aime,	"
//				+ "encores qu'il ne trouve en nous que toute meschanceté.";//
//		LinkedList<String> tokensListS9 = tokenizer.tokenize(s9);
//		String s10 = "Jesus Christ supportera l'infirmité des fideles, "
//				+ "tellement qu'encores qu'ilz n'aient pas ce qui seroit requis, "
//				+ "si est ce que tousjours il les recongnoistra des siens";//
//		LinkedList<String> tokensListS10 = tokenizer.tokenize(s10);
//		String s11 = "Et combien qu'ilz aient honte de dire qu'ilz aient "
//				+ "desservi que Dieu les regarde en pitié, toutesfois ilz brouillent "
//				+ "et meslent tellement qu'il y a tousjours quelque regard à leurs merites. ";//
//		LinkedList<String> tokensListS11 = tokenizer.tokenize(s11);
//		String s12 = "Or il faut bien que le diable les ait ensorcelez de "
//				+ "concevoir telles imaginations et si lourdes, veu qu’il y a une "
//				+ "admonition si expresse comme elle est ici contenue.";//
//		LinkedList<String> tokensListS12 = tokenizer.tokenize(s12);
		
		
//		String s13 = "*Matth. DCCV. 19. 120.";//
//		LinkedList<String> tokensListS13 = tokenizer.tokenize(s13);
//		System.out.println(tokensListS13.toString());
//		assertEquals("length1",8, tokensListS1.size());
//		assertEquals("token1_0","Aujourd'hui", tokensListS1.get(0));
//		
//		assertEquals("length",68, tokensListS2.size());
//		assertEquals("token2_2","qu'", tokensListS2.get(2));
//		assertEquals("token2_3","ainsi", tokensListS2.get(3));
//		assertEquals("token2_7","qu'", tokensListS2.get(7));
//		assertEquals("token2_8","vne", tokensListS2.get(8));
//		assertEquals("token2_33","qu'", tokensListS2.get(33));
//		assertEquals("token2_34","en", tokensListS2.get(34));
		//assertEquals("length",7, tokensListS13.size());
//		assertEquals("token13_0","*", tokensListS13.get(1));
//		assertEquals("token13_1","Matth.", tokensListS13.get(2));
//		assertEquals("token13_2","DCCV.", tokensListS13.get(4));
//		assertEquals("token13_3","19.", tokensListS13.get(6));
//		assertEquals("token13_3","120.", tokensListS13.get(8));
		
		
		
//		assertEquals("token13_1","qu'", tokensListS2.get(2));
//		assertEquals("token2_3","ainsi", tokensListS2.get(3));
//		assertEquals("token2_7","qu'", tokensListS2.get(7));
//		assertEquals("token2_8","vne", tokensListS2.get(8));
//		assertEquals("token2_33","qu'", tokensListS2.get(33));
//		assertEquals("token2_34","en", tokensListS2.get(34));
		
		String s14 = "Seigneur Iesus intercede à ce iourd'huy maintenant pour nous, d'autant qu'vne fois il nous ce iourd'huy a reconciliez à Dieu";
		LinkedList<String> tokensListS14 = tokenizer.tokenize(s14);
		System.out.println(tokensListS14.toString());
		assertEquals("token14_6","à ce iourd'huy", tokensListS14.get(6));
		assertEquals("token14_24","ce iourd'huy", tokensListS14.get(24));
		
		String s15 = "II. Cor. ch. 12. ꝟ. 9-10.";
		LinkedList<String> tokensListS15 = tokenizer.tokenize(s15);
		System.out.println(tokensListS15.toString());
		assertEquals("token15_1","II.", tokensListS15.get(0));

	}

}
