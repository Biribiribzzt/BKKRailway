package source;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

                // define the weight between interchange and regular
                int weight = e.getElement().equals("Interchange") ? interchangetime : stationtime;

                // Add a large penalty if the station is under maintenance
                int maintenancePenalty = MatainanceList.contains(v.getElement()) ? 10000 : 0;

                int newDist = dist.get(u) + weight + maintenancePenalty;

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

    public static void main(String[] args) {
        main bkkRailwayApp = new main();
        List<String> MaintenanceStation = new ArrayList<>();
        MaintenanceStation.add("Thung Song Hong");
        MaintenanceStation.add("Bang Sue");
        MaintenanceStation.add("11th Infantry Regiment");
        MaintenanceStation.add("Itsaraphap");

        bkkRailwayApp.addMaintenanceList(MaintenanceStation);


        // Use the relative path from the project root
        bkkRailwayApp.loadConnections("source/resource/connections.csv");
        System.out.println("Number of stations (vertices): " + bkkRailwayApp.railwayGraph.numVertices());
        System.out.println("Number of connections (edges): " + bkkRailwayApp.railwayGraph.numEdges());

        System.out.println("\n--- Finding Shortest Path ---");

        String start = "Min Buri";
        String end = "Kheha";

        List<Vertex<String>> path = bkkRailwayApp.findShortestPath(start, end);

        if (path != null) {
            System.out.println("Shortest path from " + start + " to " + end + ":");
            for (Vertex<String> station : path) {
                if (path.indexOf(station) == 0) {
                    System.out.print(station.getElement());
                } else
                System.out.print(" -> " + station.getElement());
            }
            System.out.println();
            System.out.println("Total stops: " + (path.size() - 1));
            System.out.println("Total cost: " + bkkRailwayApp.Totalcost(path));
            System.out.println("Estimated Total time: " + bkkRailwayApp.Totaltime(path)/60 + " hour " + bkkRailwayApp.Totaltime(path)%60 + " minute");
        } else {
            System.out.println("No path found from " + start + " to " + end);
        }
    }    
}
