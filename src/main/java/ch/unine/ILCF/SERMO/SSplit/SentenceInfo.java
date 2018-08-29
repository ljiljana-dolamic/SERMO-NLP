package ch.unine.ILCF.SERMO.SSplit;

public class SentenceInfo {
	private int noToken; //number of tokens in the sentence
	private int startOffset; //sentence start offset
	private int endOffset; // sentenceEndOffset
	private String sentence;
	/**
	 * @return the sentence
	 */
	public String getSentence() {
		return sentence;
	}

	/**
	 * @param sentence the sentence to set
	 */
	public void setSentence(String sentence) {
		this.sentence = sentence;
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

	private int sentenceId;
	 /**
	 * @return the sentenceId
	 */
	public int getSentenceId() {
		return sentenceId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SentenceInfo [noToken=" + noToken + ", startOffset=" + startOffset + ", endOffset=" + endOffset
				+ ", sentence=" + sentence + ", sentenceId=" + sentenceId + "]";
	}

	/**
	 * @param sentenceId the sentenceId to set
	 */
	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}

	public SentenceInfo() {
		super();
	}

	/**
	 * @return the noToken
	 */
	public int getNoToken() {
		return noToken;
	}

	/**
	 * @return the startOffset
	 */
	public int getStartOffset() {
		return startOffset;
	}

	
	/**
	 * @param noToken the noToken to set
	 */
	public void setNoToken(int noToken) {
		this.noToken = noToken;
	}

	/**
	 * @param startOffset the startOffset to set
	 */
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	
	 

}
