import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: Space Game
 * Purpose Details: Creating a flatfi space game.
 * Course: IST 242 Section 611 Inter App Dev
 * Author: Christopher Carlos
 * Date Developed: 06/18/24
 * Last Date Changed: 06/19/24
 * Revision: 1
 */

public class Ships_Flatfile_Creator {

    /**
     * This Ship class builds a ship with a given name and health points.
     */
    public static class Ship {
        /**
         * The name of the ship.
         */
        private String name;

        /**
         * The health points of the ship.
         */
        private int health;

        /**
         * This sets the new Ship with the specified name and health.
         *
         * @param name   The name of the ship.
         * @param health The health points of the ship.
         */
        public Ship(String name, int health) {
            this.name = name;
            this.health = health;
        }

        /**
         * Converts the Ship object to a fixed format string for flat file storage.
         *
         * @return The fixed format string of the Ship object and how it will look.
         */
        public String toFixedFormatString() {
            return String.format("%-10s %-10d", name, health);
        }

        /**
         * Creates a Ship object from a fixed format string.
         *
         * @param line The fixed format string representing the Ship object.
         * @return The Ship object created from the fixed format string.
         */
        public static Ship fromFixedFormatString(String line) {
            String name = line.substring(0, 10).trim();
            int health = Integer.parseInt(line.substring(10, 20).trim());
            return new Ship(name, health);
        }

        @Override
        public String toString() {
            return "Ship{" +
                    "name='" + name + '\'' +
                    ", health=" + health +
                    '}';
        }

        public String getName() {
            return "";
        }

        public String getHealth() {
            return "";
        }
    }

    public static void main(String[] args) {
        // Example data
        List<Ship> ships = new ArrayList<>();
        ships.add(new Ship("Starship", 100));
        ships.add(new Ship("Battleship", 800));

        // Write ships to a flat file
        try (PrintWriter writer = new PrintWriter(new FileWriter("ships.txt"))) {
            for (Ship ship : ships) {
                writer.println(ship.toFixedFormatString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read ships from the flat file
        List<Ship> loadedShips = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("ships.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                loadedShips.add(Ship.fromFixedFormatString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display loaded ships
        for (Ship ship : loadedShips) {
            System.out.println(ship);
        }
    }
}