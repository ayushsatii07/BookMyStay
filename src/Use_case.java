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

        public int getAvailability(String type) {
            return inventory.getOrDefault(type, 0);
        }

        public void decrement(String type) {
            inventory.put(type, getAvailability(type) - 1);
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

        public BookingService(RoomInventory inventory) {
            this.inventory = inventory;
        }

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
                    System.out.println("Confirmed: " + r.getGuestName() +
                            " -> " + type + " | ID: " + roomId);
                } else {
                    System.out.println("Failed: " + r.getGuestName() +
                            " -> " + type);
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

    static class AddOnService {
        private String name;
        private double cost;

        public AddOnService(String name, double cost) {
            this.name = name;
            this.cost = cost;
        }

        public String getName() { return name; }
        public double getCost() { return cost; }
    }

    static class AddOnServiceManager {
        private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

        public void addService(String reservationId, AddOnService service) {
            serviceMap.computeIfAbsent(reservationId, k -> new ArrayList<>()).add(service);
        }

        public double calculateTotal(String reservationId) {
            List<AddOnService> services = serviceMap.getOrDefault(reservationId, new ArrayList<>());
            double total = 0;
            for (AddOnService s : services) total += s.getCost();
            return total;
        }

        public void displayServices(String reservationId) {
            List<AddOnService> services = serviceMap.get(reservationId);
            if (services == null) return;
            System.out.println("Services for " + reservationId + ":");
            for (AddOnService s : services) {
                System.out.println("- " + s.getName() + " ($" + s.getCost() + ")");
            }
        }
    }

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();
        BookingQueue queue = new BookingQueue();

        queue.add(new Reservation("Alice", "Single Room"));
        queue.add(new Reservation("Bob", "Double Room"));

        BookingService bookingService = new BookingService(inventory);
        List<Reservation> confirmed = bookingService.process(queue);

        AddOnServiceManager serviceManager = new AddOnServiceManager();
        AddOnService breakfast = new AddOnService("Breakfast", 20);
        AddOnService spa = new AddOnService("Spa", 50);

        for (Reservation r : confirmed) {
            serviceManager.addService(r.getReservationId(), breakfast);
            serviceManager.addService(r.getReservationId(), spa);

            serviceManager.displayServices(r.getReservationId());
            System.out.println("Total Add-on Cost: $" +
                    serviceManager.calculateTotal(r.getReservationId()));
            System.out.println();
        }
    }
}