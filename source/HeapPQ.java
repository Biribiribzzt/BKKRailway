package source;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * A binary-heap based priority queue implementation.
 * insert: O(log n), removeMin: O(log n), min: O(1)
 */
public class HeapPQ<K, V> implements PriorityQueue<K, V> {
    private ArrayList<Entry<K, V>> heap;
    private Comparator<K> comparator;

    public HeapPQ() {
        this.heap = new ArrayList<>();
        this.comparator = new Comparator<K>() {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(K o1, K o2) {
                if (o1 instanceof Comparable) {
                    return ((Comparable<K>) o1).compareTo(o2);
                } else {
                    throw new IllegalArgumentException("Key must be Comparable or a Comparator must be provided");
                }
            }
        };
    }

    public HeapPQ(Comparator<K> comp) {
        this.heap = new ArrayList<>();
        this.comparator = comp;
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    private void checkKey(K key) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
    }

    @Override
    public void insert(K key, V value) {
        checkKey(key);
        Entry<K, V> entry = new Entry<>(key, value);
        heap.add(entry);
        upHeap(heap.size() - 1);
    }

    @Override
    public Entry<K, V> removeMin() {
        if (heap.isEmpty()) return null;
        Entry<K, V> min = heap.get(0);
        Entry<K, V> last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            downHeap(0);
        }
        return min;
    }

    @Override
    public Entry<K, V> min() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    private void upHeap(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) / 2;
            if (compare(heap.get(idx).getKey(), heap.get(parent).getKey()) >= 0) break;
            swap(idx, parent);
            idx = parent;
        }
    }

    private void downHeap(int idx) {
        int size = heap.size();
        while (true) {
            int left = 2 * idx + 1;
            int right = 2 * idx + 2;
            int smallest = idx;
            if (left < size && compare(heap.get(left).getKey(), heap.get(smallest).getKey()) < 0) {
                smallest = left;
            }
            if (right < size && compare(heap.get(right).getKey(), heap.get(smallest).getKey()) < 0) {
                smallest = right;
            }
            if (smallest == idx) break;
            swap(idx, smallest);
            idx = smallest;
        }
    }

    private int compare(K a, K b) {
        return comparator.compare(a, b);
    }

    private void swap(int i, int j) {
        Entry<K, V> tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }
}
