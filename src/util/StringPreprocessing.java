package util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Basic String preprocessing utilities
 */
public class StringPreprocessing {

    /**
     * Removes links from string
     */
    public static String removeUrl(String commentstr) {
        commentstr = commentstr.replaceAll("\\)", "").replaceAll("\\(", "").replaceAll("\\?", "");
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):(\\/\\/)+([^\r\n\t\f\\\n\\s]+))"; //"((https?|ftp|gopher|telnet|file|Unsure|http):((\\\\)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        while (m.find()) {
            if (m.group(0) != null) {
                commentstr = commentstr.replaceAll(m.group(0), "").trim();
            }
        }
        return commentstr;
    }

    /**
     * Converts new lines into whitespaces, plus removes multiple occurances of
     * whitespaces
     */
    public static String removeNewLinesMultipleWhitespaces(String text) {
        String replaceAll = text.replaceAll("\\s+", " ").replaceAll("\\\\n", " ").replaceAll("(\\t|\\r?\\n)+", " ");
        return replaceAll;
    }

    public static String standardize(String text) {
        String replaceAll = text.replaceAll("\\<.*?>", " ").replaceAll("<", "&gt;").replaceAll(">", "&lt;").replaceAll("&", "&amp;");
        return replaceAll;
    }

    /**
     * Strips /uXXXX from a string and replaces it with the correct unicode char
     *
     * @param slashed string containing '/uXXXX' to be replaced
     * @return Unicode string with '/uXXXX' converted into Unicode.
     */
    public static String unslashUnicode(String slashed) {
        ArrayList<String> pieces = new ArrayList<String>();
        String urlPattern = "(\\\\u[a-fA-F0-9]{4})";
        Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(slashed);
        while (m.find()) {
            if (m.group(0) != null) {
                pieces.add(slashed.substring(0, slashed.indexOf("\\u")));//add the bit before the /uXXXX
                char c = (char) Integer.parseInt(slashed.substring(slashed.indexOf("\\u") + 2, slashed.indexOf("\\u") + 6), 16);
                slashed = slashed.substring(slashed.indexOf("\\u") + 6, slashed.length());
                pieces.add(c + "");//add the  unicode
            }
        }
        String temp = "";
        for (String s : pieces) {
            temp = temp + s;//put humpty dumpty back together again
        }
        slashed = temp + slashed;
        return slashed;
    }

    /**
     * Does all the pre-processing in the "correct" order
     *
     * @param text the string to be pre-processed
     * @return the string pre-processed (removing multiple whitespaces and
     * lines, urls and coverts to unicode
     */
    public static String preprocess(String text) {
        return standardize(removeUrl(unslashUnicode(removeNewLinesMultipleWhitespaces(text))));
    }

     /**
     * Pre-process using lucene analyzer
     *
     * @param stringToAnalyse the string to be pre-processed
     * @param analyser the lucene analyzer used for pre-processing
     * @return the string pre-processed 
     */
    public static String analyseString(Analyzer analyser, String stringToAnalyse) {
        StringBuilder sb = new StringBuilder();
        try {
            TokenStream tokenStream = analyser.tokenStream("", new StringReader(stringToAnalyse));
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                sb.append(term).append(" ");
            }
        } catch (IOException ex) {
            Logger.getLogger(StringPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
}
