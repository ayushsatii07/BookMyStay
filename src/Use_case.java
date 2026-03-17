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

        public void displayDetails() {
            System.out.println("Room Type: " + getRoomType());
            System.out.println("Beds: " + beds);
            System.out.println("Size: " + size + " sqm");
            System.out.println("Price: $" + price);
        }
    }

    static class SingleRoom extends Room {
        public SingleRoom() {
            super(1, 20, 100);
        }

        public String getRoomType() {
            return "Single Room";
        }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() {
            super(2, 35, 180);
        }

        public String getRoomType() {
            return "Double Room";
        }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() {
            super(3, 60, 350);
        }

        public String getRoomType() {
            return "Suite Room";
        }
    }

    static class RoomInventory {
        private Map<String, Integer> inventory;

        public RoomInventory() {
            inventory = new HashMap<>();
            inventory.put("Single Room", 2);
            inventory.put("Double Room", 2);
            inventory.put("Suite Room", 1);
        }

        public int getAvailability(String roomType) {
            return inventory.getOrDefault(roomType, 0);
        }

        public void decrement(String roomType) {
            int current = getAvailability(roomType);
            if (current > 0) {
                inventory.put(roomType, current - 1);
            }
        }

        public void displayInventory() {
            System.out.println("\nUpdated Inventory:");
            for (Map.Entry<String, Integer> e : inventory.entrySet()) {
                System.out.println(e.getKey() + " Available: " + e.getValue());
            }
        }
    }

    static class Reservation {
        private String guestName;
        private String roomType;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }
    }

    static class BookingQueue {
        private Queue<Reservation> queue = new LinkedList<>();

        public void add(Reservation r) {
            queue.offer(r);
        }

        public Reservation next() {
            return queue.poll();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    static class BookingService {

        private RoomInventory inventory;
        private Set<String> allocatedRoomIds;
        private Map<String, Set<String>> allocationMap;
        private int counter = 1;

        public BookingService(RoomInventory inventory) {
            this.inventory = inventory;
            this.allocatedRoomIds = new HashSet<>();
            this.allocationMap = new HashMap<>();
        }

        public void processQueue(BookingQueue queue) {

            while (!queue.isEmpty()) {

                Reservation r = queue.next();
                String type = r.getRoomType();

                if (inventory.getAvailability(type) > 0) {

                    String roomId = generateRoomId(type);

                    allocatedRoomIds.add(roomId);

                    allocationMap
                            .computeIfAbsent(type, k -> new HashSet<>())
                            .add(roomId);

                    inventory.decrement(type);

                    System.out.println("Booking Confirmed: " + r.getGuestName()
                            + " -> " + type + " | Room ID: " + roomId);

                } else {
                    System.out.println("Booking Failed (No Availability): "
                            + r.getGuestName() + " -> " + type);
                }
            }
        }

        private String generateRoomId(String type) {
            String id;
            do {
                id = type.substring(0, 2).toUpperCase() + "-" + counter++;
            } while (allocatedRoomIds.contains(id));
            return id;
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("Alice", "Single Room"));
        queue.add(new Reservation("Bob", "Single Room"));
        queue.add(new Reservation("Charlie", "Single Room"));
        queue.add(new Reservation("David", "Suite Room"));

        BookingService bookingService = new BookingService(inventory);

        bookingService.processQueue(queue);

        inventory.displayInventory();
    }
}