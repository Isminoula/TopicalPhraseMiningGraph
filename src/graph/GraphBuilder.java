package graph;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import util.SortingSearching;

public class GraphBuilder {

    private static String newline = System.getProperty("line.separator");
    private SimpleDirectedWeightedGraph graph;
    private HashMap wordNodeMap;
    private HashSet stopWords;

    public GraphBuilder() {
        wordNodeMap = new HashMap();
        stopWords = new HashSet();
        graph = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
        loadStopWords("stopwords.txt");
    }

    public SimpleDirectedWeightedGraph getGraph() {
        return graph;
    }

    public HashMap growGraph(String str, int docid, int sid, TreeMap<String, Double> makeProbDistr) {
        return growSentenceGraph(str, docid, sid, makeProbDistr);
    }

    private void loadStopWords(String file) {
        String stopWord;
        try {
            Scanner scanner = new Scanner(new File(file), "UTF-8");
            scanner.useDelimiter(newline);
            while (scanner.hasNextLine()) {
                stopWord = scanner.nextLine().trim();
                stopWords.add(stopWord);
            }
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
    }

    private HashMap growSentenceGraph(String str, int docid, int sid, TreeMap<String, Double> makeProbDistr) {
        String words[] = str.split(" ");
        Node prevVertex = null;
        Node currVertex;
        boolean isPrevVertexNew = true;
        boolean isCurrVertexNew;
        int pos = 0;
        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();//.replaceAll("\\p{Punct}", "");
            if (word.length() != 0 && !stopWords.contains(word)) {
                isCurrVertexNew = true;
                if (wordNodeMap.containsKey(word)) {
                    isCurrVertexNew = false;
                    currVertex = (Node) wordNodeMap.get(word);
                    currVertex.addDocId(docid);
                    currVertex.addSentenceId(sid, i);
                } else {
                    currVertex = new Node();
                    currVertex.setNodeName(word);
                    if (makeProbDistr.containsKey(word) && makeProbDistr.get(word) != 0) {
                        currVertex.setNodeProb(makeProbDistr.get(word));
                    } else {
                        currVertex.setNodeProb(0.00);//do not mesh up probabilities if anything wrong happens!        
                    }
                    currVertex.addDocId(docid);
                    currVertex.addSentenceId(sid, i);
                    graph.addVertex(currVertex);
                    isCurrVertexNew = true;
                    wordNodeMap.put(word, currVertex);
                }
                if (pos == 0) {
                    currVertex.setStartNode(true);
                }
                if (isCurrVertexNew || isPrevVertexNew) {
                    if (prevVertex != null && !currVertex.equals(prevVertex) && canAdd(prevVertex)) {
                        graph.addEdge(prevVertex, currVertex);
                    }
                } else {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) graph.getEdge(prevVertex, currVertex);
                    if (e == null) {
                        try {
                            if (!currVertex.equals(prevVertex) && canAdd(prevVertex)) {
                                graph.addEdge(prevVertex, currVertex);
                            }
                        } catch (IllegalArgumentException e1) {
                            System.err.println((new StringBuilder("Problem Linking '")).append(prevVertex).append("'  and '").append(currVertex).append("'").toString());
                        }
                    } else {
                        double wt = graph.getEdgeWeight(e) + 1.0D;
                        graph.setEdgeWeight(e, wt);
                    }
                }
                prevVertex = currVertex;
                isPrevVertexNew = isCurrVertexNew;
                pos++;
            }
        }
        return wordNodeMap;
    }

    private boolean canAdd(Node prevVertex) {
        return !isTerminal(prevVertex.getNodeName());
    }

    public boolean isTerminal(String str) {
        return (str.equals("!_.") || str.equals("._.") || str.equals("?_."));
    }

    /**
     * checks if the term is a connecting punctation mark
     *
     */
    public boolean isMed(String str) {
        return (str.equals(":_:") || str.equals("-_:") || str.equals(",_,"));
    }

    public void printGraph() {
        Set vertexSet = graph.vertexSet();
        Iterator vIter = vertexSet.iterator();
        System.out.println("digraph{");
        while (vIter.hasNext()) {
            Node v = (Node) vIter.next();
            Set edges = graph.edgesOf(v);
            for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                if (!target.equals(v.getNodeName())) {
                    System.out.println((new StringBuilder(String.valueOf(v.getNodeName()))).append(":").append(v.getNodeProb()).append(":").append(v.getStartNode()).append("->").append(target).append(":").append(((Node) graph.getEdgeTarget(e)).getNodeProb()).append(":").append(((Node) graph.getEdgeTarget(e)).getStartNode()).append(";").append(graph.getEdgeWeight(e)).toString());
                    //System.out.println(graph.getEdgeWeight(e));
                }
            }
        }
        System.out.println("}");
        System.out.println(graph.vertexSet().size());
    }

    /**
     * Save to csv pairs of nodes that are not terminal or connecting
     * punctuation marks and cooccur more than once
     */
    public void saveGraphToCSV(PrintWriter p) {
        if (graph != null || !graph.vertexSet().isEmpty()) {
            p.println("source,target,value");
            Set vertexSet = graph.vertexSet();
            Iterator vIter = vertexSet.iterator();
            while (vIter.hasNext()) {
                Node v = (Node) vIter.next();
                Set edges = graph.edgesOf(v);
                for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                    String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                    String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                    if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                        double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                        double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                        double weight = graph.getEdgeWeight(e);
                        double total = (sourceProb * targetPrb * weight);
                        p.println((new StringBuilder(source)).append(",").append(target).append(",").append(total).toString());
                    }
                }
            }
        }
    }

    /**
     * Save to csv only the subset of the graph in which edges have total score
     * more than the threshold
     */
    public void saveGraphToCSV(PrintWriter p, double threshold) {
        if (graph != null || !graph.vertexSet().isEmpty()) {
            p.println("source,target,value");
            Set vertexSet = graph.vertexSet();
            Iterator vIter = vertexSet.iterator();
            while (vIter.hasNext()) {
                Node v = (Node) vIter.next();
                Set edges = graph.edgesOf(v);
                for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                    String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                    String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                    if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                        double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                        double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                        double weight = graph.getEdgeWeight(e);
                        double total = (sourceProb * targetPrb * weight);
                        if (total > threshold) {
                            p.println((new StringBuilder(source)).append(",").append(target).append(",").append(total).toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate the average score for all edges that are not incoming or
     * outcoming of terminal and connecting punctuation marks and connected
     * nodes have more than one cooccurence
     */
    public double avgGraph() {
        double avg = 0;
        double totalNodes = 0;
        Set vertexSet = graph.vertexSet();
        Iterator vIter = vertexSet.iterator();
        while (vIter.hasNext()) {
            Node v = (Node) vIter.next();
            Set edges = graph.edgesOf(v);
            for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                    double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                    double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                    double weight = graph.getEdgeWeight(e);
                    double total = (sourceProb * targetPrb * weight);
                    avg += total;
                    totalNodes++;
                }
            }
        }
        return (avg / totalNodes);
    }
    
    
    /**
     * Calculate the median score for all edges that are not incoming or
     * outcoming of terminal and connecting punctuation marks and connected
     * nodes have more than one cooccurence
     */
    public double medianGraph() {
        ArrayList<Double> totals = new ArrayList<Double>();
        Set vertexSet = graph.vertexSet();
        Iterator vIter = vertexSet.iterator();
        while (vIter.hasNext()) {
            Node v = (Node) vIter.next();
            Set edges = graph.edgesOf(v);
            for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                    double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                    double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                    double weight = graph.getEdgeWeight(e);
                    double total = (sourceProb * targetPrb * weight);
                    totals.add(total);
                }
            }
        }
        
        Collections.sort(totals);     
        double median;
        if (totals.size() % 2 == 0) {
            median = ((double) (totals.get(totals.size() / 2)) + (double) (totals.get(totals.size() / 2 - 1))) / 2;
        } else {
            median = (double) totals.get(totals.size() / 2);
        }
        return median;    
    }


    /**
     * Calculate the median weight for all edges that are not incoming or
     * outcoming of terminal and connecting punctuation marks and connected
     * nodes have more than one cooccurence
     */
    public double medianWeight() {
        Set edgeSet = graph.edgeSet();
        List sortedset = new ArrayList(edgeSet);
        Collections.sort(sortedset);
        double median;
        if (sortedset.size() % 2 == 0) {
            median = ((double) graph.getEdgeWeight(sortedset.get(sortedset.size() / 2)) + (double) graph.getEdgeWeight(sortedset.get(sortedset.size() / 2 - 1))) / 2;
        } else {
            median = (double) graph.getEdgeWeight(sortedset.get(sortedset.size() / 2));
        }

        return median;
    }

    /**
     * Rank edges based on score
     *
     * @return a hashmap that contains an array of nodes (source and target) and
     * the score of the edge between these two nodes
     */
    public HashMap<Node[], Double> rankEdges() {
        HashMap<Node[], Double> ranked = new HashMap<Node[], Double>();
        Set vertexSet = graph.vertexSet();
        Iterator vIter = vertexSet.iterator();
        while (vIter.hasNext()) {
            Node v = (Node) vIter.next();
            Set edges = graph.edgesOf(v);
            for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                    double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                    double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                    double weight = graph.getEdgeWeight(e);
                    double total = (sourceProb * targetPrb * weight);
                    Node[] nodes = new Node[2];
                    nodes[0] = ((Node) graph.getEdgeTarget(e));
                    nodes[1] = ((Node) graph.getEdgeSource(e));
                    ranked.put(nodes, total);
                }
            }
        }
        HashMap<Node[], Double> sort = SortingSearching.sort(ranked);
        return sort;
    }

    public void saveRankedEdgeListToCSV(PrintWriter p, int topK) {
        double avg = 0;
        double totalNodes = 0;
        HashMap<Node[], Double> rankEdges = rankEdges();
        int count = 0;
        p.println("source,target,value");
        for (Node[] n : rankEdges.keySet()) {
            if (count >= topK) {
                break;
            }
            count++;
            avg += rankEdges.get(n);
            totalNodes++;
            p.println(n[0].getNodeName() + "," + n[1].getNodeName() + "," + rankEdges.get(n));
        }
        System.out.println(avg / (double) totalNodes);
    }

    /**
     * Remove edges below the threshold
     */
    public void removeEdges(double thershold) {
        Set vertexSet = graph.vertexSet();
        Iterator vIter = vertexSet.iterator();
        while (vIter.hasNext()) {
            Node v = (Node) vIter.next();
            Set edges = graph.edgesOf(v);
            for (Iterator edgeIterator = edges.iterator(); edgeIterator.hasNext();) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) edgeIterator.next();
                String target = ((Node) graph.getEdgeTarget(e)).getNodeName();
                String source = ((Node) graph.getEdgeSource(e)).getNodeName();
                if (!target.equals(v.getNodeName()) && graph.getEdgeWeight(e) > 1 && !isTerminal(v.getNodeName()) && !isTerminal(target) && !isMed(v.getNodeName()) && !isMed(target)) {
                    double sourceProb = ((Node) graph.getEdgeTarget(e)).getNodeProb();
                    double targetPrb = ((Node) graph.getEdgeSource(e)).getNodeProb();
                    double weight = graph.getEdgeWeight(e);
                    double total = (sourceProb * targetPrb * weight);
                    if (total < thershold) {
                        graph.removeEdge(e);
                    }
                }
            }
            if (graph.outDegreeOf(v) <= 0 && graph.inDegreeOf(v) <= 0) {
                graph.removeVertex(v);
            }
        }
    }

    /*
        @params - true for reverse, false otherwise
        @output - graph vertex set sorted w.r.t. probability
     */
    public List<Object> getSortedVertexSet(final boolean reverse) {
        Set vertexSet = graph.vertexSet();

        // make a list
        List<Object> vertexList = new ArrayList<Object>(vertexSet);

        // sort the list
        Collections.sort(vertexList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1; Node n2 = (Node) o2;
                if (reverse) {
                    return Double.valueOf(n2.getNodeProb()).compareTo(n1.getNodeProb());
                }
                return Double.valueOf(n1.getNodeProb()).compareTo(n2.getNodeProb());
            }
        });

        return vertexList;
    }

    /*
        @params - threshold value
        @output - list of all possible walks s.t. product(walk) > threshold
     */
    public List<List<Object>> getPossibleWalks(double threshold) {
        /*
            Pseudo Code:
                1. get list of nodes sorted wrt to probability
                2. for every node in the list
                    a. make lists of walk possible
                    b. end walk if product(walk) < threshold or no more outlinks
                    c. add lists to main list
                3. return list of lists

         */
        // get reversed (i.e. max prob first) sorted list of nodes
        List<Object> vertexList = getSortedVertexSet(true);
        List<List<Object>> walks = new ArrayList<List<Object>>();

        // iterate list
        for (Object o : vertexList) {
            Node node = (Node) o;
        }

        return walks;
    }


    private List<Object> walksHelper(Node node) {
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(node);


        return list;
    }

}