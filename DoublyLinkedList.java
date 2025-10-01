import java.util.Iterator; 
import java.util.NoSuchElementException; 

public class DoublyLinkedList<E> implements Iterable<E> {
    private Node<E> head;
    private Node<E> tail;
    private int size;

    private static class Node<E> {
        E data;
        Node<E> next;
        Node<E> prev;

        Node(E data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }

    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void addFirst(E element) {
        Node<E> newNode = new Node<>(element);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    public void addLast(E element) {
        Node<E> newNode = new Node<>(element);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    
    public void addbetween(E element, int index1, int index2) {
        int idx = index1; 

        if (idx < 0 || idx > size) { 
            throw new IndexOutOfBoundsException("Index " + idx + " is out of bounds for size " + size);
        }
        if (idx == 0) {
            addFirst(element);
        } else if (idx == size) { 
            addLast(element);
        } else {
            Node<E> newNode = new Node<>(element);
            Node<E> current = head;
   
            for (int i = 0; i < idx; i++) {
                current = current.next;
            }
         
            Node<E> prevNode = current.prev;

            newNode.prev = prevNode;
            newNode.next = current;
            prevNode.next = newNode;
            current.prev = newNode;
            size++;
        }
    }


    public E removeFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        E data = head.data;
        if (head == tail) {
            head = tail = null;
        } else {
            head = head.next;
            if (head != null) { 
                head.prev = null;
            }
        }
        size--;
        return data;
    }

    public E removeLast() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        E data = tail.data; 
        if (head == tail) {
            head = tail = null;
        } else {
            tail = tail.prev;
            if (tail != null) {
                tail.next = null;
            }
        }
        size--;
        return data;
    }

   
    private E remove(Node<E> nodeToRemove) {
        if (nodeToRemove == null) {
            throw new IllegalArgumentException("Node to remove cannot be null");
        }
        if (nodeToRemove == head) {
            return removeFirst();
        } else if (nodeToRemove == tail) {
            return removeLast();
        } else {
          
            E data = nodeToRemove.data;
            Node<E> prevNode = nodeToRemove.prev;
            Node<E> nextNode = nodeToRemove.next;

            if (prevNode != null) {
                prevNode.next = nextNode;
            }
            if (nextNode != null) {
                nextNode.prev = prevNode;
            }
            size--;
            return data;
        }
    }


    public E getFirst() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return head.data; 
    }

    public E getLast() {
        if (isEmpty()) {
            throw new IllegalStateException("List is empty");
        }
        return tail.data;
    }

    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for size " + size);
        }
        Node<E> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node<E> current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
    
    public void printForward() {
        Node<E> current = head;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        System.out.println();
    }

    public void printReverse() {
        Node<E> current = tail;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.prev;
        }
        System.out.println();
    }
    
    public Iterator<E> iterator() {
        return new DoublyLinkedListIterator();
    }

    private class DoublyLinkedListIterator implements Iterator<E> {
        private Node<E> current; 
        private Node<E> lastReturned; 

        public DoublyLinkedListIterator() {
            this.current = head; 
            this.lastReturned = null; 
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = current; 
            current = current.next;
            return lastReturned.data;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException("next() has not been called or remove() has already been called after the last call to next()");
            }
            DoublyLinkedList.this.remove(lastReturned); 
            lastReturned = null;
        }
    }

}