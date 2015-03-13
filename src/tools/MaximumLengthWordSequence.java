/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.List;
import tools.WordNGrams;

/**
 * Finds the maximum common substring between two strings
 *
 */
public class MaximumLengthWordSequence {

    /**
     * First version of finding the longerst common substring, returns the
     * maximum length of the common substring
     *
     * @param str1 string to compare
     * @param str2 string to compare
     * @return the maximum length of the common substring
     */
    public static int lcs(String str1, String str2) {
        List<String> ngrams = WordNGrams.ngrams(str2.split("\\s+").length, str2);
        WordNGrams.reduceNgrams(ngrams, str2.split("\\s+").length);
        //printList(ngrams);
        int max = 0;
        for (String s : ngrams) {
            if (str1.contains(s)) {
                max = Math.max(max, s.length());
            }
        }
        return max;
    }

    /**
     * Second version of finding the longerst common substring, returns the
     * maximum length of the common substring
     *
     * @param str1 string to compare
     * @param str2 string to compare
     * @return the maximum length of the common substring
     */
    public static int longestSubstr(String str1, String str2) {
        if (str1 == null || str2 == null || str1.length() == 0 || str2.length() == 0) {
            return 0;
        }

        int maxLen = 0;
        int fl = str1.length();
        int sl = str2.length();
        int[][] table = new int[fl + 1][sl + 1];

        for (int s = 0; s <= sl; s++) {
            table[0][s] = 0;
        }
        for (int f = 0; f <= fl; f++) {
            table[f][0] = 0;
        }

        for (int i = 1; i <= fl; i++) {
            for (int j = 1; j <= sl; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    if (i == 1 || j == 1) {
                        table[i][j] = 1;
                    } else {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }
                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                    }
                }
            }
        }
        return maxLen;
    }

    /**
     * Thrid version of finding the longerst common substring, returns the
     * common substring
     *
     * @param str1 string to compare
     * @param str2 string to compare
     * @return the common substring between two strings
     */
    public static String longestCommonSubstring(String str1, String str2) {
        int Start = 0;
        int Max = 0;
        for (int i = 0; i < str1.length(); i++) {
            for (int j = 0; j < str2.length(); j++) {
                int x = 0;
                while (str1.charAt(i + x) == str2.charAt(j + x)) {
                    x++;
                    if (((i + x) >= str1.length()) || ((j + x) >= str2.length())) {
                        break;
                    }
                }
                if (x > Max) {
                    Max = x;
                    Start = i;
                }
            }
        }
        return str1.substring(Start, (Start + Max));
    }
}
