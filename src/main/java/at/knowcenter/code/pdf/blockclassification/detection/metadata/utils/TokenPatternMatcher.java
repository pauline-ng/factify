/*******************************************************************************
 * Copyright (C) 2010 
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH" 
 * (Know-Center), Graz, Austria, office@know-center.at.
 * 
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with 
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.knowcenter.code.pdf.blockclassification.detection.metadata.utils;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Matcher for token patterns.
 * 
 * Note: Not thread safe.
 * 
 * @param <T> the type of the payload
 * 
 * @author rkern@know-center.at
 */
public class TokenPatternMatcher<T> {
    private final Map<String, HeadEntry<T>> patternMap;
    private final TokenTextReader tokenTextReader; 
    
    /**
     * Creates a new instance of this class.
     * @param tokenTextReader 
     * @param patternMap
     */
    TokenPatternMatcher(TokenTextReader tokenTextReader, Map<String, HeadEntry<T>> patternMap) {
        this.tokenTextReader = tokenTextReader;
        this.patternMap = patternMap;
    }

    /**
     * Builder for token pattern matchers
     * 
     * @param <T>
     * 
     * @author rkern@know-center.at
     */
    public static final class Builder<T> {
        Map<String, HeadEntry<T>> patternMap = new TreeMap<String, HeadEntry<T>>();
        int maxPatternLength = -1;
        private final TokenTextReader tokenTextReader;
        
        /**
         * Creates a new instance of this class.
         * @param tokenTextReader 
         */
        public Builder(TokenTextReader tokenTextReader) {
            this.tokenTextReader = tokenTextReader;
        }
        
        /**
         * @param payload
         * @param patternTokens
         */
        public void addPattern(Iterable<TextSpan> patternTokens, T payload) {
            if (patternTokens == null) { throw new IllegalArgumentException("Argument 'patternTokens' must not be null."); }
            if (payload == null) { throw new IllegalArgumentException("Argument 'payload' must not be null."); }
            
            String lastTokenText = null;
            List<String> tokens = new ArrayList<String>();
            for (TextSpan a : patternTokens) {
                if (lastTokenText != null) { tokens.add(lastTokenText); }
                lastTokenText = tokenTextReader.getTokenText(a);
            }
            if (lastTokenText == null) { throw new IllegalArgumentException("No tokens found for the payload '" + payload + "'"); }
            
            HeadEntry<T> headEntry = patternMap.get(lastTokenText);
            if (headEntry == null) {
                headEntry = new HeadEntry<T>(lastTokenText);
                patternMap.put(lastTokenText, headEntry);
            }
            headEntry.addEntry(tokens, payload);
            
            if (maxPatternLength < tokens.size()+1) {
                maxPatternLength = tokens.size()+1;
            }
        }
        
        /**
         * @return the matcher
         */
        public TokenPatternMatcher<T> build() {
            return new TokenPatternMatcher<T>(tokenTextReader, patternMap);
        }
    }
    /**
     * @param tokens
     * @param callback 
     */
    public void processDocument(Iterable<TextSpan> tokens, Callback<T> callback) {
        if (tokens == null) { throw new IllegalArgumentException("Argument 'tokens' must not be null."); }
        
        List<String> previousTokens = new LinkedList<String>();
        TIntArrayList startPositions = new TIntArrayList();
        for (TextSpan a : tokens) {
            String tokenText = tokenTextReader.getTokenText(a);
            
            HeadEntry<T> headEntry = patternMap.get(tokenText);
            if (headEntry != null) {
                List<Match<T>> matches = new LinkedList<Match<T>>();
                Entry<T> entry = headEntry.firstEntry;
                while (entry != null) {
                    if (entry.tokens == null) {
                        matches.add(new Match<T>(entry.payload, a.getStart(), a.getEnd()));
                    } else {
                        if (endsWith(previousTokens, entry.tokens)) {
                            matches.add(new Match<T>(entry.payload, 
                                    startPositions.get(startPositions.size()-entry.tokens.size()), a.getEnd()));
                        }
                    }
                    entry = entry.nextEntry;
                }
                if (!matches.isEmpty()) {
                    callback.onMatch(matches);
                }
            }
            
            previousTokens.add(tokenText);
            startPositions.add(a.getStart());
        }
    }
    
    private boolean endsWith(List<String> sequence, List<String> suffix) {
        if (sequence.size() < suffix.size()) { return false; }
        if (suffix.isEmpty()) { return true; }
        
        ListIterator<String> seq = sequence.listIterator(sequence.size());
        ListIterator<String> suf = suffix.listIterator(suffix.size());
        boolean isEqual = true;
        while (suf.hasPrevious()) {
            if (!suf.previous().equals(seq.previous())) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    private static final class HeadEntry<T> {
        final String lastToken;
        Entry<T> firstEntry;

        /**
         * Creates a new instance of this class.
         * @param lastToken
         */
        public HeadEntry(String lastToken) {
            this.lastToken = lastToken;
        }
        
        public void addEntry(List<String> tokens, T link) {
            Entry<T> linkEntry = new Entry<T>(!tokens.isEmpty() ? tokens : null, link, firstEntry);
            firstEntry = linkEntry;
        }
    }
    
    private static final class Entry<T> {
        final List<String> tokens;
        final T payload;
        final Entry<T> nextEntry;
        
        /**
         * Creates a new instance of this class.
         * @param list
         * @param payload 
         */
        Entry(List<String> list, T payload, Entry<T> nextEntry) {
            this.tokens = list;
            this.payload = payload;
            this.nextEntry = nextEntry;
        }
    }
    
    /**
     * @param <T>
     * 
     * @author rkern@know-center.at
     */
    public static final class Match<T> {
        public final T payload;
        public final int start;
        public final int end;

        /**
         * Creates a new instance of this class.
         * @param payload
         * @param start
         * @param end
         */
        Match(T payload, int start, int end) {
            this.payload = payload;
            this.start = start;
            this.end = end;
        }
        
    }
    
    public interface Callback<T> {

        /**
         * @param matches
         */
        void onMatch(List<Match<T>> matches);

        
    }


}
