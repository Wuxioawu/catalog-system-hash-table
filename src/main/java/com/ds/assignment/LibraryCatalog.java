package com.ds.assignment;

import java.util.List;
import java.util.ArrayList;

/**
 * LibraryCatalog – Application domain: University Library Book Management System.
 *
 * Demonstrates the HashTable in a realistic scenario:
 *   - ISBN (String) is the key  →  Book record is the value
 *   - Fast O(1) lookups by ISBN are essential for large collections
 *   - Supports add, search, remove, list-by-author operations
 */
public class LibraryCatalog {

    // ─── Book record ─────────────────────────────────────────────────────────

    public static class Book {
        public final String isbn;
        public final String title;
        public final String author;
        public final int    year;
        public       int    copiesAvailable;

        public Book(String isbn, String title, String author, int year, int copies) {
            this.isbn             = isbn;
            this.title            = title;
            this.author           = author;
            this.year             = year;
            this.copiesAvailable  = copies;
        }

        @Override
        public String toString() {
            return String.format("Book{isbn='%s', title='%s', author='%s', year=%d, copies=%d}",
                    isbn, title, author, year, copiesAvailable);
        }
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    private final HashTable<String, Book> catalog;

    public LibraryCatalog() {
        this.catalog = new HashTable<>(64, 0.70);
    }

    // ─── API ─────────────────────────────────────────────────────────────────

    /** Add or update a book in the catalog. */
    public void addBook(Book book) {
        catalog.put(book.isbn, book);
    }

    /** Look up a book by ISBN.  Returns null if not found. */
    public Book findByISBN(String isbn) {
        return catalog.get(isbn);
    }

    /** Remove a book from the catalog.  Returns the removed book or null. */
    public Book removeBook(String isbn) {
        return catalog.remove(isbn);
    }

    /** Returns true if the ISBN exists in the catalog. */
    public boolean isAvailable(String isbn) {
        Book b = catalog.get(isbn);
        return b != null && b.copiesAvailable > 0;
    }

    /** List all books by a specific author (O(n) full-table scan — expected for this query). */
    public List<Book> findByAuthor(String author) {
        List<Book> result = new ArrayList<>();
        for (String isbn : catalog.keys()) {
            Book b = catalog.get(isbn);
            if (b != null && b.author.equalsIgnoreCase(author)) result.add(b);
        }
        return result;
    }

    /** Checkout: decrement available copies. */
    public boolean checkout(String isbn) {
        Book b = catalog.get(isbn);
        if (b == null || b.copiesAvailable <= 0) return false;
        b.copiesAvailable--;
        return true;
    }

    /** Return: increment available copies. */
    public boolean returnBook(String isbn) {
        Book b = catalog.get(isbn);
        if (b == null) return false;
        b.copiesAvailable++;
        return true;
    }

    public int totalBooks()  { return catalog.size(); }
    public HashTable<String, Book> getTable() { return catalog; }

    // ─── Demo main ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        LibraryCatalog lib = new LibraryCatalog();

        // Populate catalog
        lib.addBook(new Book("978-0-13-468599-1", "Effective Java",            "Joshua Bloch",       2018, 3));
        lib.addBook(new Book("978-0-13-235088-4", "Clean Code",                "Robert C. Martin",   2008, 5));
        lib.addBook(new Book("978-0-596-51774-8", "Java: The Good Parts",      "Jim Waldo",          2010, 2));
        lib.addBook(new Book("978-0-13-110163-0", "The C Programming Language","Brian W. Kernighan", 1988, 4));
        lib.addBook(new Book("978-0-201-63361-0", "Design Patterns",           "Gang of Four",       1994, 2));

        // Demonstrate operations
        System.out.println("=== Library Catalog Demo ===\n");

        Book b = lib.findByISBN("978-0-13-468599-1");
        System.out.println("Found: " + b);

        System.out.println("\nCheckout 'Effective Java': " + lib.checkout("978-0-13-468599-1"));
        System.out.println("Copies left: " + lib.findByISBN("978-0-13-468599-1").copiesAvailable);

        System.out.println("\nBooks by Joshua Bloch:");
        lib.findByAuthor("Joshua Bloch").forEach(System.out::println);

        System.out.println("\nTable stats:");
        System.out.println("  Size     : " + lib.catalog.size());
        System.out.println("  Capacity : " + lib.catalog.capacity());
        System.out.println("  Load     : " + String.format("%.2f", lib.catalog.currentLoadFactor()));
        System.out.println("  Max chain: " + lib.catalog.maxChainLength());
    }
}