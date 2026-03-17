import java.io.*;
import java.util.*;

public class Use_case implements Serializable {

    abstract static class Room implements Serializable {
        protected int beds;
        protected double size;
        protected double price;

        public Room(int beds, double size, double price) {
            this.beds = beds;
            this.size = size;
            this.price = price;
        }

        public abstract String getRoomType();
    }

    static class SingleRoom extends Room {
        public SingleRoom() { super(1, 20, 100); }
        public String getRoomType() { return "Single Room"; }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() { super(2, 35, 180); }
        public String getRoomType() { return "Double Room"; }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() { super(3, 60, 350); }
        public String getRoomType() { return "Suite Room"; }
    }

    static class RoomInventory implements Serializable {
        private Map<String, Integer> inventory = new HashMap<>();

        public RoomInventory() {
            inventory.put("Single Room", 2);
            inventory.put("Double Room", 2);
            inventory.put("Suite Room", 1);
        }

        public int getAvailability(String type) { return inventory.getOrDefault(type, 0); }

        public void decrement(String type) { inventory.put(type, getAvailability(type) - 1); }

        public void increment(String type) { inventory.put(type, getAvailability(type) + 1); }

        public boolean isValidRoomType(String type) { return inventory.containsKey(type); }

        public Map<String, Integer> getInventorySnapshot() { return new HashMap<>(inventory); }

        public void restoreInventory(Map<String, Integer> snapshot) { this.inventory = snapshot; }
    }

    static class Reservation implements Serializable {
        private String guestName;
        private String roomType;
        private String reservationId;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public String getReservationId() { return reservationId; }
        public void setReservationId(String id) { this.reservationId = id; }
    }

    static class BookingHistory implements Serializable {
        private List<Reservation> history = new ArrayList<>();

        public void add(Reservation r) { history.add(r); }
        public List<Reservation> getHistory() { return Collections.unmodifiableList(history); }
    }

    static class PersistenceService {
        private final String filename;

        public PersistenceService(String filename) { this.filename = filename; }

        public void save(RoomInventory inventory, BookingHistory history) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(inventory);
                oos.writeObject(history);
                System.out.println("System state saved successfully.");
            } catch (IOException e) {
                System.out.println("Failed to save state: " + e.getMessage());
            }
        }

        public Object[] restore() {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("No persisted state found. Starting fresh.");
                return null;
            }
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
                RoomInventory inventory = (RoomInventory) ois.readObject();
                BookingHistory history = (BookingHistory) ois.readObject();
                System.out.println("System state restored successfully.");
                return new Object[]{inventory, history};
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Failed to restore state: " + e.getMessage());
                return null;
            }
        }
    }

    public static void main(String[] args) {

        String persistenceFile = "hotel_state.ser";
        PersistenceService persistence = new PersistenceService(persistenceFile);

        Object[] restored = persistence.restore();
        RoomInventory inventory;
        BookingHistory history;

        if (restored != null) {
            inventory = (RoomInventory) restored[0];
            history = (BookingHistory) restored[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        // Simulate some bookings
        Reservation r1 = new Reservation("Alice", "Single Room");
        inventory.decrement(r1.getRoomType());
        r1.setReservationId("SR-1");
        history.add(r1);

        Reservation r2 = new Reservation("Bob", "Double Room");
        inventory.decrement(r2.getRoomType());
        r2.setReservationId("DR-1");
        history.add(r2);

        // Save state
        persistence.save(inventory, history);

        // Display restored state
        System.out.println("\nCurrent Booking History:");
        for (Reservation r : history.getHistory()) {
            System.out.println(r.getReservationId() + " | " + r.getGuestName() + " -> " + r.getRoomType());
        }
        System.out.println("\nCurrent Inventory:");
        for (Map.Entry<String, Integer> entry : inventory.getInventorySnapshot().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}