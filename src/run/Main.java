package run;


import graph.GraphBuilder;
import indexing.MyStandardAnalyzer;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import util.*;

/**
 *
 * @author isminilourentzou
 */
public class Main {//

    HashSet<String> sentences = new HashSet<String>();
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
    public void doGenerateSummary(String fileName, String distr, String outfile, int topicId, boolean removeEdges, int topK) {
        PrintWriter csv = null;
        try {
            csv = new PrintWriter(new File(outfile));
            //Create a new empty graph
            SimpleDirectedWeightedGraph g;
            GraphBuilder builder = new GraphBuilder();
            //Read the topic model distributions
            ReadDistributions.readFile(distr);
            //Get the topic of interest
            TreeMap<String, Double> topics = ReadDistributions.commonModel.get(topicId);
            HashMap wordNodeMap = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String str;
                int sentenceid = 0;
                while ((str = reader.readLine()) != null) {
                    sentenceid++;
                    str = StringPreprocessing.analyseString(analyze, str);
                    //unfortunatelly the lucene analyzer messes up punctuaction, will be solved later on ...
                    sentences.add(str.toLowerCase() + " ._.");
                    wordNodeMap = builder.growGraph(str, 1, sentenceid, topics);
                }
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
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


    }

    public static void main(String[] args) {
        Main main = new Main();
        String choice = "test/allafricaApril/titles_allafrica.txt";
        String distr = "test/allafricaApril/allafrica_ctm_results_titles_4topics.txt";
        String outputFile = "test/allafricaApril/force.csv";
        int numberOfTopic = 2;
        main.doGenerateSummary(choice, distr, outputFile, numberOfTopic, true, 100);
    }
}
