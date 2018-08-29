package ch.unine.ILCF.SERMO.SSplit;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;

/** This class demonstrates building and using a Stanford CoreNLP pipeline. */
public class StanfordCoreNlpDemo {

  /** Usage: java -cp "*" edu.stanford.nlp.pipeline.demo.StanfordCoreNlpDemo [inputFile [outputTextFile [outputXmlFile]]] */
  public static void main(String[] args) throws IOException {
    // set up optional output files
    PrintWriter out = new PrintWriter(System.out);
   

    // Create a CoreNLP pipeline. To build the default pipeline, you can just use:
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // Here's a more complex setup example:
    //   Properties props = new Properties();
    //   props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
    //   props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
    //   props.put("ner.applyNumericClassifiers", "false");
    //props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
    //   StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Add in sentiment
    Properties props = new Properties();
    
    props.setProperty("annotators", "tokenize, ssplit");
    //props.setProperty("annotators", "tokenize, ssplit");
    props.put("tokenize.language","French");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

    // Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
    Annotation annotation;
    if (args.length > 0) {
      annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
    } else {
      annotation = new Annotation("Kosgi Santosh sent an email to Stanford University. He didn't get a reply.");
    }

    // run all the selected Annotators on this text
    pipeline.annotate(annotation);

    // this prints out the results of sentence analysis to file(s) in good formats
    pipeline.prettyPrint(annotation, out);

    // An Annotation is a Map with Class keys for the linguistic analysis types.
    // You can get and use the various analyses individually.
    // For instance, this gets the parse tree of the first sentence in the text.
//    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//   for(CoreMap sentence: sentences) {
//     
//	  System.out.println(sentence.toShorterString());
//      System.out.println("The  sentence text:");
//      System.out.println(sentence.get(CoreAnnotations.SentenceIndexAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.TokenBeginAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.TokenEndAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
//      System.out.println(sentence.get(CoreAnnotations.TokensAnnotation.class).size());
//      
//      
//   }

  }

}