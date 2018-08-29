/**
 * 
 */
package ch.unine.ILCF.SERMO.LGeRM;

/**
 * @author dolamicl
 *
 */
public class GraphicsWordInfo {

	private String token;
	private String pos;
	private String lemma;
	private String modern;
	
	@Override
	public String toString() {
		return "GraphisWordInfo [token=" + token + ", pos=" + pos + ", lemma=" + lemma + ", modern" +modern+"]";
	}
	public GraphicsWordInfo(String token, String pos, String lemma,String modern) {
		super();
		this.token = token;
		this.pos = pos;
		this.lemma = lemma;
		this.modern = modern;
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
	public String getModern() {
		return modern;
	}
	public void setModern(String modern) {
		this.modern = modern;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
