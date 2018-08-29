/**
 * 
 */
package ch.unine.ILCF.SERMO.collection;

import java.util.ArrayList;

/**
 * @author dolamicl
 *
 */
public class TokenInfo {
	
	private int token_id;
	private String doc_id;
	private String section_id; // front, text, sermon, sermon_sub
	private String sub_id; //0 - not a sub(div type sub!)
	private int head; // head or paragraph (0-text;1-head)
	
	private int par_id; // paragraph(0 if title) 
	
	private String pageNo;
	private int lineNo;//per page
    private int columnNo;// 0 if no column
    
    private String token;
    private ArrayList<String> finalToken;
    private String lemma;
    private String pos;
    private String modern;
    
    public String getModern() {
		return modern;
	}
	public void setModern(String modern) {
		this.modern = modern;
	}
	private int startOffset;
    private int endOffset;
    private int break_point;
    
    private int sentNo;
	
    /**
	 * @return the sentNo
	 */
	public int getSentNo() {
		return sentNo;
	}
	/**
	 * @param sentNo the sentNo to set
	 */
	public void setSentNo(int sentNo) {
		this.sentNo = sentNo;
	}
	/**
	 * @return the token_id
	 */
	public int getToken_id() {
		return token_id;
	}
	/**
	 * @param token_id the token_id to set
	 */
	public void setToken_id(int token_id) {
		this.token_id = token_id;
	}
	/**
	 * @return the doc_id
	 */
	public String getDoc_id() {
		return doc_id;
	}
	/**
	 * @param doc_id the doc_id to set
	 */
	public void setDoc_id(String doc_id) {
		this.doc_id = doc_id;
	}
	/**
	 * @return the section_id
	 */
	public String getSection_id() {
		return section_id;
	}
	/**
	 * @param section_id the section_id to set
	 */
	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}
	/**
	 * @return the sub_id
	 */
	public String getSub_id() {
		return sub_id;
	}
	/**
	 * @param sub_id the sub_id to set
	 */
	public void setSub_id(String sub_id) {
		this.sub_id = sub_id;
	}
	/**
	 * @return the head
	 */
	public int getHead() {
		return head;
	}
	/**
	 * @param head the head to set
	 */
	public void setHead(int head) {
		this.head = head;
	}
	/**
	 * @return the par_id
	 */
	public int getPar_id() {
		return par_id;
	}
	/**
	 * @param par_id the par_id to set
	 */
	public void setPar_id(int par_id) {
		this.par_id = par_id;
	}
	/**
	 * @return the pageNo
	 */
	public String getPageNo() {
		return pageNo;
	}
	/**
	 * @param pageNo the pageNo to set
	 */
	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}
	/**
	 * @return the lineNo
	 */
	public int getLineNo() {
		return lineNo;
	}
	/**
	 * @param lineNo the lineNo to set
	 */
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the startOffset
	 */
	public int getStartOffset() {
		return startOffset;
	}
	/**
	 * @param startOffset the startOffset to set
	 */
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	/**
	 * @return the endOffset
	 */
	public int getEndOffset() {
		return endOffset;
	}
	/**
	 * @param endOffset the endOffset to set
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	/**
	 * @return the columnNo
	 */
	public int getColumnNo() {
		return columnNo;
	}
	/**
	 * @param columnNo the columnNo to set
	 */
	public void setColumnNo(int columnNo) {
		this.columnNo = columnNo;
	}
	/**
	 * @return the finalToken
	 */
	public ArrayList<String> getFinalToken() {
		return finalToken;
	}
	/**
	 * @param finalToken the finalToken to set
	 */
	public void setFinalToken(ArrayList<String> finalToken) {
		this.finalToken = finalToken;
	}
	/**
	 * @return the break_point
	 */
	public int getBreak_point() {
		return break_point;
	}
	/**
	 * @param break_point the break_point to set
	 */
	public void setBreak_point(int break_point) {
		this.break_point = break_point;
	}
	/**
	 * @return the lemma
	 */
	public String getLemma() {
		return lemma;
	}
	/**
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}
	/**
	 * @param lemma the lemma to set
	 */
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	/**
	 * @param pos the pos to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}
	@Override
	public String toString() {
		return "TokenInfo [token_id=" + token_id + ", doc_id=" + doc_id + ", section_id=" + section_id + ", sub_id="
				+ sub_id + ", head=" + head + ", par_id=" + par_id + ", pageNo=" + pageNo + ", lineNo=" + lineNo
				+ ", columnNo=" + columnNo + ", token=" + token + ", finalToken=" + finalToken + ", lemma=" + lemma
				+ ", pos=" + pos + ", modern=" + modern + ", startOffset=" + startOffset + ", endOffset=" + endOffset
				+ ", break_point=" + break_point + ", sentNo=" + sentNo + "]";
	}
    

}
