/**
 * 
 */
package ch.unine.ILCF.SERMO.TT;

/**
 * @author dolamicl
 *
 */
public class TtOutputLine {
	
	private String token;
	private String pos;
	private String lemma;
	
	@Override
	public String toString() {
		return "TtOutputLine [token=" + token + ", pos=" + pos + ", lemma=" + lemma + "]";
	}
	public TtOutputLine(String token, String pos, String lemma) {
		super();
		this.token = token;
		this.pos = pos;
		this.lemma = lemma;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

}
