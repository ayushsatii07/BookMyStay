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

        public void decrement(String type) throws InvalidBookingException {
            int available = getAvailability(type);
            if (available <= 0) throw new InvalidBookingException("No availability for: " + type);
            inventory.put(type, available - 1);
        }

        public void increment(String type) { inventory.put(type, getAvailability(type) + 1); }

        public boolean isValidRoomType(String type) { return inventory.containsKey(type); }
    }

    static class Reservation {
        private String guestName;
        private String roomType;
        private String reservationId;
        private boolean cancelled = false;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public String getReservationId() { return reservationId; }
        public void setReservationId(String id) { this.reservationId = id; }
        public boolean isCancelled() { return cancelled; }
        public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    }

    static class BookingQueue {
        private Queue<Reservation> queue = new LinkedList<>();
        public void add(Reservation r) { queue.offer(r); }
        public Reservation next() { return queue.poll(); }
        public boolean isEmpty() { return queue.isEmpty(); }
    }

    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) { super(message); }
    }

    static class BookingService {
        private RoomInventory inventory;
        private Set<String> roomIds = new HashSet<>();
        private int counter = 1;

        public BookingService(RoomInventory inventory) { this.inventory = inventory; }

        public List<Reservation> process(BookingQueue queue) {
            List<Reservation> confirmed = new ArrayList<>();
            while (!queue.isEmpty()) {
                Reservation r = queue.next();
                try {
                    validateReservation(r);
                    String roomId = generateRoomId(r.getRoomType());
                    roomIds.add(roomId);
                    inventory.decrement(r.getRoomType());
                    r.setReservationId(roomId);
                    confirmed.add(r);
                    System.out.println("Confirmed: " + r.getGuestName() + " -> " + r.getRoomType() + " | ID: " + roomId);
                } catch (InvalidBookingException e) {
                    System.out.println("Booking Failed for " + r.getGuestName() + ": " + e.getMessage());
                }
            }
            return confirmed;
        }

        private void validateReservation(Reservation r) throws InvalidBookingException {
            if (r.getGuestName() == null || r.getGuestName().isEmpty())
                throw new InvalidBookingException("Guest name required");
            if (!inventory.isValidRoomType(r.getRoomType()))
                throw new InvalidBookingException("Invalid room type: " + r.getRoomType());
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
        public Reservation findById(String id) {
            for (Reservation r : history) if (r.getReservationId().equals(id)) return r;
            return null;
        }
    }

    static class CancellationService {
        private RoomInventory inventory;
        private Stack<String> releasedRooms = new Stack<>();

        public CancellationService(RoomInventory inventory) { this.inventory = inventory; }

        public void cancelReservation(Reservation r, BookingHistory history) {
            if (r == null || r.isCancelled()) {
                System.out.println("Cancellation failed: Invalid or already cancelled reservation.");
                return;
            }
            r.setCancelled(true);
            inventory.increment(r.getRoomType());
            releasedRooms.push(r.getReservationId());
            System.out.println("Cancelled: " + r.getGuestName() + " | Room ID: " + r.getReservationId());
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

        CancellationService cancelService = new CancellationService(inventory);

        // Cancel Alice's reservation
        cancelService.cancelReservation(history.findById(confirmed.get(0).getReservationId()), history);

        // Attempt to cancel same reservation again
        cancelService.cancelReservation(history.findById(confirmed.get(0).getReservationId()), history);

        // Cancel Bob's reservation
        cancelService.cancelReservation(history.findById(confirmed.get(1).getReservationId()), history);
    }
}