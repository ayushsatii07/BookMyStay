import java.util.HashMap;
import java.util.Map;

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
            inventory.put("Single Room", 5);
            inventory.put("Double Room", 3);
            inventory.put("Suite Room", 0);
        }

        public int getAvailability(String roomType) {
            return inventory.getOrDefault(roomType, 0);
        }
    }

    static class SearchService {

        private RoomInventory inventory;

        public SearchService(RoomInventory inventory) {
            this.inventory = inventory;
        }

        public void searchAvailableRooms(Room[] rooms) {
            for (Room room : rooms) {
                int available = inventory.getAvailability(room.getRoomType());

                if (available > 0) {
                    room.displayDetails();
                    System.out.println("Available: " + available);
                    System.out.println();
                }
            }
        }
    }

    public static void main(String[] args) {

        Room[] rooms = {
                new SingleRoom(),
                new DoubleRoom(),
                new SuiteRoom()
        };

        RoomInventory inventory = new RoomInventory();

        SearchService searchService = new SearchService(inventory);

        System.out.println("Available Rooms:");
        searchService.searchAvailableRooms(rooms);
    }
}