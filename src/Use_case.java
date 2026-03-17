import java.util.*;
import java.util.concurrent.*;

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
        private final Map<String, Integer> inventory = new HashMap<>();

        public RoomInventory() {
            inventory.put("Single Room", 2);
            inventory.put("Double Room", 2);
            inventory.put("Suite Room", 1);
        }

        public synchronized int getAvailability(String type) { return inventory.getOrDefault(type, 0); }

        public synchronized boolean allocate(String type) {
            int available = inventory.getOrDefault(type, 0);
            if (available <= 0) return false;
            inventory.put(type, available - 1);
            return true;
        }

        public synchronized void release(String type) {
            inventory.put(type, inventory.getOrDefault(type, 0) + 1);
        }
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

    static class ConcurrentBookingService {
        private RoomInventory inventory;
        private Set<String> roomIds = Collections.synchronizedSet(new HashSet<>());
        private int counter = 1;

        public ConcurrentBookingService(RoomInventory inventory) { this.inventory = inventory; }

        public void processReservation(Reservation r) {
            synchronized (this) {
                if (!inventory.allocate(r.getRoomType())) {
                    System.out.println("Booking failed for " + r.getGuestName() +
                            " -> " + r.getRoomType() + " (No availability)");
                    return;
                }
                String roomId = generateRoomId(r.getRoomType());
                roomIds.add(roomId);
                r.setReservationId(roomId);
                System.out.println("Confirmed: " + r.getGuestName() + " -> " + r.getRoomType() + " | ID: " + roomId);
            }
        }

        private String generateRoomId(String type) {
            return type.substring(0,2).toUpperCase() + "-" + counter++;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        RoomInventory inventory = new RoomInventory();
        ConcurrentBookingService bookingService = new ConcurrentBookingService(inventory);

        List<Reservation> requests = Arrays.asList(
                new Reservation("Alice", "Single Room"),
                new Reservation("Bob", "Single Room"),
                new Reservation("Charlie", "Single Room"),
                new Reservation("David", "Double Room"),
                new Reservation("Eve", "Suite Room")
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (Reservation r : requests) {
            executor.submit(() -> bookingService.processReservation(r));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}