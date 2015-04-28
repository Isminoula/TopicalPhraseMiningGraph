/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Two distinct methods for sentence chunking
 */
public class SentenceTokenization {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
    static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY, SENTENCE_MODEL);

    /**
     * LingPipe: extremely fast but less accurate
     *
     * @param text the text to be splitted into sentences
     * @return an arraylist of the sentences
     */
    public static ArrayList<String> sentenceSplitter(String text) {
        text = text.replaceAll("\\.+", ".");
        ArrayList<String> sent = new ArrayList<String>();
        Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
        Set<Chunk> sentences = chunking.chunkSet();
        if (sentences.size() < 1) {
            //System.out.println("No sentence chunks found.\n"+text);
            sent.add(text);
            return sent;
        }
        String slice = chunking.charSequence().toString();
        int i = 1;
        for (Iterator<Chunk> it = sentences.iterator(); it.hasNext();) {
            Chunk sentence = it.next();
            int start = sentence.start();
            int end = sentence.end();
            sent.add(slice.substring(start, end));
        }
        return sent;
    }

           
}
