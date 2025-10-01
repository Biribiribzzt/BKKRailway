# BKK Railway Pathfinding System

This project is a Java-based application that models the Bangkok railway network (including BTS, MRT, ARL, and other lines) as a graph. It implements Dijkstra's algorithm to find the optimal route between any two stations based on the shortest travel time, while also calculating the estimated fare and total time for the journey.

## Features

*   **Graph-Based Network Model:** Represents the entire BKK railway system using a custom graph data structure (`AdjacencyMapGraph`).
*   **CSV Data Loading:** Dynamically loads station and line connection data from a `connections.csv` file.
*   **Shortest Path Calculation:** Implements Dijkstra's algorithm to find the route with the minimum travel time.
*   **Dynamic Weighting:** Considers different travel times for station-to-station travel vs. line interchanges.
*   **Maintenance Handling:** Allows for specifying stations under maintenance, which are heavily penalized to be avoided in pathfinding.
*   **Fare Calculation:** Calculates the total travel cost based on the lines used and the number of stops, including special promotional fares.
*   **Time Estimation:** Provides an estimated total travel time for the calculated route.

## How It Works

### 1. Graph Modeling

The railway network is modeled as an **undirected graph** where:
*   **Vertices**: Each unique station (e.g., 'Siam', 'Asok') is a vertex in the graph.
*   **Edges**: A connection between two stations is an edge. The data stored in the edge is the name of the railway line (e.g., `BTS Sukhumvit Line`, `Interchange`).

This model is built by the `loadConnections` method, which parses the `source/resource/connections.csv` file.

### 2. Pathfinding Algorithm

The `findShortestPath` method uses **Dijkstra's algorithm** to find the optimal route. The "cost" or "weight" for each path segment is determined by time:

*   A standard trip between two adjacent stations has a default time cost (e.g., 3 units).
*   An interchange between lines at the same physical station has a higher time cost (e.g., 10 units).
*   A station listed in the `MatainanceList` adds a massive penalty (10,000 units) to any route passing through it, effectively forcing the algorithm to find an alternative.

The algorithm uses a custom-built `SortedPQ` (a Priority Queue implemented with a sorted Doubly Linked List) to always explore the path with the lowest accumulated time.

### 3. Cost and Time Calculation

Once the shortest path (a list of station vertices) is found:
*   `Totaltime()`: Calculates the total estimated time by summing the time costs of each edge in the path.
*   `Totalcost()`: Calculates the fare by segmenting the journey by line, counting the stops on each line, and applying a fare structure. It correctly handles promotional fares for specific lines.

## Data Structures Used

This project relies on several custom data structures built from the ground up:

*   `GraphADT` / `AdjacencyMapGraph`: An adjacency map implementation of a graph.
*   `PriorityQueue` / `SortedPQ`: A priority queue used for Dijkstra's algorithm.
*   `DoublyLinkedList`: The underlying structure for the `SortedPQ`.
*   `Vertex` and `Edge` interfaces.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 11 or higher.

### How to Run

1.  Compile and run the `source/main.java` file.
    ```bash
    # Navigate to the project's root directory
    javac -d . source/*.java
    java source.main
    ```

2.  The program will:
    *   Load the station data from `source/resource/connections.csv`.
    *   Set a predefined list of stations under maintenance.
    *   Calculate the shortest path between a predefined `start` and `end` station (e.g., "Min Buri" to "Kheha").
    *   Print the resulting path, total stops, total cost, and estimated time to the console.

### Example Output

```
Number of stations (vertices): 119
Number of connections (edges): 135

--- Finding Shortest Path ---
Shortest path from Min Buri to Kheha:
Min Buri -> Min Buri Market -> ... -> Samrong -> ... -> Kheha
Total stops: 39
Total cost: 124
Estimated Total time: 2 hour 21 minute
```

## Configuration

You can easily configure the pathfinding parameters directly in the `main.java` file:

*   **Start and End Stations:** Change the `start` and `end` string variables in the `main` method.
*   **Maintenance Stations:** Add or remove station names from the `MaintenanceStation` list in the `main` method.
*   **Time and Cost Variables:** Adjust the instance variables for `stationtime`, `interchangetime`, and `promotioncost` to see how they affect the results.

## File Structure

```
BKKRailway/
├── source/
│   ├── main.java               # Main application logic, Dijkstra's, cost/time calculation.
│   ├── AdjacencyMapGraph.java  # Graph implementation.
│   ├── SortedPQ.java           # Priority Queue implementation.
│   ├── DoublyLinkedList.java   # Linked list implementation.
│   ├── ... (Interfaces: GraphADT, Vertex, Edge, etc.)
│   └── resource/
│       └── connections.csv     # Data file for all station connections.
└── README.md                   # This file.
```

This `README.md` provides a clear and professional overview of your project, making it easy for others (or your future self) to understand its purpose, design, and usage.

<!--
[PROMPT_SUGGESTION]Create an API endpoint in Java to expose the shortest path, cost, and time.[/PROMPT_SUGGESTION]
[PROMPT_SUGGESTION]How can I improve the efficiency of the `SortedPQ` by using a binary heap?[/PROMPT_SUGGESTION]
