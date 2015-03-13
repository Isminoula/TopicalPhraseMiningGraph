package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a node in the graph. Each node is a unique term in our vocabulary.
 * It includes a list of the document ids and a list of the sentence ids that
 * contains this term, prob of term given the topic, total number of nodes
 *
 */
public class Node {

    private List<Integer> docIds = new ArrayList();
    private List<int[]> sentenceIds = new ArrayList(); //<sentenceId,position>
    private String nodeName = "-1";
    private static int TOTAL_TOKENS = 0;
    private int minPos = -1;
    private int avgPos = 0; //the avg term position in a sentence (2nd, 3rd etc term)
    private double currentProb = 0;
    private boolean startNode = false;

    public boolean getStartNode() {
        return startNode;
    }

    public void setStartNode(boolean x) {
        this.startNode = x;
    }

    public double getNodeProb() {
        //double currProb = (this.nodeCount + 0.01D) / (TOTAL_TOKENS + 0.01D);
        return currentProb;
    }

    public void setNodeProb(double x) {
        this.currentProb = x;
    }

    public List<Integer> getDocIds() {
        return this.docIds;
    }

    /**
     * Returns a list of sentenceIds along with the positions in each sentence
     * each value = [sentenceId,position]
     */
    public List<int[]> getSentenceIds() {
        return this.sentenceIds;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void addDocId(int docId) {
        this.docIds.add(Integer.valueOf(docId));
    }

    public void addSentenceId(int sentenceId, int pos) {
        TOTAL_TOKENS += 1;
        int[] k = new int[2];
        k[0] = sentenceId;
        k[1] = pos;
        this.sentenceIds.add(k);
        if (pos < this.minPos) {
            this.minPos = pos;
        }
        this.avgPos += pos;
    }

    public double getAveragePos() {
        return this.avgPos / this.sentenceIds.size();
    }

    public static double getTotalNodeCount() {
        return TOTAL_TOKENS;
    }

    /**
     * Retrieves document ids that this node with another node share
     */
    public List<Integer> getDocumentOverlap(Node n2) {
        List l1 = getDocIds();
        List l2 = n2.getDocIds();
        List l3 = new ArrayList();
        l3.addAll(l1);
        l3.retainAll(l2);
        return l3;
    }

    /**
     * Retrieves sentence ids that this node with another node share
     */
    public List<int[]> getSetenceOverlap(Node n2) {
        List l2 = getSentenceIds();
        List l3 = new ArrayList();
        List<int[]> l1 = n2.getSentenceIds();

        int pointer = 0;
        for (int i = 0; i < l1.size(); i++) {
            int[] elem1 = (int[]) l1.get(i);
            if (pointer > l2.size()) {
                break;
            }
            for (int j = pointer; j < l2.size(); j++) {
                int[] elem2 = (int[]) l2.get(j);

                if (elem2[0] == elem1[0]) {
                    l3.add(elem2);
                    pointer = j + 1;
                } else {
                    if (elem2[0] > elem1[0]) {
                        break;
                    }
                }
            }
        }
        return l3;
    }

    /**
     * Computes Jaccard Similarity (based on common sentence ids) between two
     * nodes
     */
    public double getSetenceJaccardOverlap(Node n2) {
        List<int[]> l1 = getSentenceIds();
        List<int[]> l2 = n2.getSentenceIds();
        int last = 0;
        int intersect = 0;
        HashSet union = new HashSet();
        for (int i = 0; i < l1.size(); i++) {
            int elem1 = ((int[]) l1.get(i))[0];
            union.add(Integer.valueOf(elem1));
            for (int j = last; j < l2.size(); j++) {
                int elem2 = ((int[]) l2.get(j))[0];
                union.add(Integer.valueOf(elem2));
                if (elem2 == elem1) {
                    intersect++;
                    last = j + 1;
                } else {
                    if (elem2 > elem1) {
                        break;
                    }
                }
            }
        }
        double overlap = intersect / union.size();
        return overlap;
    }

    /**
     * Combines a list of sentence ids for two nodes
     */
    public List getSetenceUnion(Node n2) {
        List<int[]> l1 = n2.getSentenceIds();
        List l2 = getSentenceIds();
        List longer;
        List shorter;
        if (l1.size() < l2.size()) {
            shorter = l1;
            longer = l2;
        } else {
            shorter = l2;
            longer = l1;
        }
        List l3 = new ArrayList();
        int pointer = 0;
        for (int i = 0; i < longer.size(); i++) {
            int[] elem1 = (int[]) longer.get(i);
            if (pointer >= shorter.size()) {
                l3.add(elem1);
            }
            for (int j = pointer; j < shorter.size(); j++) {
                int[] elem2 = (int[]) shorter.get(j);
                if (elem2[0] == elem1[0]) {
                    l3.add(elem2);
                    pointer = j + 1;
                    break;
                }
                if (elem2[0] > elem1[0]) {
                    l3.add(elem1);
                    break;
                }
                l3.add(elem2);
                pointer = j + 1;
            }
        }
        return l3;
    }

    public int getDocumentOverlapCount(Node n2) {
        return this.getDocumentOverlap(n2).size();
    }

    @Override
    public int hashCode() {
        return this.nodeName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node n = (Node) o;
            if (n.nodeName.equals(this.nodeName)) {
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Retrieves sentence ids that this node with another node share, but only
     * if the node n2 is on the right of this node
     */
    public List<int[]> getSetenceOverlapRight(Node n2) {
        List<int[]> left = getSentenceIds();
        List right = n2.getSentenceIds();
        List l3 = new ArrayList();
        int pointer = 0;
        for (int i = 0; i < left.size(); i++) {
            int[] eleft = (int[]) left.get(i);
            if (pointer > right.size()) {
                break;
            }
            for (int j = pointer; j < right.size(); j++) {
                int[] eright = (int[]) right.get(j);

                if (eright[0] == eleft[0]) {
                    if ((eright[1] > eleft[1]) && (Math.abs(eright[1] - eleft[1]) <= 4)) {
                        l3.add(eright);
                        pointer = j + 1;
                        break;
                    }
                    eright[1] = eleft[1];
                } else {
                    if (eright[0] > eleft[0]) {
                        break;
                    }
                }
            }
        }
        return l3;
    }

    /**
     * The path probability defined as the number of directed edges going from
     * this node to node n2 (right overlap), divided by the number of total
     * edges that these two nodes have (union)
     */
    public double getSetencePathProb(Node n2) {
        List l2 = getSetenceOverlapRight(n2);
        List l3 = getSetenceUnion(n2);
        double currProb = l2.size() / l3.size();
        return currProb;
    }
}