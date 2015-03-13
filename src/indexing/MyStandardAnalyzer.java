package indexing;


import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;


/*
 * Caveat! Make my own Analyzer by copy-pasting the StandardAnalyzer source code
 * a then make changes according to my needs,  for example removing stemming 
 */

/*
 * 4 * Licensed to the Apache Software Foundation (ASF) under one or more 5 *
 * contributor license agreements. See the NOTICE file distributed with 6 * this
 * work for additional information regarding copyright ownership. 7 * The ASF
 * licenses this file to You under the Apache License, Version 2.0 8 * (the
 * "License"); you may not use this file except in compliance with 9 * the
 * License. You may obtain a copy of the License at 10 * 11 *
 * http://www.apache.org/licenses/LICENSE-2.0 12 * 13 * Unless required by
 * applicable law or agreed to in writing, software 14 * distributed under the
 * License is distributed on an "AS IS" BASIS, 15 * WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. 16 * See the License for
 * the specific language governing permissions and 17 * limitations under the
 * License. 18
 */
public final class MyStandardAnalyzer extends StopwordAnalyzerBase {

    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
    public int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    public MyStandardAnalyzer(Version matchVersion, CharArraySet stopWords) {
        super(matchVersion, stopWords);
    }

    public MyStandardAnalyzer(Version matchVersion) {
        this(matchVersion, STOP_WORDS_SET);
    }

    public MyStandardAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
        this(matchVersion, loadStopwordSet(stopwords, matchVersion));
    }

    public void setMaxTokenLength(int length) {
        maxTokenLength = length;
    }

    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    @Override
    public TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
        src.setMaxTokenLength(maxTokenLength);
        TokenStream tok = new StandardFilter(matchVersion, src);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {
                
            @Override
            public void setReader(final Reader reader) throws IOException {
                src.setMaxTokenLength(MyStandardAnalyzer.this.maxTokenLength);
                super.setReader(reader);
            }
        };
    }
    
    
}