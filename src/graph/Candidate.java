package graph;

import java.util.List;

/**
 * Represents a candidate considered as a topical phrase
 * @author ismini
 */
public class Candidate implements Comparable<Candidate> {

    private boolean discard = false;
    private double support; //product of probabilities of terms
    private double readability;
    private int coverage;
    private String sentence;
    private List<int[]> sentenceList;

    public Candidate(String sentence, List<int[]> sentenceList) {
        this.sentence = sentence;
        this.sentenceList = sentenceList;
    }

    public Candidate(String sentence, double support, List<int[]> sentenceList) {
        this.sentence = sentence;
        this.support = support;
        this.sentenceList = sentenceList;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    public void setSupport(double support) {
        this.support = support;
    }

    public void setReadability(double readability) {
        this.readability = readability;
    }

    public void setCoverage(int coverage) {
        this.coverage = coverage;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setSentList(List<int[]> sentList) {
        this.sentenceList = sentList;
    }
    
    
    public boolean setDiscard() {
       return this.discard ;
    }

    public double setSupport() {
       return this.support;
    }

    public double setReadability() {
       return this.readability ;
    }

    public int setCoverage() {
       return this.coverage;
    }

    public String setSentence() {
       return this.sentence;
    }

    public List<int[]> setSentList() {
       return this.sentenceList;
    }
      
    @Override
    public boolean equals(Object b) {
        if (b instanceof Candidate) {
            Candidate infob = (Candidate) b;
            if (this.sentence.equals(infob.sentence)) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.sentence.hashCode();
    }

    public int compareTo(Candidate info) {
        List sentList2 = info.sentenceList;
        if (((int[]) sentList2.get(0))[0] == ((int[]) this.sentenceList.get(0))[0]) {
            if (this.sentenceList.size() > sentList2.size()) {
                return 1;
            }
            if (this.sentenceList.size() < sentList2.size()) {
                return -1;
            }
            return 0;
        }
        if (((int[]) sentList2.get(0))[0] > ((int[]) this.sentenceList.get(0))[0]) {
            return 1;
        }
        return -1;
    }
}