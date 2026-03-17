import java.util.*;

public class Use_case {

    abstract static class Room {
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

    static class RoomInventory {
        private Map<String, Integer> inventory = new HashMap<>();
        public RoomInventory() {
            inventory.put("Single Room", 2);
            inventory.put("Double Room", 2);
            inventory.put("Suite Room", 1);
        }
        public int getAvailability(String type) { return inventory.getOrDefault(type, 0); }
        public void decrement(String type) { inventory.put(type, getAvailability(type) - 1); }
    }

    static class Reservation {
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

    static class BookingQueue {
        private Queue<Reservation> queue = new LinkedList<>();
        public void add(Reservation r) { queue.offer(r); }
        public Reservation next() { return queue.poll(); }
        public boolean isEmpty() { return queue.isEmpty(); }
    }

    static class BookingService {
        private RoomInventory inventory;
        private Set<String> roomIds = new HashSet<>();
        private Map<String, Set<String>> allocation = new HashMap<>();
        private int counter = 1;

        public BookingService(RoomInventory inventory) { this.inventory = inventory; }

        public List<Reservation> process(BookingQueue queue) {
            List<Reservation> confirmed = new ArrayList<>();
            while (!queue.isEmpty()) {
                Reservation r = queue.next();
                String type = r.getRoomType();
                if (inventory.getAvailability(type) > 0) {
                    String roomId = generateRoomId(type);
                    roomIds.add(roomId);
                    allocation.computeIfAbsent(type, k -> new HashSet<>()).add(roomId);
                    inventory.decrement(type);
                    r.setReservationId(roomId);
                    confirmed.add(r);
                    System.out.println("Confirmed: " + r.getGuestName() + " -> " + type + " | ID: " + roomId);
                } else {
                    System.out.println("Failed: " + r.getGuestName() + " -> " + type);
                }
            }
            return confirmed;
        }

        private String generateRoomId(String type) {
            String id;
            do { id = type.substring(0,2).toUpperCase() + "-" + counter++; }
            while (roomIds.contains(id));
            return id;
        }
    }

    static class BookingHistory {
        private List<Reservation> history = new ArrayList<>();
        public void add(Reservation r) { history.add(r); }
        public List<Reservation> getHistory() { return Collections.unmodifiableList(history); }
    }

    static class BookingReportService {
        public void generateReport(BookingHistory history) {
            System.out.println("\nBooking History Report:");
            for (Reservation r : history.getHistory()) {
                System.out.println(r.getReservationId() + " | " + r.getGuestName() + " -> " + r.getRoomType());
            }
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("Alice", "Single Room"));
        queue.add(new Reservation("Bob", "Double Room"));
        queue.add(new Reservation("Charlie", "Suite Room"));

        BookingService bookingService = new BookingService(inventory);
        List<Reservation> confirmed = bookingService.process(queue);

        BookingHistory history = new BookingHistory();
        for (Reservation r : confirmed) history.add(r);

        BookingReportService reportService = new BookingReportService();
        reportService.generateReport(history);
    }
}