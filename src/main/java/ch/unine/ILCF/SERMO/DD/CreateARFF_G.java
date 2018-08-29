/**
 * 
 */
package ch.unine.ILCF.SERMO.DD;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.unine.ILCF.SERMO.LGeRM.OutputLine;
import ch.unine.ILCF.SERMO.XML.Utils.XMLutils;



/**
 * @author dolamicl
 *
 */
public class CreateARFF_G {
	
	
//	private File wordCountInput;
//	private File ddManuelInput;
	private Document domWordCount;
	private Document domDDManuel;
	private Document domDDTest;
	private HashMap<String,String> sentenceClass = new HashMap<String,String>();
	private HashMap<String,String> sentenceClassTest = new HashMap<String,String>();
	
	LinkedList<String> attributes = new LinkedList<String>(); 
	HashMap<String,HashMap<String,Integer>> attributesPerSentence = new HashMap<String,HashMap<String,Integer>> ();
	HashMap<String,HashMap<String,Integer>> attributesPerSentenceTest = new HashMap<String,HashMap<String,Integer>> ();
	
	private boolean is_test=false;
	
	static LinkedList<String> insRegExp;
	static{
		insRegExp=new LinkedList<String>();
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]"); //79
		
		
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");//130
		
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");//131
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");//133
		
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)"); //133
		
		
		///magda 29.05
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
		
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		
		
		
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
		
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("Fw_,([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_\\)");
		
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_\\)");
		
		
		
	
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_\\)");
		
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		insRegExp.add("Fw_\\(([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_\\)");
		
		
		
		//start of the sentence
		
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]"); //79
		
		
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DÉCLARER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_PARLER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ASSURER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_CRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_MONTRER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");//130
		
		
		
		
		///magda 29.05
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Pp_IL([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
		
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Pp_IL([^,:;]*)?Fw_[,:]");
		
		
		
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?Np_([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Fw_[,:]");
		
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_ÉCRIRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_DEMANDER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_AJOUTER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPLIQUER([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		insRegExp.add("([^,:;]*)?[Vvc|Ga|Ge]_REPONDRE([^,:;]*)?Np_([^,:;]*)?Fw_[,:]");
		
		
		
		

	}

	public CreateARFF_G  (File wCI, File ddM)throws Exception{
		
		this.domWordCount = XMLutils.getDoc(wCI); 
		this.domDDManuel =  XMLutils.getDoc(ddM);
	}
	
  public CreateARFF_G  (File wCI, File ddM,File ddT)throws Exception{
		
	    this(wCI,ddM);
		this.domDDTest = XMLutils.getDoc(ddT);
	}
	
	public void buildSentenceClass(){
		
		NodeList sentences = XMLutils.getNodeList(this.domDDManuel, "/root/par/s");
		for (int i=0;i< sentences.getLength();i++){
			Node thisS= sentences.item(i);
			
			if(thisS.getAttributes().getNamedItem("DD")!=null){
				Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
				String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
				String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
				String sent_id = thisS.getAttributes().getNamedItem("n").getNodeValue();
				String ddClass = thisS.getAttributes().getNamedItem("DD").getNodeValue().equals("N") ? "noDD": "DD";
				this.sentenceClass.put(doc_id+"_"+par_id+"_"+sent_id, ddClass);
				//System.out.println(doc_id+" ; "+par_id+" ; "+sent_id);
			}
			
		}
		//if we have a dom for the test file build it's sentences test
		if(this.domDDTest != null){
			//this.is_test=true;
			NodeList testSent = XMLutils.getNodeList(this.domDDTest, "/root/par/s");
			for (int i=0;i< testSent.getLength();i++){
				Node thisTS= testSent.item(i);
				
				if(thisTS.getAttributes().getNamedItem("DD")!=null){
					Node thisTSpar= thisTS.getParentNode();  // paragraph contains doc_id and par_no as attributes
					String doc_id = thisTSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
					String par_id = thisTSpar.getAttributes().getNamedItem("par_no").getNodeValue();
					String sent_id = thisTS.getAttributes().getNamedItem("n").getNodeValue();
					String ddClass = thisTS.getAttributes().getNamedItem("DD").getNodeValue().equals("N") ? "noDD": "DD";
					this.sentenceClassTest.put(doc_id+"_"+par_id+"_"+sent_id, ddClass);
					//System.out.println(doc_id+" ; "+par_id+" ; "+sent_id);
				}
				
			}
		}
		
	}
	
	public void buildAttributesList(){
		NodeList sentences = XMLutils.getNodeList(this.domWordCount, "/root/par/s");
		this.attributes.add("length");
		this.attributes.add("italic");
		this.attributes.add("comma_maj_mid");
		this.attributes.add("has_ins");
		for (int i=0;i< sentences.getLength();i++){
			Node thisS= sentences.item(i);
			Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
			String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
			String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
			String sent_id = doc_id + "_" + par_id + "_" + thisS.getAttributes().getNamedItem("n").getNodeValue();
			
			if(sentenceClass.containsKey(sent_id)){
				//System.out.println(sent_id);
				attributesPerSentence.put(sent_id, getAttributes(thisS));
				
			}
		}
		
		if(this.domDDTest != null){
			this.is_test=true;
			for (int i=0;i< sentences.getLength();i++){
				Node thisS= sentences.item(i);
				Node thisSpar= thisS.getParentNode();  // paragraph contains doc_id and par_no as attributes
				String doc_id = thisSpar.getAttributes().getNamedItem("doc_id").getNodeValue();
				String par_id = thisSpar.getAttributes().getNamedItem("par_no").getNodeValue();
				String sent_id = doc_id + "_" + par_id + "_" + thisS.getAttributes().getNamedItem("n").getNodeValue();
				
				if(sentenceClassTest.containsKey(sent_id)){
					//System.out.println(sent_id);
					attributesPerSentenceTest.put(sent_id, getAttributes(thisS));
					
				}
			}
		}
		}
	
	
	// check whether the sentence contains the insize of the type ",dit-il,"
	private int hasIncise(String [] words){
		int has_it=0;
		
		StringBuilder tmpSent= new StringBuilder();// reconstruct the sentence just by gluing the pos and lemma together like: ",_Fw"
		
		 for(int i = 0;i<words.length;i++){
			 String[] parts = words[i].split("\\t"); //token final pos lemma
			 
			 String pos = (parts[2].equals("Nc") && parts[0].matches("\\p{Lu}.*"))? "Np": parts[2];// to deal with Apôtre and others
			 
			 tmpSent.append(pos).append("_").append(parts[3]).append(" ");  // 164
			// tmpSent.append(parts[2]).append("_").append(parts[3]).append(" ");
		 }
		//System.out.println(tmpSent);	 
		 for(String exp: this.insRegExp){
			// System.out.println(exp);
			 Pattern expPattern = Pattern.compile(exp);
			 Matcher expMacher = expPattern.matcher(tmpSent);
				if(expMacher.find()){

					has_it=1;
					
					//System.out.println("Matched:"+expMacher.group(0));
					
				}

			 
		 }
		
		
		return has_it;
		
	}
	
	// check whether the sentence contains a comma followed by a word in capital 
		private int hasCommaCapital(String [] words){
			int has_it=0;
			
			StringBuilder tmpSent= new StringBuilder();// reconstruct the sentence just by gluing the pos and lemma together like: ",_Fw"
			
			 for(int i = 0;i<words.length;i++){
				 String[] parts = words[i].split("\\t"); //token final pos lemma
				 
				 
				 tmpSent.append(parts[0]).append(" ");  
			 }
			 
			 System.out.println(tmpSent);
			 
			 String commaCapitalRegExp =", \\p{Lu}";
			
				 Pattern expPattern = Pattern.compile(commaCapitalRegExp);
				 Matcher expMacher = expPattern.matcher(tmpSent);
					if(expMacher.find()){

						has_it=1;
						
						System.out.println("Matched:"+expMacher.group(0));
						
					}

				 
			
			
			
			return has_it;
			
		}
	
		HashMap<String,Integer> getAttributes(Node sent){
			HashMap<String,Integer> sentAttr = new HashMap<String,Integer>();
			//boolean mid_maj=false;
			boolean comma_mid_maj=false; // sentence contains a non noun after a comma written in capital letters
			if(sent.getAttributes().getNamedItem("italic")!=null){

				sentAttr.put("italic", Integer.parseInt(sent.getAttributes().getNamedItem("italic").getNodeValue()) );
			}
			String[] words = sent.getTextContent().trim().split("\\n");
			sentAttr.put("length", words.length);
			sentAttr.put("has_ins", hasIncise(words));
			// sentAttr.put("has_cc", hasCommaCapital(words));


			boolean prev_comma=false;// was the previous character a comma


			for(int i = 0;i<words.length;i++){

				String[] parts = words[i].split("\\t");




				String ft = "final_"+parts[1].trim();
				if(!is_test && !this.attributes.contains(ft)){
					this.attributes.add(ft);

				}
				if(sentAttr.containsKey(ft)){
					sentAttr.put(ft, sentAttr.get(ft)+1);

				}else{

					sentAttr.put(ft, 1);
				}


				String pos = "pos_"+parts[2].trim();
				if(!is_test && !this.attributes.contains(pos)){
					this.attributes.add(pos);

				}
				if(sentAttr.containsKey(pos)){
					sentAttr.put(pos, sentAttr.get(pos)+1);

				}else{

					sentAttr.put(pos, 1);
				}
				// mid upper case counts only if it is not Np 
				//System.out.println("i: "+ i+ ", word  "+ parts[0]+", pos: "+pos + ", comma :"+ prev_comma);
				if(i !=0 && 
						prev_comma &&
						Character.isUpperCase(parts[0].trim().charAt(0)) && 
						!pos.equals("pos_Np") && !pos.equals("pos_Nc")){
					//System.out.println("GOT-IT");

					comma_mid_maj=true;
				}

				String lemma = "lemma_"+parts[3].trim();
				if(!is_test && !this.attributes.contains(lemma)){
					this.attributes.add(lemma);
					//				 this.attributes.add(lemma+"_p");
				}
				if(sentAttr.containsKey(lemma)){
					sentAttr.put(lemma, sentAttr.get(lemma)+1);

				}else{
					//					sentAttr.put(lemma+"_p", 1);
					sentAttr.put(lemma, 1);
				}

				

				if(lemma.equals("lemma_,") && pos.equals("pos_Fw")){

					// System.out.println("Warrning: "+ pos);
					prev_comma = true;
				}else{
					prev_comma = false;
				}

			}
			if(comma_mid_maj){
				sentAttr.put("comma_maj_mid", 1);
              // System.out.println("REG");
			}else{
				sentAttr.put("comma_maj_mid", 0);;
			}

			return sentAttr;

		}

	

	
	
	public void buildARFF(){
		buildSentenceClass();
		buildAttributesList();
		writeARFF();
		
	}
	
	public void writeARFF(){
		StringBuilder sbTest = new StringBuilder();
		//header
		System.out.println("@relation SERMO-DD");
		sbTest.append("@relation SERMO-DD").append("\n");
		for(String s :attributes){
			//System.out.println("@attribute '"+s.replaceAll("\\s","_")+"' numeric");
			if(s.matches(".*_p")){
		  
			System.out.println("@attribute '"+s.replaceAll("'","£")+"' {0,1}");
			sbTest.append("@attribute '"+s.replaceAll("'","£")+"' {0,1}").append("\n");
			}
			else{
			
			  System.out.println("@attribute '"+s.replaceAll("'","£")+"' numeric");
			  sbTest.append("@attribute '"+s.replaceAll("'","£")+"' numeric").append("\n");
			}
		}
		System.out.println("@attribute class {noDD,DD}");	
		 sbTest.append("@attribute class {noDD,DD}").append("\n");
		
		System.out.println("@data");
		 sbTest.append("@data").append("\n");
		//training
		Set<String> sentences= sentenceClass.keySet();
		
		for(String sent_id: sentences){
			
			HashMap<String,Integer> sentAttr= this.attributesPerSentence.get(sent_id);
			StringBuilder line= new StringBuilder();
			//line.append(sent_id);
			for(String s :attributes){
				if(line.length()!=0){
					line.append(",");
				}
				if(sentAttr.containsKey(s)){
					line.append(sentAttr.get(s));
				}else{
					line.append(0);
				}
				
			}
			line.append(",").append(this.sentenceClass.get(sent_id));
			  //System.out.println(sent_id);
		   System.out.println(line);	
		}
		//test
		
		if(is_test){
		Set<String> sentencesTest= sentenceClassTest.keySet();
		StringBuilder testIDsb= new StringBuilder(); 
		
		for(String sent_id: sentencesTest){
			testIDsb.append(sent_id).append("\n");
			
			HashMap<String,Integer> sentAttrTest= this.attributesPerSentenceTest.get(sent_id);
			StringBuilder lineTest= new StringBuilder();
			//lineTest.append(sent_id);
			for(String s :attributes){
				if(lineTest.length()!=0){
					lineTest.append(",");
				}
				if(sentAttrTest.containsKey(s)){
					lineTest.append(sentAttrTest.get(s));
				}else{
					lineTest.append(0);
				}
				
			}
			lineTest.append(",").append(this.sentenceClassTest.get(sent_id)).append("\n");
			  //System.out.println(sent_id);
		  sbTest.append(lineTest);	
		}
		
		writeTestARFF(sbTest.toString(),testIDsb.toString());
		}
	}
	
	public void writeTestARFF(String s ,String ids){
		try{
			File fout = new File("M:\\Grenoble\\test\\weka_inputs_0206\\testDD_start.arff");

			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			
				bw.write(s);
				bw.newLine();
			

			bw.close();
			
			File idsOout = new File("M:\\Grenoble\\test\\inputs\\testIds.txt");

			fos = new FileOutputStream(idsOout);

			 bw = new BufferedWriter(new OutputStreamWriter(fos));

			
				bw.write(ids);
				bw.newLine();
			

			bw.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{

			JFileChooser window= new JFileChooser("M:\\Grenoble\\test\\inputs");
			window.setDialogTitle("Choose full info file: ");
			int rv= window.showOpenDialog(null);
			File infoFile = window.getSelectedFile();
			
			JFileChooser wM= new JFileChooser("M:\\Grenoble\\test\\inputs");
			wM.setDialogTitle("Choose manually annotated file: ");
			int rM= wM.showOpenDialog(null);
			File manual = wM.getSelectedFile();
			
			JFileChooser wT= new JFileChooser("M:\\Grenoble\\test\\inputs");
			wT.setDialogTitle("Choose test file: ");
			int rT= wT.showOpenDialog(null);
			File test = wT.getSelectedFile();
			
			CreateARFF_G cWArff;
			if(test!=null){
				 cWArff = new CreateARFF_G(infoFile,manual,test);
			}else{
			 cWArff = new CreateARFF_G(infoFile,manual);
			}
			cWArff.buildARFF();
		
		}catch(Exception e){
			System.out.println("Error:"+e.getMessage());
			e.printStackTrace();
			//e.getMessage();
		}
	}

}
