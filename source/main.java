package source;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class main {

    private GraphADT<String, String> railwayGraph; // GraphADT of this map
    private Map<String, Vertex<String>> stations; //MapADT for Dijkstra to detect the passed station
    private int stationtime = 3; //second
    private int interchangetime = 10; //second
    private int promotioncost = 20; //baht
    public List<String> MatainanceList = new ArrayList<>(); //The list of station that currently unavaliable
    private boolean excludeMaintenance = false; // if true, maintenance stations are excluded from routes

    public main() {
        // Initialize as an undirected graph
        this.railwayGraph = new AdjacencyMapGraph<>(false); 
        this.stations = new HashMap<>();
    }

    public void addMaintenanceList(List<String> station){
        MatainanceList.addAll(station);
    }

    public void removeMaintenanceList(String station){
        MatainanceList.remove(station);
    }

    public void setstationtime(int stationtime){
        this.stationtime = stationtime;
    }

    public void setinterchangetime(int interchangetime){
        this.interchangetime = interchangetime;
    }

    public void setpromotioncost(int promotioncost){
        this.promotioncost = promotioncost;
    }

    public void setExcludeMaintenance(boolean exclude) {
        this.excludeMaintenance = exclude;
    }
    /**load all of the connected lines and stations to the Graph Structure using AdjacentMapGraph
     * using java.io.file
     * @param connectionPATH // the Path of the all connection of the Railway
     */
    public void loadConnections(String connectionPATH) {
        try (BufferedReader br = new BufferedReader(new FileReader(connectionPATH))) {
            String line;
            br.readLine(); // Skip header line

            //read the line and skip the comma and place in 3 variable
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length < 3) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }
                String stationNameA = values[0].trim();
                String stationNameB = values[1].trim();
                String railwayLine = values[2].trim();

                // Get or create vertex for station A
                Vertex<String> vertexA = stations.get(stationNameA);
                if (vertexA == null) {
                    vertexA = railwayGraph.insertVertex(stationNameA);
                    stations.put(stationNameA, vertexA);
                }

                // Get or create vertex for station B
                Vertex<String> vertexB = stations.get(stationNameB);
                if (vertexB == null) {
                    vertexB = railwayGraph.insertVertex(stationNameB);
                    stations.put(stationNameB, vertexB);
                }

                // Insert an edge between the two stations, if it doesn't exist yet
                if (railwayGraph.getEdge(vertexA, vertexB) == null) {
                    railwayGraph.insertEdge(vertexA, vertexB, railwayLine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the connections file: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

    /**
     * Implements Dijkstra's algorithm to find the shortest path between two stations.
     * The "distance" is the number of stations traveled, with interchanges costing 10. and regular costing 3
     * @param startStationName The name of the starting station.
     * @param endStationName The name of the destination station.
     * @return A List of station names representing the shortest path, or null if no path is found.
     */
    public List<Vertex<String>> findShortestPath(String startStationName, String endStationName) {
        Vertex<String> startVertex = stations.get(startStationName);
        Vertex<String> endVertex = stations.get(endStationName);

        if (startVertex == null || endVertex == null) {
            System.err.println("Invalid start or end station.");
            return null;
        }

        // If excluding maintenance, ensure start/end are not under maintenance
        if (excludeMaintenance) {
            if (MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(startStationName))) {
                System.out.println("Start station '" + startStationName + "' is under maintenance and excluded.");
                return null;
            }
            if (MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(endStationName))) {
                System.out.println("Destination station '" + endStationName + "' is under maintenance and excluded.");
                return null;
            }
        }

        // Priority queue stores entries of <Distance, Vertex>
        PriorityQueue<Integer, Vertex<String>> pq = new SortedPQ<>();
        // Maps to store distances, predecessors (for path reconstruction), and entries in the PQ
        Map<Vertex<String>, Integer> dist = new HashMap<>();
        Map<Vertex<String>, Vertex<String>> predecessor = new HashMap<>();
        Map<Vertex<String>, Entry<Integer, Vertex<String>>> pqEntries = new HashMap<>();

        // Initialize all distances to infinity and add to the distance map
        for (Vertex<String> v : railwayGraph.vertices()) {
            dist.put(v, Integer.MAX_VALUE);
        }

        // Set distance for the start vertex to 0 and add to PQ
        dist.put(startVertex, 0);
        Entry<Integer, Vertex<String>> startEntry = new Entry<>(0, startVertex);
        pq.insert(startEntry.getKey(), startEntry.getValue());
        pqEntries.put(startVertex, startEntry);

        while (!pq.isEmpty()) {
            Entry<Integer, Vertex<String>> entry = pq.removeMin();
            Vertex<String> u = entry.getValue();

            // If we've reached the destination, we can stop
            if (u.equals(endVertex)) {
                break;
            }

            // For each neighbor of the current vertex

            for (Edge<String> e : railwayGraph.outgoingEdges(u)) {
                Vertex<String> v = railwayGraph.opposite(u, e);

                // If we are excluding maintenance, skip neighbor vertices that are under maintenance
                if (excludeMaintenance && MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(v.getElement()))) {
                    continue;
                }

                // define the weight between interchange and regular
                int weight = e.getElement().equals("Interchange") ? interchangetime : stationtime;

                int newDist = dist.get(u) + weight;

                // If the route exceeds the time limit, do not consider it further
                if (newDist > 5000) {
                    continue;
                }

                // If we found a shorter path to v
                if (newDist < dist.get(v)) {
                    // Update distance and predecessor
                    dist.put(v, newDist);
                    predecessor.put(v, u);

                    // re-insert. if PQ doesn't have key
                    // A more advanced PQ (like a heap) would be more efficient here.
                    Entry<Integer, Vertex<String>> newEntry = new Entry<>(newDist, v);
                    pq.insert(newEntry.getKey(), newEntry.getValue());
                    pqEntries.put(v, newEntry);
                }
            }
        }

        return reconstructPath(predecessor, startVertex, endVertex);
    }

    /**
     * Pair distance for lexicographic comparison: primary transfers, secondary time.
     */
    private static class DistancePair implements Comparable<DistancePair> {
        int transfers;
        int time;

        DistancePair(int transfers, int time) {
            this.transfers = transfers;
            this.time = time;
        }

        @Override
        public int compareTo(DistancePair o) {
            if (this.transfers != o.transfers) return Integer.compare(this.transfers, o.transfers);
            return Integer.compare(this.time, o.time);
        }
    }

    /**
     * Find path that minimizes the number of transfers (primary) and then total time (secondary).
     */
    public List<Vertex<String>> findPathFewestTransfers(String startStationName, String endStationName) {
        Vertex<String> startVertex = stations.get(startStationName);
        Vertex<String> endVertex = stations.get(endStationName);

        if (startVertex == null || endVertex == null) {
            System.err.println("Invalid start or end station.");
            return null;
        }

        if (excludeMaintenance) {
            if (MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(startStationName))) {
                System.out.println("Start station '" + startStationName + "' is under maintenance and excluded.");
                return null;
            }
            if (MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(endStationName))) {
                System.out.println("Destination station '" + endStationName + "' is under maintenance and excluded.");
                return null;
            }
        }

        PriorityQueue<DistancePair, Vertex<String>> pq = new SortedPQ<>();
        Map<Vertex<String>, DistancePair> dist = new HashMap<>();
        Map<Vertex<String>, Vertex<String>> predecessor = new HashMap<>();

        for (Vertex<String> v : railwayGraph.vertices()) {
            dist.put(v, new DistancePair(Integer.MAX_VALUE/2, Integer.MAX_VALUE/2));
        }

        DistancePair startD = new DistancePair(0, 0);
        dist.put(startVertex, startD);
        pq.insert(startD, startVertex);

        while (!pq.isEmpty()) {
            Entry<DistancePair, Vertex<String>> entry = pq.removeMin();
            Vertex<String> u = entry.getValue();
            DistancePair du = entry.getKey();

            if (u.equals(endVertex)) break;

            for (Edge<String> e : railwayGraph.outgoingEdges(u)) {
                Vertex<String> v = railwayGraph.opposite(u, e);

                if (excludeMaintenance && MatainanceList.stream().anyMatch(m -> m.equalsIgnoreCase(v.getElement()))) continue;

                int weight = e.getElement().equals("Interchange") ? interchangetime : stationtime;
                int transferInc = e.getElement().equals("Interchange") ? 1 : 0;

                DistancePair candidate = new DistancePair(du.transfers + transferInc, du.time + weight);
                DistancePair current = dist.get(v);
                if (candidate.compareTo(current) < 0) {
                    dist.put(v, candidate);
                    predecessor.put(v, u);
                    pq.insert(candidate, v);
                }
            }
        }

        return reconstructPath(predecessor, startVertex, endVertex);
    }

    private List<Vertex<String>> reconstructPath(Map<Vertex<String>, Vertex<String>> predecessor, Vertex<String> start, Vertex<String> end) {
        List<Vertex<String>> path = new ArrayList<>();
        Vertex<String> current = end;
        while (current != null) {
            path.add(current);
            if (current.equals(start)) break; // Path found
            current = predecessor.get(current);
        }
        
        if (!Objects.equals(path.get(path.size() - 1),(start))) return null; // No path found

        Collections.reverse(path);
        return path;
    }

    public class linePassinfo{
        String name = null;
        int numberofStation = 0;

        public linePassinfo(String station, int numberofStation){
            this.name = station;
            this.numberofStation = numberofStation;
        }
    }
    
    /**this method calculate the total cost based on numberof the station and line that the train visited
     * @param path // the List of the vertex(station) of the shortest path
     * @return 
     */
    public int Totalcost(List<Vertex<String>> path) {
        if (path == null || path.size() <= 1) {
            return 0;
        }

        List<linePassinfo> lineSegments = new ArrayList<>();
        
        Vertex<String> u = path.get(0);
        Vertex<String> v = path.get(1);
        Edge<String> firstEdge = railwayGraph.getEdge(u, v);
        if (firstEdge == null) return 0; 

        String currentLine = firstEdge.getElement();
        if (!currentLine.equals("Interchange")) {
            lineSegments.add(new linePassinfo(currentLine, 1));
        }

        for (int i = 1; i < path.size() - 1; i++) {
            u = path.get(i);
            v = path.get(i + 1);
            Edge<String> edge = railwayGraph.getEdge(u, v);
            if (edge == null) continue;

            String nextLine = edge.getElement();
            if (nextLine.equals("Interchange")) {
                continue; 
            }

            if (nextLine.equals(currentLine)) {
                lineSegments.get(lineSegments.size() - 1).numberofStation++;
            } else {
                currentLine = nextLine;
                lineSegments.add(new linePassinfo(currentLine, 1));
            }
        }

        int totalcost = 0;
        for (linePassinfo segment : lineSegments) {
           
            if (segment.name.equals("MRT Purple Line") || segment.name.equals("SRT Dark Red Line")) {
                totalcost += promotioncost; 
            } else {
                if(segment.numberofStation <= 17 && segment.numberofStation > 0)
                {
                    totalcost+=15;
                }
                else if(segment.numberofStation > 17 && segment.numberofStation < 26)
                {
                    totalcost+=50;
                }
                else
                {
                    totalcost+=62;
                }
            }
        }
        return totalcost;
    }
    /**
     * this method calculated  the totalamout of estimated time of current station to destination station
     * @param path
     * @return
     */
    public int Totaltime(List<Vertex<String>> path) {
        if (path == null || path.size() <= 1) {
            return 0;
        }
        int totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Vertex<String> prev = path.get(i);
            Vertex<String> next = path.get(i + 1);
            Edge<String> edge = railwayGraph.getEdge(prev, next);
            if (edge == null) continue; 
            totalTime += edge.getElement().equals("Interchange") ? interchangetime : stationtime;
        }
        return totalTime;
    }

    /**
     * Compute the Levenshtein distance between two strings (case-sensitive caller may lower-case first).
     */
    private int levenshtein(String s1, String s2) {
        if (s1 == null) return (s2 == null) ? 0 : s2.length();
        if (s2 == null) return s1.length();

        int len1 = s1.length();
        int len2 = s2.length();
        int[] prev = new int[len2 + 1];
        for (int j = 0; j <= len2; j++) prev[j] = j;

        for (int i = 1; i <= len1; i++) {
            int[] cur = new int[len2 + 1];
            cur[0] = i;
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                cur[j] = Math.min(Math.min(prev[j] + 1, cur[j - 1] + 1), prev[j - 1] + cost);
            }
            prev = cur;
        }

        return prev[len2];
    }

    /**
     * Check if a station is available.
     * - If exact match (case-insensitive) and not in maintenance: return true.
     * - If exact match but in maintenance: print unavailable and return false.
     * - If no exact match: print suggested similar station names and return false.
     * Suggestions are built from contains/startsWith matches and short Levenshtein distances.
     */
    public boolean checkStationAvailable(String stationName) {
        if (stationName == null || stationName.trim().isEmpty()) {
            System.out.println("Station name is empty.");
            return false;
        }

        String query = stationName.trim();

        // Try to find exact match ignoring case
        String matchedKey = null;
        for (String key : stations.keySet()) {
            if (key.equalsIgnoreCase(query)) {
                matchedKey = key;
                break;
            }
        }

        if (matchedKey != null) {
            // Check maintenance list (case-insensitive)
            for (String m : MatainanceList) {
                if (m.equalsIgnoreCase(matchedKey)) {
                    System.out.println("Station '" + matchedKey + "' is currently unavailable (maintenance).");
                    return false;
                }
            }
            // Found and available
            return true;
        }

        // No exact match: build suggestions
        List<String> suggestions = new ArrayList<>();
        String qLower = query.toLowerCase();

        // First pass: contains or startsWith matches
        for (String key : stations.keySet()) {
            String kLower = key.toLowerCase();
            if (kLower.contains(qLower) || kLower.startsWith(qLower)) {
                suggestions.add(key);
            }
        }

        // Second pass: levenshtein-based close matches (distance threshold)
        Map<String, Integer> close = new HashMap<>();
        for (String key : stations.keySet()) {
            int d = levenshtein(qLower, key.toLowerCase());
            if (d <= 3) { // threshold: allow small typos
                close.put(key, d);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(close.entrySet());
        entries.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return Integer.compare(a.getValue(), b.getValue());
            }
        });

        for (Map.Entry<String, Integer> e : entries) {
            if (!suggestions.contains(e.getKey())) suggestions.add(e.getKey());
            if (suggestions.size() >= 5) break;
        }

        if (suggestions.isEmpty()) {
            System.out.println("Station '" + stationName + "' not found and no similar stations were found.");
        } else {
            System.out.println("Station '" + stationName + "' not found. Did you mean:");
            for (String s : suggestions) System.out.println(" - " + s);
        }

        return false;
    }

    public static void main(String[] args) {
        main bkkRailwayApp = new main();
        List<String> MaintenanceStation = new ArrayList<>();
        bkkRailwayApp.addMaintenanceList(MaintenanceStation);

        // MaintananceStation List
        bkkRailwayApp.loadConnections("source/resource/connections.csv");
        System.out.println("Number of stations (vertices): " + bkkRailwayApp.railwayGraph.numVertices());
        System.out.println("Number of connections (edges): " + bkkRailwayApp.railwayGraph.numEdges());

        System.out.println("\n--- Finding Shortest Path ---");
        
        java.io.Console console = System.console();
        java.util.Scanner scanner = null;
        if (console == null) {
            // Fallback for IDEs where System.console() is null
            scanner = new java.util.Scanner(System.in);
            System.out.println("No console available. Falling back to standard input. Type 'exit' to quit.");
        }

        String start = null;
        // Prompt until a valid station is entered or user types 'exit'
        while (true) {
            String input;
            if (console != null) {
                input = console.readLine("Enter starting station (or 'exit' to quit): ");
            } else {
                System.out.print("Enter starting station (or 'exit' to quit): ");
                input = scanner.hasNextLine() ? scanner.nextLine() : null;
            }
            if (input == null) {
                System.out.println("No input received. Exiting.");
                if (scanner != null) scanner.close();
                return;
            }
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting.");
                if (scanner != null) scanner.close();
                return;
            }
            if (bkkRailwayApp.checkStationAvailable(input)) {
                start = input.trim();
                break;
            }
            // else loop and let checkStationAvailable print suggestions
        }

        String end = null;
        while (true) {
            String input;
            if (console != null) {
                input = console.readLine("Enter destination station (or 'exit' to quit): ");
            } else {
                System.out.print("Enter destination station (or 'exit' to quit): ");
                input = scanner.hasNextLine() ? scanner.nextLine() : null;
            }
            if (input == null) {
                System.out.println("No input received. Exiting.");
                if (scanner != null) scanner.close();
                return;
            }
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Exiting.");
                if (scanner != null) scanner.close();
                return;
            }
            if (bkkRailwayApp.checkStationAvailable(input)) {
                end = input.trim();
                break;
            }
        }
    // Do not close scanner yet; we'll use it for additional prompts below in IDE fallback

        // Check for exclude-maintenance flag in CLI args
        for (String a : args) {
            if (a.equalsIgnoreCase("-e") || a.equalsIgnoreCase("--exclude-maintenance")) {
                bkkRailwayApp.setExcludeMaintenance(true);
                System.out.println("Maintenance-exclusion enabled: routes will avoid maintenance stations.");
                break;
            }
        }

        // Ask whether user wants a route that minimizes transfers
        boolean minimizeTransfers = false;
        String choice = null;
        if (console != null) {
            choice = console.readLine("Would you like to minimize transfers? (y/N): ");
        } else {
            System.out.print("Would you like to minimize transfers? (y/N): ");
            choice = scanner.hasNextLine() ? scanner.nextLine() : null;
        }
        if (choice != null && (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes"))) minimizeTransfers = true;

        List<Vertex<String>> path;
        if (minimizeTransfers) {
            path = bkkRailwayApp.findPathFewestTransfers(start, end);
        } else {
            path = bkkRailwayApp.findShortestPath(start, end);
        }

        if (path != null) {
            System.out.println((minimizeTransfers ? "Route (minimized transfers)" : "Shortest path") + " from " + start + " to " + end + ":");
            // Print path with line visualization between each segment
            for (int i = 0; i < path.size() - 1; i++) {
                Vertex<String> u = path.get(i);
                Vertex<String> v = path.get(i + 1);
                Edge<String> edge = bkkRailwayApp.railwayGraph.getEdge(u, v);
                String lineName = (edge == null) ? "Unknown" : edge.getElement();
                System.out.print(i + 1 + ". " + u.getElement() + " (" + lineName + ")");
                System.out.println();
            }
            // print final station
            System.out.println(path.get(path.size() - 1).getElement() + " (Destination)");

            System.out.println("Total stops: " + (path.size() - 1));
            System.out.println("Total cost: " + bkkRailwayApp.Totalcost(path));
            System.out.println("Estimated Total time: " + bkkRailwayApp.Totaltime(path)/60 + " hour " + bkkRailwayApp.Totaltime(path)%60 + " minute");
        } else {
            System.out.println("No path found from " + start + " to " + end);
        }
    }    
}
