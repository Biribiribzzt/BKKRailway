import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class AdjacencyMapGraph<V, E> implements GraphADT<V, E> {
    private boolean isDirected;
    private int numVertices;
    private int numEdges;

    private Map<Vertex<V>, Map<Vertex<V>, Edge<E>>> adjMap = new java.util.HashMap<>();

    @SuppressWarnings("hiding")
    private class InnerAdjacencyMapGraph<V> implements Vertex<V> {
        private V element;
        public InnerAdjacencyMapGraph(V element) {
            this.element = element;
        }
        public V getElement() {
            return element;
        }
        public int hashCode() {
            return element.hashCode();
        }
        @SuppressWarnings("unused")
        public void setElement(V element) {
            this.element = element;
        }
    }
    @SuppressWarnings("hiding")
    private class InnerEdge<E> implements Edge<E> {
        private E element;
        public InnerEdge(E element) {
            this.element = element;
        }
        public E getElement() {
            return element;
        }
    }
    public AdjacencyMapGraph(boolean isDirected) {
        this.isDirected = isDirected;
        numVertices = 0;
        numEdges = 0;
        adjMap = new java.util.HashMap<>();
    }

    public int numVertices() {
        return numVertices;
    }

    public int numEdges() {
        return numEdges;
    }

    public Iterable<Vertex<V>> vertices() {
        return adjMap.keySet();
    }

    public Iterable<Edge<E>> edges() {
        java.util.HashMap<Edge<E>, Boolean> edgeSet = new java.util.HashMap<>();
        for (Map<Vertex<V>, Edge<E>> map : adjMap.values()) {
            for (Edge<E> edge : map.values()) {
                edgeSet.put(edge, true);
            }
        }
        return edgeSet.keySet();
    }

    public Edge<E> getEdge(Vertex<V> u, Vertex<V> v) {
        if (adjMap.get(u) == null || adjMap.get(v) == null) {
            return null;
        }
        return adjMap.get(u).get(v);
    }


    public Vertex<V> opposite(Vertex<V> v, Edge<E> e) {
        Vertex<V>[] ends = endVertices(e);
        if (ends[0] == v) {
            return ends[1];
        } else if (ends[1] == v) {
            return ends[0];
        } else {
            throw new IllegalArgumentException("Vertex not incident to edge");
        }
    }

    public int outDegree(Vertex<V> v) {
        if (adjMap.get(v) == null) {
            throw new IllegalArgumentException("Vertex not found");
        }
        return adjMap.get(v).size();
    }

    public int inDegree(Vertex<V> v) {
        if (adjMap.get(v) == null) {
            throw new IllegalArgumentException("Vertex not found");
        }
        int inDegree = 0;
        for (Map<Vertex<V>, Edge<E>> map : adjMap.values()) {
            if (map.get(v) != null) {
                inDegree++;
            }
        }
        return inDegree;
    }

    public Iterable<Edge<E>> outgoingEdges(Vertex<V> v) {
        if (adjMap.get(v) == null) {
            throw new IllegalArgumentException("Vertex not found");
        }
        return adjMap.get(v).values();
    }

    public Iterable<Edge<E>> incomingEdges(Vertex<V> v) {
        if (adjMap.get(v) == null) {
            throw new IllegalArgumentException("Vertex not found");
        }
        java.util.HashMap<Edge<E>, Boolean> inEdges = new java.util.HashMap<>();
        for (Map<Vertex<V>, Edge<E>> map : adjMap.values()) {
            if (map.get(v) != null) {
                inEdges.put(map.get(v), true);
            }
        }
        return inEdges.keySet();
    }

    public Vertex<V> insertVertex(V element) {
        Vertex<V> v = new InnerAdjacencyMapGraph<>(element);
        if (adjMap.get(v) != null) {
            throw new IllegalArgumentException("Vertex already exists");
        }
        else if (element == null) {
            throw new IllegalArgumentException("Vertex element cannot be null");
        }
        adjMap.put(v, new java.util.HashMap<>());
        numVertices++;
        return v;
    }

    public Edge<E> insertEdge(Vertex<V> u, Vertex<V> v, E element) {
        if (adjMap.get(u) == null || adjMap.get(v) == null) {
            throw new IllegalArgumentException("One or both vertices not found");
        }
        if (getEdge(u, v) != null) {
            throw new IllegalArgumentException("Edge already exists");
        }
        Edge<E> e = new InnerEdge<>(element);
        adjMap.get(u).put(v, e);
        if (!isDirected) {
            adjMap.get(v).put(u, e);
        }
        numEdges++;
        return e;
    }

    public void removeVertex(Vertex<V> v) {
        if (adjMap.get(v) == null) {
            throw new IllegalArgumentException("Vertex not found");
        }
        // Remove all edges incident to v
        for (Map<Vertex<V>, Edge<E>> map : adjMap.values()) {
            if (map.get(v) != null) {
                map.remove(v);
                numEdges--;
            }
        }
        // Remove the vertex
        numEdges -= adjMap.get(v).size();
        adjMap.remove(v);
        numVertices--;
    }

    public void removeEdge(Edge<E> e) {
        boolean found = false;
        for (Map<Vertex<V>, Edge<E>> map : adjMap.values()) {
            Iterator<Entry<Vertex<V>, Edge<E>>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Vertex<V>, Edge<E>> entry = it.next();
                if (entry.getValue() == e) {
                    it.remove();
                    found = true;
                    numEdges--;
                    if (!isDirected) {
                        // Also remove the reverse edge
                        Vertex<V> u = entry.getKey();
                        for (Map<Vertex<V>, Edge<E>> revMap : adjMap.values()) {
                            if (revMap.get(u) == e) {
                                revMap.remove(u);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            if (found) break;
        }
        if (!found) {
            throw new IllegalArgumentException("Edge not found");
        }
    }  
    public Vertex<V>[] endVertices(Edge<E> e) {
        @SuppressWarnings("unchecked")
        Vertex<V>[] ends = (Vertex<V>[]) new Vertex[2];
        boolean found = false;
        for (Map.Entry<Vertex<V>, Map<Vertex<V>, Edge<E>>> entry : adjMap.entrySet()) {
            Vertex<V> u = entry.getKey();
            Map<Vertex<V>, Edge<E>> map = entry.getValue();
            for (Map.Entry<Vertex<V>, Edge<E>> innerEntry : map.entrySet()) {
                Vertex<V> v = innerEntry.getKey();
                Edge<E> edge = innerEntry.getValue();
                if (edge == e) {
                    ends[0] = u;
                    ends[1] = v;
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        if (!found) {
            throw new IllegalArgumentException("Edge not found");
        }
        return ends;
    }
}