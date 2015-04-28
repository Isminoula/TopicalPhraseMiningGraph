package run;

import graph.GraphBuilder;
import graph.Node;
import indexing.MyStandardAnalyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import util.*;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Scanner;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import tools.SentenceTokenization;

/**
 *
 * @author isminilourentzou
 */
public class Main {//

    static Analyzer analyze = new MyStandardAnalyzer(Version.LUCENE_42);

    public static String fixPunct(String text) {
        text = text.replaceAll("!", " !_. ").replaceAll("\\.", " ._. ").replaceAll("\\?", " ?_. ").replaceAll("\"", "");
        text = text.replaceAll(":", " :_: ").replaceAll("-", " -_: ").replaceAll(",", " ,_, ");
        return text;
    }

    public static HashMap<String, String> makeSentences(String path) {
        HashMap<String, String> sentences = new HashMap<String, String>();
            String txt = "";
            try {
                txt = new Scanner(new File(path)).useDelimiter("\\Z").next();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            org.jsoup.nodes.Document doc = Jsoup.parse(txt, "", Parser.xmlParser());
            for (Element e : doc.getElementsByTag("article_info")) {
                String content = e.select("title").text();
                String id = e.select("id").text();
                   ArrayList<String> sentenceSplitter = SentenceTokenization.sentenceSplitter(content);
                    for (String sentence : sentenceSplitter) {
                        if (!sentence.trim().isEmpty()) {
                           sentence = StringPreprocessing.analyseString(analyze, fixPunct(sentence));
                           sentences.put(sentence, id);
                    }
                }
            }
           return sentences;
    }

    /**
     * Generates the csv file for visualizing the graph
     *
     * @param sentences text input
     * @param distr the topical distributions file from CPLSA
     * @param outfile the filename for the new csv graph file
     * @param topicId the topic cluster we are interested in
     * @param removeEdges whether to remove edges with score lower than the
     * threshold (average) or not
     * @param topK the number of top scored pairs of nodes with their respective
     * edges to save to csv. If topK is set to -1 then we include the whole edge
     * set (independent to removing edges lower than a thershold)
     */
    public static GraphBuilder doGenerateSummary(HashMap<String, String> sentences, String distr, String outfile, int topicId, boolean removeEdges, int topK) {
        PrintWriter csv = null;
        GraphBuilder builder = new GraphBuilder();
        SimpleDirectedWeightedGraph g;

        try {
            csv = new PrintWriter(new File(outfile));
			//Create a new empty graph
            //Read the topic model distributions
            ReadDistributions.readFile(distr);
            //Get the topic of interest
            TreeMap<String, Double> topics = ReadDistributions.commonModel.get(topicId);
            TreeMap<String, Double> documents = ReadDistributions.docProbs.get(topicId);
            HashMap wordNodeMap = null;
            int sentenceid = 0;
            for(String str: sentences.keySet()){         
                    sentenceid++;
                    if (!str.isEmpty()) {
                        //System.out.println(both[0]+" "+both[1]);
                        String docId = sentences.get(str);
                        //unfortunatelly the lucene analyzer messes up punctuaction, will be solved later on ...
                        wordNodeMap = builder.growGraph(str, docId, sentenceid, topics, documents);
                    }
                }
            HashMap<Node[], Double> rankEdges = builder.rankEdges();
            Collection<Double> c = rankEdges.values();
            Double [] weights = (Double[]) rankEdges.values().toArray(new Double[c.size()]);
            double thr = weights[topK+1];
            builder.removeEdges(thr);
            
            if (topK != -1) {
                builder.saveRankedEdgeListToCSV(csv, topK);
            } else if (removeEdges) {
                double median = builder.medianGraph();
                builder.saveGraphToCSV(csv, median);
            } else if (topK == -1) {
                builder.saveGraphToCSV(csv);
            }

            // testing .getSortedVertex()
            g = builder.getGraph();
            builder.printGraph();
            builder.getSortedVertexSet(true);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            csv.close();
        }
        g = builder.getGraph();
        return builder;
    }

    public static void main(String[] args) {
        String input = "test/allafrica/allafrica.xml";//"test/tech_sports/evaluation_dataset_topicgraph.xml";
        String distr = "test/allafrica/allafrica_ctm_results_titles_4topics.txt";//test/tech_sports/ctm_results_4topics.txt";
        String outputFile = "test/allafrica/force.csv";//"test/tech_sports/forceA.csv";
        //int numberOfTopic = 2;
        HashMap<String, String> makeSentences = makeSentences(input);
      
        for(int i=2;i<=2;i++){
        GraphBuilder gb = doGenerateSummary(makeSentences, distr, outputFile, i, true, 30);
        System.out.println("\n\n\ntopic "+i+": ");
        extract(gb);
        }
    }

    public static void extract(GraphBuilder graphBuilder) {

        SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph = graphBuilder.getGraph();
        List<Object> sorted = graphBuilder.getSortedVertexSet(true);
        System.out.println("size: " + sorted.size());

        List< ArrayDeque<Node>> forwardPhrases = new ArrayList< ArrayDeque<Node>>();
        List< ArrayDeque<Node>> bothwardPhrases = new ArrayList< ArrayDeque<Node>>();
        List< ArrayDeque<Node>> backwardPhrases = new ArrayList< ArrayDeque<Node>>();
        double threshold = -100;

        int i = 0;
        for (Iterator vIter = sorted.iterator(); i < 10 && vIter.hasNext(); i++) {
            Node v = (Node) vIter.next();
            forwardPhrases.add(getForwardPhrase(graph, v, threshold, 0));
        }

        i = 0;
        for (Iterator vIter = sorted.iterator(); i < 10 && vIter.hasNext(); i++) {
            Node v = (Node) vIter.next();
            bothwardPhrases.add(getBothwardPhrase(graph, v, threshold, 0));
        }

        i = 0;
        for (Iterator vIter = sorted.iterator(); i < 10 && vIter.hasNext(); i++) {
            Node v = (Node) vIter.next();
            backwardPhrases.add(getBackwardPhrase(graph, v, threshold, 0));
        }

        System.out.println("\nforwardPhrases: ");
        printPhrases(forwardPhrases);
        System.out.println("\nbothwardPhrases: ");
        printPhrases(bothwardPhrases);
        System.out.println("\nbackwardPhrases: ");
        printPhrases(backwardPhrases);
    }

    public static ArrayDeque<Node> getBothwardPhrase(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Node node, double threshold, double acc) {
        ArrayDeque<Node> bothwardPhrase = new ArrayDeque<Node>();
        bothwardPhrase.addLast(node);
        Node forwardNode = node;
        Node backwardNode = node;
        while (acc > threshold && (forwardNode != null || backwardNode != null)) {
            if (forwardNode != null) {
                Node forwardCandidate = getForwardWord(graph, forwardNode, bothwardPhrase);
                if (forwardCandidate != null) {
                    bothwardPhrase.addLast(forwardCandidate);
                    acc += Math.log(forwardCandidate.getNodeProb());
                }
                forwardNode = forwardCandidate;
            }
            if (backwardNode != null) {
                Node backwardCandidate = getBackwardWord(graph, backwardNode, bothwardPhrase);
                if (backwardCandidate != null) {
                    bothwardPhrase.addFirst(backwardCandidate);
                    acc += Math.log(backwardCandidate.getNodeProb());
                }
                backwardNode = backwardCandidate;
            }
        }
        return bothwardPhrase;
    }

    public static ArrayDeque<Node> getForwardPhrase(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Node node, double threshold, double acc) {
        ArrayDeque<Node> forwardPhrase = new ArrayDeque<Node>();
        forwardPhrase.addLast(node);
        while (acc > threshold && node != null) {
            Node forwardCandidate = getForwardWord(graph, node, forwardPhrase);
            if (forwardCandidate != null) {
                forwardPhrase.addLast(forwardCandidate);
                acc += Math.log(forwardCandidate.getNodeProb());
            }
            node = forwardCandidate;
        }
        return forwardPhrase;
    }

    public static ArrayDeque<Node> getBackwardPhrase(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Node node, double threshold, double acc) {
        ArrayDeque<Node> backwardPhrase = new ArrayDeque<Node>();
        backwardPhrase.addFirst(node);
        while (acc > threshold && node != null) {
            Node backwardCandidate = getBackwardWord(graph, node, backwardPhrase);
            if (backwardCandidate != null) {
                backwardPhrase.addFirst(backwardCandidate);
                acc += Math.log(backwardCandidate.getNodeProb());
            }
            node = backwardCandidate;
        }
        return backwardPhrase;
    }

    public static Node getForwardWord(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Node node, ArrayDeque<Node> phrase) {
        Set<DefaultWeightedEdge> outEdges = graph.outgoingEdgesOf(node);
        DefaultWeightedEdge edge = pickHeaviestEdge(graph, outEdges);
        if (edge != null) {
            Node candidate = graph.getEdgeTarget(edge);
            if (!phrase.contains(candidate)) {//prevent cycles
                return candidate;
            }
        }//if we had the 2nd best candidate or 3rd best candidate, we would have tried them before returning null
        return null;
    }

    public static Node getBackwardWord(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Node node, ArrayDeque<Node> phrase) {
        Set<DefaultWeightedEdge> inEdges = graph.incomingEdgesOf(node);
        DefaultWeightedEdge edge = pickHeaviestEdge(graph, inEdges);
        if (edge != null) {
            Node candidate = graph.getEdgeSource(edge);
            if (!phrase.contains(candidate)) {//prevent cycles
                return candidate;
            }
        }//if we had the 2nd best candidate or 3rd best candidate, we would have tried them before returning null
        return null;
    }

    public static DefaultWeightedEdge pickHeaviestEdge(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> graph, Set<DefaultWeightedEdge> edges) {
        DefaultWeightedEdge edge = null;
        for (Iterator eIter = edges.iterator(); eIter.hasNext();) {
            DefaultWeightedEdge e = (DefaultWeightedEdge) eIter.next();
            if (edge == null || graph.getEdgeWeight(e) > graph.getEdgeWeight(edge)) {
                edge = e;
            }
        }
        return edge;
    }

    public static void printPhrases(List< ArrayDeque<Node>> phrases) {
        for (Iterator pIter = phrases.iterator(); pIter.hasNext();) {
            ArrayDeque<Node> phrase = (ArrayDeque<Node>) pIter.next();
            for (Iterator wIter = phrase.iterator(); wIter.hasNext();) {
                Node word = (Node) wIter.next();
                if (word != null) {
                    System.out.print(word.getNodeName() + " ");
                }
            }
            System.out.println();
        }
    }

    /*
     public static void practice(SimpleDirectedWeightedGraph graph){//not a real method, I just made this for practice
     Set vertexSet = graph.vertexSet();
     Iterator vIter = vertexSet.iterator();
     while (vIter.hasNext()) {//every node
     Node v = (Node) vIter.next();
     Set edges = graph.edgesOf(v);
     for (Iterator eIter = edges.iterator(); eIter.hasNext();) {//every edge of that node
     DefaultWeightedEdge e = (DefaultWeightedEdge) eIter.next();
     String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
     String source = ((Node) graph.getEdgeSource(e)).getNodeName();
     if (!target.equals(v.getNodeName()) && ((Node) graph.getEdgeTarget(e)).getStartNode()) {
     System.out.println((new StringBuilder(
     String.valueOf(v.getNodeName())))
     .append("\t")
     .append(v.getNodeProb())
     .append("\t")
     .append(v.getStartNode())
     .append("->")
     .append(target)
     .append("\t")
     .append(((Node) graph.getEdgeTarget(e)).getNodeProb())
     .append("\t")
     .append(((Node) graph.getEdgeTarget(e)).getStartNode())
     .append("; ")
     .append(graph.getEdgeWeight(e)).toString()
     );
     }
     }
     }
     }*/
}
