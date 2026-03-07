import java.util.*;

public class FlipItCard {

    static class Card {
        int id;
        String heading;
        String body;

        Card(int id, String h, String b) {
            this.id = id;
            heading = h;
            body = b;
        }
    }

    static class Node {
        Card card;
        Node next;

        Node(Card c) {
            card = c;
        }
    }

    // Linked list for all cards
    static Node head = null;
    static int size = 0;
    static int idCounter = 1;

    // Stacks for undo/redo
    static class StackNode {
        Card card;
        StackNode next;
        StackNode(Card c) { card = c; }
    }

    static StackNode undoStack = null;
    static StackNode redoStack = null;

    static void pushUndo(Card c) {
        StackNode n = new StackNode(c);
        n.next = undoStack;
        undoStack = n;
    }

    static Card popUndo() {
        if (undoStack == null) return null;
        Card c = undoStack.card;
        undoStack = undoStack.next;
        return c;
    }

    static void pushRedo(Card c) {
        StackNode n = new StackNode(c);
        n.next = redoStack;
        redoStack = n;
    }

    static Card popRedo() {
        if (redoStack == null) return null;
        Card c = redoStack.card;
        redoStack = redoStack.next;
        return c;
    }

    // Simple hashing using separate chaining
    static class HashTable {
        static class Entry {
            String key;
            Card card;
            Entry next;
            Entry(String key, Card card) { this.key = key; this.card = card; }
        }

        private Entry[] table;
        private int capacity;

        HashTable(int cap) {
            capacity = cap;
            table = new Entry[capacity];
        }

        private int hash(String key) {
            int h = 0;
            for (int i = 0; i < key.length(); i++) {
                h = (31 * h + key.charAt(i)) % capacity;
            }
            return h;
        }

        void put(String key, Card card) {
            int idx = hash(key);
            Entry e = new Entry(key, card);
            e.next = table[idx];
            table[idx] = e;
        }

        Card get(String key) {
            int idx = hash(key);
            Entry e = table[idx];
            while (e != null) {
                if (e.key.equals(key)) return e.card;
                e = e.next;
            }
            return null;
        }

        void remove(String key) {
            int idx = hash(key);
            Entry e = table[idx];
            Entry prev = null;
            while (e != null) {
                if (e.key.equals(key)) {
                    if (prev == null)
                        table[idx] = e.next;
                    else
                        prev.next = e.next;
                    return;
                }
                prev = e;
                e = e.next;
            }
        }
    }

