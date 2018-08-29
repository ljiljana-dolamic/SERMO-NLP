/**
 * 
 */
package ch.unine.ILCF.SERMO.XML.Utils;

/**
 * @author dolamicl
 *
 */

import java.util.Map;

import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;

public class TagInfo {
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TagInfo [doc_id=" + doc_id + ", tag_id=" + tag_id + ", name=" + name + ", startOffset=" + startOffset
				+ ", endOffset=" + endOffset + ", tagAttributes=" + tagAttributes + ", textContent=" + textContent
				+ ", sub_id=" + sub_id + "]";
	}
	private String doc_id;
	private int tag_id;
	
	private String name;
	private int startOffset;
	private int endOffset;
    private Map<String,String> tagAttributes;
    private String textContent;  // used only for removed, unparsed nodes
    private String sub_id;
    
    public TagInfo(){
    	tagAttributes = new HashMap<String,String>();
    }
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getStartOffset() {
		return startOffset;
	}
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	public Map<String, String> getTagAttributes() {
		return tagAttributes;
	}
	public void setTagAttributes(Map<String, String> tagAttributes) {
		this.tagAttributes = tagAttributes;
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
	 * @return the tag_id
	 */
	public int getTag_id() {
		return tag_id;
	}

	/**
	 * @param tag_id the tag_id to set
	 */
	public void setTag_id(int tag_id) {
		this.tag_id = tag_id;
	}
	public void createAttributeMap(NamedNodeMap nodeMap){
		HashMap<String,String> attr=new HashMap<String,String>();
		 for(int j=0; j <nodeMap.getLength(); j++){
			 attr.put(nodeMap.item(j).getNodeName(), nodeMap.item(j).getNodeValue());					 
		 }
		 setTagAttributes(attr);
	}

	/**
	 * @return the textContent
	 */
	public String getTextContent() {
		return textContent;
	}

	/**
	 * @param textContent the textContent to set
	 */
	public void setTextContent(String textContent) {
		this.textContent = textContent;
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
	 * 
	 * @return
	 */
	public HashMap<String,String> asHashmap(){
		HashMap <String, String> tmp = new HashMap<String, String>();

		tmp.put("tag_id", Integer.toString(this.getTag_id()) );
		tmp.put("doc_id", this.doc_id );
		tmp.put("sub_id", this.getSub_id() );
		tmp.put("tag_name",this.getName() );
		tmp.put("attributes", this.getTagAttributes().toString());
		tmp.put("content",this.getTextContent() );

		tmp.put("start_offset",Integer.toString(this.getStartOffset()));
		tmp.put("end_offset", Integer.toString(this.getEndOffset()));


		return tmp;

	}
	
}
