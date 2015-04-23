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
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import util.*;

import java.util.ArrayDeque;

/**
 *
 * @author isminilourentzou
 */
public class Main {//

    Analyzer analyze = new MyStandardAnalyzer(Version.LUCENE_42);

    /**
     * Generates the csv file for visualizing the graph
     *
     * @param fileName the file that contains the sentences or titles
     * @param distr the topical distributions file from CPLSA
     * @param outfile the filename for the new csv graph file
     * @param topicId the topic cluster we are interested in
     * @param removeEdges whether to remove edges with score lower than the
     * threshold (average) or not
     * @param topK the number of top scored pairs of nodes with their respective
     * edges to save to csv. If topK is set to -1 then we include the whole edge
     * set (independent to removing edges lower than a thershold)
     */
    public GraphBuilder doGenerateSummary(String fileName, String distr, String outfile, int topicId, boolean removeEdges, int topK) {
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
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String str;
                int sentenceid = 0;
                while ((str = reader.readLine()) != null) {
                    sentenceid++;
                    String both[] = str.split("\t");
                    if (both.length >= 2 && !both[1].isEmpty()) {
                        //System.out.println(both[0]+" "+both[1]);
                        str = StringPreprocessing.analyseString(analyze, both[1]);
                        String docId = both[0];
                        //unfortunatelly the lucene analyzer messes up punctuaction, will be solved later on ...
                        wordNodeMap = builder.growGraph(str, docId, sentenceid, topics, documents);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
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
        Main main = new Main();
        String choice = "test/allafrica/allafrica_titles.txt";
        String distr = "test/allafrica/allafrica_ctm_results_titles_4topics.txt";
        String outputFile = "test/allafrica/forceA.csv";
        int numberOfTopic = 2;

        GraphBuilder gb = main.doGenerateSummary(choice, distr, outputFile, numberOfTopic, true, 100);

        System.out.println();
        //traverse(a);
        extract(gb);

//pass parameters, set edges cooccurances, prob|topic, median-
    }

    public static void extract(GraphBuilder graphBuilder) {

        SimpleDirectedWeightedGraph graph = graphBuilder.getGraph();
        List<Object> sorted = graphBuilder.getSortedVertexSet(true);
        System.out.println("size: " + sorted.size());

        List< ArrayDeque<Node>> phrases = new ArrayList< ArrayDeque<Node>>();
        double threshold = -10;

        Iterator vIter = sorted.iterator();
        for (int i = 0; i < 10 && vIter.hasNext(); i++) {//much will be delegated to getForwardPhrase() later
            Node v = (Node) vIter.next();
            ArrayDeque<Node> forwardPhrase = new ArrayDeque<Node>();
            forwardPhrase.addLast(v);

            Node currNode = v;
            double acc = 0;
            while (acc > threshold) {
                Set outEdges = graph.outgoingEdgesOf(currNode);
                ArrayList<Node> outNodes = new ArrayList<Node>();//didn't know how to construct a set
                for (Iterator eIter = outEdges.iterator(); eIter.hasNext();) {
                    outNodes.add((Node) graph.getEdgeTarget(eIter.next()));//converts set of edges to arraylist of respective target nodes
                }

                Node forwardNode = getForwardNode(sorted, outNodes, forwardPhrase);
                if (forwardNode == null) {
                    break;
                }
                forwardPhrase.addLast(forwardNode);
                acc += Math.log(forwardNode.getNodeProb());
                //System.out.println(Math.log(forwardNode.getNodeProb()));
                currNode = forwardNode;
            }

            phrases.add(forwardPhrase);

            //Set inEdges = graph.incomingEdgesOf(v);
        }

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

    	//Set edges = graph.edgesOf(bestVertex);
		/*
         double bestIncomingCount = 0;
         DefaultWeightedEdge bestInEdge = (DefaultWeightedEdge) inEdges.iterator().next();//hopefully not empty
         for (Iterator eIter = inEdges.iterator(); eIter.hasNext();) {//every edge of that node
         DefaultWeightedEdge e = (DefaultWeightedEdge) eIter.next();
         //System.out.println(graph.getEdgeWeight(e));
         double curr = graph.getEdgeWeight(e); //why are edge weights doubles? 
         if (curr > bestIncomingCount){
         bestIncomingCount = curr;
         bestInEdge = e;
         }
         }
         Node inNode = (Node) graph.getEdgeSource(bestInEdge);
         System.out.println(inNode.getNodeName() 
         + "\t" + bestIncomingCount
         + "\t" + inNode.getNodeProb()
         + "\t" + Math.log(inNode.getNodeProb()));
		
         //Set edges = graph.edgesOf(bestVertex);
         double bestOutgoingCount = 0;
         DefaultWeightedEdge bestOutEdge = (DefaultWeightedEdge) outEdges.iterator().next();//hopefully not empty
         for (Iterator eIter = outEdges.iterator(); eIter.hasNext();) {//every edge of that node
         DefaultWeightedEdge e = (DefaultWeightedEdge) eIter.next();
         //System.out.println(graph.getEdgeWeight(e));
         double curr = graph.getEdgeWeight(e); //why are edge weights doubles? 
         if (curr > bestOutgoingCount){
         bestOutgoingCount = curr;
         bestOutEdge = e;
         }
         }
         Node outNode = (Node) graph.getEdgeTarget(bestOutEdge);
         System.out.println(outNode.getNodeName() 
         + "\t" + bestOutgoingCount
         + "\t" + outNode.getNodeProb()
         + "\t" + Math.log(outNode.getNodeProb()));
    	
         System.out.println();
         System.out.println(inNode.getNodeName() 
         + " " + bestVertex.getNodeName() 
         + " " + outNode.getNodeName());
         */
    }

    public static void getForwardPhrase(double threshold) {

    }

    /**
     * @param sorted
     * @param outNodes
     * @return the Node in outNodes with the highest probability given the topic
     */
    public static Node getForwardNode(List<Object> sorted, ArrayList<Node> outNodes, ArrayDeque<Node> forwardPhrase) {
        Iterator vIter = sorted.iterator();

        while (vIter.hasNext()) {
            Node candidate = (Node) vIter.next();
            if (outNodes.contains(candidate) && !forwardPhrase.contains(candidate)) {//!forwardPhrase.contains() prevents cycles
                return candidate;
            }
        }
        return null;
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
