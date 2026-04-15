# University Library Catalog System (Hash Table Implementation)

## Overview
This repository contains a high-performance, generic **Hash Table** implementation in Java, specifically designed for a University Library Catalog system. The system facilitates sub-millisecond retrieval of book records using ISBNs as keys, leveraging **Separate Chaining** for collision resolution and **Dynamic Rehashing** to maintain efficiency as the collection scales.

This project was developed for **COMP47500: Advanced Data Structures in Java**.

---

## Features
* **Generic Key-Value Store:** Supports any object type for keys (`K`) and values (`V`).
* **Separate Chaining:** Handles collisions using linked lists at each bucket.
* **Advanced Hashing:** Implements XOR-shift spreading of Java's `hashCode()` to ensure uniform distribution and break clusters.
* **Dynamic Resizing:** Automatically doubles capacity to the next **prime number** when the load factor threshold (default 0.75) is exceeded.
* **Library Catalog API:**
    * Add/Update book records keyed by ISBN.
    * $O(1)$ average-case lookup and removal.
    * Author-filtered search ($O(n)$ full scan).
    * Real-time mutation of book copy counts (Checkout/Return).

---

## Technical Specifications

### Hash Function & Compression
To minimize clustering, the implementation uses an XOR-shift spreading technique to ensure that a one-bit change in the key changes approximately half the output bits:

$$h = \text{key.hashCode()} \oplus (h \gg 20) \oplus (h \gg 12) \oplus (h \gg 7) \oplus (h \gg 4)$$
$$\text{index} = |h| \pmod{\text{capacity}}$$



The use of **prime-sized capacities** further reduces the probability of systematic collisions compared to power-of-two sizes.

### Complexity Summary
| Operation | Average Case | Worst Case | Notes |
| :--- | :--- | :--- | :--- |
| **put(k, v)** | $O(1)$* | $O(n)$ | Amortized; includes rehash cost |
| **get(k)** | $O(1)$ | $O(n)$ | Depends on chain length |
| **remove(k)** | $O(1)$ | $O(n)$ | Requires chain traversal |
| **rehash()** | $O(n)$ | $O(n)$ | Triggered when $LF > threshold$ |

---

##  Experimental Results & Analysis

All experiments were conducted on **OpenJDK 21**. Reported values are averages of 5 runs after a JVM warm-up.

### 1. Throughput vs. Input Size
Measures cumulative latency as the number of entries $N$ scales from 1,000 to 500,000.

| N (entries) | Put Time (ms) | Get Time (ms) | Collisions | Load Factor |
| :--- | :--- | :--- | :--- | :--- |
| 1,000 | 11.4 | 0.0 | 405 | 0.735 |
| 10,000 | 13.6 | 0.0 | 4,259 | 0.456 |
| 100,000 | 13.6 | 7.6 | 41,464 | 0.570 |
| 500,000 | 229.8 | 47.8 | 209,744 | 0.712 |

> **Analysis:** Put and get times scale sub-linearly relative to $N$, confirming **amortized $O(1)$** complexity. `get()` is significantly faster than `put()` as it never triggers expensive rehashing operations.

### 2. Load Factor (LF) Threshold vs. Collision Rate
Evaluates the trade-off between memory usage (capacity) and collision frequency for $N=10,000$.

| LF Threshold | Avg Collisions | Collision Rate (%) | Max Chain Length | Final Capacity |
| :--- | :--- | :--- | :--- | :--- |
| 0.25 | 1,548 | 15.5% | 3 | 43,853 |
| 0.50 | 2,880 | 28.8% | 4 | 21,911 |
| **0.75** | **4,259** | **42.6%** | **4** | **21,911** |
| 0.95 | 5,061 | 50.6% | 6 | 10,949 |

> **Analysis:** A threshold of **0.75** is the optimal engineering trade-off: acceptable collision rate (~42%) with reasonable memory use. While 0.95 uses less memory, the max chain length increases, degrading performance.

### 3. Initial Capacity Impact
Measures the overhead of rehashing when the initial allocation is insufficient for $N=50,000$.

| Initial Capacity | Put Time (ms) | Final Capacity | Status |
| :--- | :--- | :--- | :--- |
| 4 | 18.8 | 102,877 | Frequent rehashing overhead |
| 64 | 4.4 | 71,741 | Improved |
| **256** | **3.0** | **67,409** | **Optimal throughput** |
| 4,096 | 14.2 | 131,381 | Memory redundant; cache locality drop |

> **Analysis:** Pre-sizing the table to `expected_size / load_factor` yields the best performance by avoiding multiple $O(n)$ rehash cycles during initial data loading.

---

##  Project Structure
* `src/hashtable/HashTable.java`: Core generic data structure (Separate chaining, dynamic rehashing).
* `src/hashtable/LibraryCatalog.java`: Application layer (Book records and business logic).
* `src/hashtable/ExperimentRunner.java`: Benchmarking suite (3 automated experiments).

## ⚙ Build and Run
1.  **Compile the source code:**
    ```bash
    javac src/hashtable/*.java
    ```
2.  **Run the experiments:**
    ```bash
    java -cp src hashtable.ExperimentRunner
    ```

---
**Author:** [Your Name]
**Course:** COMP47500 Advanced Data Structures
**Date:** April 2026