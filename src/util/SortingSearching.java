/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ismini
 */
public class SortingSearching {

    /**
     * Sorts a HashMap
     *  ftinousa
     * @param map the hashmap to be sorted
     * @return the map sorted
     */
    public static HashMap sort(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
    
       /**
     * Sorts a HashMap
     *  auskousa
     * @param map the hashmap to be sorted
     * @return the map sorted
     */
    public static HashMap sortAusksousa(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    /**
     * Search by value in hashmap
     * 
     * @param temp : the hashmap to be searched
     * @param value the value searched
     * @return term that matches the value in search
     */
    public static String findValue(HashMap<String, Integer> temp, int value) {
        String found;
        for (String t : temp.keySet()) {
            if (temp.get(t).equals(value)) {
                found = t;
                return found;
            }
        }
        return null;
    }

    /**
     * Binary search in a sorted array.
     *
     * @param arr the array searched
     * @param targetValue value searched
     * @return true if arr contains targetValue, false otherwise
     */
    public static boolean containsBinarySearch(int[] arr, int targetValue) {
        int a = Arrays.binarySearch(arr, targetValue);
        if (a >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
}
