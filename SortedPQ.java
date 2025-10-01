import java.util.Iterator;

public class SortedPQ<K ,V> implements PriorityQueue<K,V>{
    private DoublyLinkedList<Entry<K,V>> list;
    private java.util.Comparator<K> comparator;

    public SortedPQ() {
        list = new DoublyLinkedList<>();
        comparator = new java.util.Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                if (o1 instanceof Comparable) {
                    return ((Comparable<K>) o1).compareTo(o2);
                } else {
                    throw new IllegalArgumentException("Key must be Comparable");
                }
            }
        };
    }

    public SortedPQ(java.util.Comparator<K> comp) {
        list = new DoublyLinkedList<>();
        comparator = comp;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public void insert(K key, V value) throws IllegalArgumentException {
        checkKey(key);
        Entry<K,V> newEntry = new Entry<>(key, value);
        Iterator<Entry<K,V>> walk = list.iterator();
        int index = 0;
        int prevIndex = -1;
        while (walk.hasNext()) {
            Entry<K,V> entry = walk.next();
            if (comparator.compare(key, entry.getKey()) < 0) {
                if(index == 0){
                    list.addFirst(newEntry);
                    return;
                }
                list.addbetween(entry, index, prevIndex);


            }
            index++;
            prevIndex++;
        }
        list.addLast(newEntry);
    }

    private void checkKey(K key) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
    }

    @Override
    public Entry<K, V> removeMin() {
        return list.removeFirst();
    }

    @Override
    public Entry<K, V> min() {
        return list.getFirst();
    }
     
}
