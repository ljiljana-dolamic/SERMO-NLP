package ch.unine.ILCF.SERMO.XML.Utils;

import java.util.Arrays;

public class TeiHeaderData {
	
	private String titreDuRecueil;
	private String responsableDuProjet = "Carine Skupien Dekens" ; 
	private String transcriptionWord ;
	private String transcriptionWordID;
	private String transcriptionWordMail;
	private String transcriptionDate ;
	private String auteurFirstName;
	private String auteurLastName; 
	private String projectName="SERMO";
	private String [] ILCFaddresse = {"Institut de langue et civilisation françaises","Université de Neuchâtel","Fbg de l’Hôpital 61-63","CH-2000 Neuchâtel"};
	private String sourceNumerique;
	private String fileName;
	private String editionYear;
	
	public String getTitreDuRecueil() {
		return titreDuRecueil;
	}

	public void setTitreDuRecueil(String titreDuRecueil) {
		this.titreDuRecueil = titreDuRecueil;
	}
	
	public void setTranscriptionWord (String transcriptionWord ){
		this.transcriptionWord = transcriptionWord;
	}    
	public String getTranscriptionWord() {
		return transcriptionWord;
	}
	
	public String getTranscriptionDate() {
		return transcriptionDate;
	}

	public void setTranscriptionDate(String transcriptionDate) {
		this.transcriptionDate = transcriptionDate;
	}
	
	public String getTranscriptionWordID() {
		return transcriptionWordID;
	}

	public void setTranscriptionWordID(String transcriptionWordID) {
		this.transcriptionWordID = transcriptionWordID;
	}

	public String getTranscriptionWordMail() {
		return transcriptionWordMail;
	}

	public void setTranscriptionWordMail(String transcriptionWordMail) {
		this.transcriptionWordMail = transcriptionWordMail;
	}

	public String getAuteurFirstName() {
		return auteurFirstName;
	}

	public void setAuteurFirstName(String auteurFirstName) {
		this.auteurFirstName = auteurFirstName;
	}

	public String getAuteurLastName() {
		return auteurLastName;
	}

	public void setAuteurLastName(String auteurLastName) {
		this.auteurLastName = auteurLastName;
	}

	public void setSourceNumerique (String sourceNumerique ){
		this.sourceNumerique = sourceNumerique;
	}

	public String getResponsableDuProjet() {
		return responsableDuProjet;
	}

	public String getProjectName() {
		return projectName;
	}

	public String[] getILCFaddresse() {
		return ILCFaddresse;
	}

	public String getSourceNumerique() {
		return sourceNumerique;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getEditionYear() {
		return editionYear;
	}

	public void setEditionYear(String editionYear) {
		this.editionYear = editionYear;
	}

	@Override
	public String toString() {
		return "TeiHeaderData [titreDuRecueil=" + titreDuRecueil + ", responsableDuProjet=" + responsableDuProjet
				+ ", transcriptionWord=" + transcriptionWord + ", transcriptionWordID=" + transcriptionWordID
				+ ", transcriptionWordMail=" + transcriptionWordMail + ", transcriptionDate=" + transcriptionDate
				+ ", auteurFirstName=" + auteurFirstName + ", auteurLastName=" + auteurLastName + ", projectName="
				+ projectName + ", ILCFaddresse=" + Arrays.toString(ILCFaddresse) + ", sourceNumerique="
				+ sourceNumerique + ", fileName=" + fileName + ", editionYear=" + editionYear + "]";
	}
	

}
