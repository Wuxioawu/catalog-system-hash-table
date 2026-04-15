package com.ds.assignment;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Hash Table implementation using Separate Chaining for collision resolution.
 * Supports dynamic resizing (rehashing) when load factor exceeds threshold.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class HashTable<K, V> {

    // ─── Inner classes ────────────────────────────────────────────────────────

    /** A single key-value entry stored inside the hash table. */
    public static class Entry<K, V> {
        K key;
        V value;

        public Entry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + key + " -> " + value + "]";
        }
    }

    // ─── Constants ────────────────────────────────────────────────────────────

    private static final int    DEFAULT_CAPACITY    = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    // ─── Fields ───────────────────────────────────────────────────────────────

    private LinkedList<Entry<K, V>>[] table;
    private int    size;           // number of key-value pairs stored
    private int    capacity;       // number of buckets
    private double loadFactor;     // threshold that triggers rehashing

    // Statistics counters (reset on clear / rehash)
    private long totalPutOps    = 0;
    private long totalGetOps    = 0;
    private long totalCollisions = 0;

    // ─── Constructors ─────────────────────────────────────────────────────────

    public HashTable() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public HashTable(int initialCapacity, double loadFactor) {
        if (initialCapacity <= 0)  throw new IllegalArgumentException("Capacity must be positive");
        if (loadFactor <= 0 || loadFactor > 1) throw new IllegalArgumentException("Load factor must be in (0,1]");

        this.capacity   = nextPrime(initialCapacity);
        this.loadFactor = loadFactor;
        this.table      = new LinkedList[this.capacity];
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Inserts or updates a key-value pair.  O(1) amortised.
     */
    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        totalPutOps++;
        int index = hash(key);

        if (table[index] == null) {
            table[index] = new LinkedList<>();
        } else {
            // Collision detected — probe existing chain
            totalCollisions++;
            for (Entry<K, V> entry : table[index]) {
                if (entry.key.equals(key)) {
                    entry.value = value;   // update in place
                    return;
                }
            }
        }

        table[index].add(new Entry<>(key, value));
        size++;

        // Rehash if load factor is exceeded
        if ((double) size / capacity > loadFactor) {
            rehash();
        }
    }

    /**
     * Returns the value associated with key, or null if absent.  O(1) average.
     */
    public V get(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        totalGetOps++;
        int index = hash(key);

        if (table[index] != null) {
            for (Entry<K, V> entry : table[index]) {
                if (entry.key.equals(key)) return entry.value;
            }
        }
        return null;
    }

    /**
     * Removes the entry for key and returns its value, or null if absent.  O(1) average.
     */
    public V remove(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");

        int index = hash(key);

        if (table[index] != null) {
            for (Entry<K, V> entry : table[index]) {
                if (entry.key.equals(key)) {
                    table[index].remove(entry);
                    size--;
                    return entry.value;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if key is present.
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Number of key-value pairs stored. */
    public int size() { return size; }

    /** Current number of buckets. */
    public int capacity() { return capacity; }

    /** Current load factor (size / capacity). */
    public double currentLoadFactor() { return (double) size / capacity; }

    public long getTotalPutOps()     { return totalPutOps;     }
    public long getTotalGetOps()     { return totalGetOps;      }
    public long getTotalCollisions() { return totalCollisions;  }

    /**
     * Returns the length of the longest chain (worst-case lookup).
     */
    public int maxChainLength() {
        int max = 0;
        for (LinkedList<Entry<K, V>> bucket : table) {
            if (bucket != null && bucket.size() > max) max = bucket.size();
        }
        return max;
    }

    /** Returns the number of non-empty buckets. */
    public int occupiedBuckets() {
        int count = 0;
        for (LinkedList<Entry<K, V>> bucket : table) {
            if (bucket != null && !bucket.isEmpty()) count++;
        }
        return count;
    }

    /** Returns all keys stored in the table. */
    public List<K> keys() {
        List<K> result = new ArrayList<>(size);
        for (LinkedList<Entry<K, V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K, V> e : bucket) result.add(e.key);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HashTable{capacity=").append(capacity)
          .append(", size=").append(size)
          .append(", load=").append(String.format("%.2f", currentLoadFactor()))
          .append("}\n");
        for (int i = 0; i < capacity; i++) {
            if (table[i] != null && !table[i].isEmpty()) {
                sb.append("  [").append(i).append("] -> ").append(table[i]).append("\n");
            }
        }
        return sb.toString();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * MAD (Multiply-Add-Divide) hash function.
     * Uses Java's built-in hashCode() and spreads bits with XOR-shift,
     * then reduces modulo a prime-sized table.
     */
    private int hash(K key) {
        int h = key.hashCode();
        // XOR-shift to reduce clustering from poor hashCode() implementations
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7)  ^ (h >>> 4);
        return Math.abs(h) % capacity;
    }

    /**
     * Doubles the table size (to next prime) and re-inserts all entries.
     * O(n) time.
     */
    @SuppressWarnings("unchecked")
    private void rehash() {
        int newCapacity        = nextPrime(capacity * 2);
        LinkedList<Entry<K, V>>[] newTable = new LinkedList[newCapacity];

        for (LinkedList<Entry<K, V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    int newIndex = rehashIndex(entry.key, newCapacity);
                    if (newTable[newIndex] == null) newTable[newIndex] = new LinkedList<>();
                    newTable[newIndex].add(entry);
                }
            }
        }

        this.table    = newTable;
        this.capacity = newCapacity;
    }

    private int rehashIndex(K key, int cap) {
        int h = key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7)  ^ (h >>> 4);
        return Math.abs(h) % cap;
    }

    /** Returns the smallest prime >= n. */
    private static int nextPrime(int n) {
        if (n <= 2) return 2;
        int candidate = (n % 2 == 0) ? n + 1 : n;
        while (!isPrime(candidate)) candidate += 2;
        return candidate;
    }

    private static boolean isPrime(int n) {
        if (n < 2)  return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}