/**
 * This program implements a generic library catalog system in Java.
 * It manages various library items like books, DVDs, and magazines using 
 * generic classes for flexibility. Key features include adding, removing, 
 * retrieving items, and viewing the catalog through a simple CLI, with 
 * proper error handling for operations like removing non-existent items.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class ItemNotFoundException extends Exception {
    public ItemNotFoundException(String msg) {
        super(msg);
    }
}

class LibraryItem<T> {
    private String title;
    private String author;
    private T itemID;

    public LibraryItem(String title, String author, T itemID) {
        this.title = title;
        this.author = author;
        this.itemID = itemID;
    }

    public T getItemID() {
        return itemID;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": ItemID: " + itemID + ", Title: " + title + ", Author: " + author;
    }
}

class Book extends LibraryItem<String> {
    public Book(String title, String author, String itemID) {
        super(title, author, itemID);
    }
}

class DVD extends LibraryItem<String> {
    public DVD(String title, String author, String itemID) {
        super(title, author, itemID);
    }
}

class Magazine extends LibraryItem<String> {
    public Magazine(String title, String author, String itemID) {
        super(title, author, itemID);
    }
}

class Catalog<T extends LibraryItem<ID>, ID> {
    private List<T> libraryItems = new ArrayList<>();

    public void addItem(T item) {
        libraryItems.add(item);
    }

    public void removeItem(ID itemID) throws ItemNotFoundException {
        boolean found = false;
        for (int i = 0; i < libraryItems.size(); i++) {
            if (libraryItems.get(i).getItemID().equals(itemID)) {
                libraryItems.remove(i);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new ItemNotFoundException("No item found with ID: " + itemID);
        }
    }

    public T getItem(ID itemID) throws ItemNotFoundException {
        for (T item : libraryItems) {
            if (item.getItemID().equals(itemID)) {
                return item;
            }
        }
        throw new ItemNotFoundException("No item found with ID: " + itemID);
    }

    public void displayCatalog() {
        if (libraryItems.isEmpty()) {
            System.out.println("The catalog is empty right now.");
            return;
        }
        for (T item : libraryItems) {
            System.out.println(item);
        }
    }
}

public class LibraryCatalog {
    public static void main(String[] args) {
        Catalog<LibraryItem<String>, String> catalog = new Catalog<>();

        // Adding some sample items for testing
        catalog.addItem(new Book("Java Programming", "Author1", "B001"));
        catalog.addItem(new DVD("Inception", "Director1", "D001"));
        catalog.addItem(new Magazine("Tech Monthly", "Editor1", "M001"));

        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        do {
            System.out.println("\nLibrary Catalog Menu:");
            System.out.println("1. Add a new item");
            System.out.println("2. Remove an item");
            System.out.println("3. View the catalog");
            System.out.println("4. Get item details");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    String input;
                    String selectedType = "";
                    do {
                        System.out.print("Enter item type (a:Book, b:DVD, c:Magazine): ");
                        input = scanner.nextLine().trim().toLowerCase();
                        if (input.equals("a")) {
                            selectedType = "Book";
                        } else if (input.equals("b")) {
                            selectedType = "DVD";
                        } else if (input.equals("c")) {
                            selectedType = "Magazine";
                        } else {
                            System.out.println("Invalid type. Please enter a, b, or c.");
                            continue;
                        }
                        System.out.println("Item selected: " + selectedType);
                        break;
                    } while (true);

                    System.out.print("Enter title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter author/director/editor: ");
                    String author = scanner.nextLine();
                    System.out.print("Enter item ID: ");
                    String itemID = scanner.nextLine();
                    LibraryItem<String> newItem;
                    if (input.equals("a")) {
                        newItem = new Book(title, author, itemID);
                    } else if (input.equals("b")) {
                        newItem = new DVD(title, author, itemID);
                    } else {
                        newItem = new Magazine(title, author, itemID);
                    }
                    catalog.addItem(newItem);
                    System.out.println("Item added successfully.");
                    break;
                case 2:
                    System.out.print("Enter item ID to remove: ");
                    String removeID = scanner.nextLine();
                    try {
                        catalog.removeItem(removeID);
                        System.out.println("Item removed successfully.");
                    } catch (ItemNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    catalog.displayCatalog();
                    break;
                case 4:
                    System.out.print("Enter item ID to retrieve: ");
                    String getID = scanner.nextLine();
                    try {
                        LibraryItem<String> retrievedItem = catalog.getItem(getID);
                        System.out.println(retrievedItem);
                    } catch (ItemNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 5:
                    System.out.println("Exiting the program.");
                    break;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        } while (choice != 5);
        scanner.close();
    }
}