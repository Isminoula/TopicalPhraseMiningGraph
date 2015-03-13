package util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Reads term and document distributions from CPLSA suplementary file currently
 * reading expert models as well, but not being used anywhere Takes into account
 * the structure of the CPLSA suplementary file, must be careful with changes TO
 * BE CHANGED LATER (STRUCTURE OF INPUT IS NOT WELL DEFINED).
 *
 * @author isminilourentzou
 */
public class ReadDistributions {

    public static TreeMap<Integer, TreeMap<String, Double>> docProbs = new TreeMap<Integer, TreeMap<String, Double>>();
    public static TreeMap<Integer, TreeMap<String, Double>> commonModel = new TreeMap<Integer, TreeMap<String, Double>>();
    public static TreeMap<Integer, TreeMap<Integer, TreeMap<String, Double>>> expertModels = new TreeMap<Integer, TreeMap<Integer, TreeMap<String, Double>>>();

    /**
     * Read output of CTM and stores the document and term distributions for
     * each topic in three static treemaps.
     *
     * @param fileName the path of the file that contains the CTM output
     */
    public static void readFile(String fileName) {
        BufferedReader br = null;
        try {

            String line;
            boolean foundDocProb = false;
            boolean foundCommonTermProb = false;
            int cluster = 0;
            int expertModel = -1;

            br = new BufferedReader(new FileReader(fileName));

            while ((line = br.readLine()) != null) {
                if (line.equals("Document Probabilities Start")) {
                    foundDocProb = true;
                } else if (line.equals("Document Probabilities End")) {
                    foundDocProb = false;
                } else if (foundDocProb) {
                    String[] split = line.split("\\s+");
                    String docIndex = split[1];
                    int topicIndex = Integer.parseInt(split[3]);
                    double docProb = Double.parseDouble(split[5]);
                    if (!docProbs.containsKey(topicIndex)) {
                        docProbs.put(topicIndex, new TreeMap<String, Double>());
                    }
                    docProbs.get(topicIndex).put(docIndex, docProb);
                } else if (line.contains("cluster ")) {
                    cluster = Integer.parseInt(line.split("\\s+")[1]);
                } else if (line.equals("common model all probabilities start")) {
                    foundCommonTermProb = true;
                    commonModel.put(cluster, new TreeMap<String, Double>());
                } else if (line.equals("common model all probabilities end")) {
                    foundCommonTermProb = false;
                } else if (foundCommonTermProb) {
                    String[] split = line.split("\\s+");
                    String term = split[0].trim();
                    double termProb = Double.parseDouble(split[1]);
                    commonModel.get(cluster).put(term, termProb);
                } else if (line.contains("expert model") && line.contains("all probabilities start")) {
                    expertModel = Integer.parseInt(line.replace("expert model (", "").replace(") all probabilities start", ""));
                    if (!expertModels.containsKey(expertModel)) {
                        expertModels.put(expertModel, new TreeMap<Integer, TreeMap<String, Double>>());
                    }
                    expertModels.get(expertModel).put(cluster, new TreeMap<String, Double>());
                } else if (line.contains("expert model") && line.contains("all probabilities end")) {
                    expertModel = -1;
                } else if (expertModel != -1) {
                    String[] split = line.split("\\s+");
                    String term = split[0].trim();
                    double termProb = Double.parseDouble(split[1]);
                    expertModels.get(expertModel).get(cluster).put(term, termProb);
                }
            }
        } catch (IOException e) {
            Logger.getLogger(ReadDistributions.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ReadDistributions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
