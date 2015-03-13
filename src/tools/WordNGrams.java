package tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a string into n-grams
 * 
 */
public class WordNGrams {

    /**
     * Splits string into n-grams.
     *
     * @param max the order of the n-grams (unigrams, bigrams etc)
     * @param val the string to be splitted into n-grams
     * @return a list of n-grams
     */
    public static List<String> ngrams(int max, String val) {
        List<String> out = new ArrayList<String>(1000);
        //String[] words = val.toLowerCase().replaceAll("(/[a-z,:.;$]+(\\s+|$))", " ").split("\\s+");
        String[] words = val.toLowerCase().split("\\s+");

        for (int i = 0; i < words.length - max + 1; i++) {
            out.add(makeString(words, i, max));
        }
        return out;
    }

    private static String makeString(String[] words, int start, int length) {
        StringBuilder tmp = new StringBuilder(100);
        for (int i = start; i < start + length; i++) {
            tmp.append(words[i]).append(" ");
        }
        return tmp.substring(0, tmp.length() - 1);
    }

    /**
     * Creates lower-order ngrams, given a list of n-grams
     *
     * @param in the list of n-grams to be reduced to lower n-grams
     * @param size the minimum order of n-grams we will produce
     * @return a list of n-grams (all n-grams in one list)
     */
    public static List<String> reduceNgrams(List<String> in, int size) {
        if (1 < size) {
            List<String> working = reduceByOne(in);
            in.addAll(working);
            for (int i = size - 2; i > 0; i--) {
                working = reduceByOne(working);
                in.addAll(working);
            }
        }
        return in;
    }

    private static List<String> reduceByOne(List<String> in) {
        List<String> out = new ArrayList<String>(in.size());
        int end;
        for (String s : in) {
            end = s.lastIndexOf(" ");
            out.add(s.substring(0, -1 == end ? s.length() : end));
        }
        String s = in.get(in.size() - 1);
        out.add(s.substring(s.indexOf(" ") + 1));
        return out;
    }

    /**
     * Prints list of ngrams
     *
     * @param ngrams the list of n-grams to be printed
     */
    public static void printList(List<String> ngrams) {
        for (String s : ngrams) {
            System.out.println(s);
        }
    }
    /*
     * public static void main(String[] args) { long start; start =
     * System.currentTimeMillis(); List<String> ngrams = ngrams(2, "According to
     * news reports from Ato near the Ethopian/Somalia border,");
     * //reduceNgrams(ngrams, 3); printList(ngrams);
     * System.out.println(System.currentTimeMillis() - start); }
     *
     */
}