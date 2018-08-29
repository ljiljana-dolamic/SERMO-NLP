/**
 * 
 */
package ch.unine.ILCF.SERMO.collection;

import java.util.HashMap;
import java.util.LinkedList;

import ch.unine.ILCF.SERMO.XML.Utils.TagInfo;


/**
 * @author dolamicl
 *
 */
public class FullPartInfo implements Cloneable{
	private LinkedList<TokenInfo> tokens;
	private HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagStart;
	private HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagEnd;
	
	
	public FullPartInfo(LinkedList<TokenInfo> tokens,
	 HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagStart,
	 HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagEnd){
		this.tokens=tokens;
		this.tagStart=tagStart;
		this.tagEnd = tagEnd;
		
	}

	public FullPartInfo () {
        
    }
	
	public FullPartInfo (FullPartInfo fpi) {
		this.tokens=fpi.tokens;
		this.tagStart=fpi.tagStart;
		this.tagEnd = fpi.tagEnd;
		
    }
	/**
	 * @return the tokens
	 */
	public LinkedList<TokenInfo> getTokens() {
		return tokens;
	}


	/**
	 * @return the tagStart
	 */
	public HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> getTagStart() {
		return tagStart;
	}


	/**
	 * @return the tagEnd
	 */
	public HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> getTagEnd() {
		return tagEnd;
	}


	/**
	 * @param tokens the tokens to set
	 */
	public void setTokens(LinkedList<TokenInfo> tokens) {
		this.tokens = tokens;
	}


	/**
	 * @param tagStart the tagStart to set
	 */
	public void setTagStart(HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagStart) {
		this.tagStart = tagStart;
	}


	/**
	 * @param tagEnd the tagEnd to set
	 */
	public void setTagEnd(HashMap<Integer, HashMap<Integer, LinkedList<TagInfo>>> tagEnd) {
		this.tagEnd = tagEnd;
	}
	
}