    static HashTable headingTable = new HashTable(100);
    static HashTable idTable = new HashTable(100);

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            menu();
            int choice = readInt();
            switch (choice) {
                case 1: createCard(); break;
                case 2: viewCards(); break;
                case 3: studyCards(); break;
                case 4: shuffleStudy(); break;
                case 5: undo(); break;
                case 6: redo(); break;
                case 7: searchCard(); break;
                case 8: deleteCard(); break;
                case 9: wordCharCount(); break;
                case 10: changeOrder(); break;
                case 11:
                    System.out.println("\nThank you for using FlipIt Cards!");
                    return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    static void menu() {
        System.out.println("\n============== FlipIt Cards ==============");
        System.out.println("Total Cards : " + size);
        System.out.println("1. Create Card");
        System.out.println("2. View Cards");
        System.out.println("3. Study Cards");
        System.out.println("4. Shuffle Cards");
        System.out.println("5. Undo Last Add");
        System.out.println("6. Redo Last Undo");
        System.out.println("7. Search Card");
        System.out.println("8. Delete Card");
        System.out.println("9. Word & Character Count");
        System.out.println("10. Change Card Order");
        System.out.println("11. Exit");
        System.out.print("Choose: ");
    }

    static void createCard() {
        System.out.print("Enter heading: ");
        String heading = sc.nextLine().toLowerCase();
        if (headingTable.get(heading) != null) {
            System.out.println("Card already exists with this heading.");
            return;
        }
        System.out.print("Enter body: ");
        String body = sc.nextLine();
        int id = idCounter++;
        if (idTable.get(String.valueOf(id)) != null) {
            System.out.println("Card already exists with this ID.");
            return;
        }
        Card card = new Card(id, heading, body);
        Node n = new Node(card);
        if (head == null) head = n;
        else {
            Node t = head;
            while (t.next != null) t = t.next;
            t.next = n;
        }
        size++;
        pushUndo(card);
        redoStack = null; // clear redo
        headingTable.put(heading, card);
        idTable.put(String.valueOf(id), card);
        System.out.println("Card created with ID: " + id);
    }

    static void viewCards() {
        if (head == null) { System.out.println("No cards available."); return; }
        Node t = head;
        while (t != null) { printCard(t.card); t = t.next; }
    }

    static void studyCards() {
        if (head == null) { System.out.println("No cards to study."); return; }
        Node t = head;
        while (t != null) {
            System.out.println("\nPress ENTER to reveal card...");
            sc.nextLine();
            printCard(t.card);
            t = t.next;
        }
    }

    static void shuffleStudy() {
        if (head == null) { System.out.println("No cards available."); return; }
        // Convert to linked list array manually
        Node[] nodes = new Node[size];
        Node t = head;
        int i = 0;
        while (t != null) { nodes[i++] = t; t = t.next; }
        // Shuffle using Fisher-Yates
        Random rand = new Random();
        for (i = size - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            Node tmp = nodes[i]; nodes[i] = nodes[j]; nodes[j] = tmp;
        }
        for (i = 0; i < size; i++) {
            System.out.println("\nPress ENTER to reveal card...");
            sc.nextLine();
            printCard(nodes[i].card);
        }
    }

    static void undo() {
        Card last = popUndo();
        if (last == null) { System.out.println("Nothing to undo."); return; }
        pushRedo(last);
        removeFromList(last);
        headingTable.remove(last.heading.toLowerCase());
        idTable.remove(String.valueOf(last.id));
        size--;
        System.out.println("Last card removed.");
    }

    static void redo() {
        Card card = popRedo();
        if (card == null) { System.out.println("Nothing to redo."); return; }
        Node n = new Node(card);
        if (head == null) head = n;
        else {
            Node t = head;
            while (t.next != null) t = t.next;
            t.next = n;
        }
        pushUndo(card);
        headingTable.put(card.heading.toLowerCase(), card);
        idTable.put(String.valueOf(card.id), card);
        size++;
        System.out.println("Card restored.");
    }

    static void searchCard() {
        System.out.print("Enter keyword: ");
        String key = sc.nextLine().toLowerCase();
        Node t = head;
        boolean found = false;
        while (t != null) {
            if (t.card.heading.toLowerCase().contains(key) || t.card.body.toLowerCase().contains(key)) {
                printCard(t.card);
                found = true;
            }
            t = t.next;
        }
        if (!found) System.out.println("No card found.");
    }

    static void deleteCard() {
        System.out.print("Enter Card ID to delete: ");
        int id = readInt();
        Node prev = null, cur = head;
        while (cur != null) {
            if (cur.card.id == id) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                headingTable.remove(cur.card.heading.toLowerCase());
                idTable.remove(String.valueOf(cur.card.id));
                size--;
                System.out.println("Card deleted.");
                return;
            }
            prev = cur;
            cur = cur.next;
        }
        System.out.println("Card not found.");
    }

    static void wordCharCount() {
        System.out.print("Enter Card ID: ");
        int id = readInt();
        Card c = idTable.get(String.valueOf(id));
        if (c == null) { System.out.println("Card not found."); return; }
        int chars = c.body.length();
        int words = c.body.trim().isEmpty() ? 0 : c.body.trim().split("\\s+").length;
        System.out.println("\nHeading: " + c.heading);
        System.out.println("Word Count: " + words);
        System.out.println("Character Count: " + chars);
    }

    static void changeOrder() {
        if (head == null || head.next == null) { System.out.println("Not enough cards."); return; }
        // Simple bubble sort on linked list
        boolean swapped;
        do {
            swapped = false;
            Node t = head;
            while (t.next != null) {
                if (t.card.heading.toLowerCase().compareTo(t.next.card.heading.toLowerCase()) > 0) {
                    Card tmp = t.card;
                    t.card = t.next.card;
                    t.next.card = tmp;
                    swapped = true;
                }
                t = t.next;
            }
        } while (swapped);
        System.out.println("Cards reordered alphabetically.");
    }

    static void removeFromList(Card card) {
        Node prev = null, cur = head;
        while (cur != null) {
            if (cur.card == card) {
                if (prev == null) head = cur.next;
                else prev.next = cur.next;
                return;
            }
            prev = cur;
            cur = cur.next;
        }
    }

    static void printCard(Card c) {
        System.out.println("\n=======================================");
        System.out.println("Card ID : " + c.id);
        System.out.println("Heading : " + c.heading);
        System.out.println("---------------------------------------");
        System.out.println(c.body);
        System.out.println("=======================================");
    }

    static int readInt() {
        try { return Integer.parseInt(sc.nextLine()); } 
        catch (Exception e) { return -1; }
    }
}