/**
 * 
 */
package ch.unine.ILCF.SERMO.LGeRM;

/**
 * @author dolamicl
 *
 */
public class OutputLine {
	
	private String word;
	private String lemma;
	private String tag;
	
	
	public OutputLine(){
		this.word = "";
		this.lemma = "";
		this.tag = "";
	}
	public OutputLine(String word){
		this.word = word;
		this.lemma = "";
		this.tag = "";
	}
	@Override
	public String toString() {
		return "OutputLine [word=" + word + ", lemma=" + lemma + ", tag=" + tag + "]";
	}
	public OutputLine(String word,String lemma){
		this.word=word;
		this.lemma=lemma;
		this.tag="";
	}
	public OutputLine(String word,String lemma, String tag){
		this.word=word;
		this.lemma=lemma;
		this.tag=tag;
	}
	public void setWord(String word){
		this.word=word;
	}
	
	public void setLemma(String lemma){
		this.lemma=lemma;
		
	}
	public void setTag(String tag){
		this.tag=tag;
		
	}
	public String getWord(){
		return this.word;
	}
	
	public String getLemma(){
		return this.lemma;
		
	}
	public String getTag(){
		return this.tag;
		
	}


}
