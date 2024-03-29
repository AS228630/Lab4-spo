package com.company;

import javax.swing.text.html.parser.Parser;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import com.sun.tools.javac.util.Pair;

import java.util.concurrent.atomic.AtomicMarkableReference;
public class Main {

    public interface Set<T extends Comparable<T>> {
        boolean add(T value);
        boolean remove(T value);
        boolean contains(T value);
        boolean isEmpty();
        java.util.Iterator<T>  iterator();
    }
    public class LockFreeSet<T extends Comparable<T>> implements Set<T> {
        private class Node {
            public T value;
            public AtomicMarkableReference<Node> next;

            public Node() {
                this.value = null;
                this.next = new AtomicMarkableReference<>(null, false);
            }

            public Node(T value) {
                this.value = value;
                this.next = new AtomicMarkableReference<>(null, false);
            }
        }
        private Node head;


        public LockFreeSet() {
            this.head = new Node();
        }

        @Override
        public boolean add(T value) {
            while (true) {
                Node pred, curr;
                Pair<Node, Node> findKey = find(value);
                pred = findKey.fst;
                curr = findKey.snd;

                if (curr != null && curr.value.compareTo(value) == 0) {
                    return false;
                } else {
                    Node node = new Node(value);
                    node.next.set(curr, false);

                    if (pred.next.compareAndSet(curr, node, false, false)) {
                        return true;
                    }
                }
            }
        }

        private Pair<Node, Node> find(T key) {
            retry: while (true) {
                Node pred = head;

                Node curr = pred.next.getReference();
                if (curr == null) {
                    return new Pair<>(pred, null);
                }

                Node succ;

                while (true) {
                    succ = curr.next.getReference();
                    boolean[] holder = new boolean[1];

                    curr.next.get(holder);
                    boolean cmk = holder[0];

                    if (cmk) {
                        if (!pred.next.compareAndSet(curr, succ, false, false))
                            continue retry;
                        curr = succ;
                    } else {
                        if (curr.value.compareTo(key) >= 0) return new Pair<>(pred, curr);
                        pred = curr;
                        curr = succ;

                        if (curr == null) {
                            return new Pair<>(pred, null);
                        }
                    }
                }
            }
        }

        @Override
        public boolean remove(T value) {
            while (true) {
                Node pred, curr;
                Node succ;
                Pair<Node, Node> findKey = find(value);
                pred = findKey.fst;
                curr = findKey.snd;

                if (curr == null || curr.value.compareTo(value) != 0) {
                    return false;
                } else {
                    succ = curr.next.getReference();

                    if (!curr.next.compareAndSet(curr.next.getReference(), curr.next.getReference(), false, true)) {
                        continue;
                    }
                }


                pred.next.compareAndSet(curr, succ, false, false);

                return true;
            }
        }

        @Override
        public boolean contains(T value) {
            Node curr = head.next.getReference();
            while (curr != null && curr.value.compareTo(value) < 0) {
                curr = curr.next.getReference();
            }

            return curr != null && value.compareTo(curr.value) == 0 && !curr.next.isMarked();
        }

        @Override
        public boolean isEmpty() {
            return head.next.getReference() == null;
        }
        class SetIterator implements Iterator<T> {
            private int index = -1;
            private LockFreeSet<T> m;

            public SetIterator(LockFreeSet<T> m) {
                this.m = m;
            }

            public boolean hasNext() {

                if(m.head.next!=null)
                    return true;
                return false;
            }

            public T next() {
                return m.head.value;
            }

            public void remove() {

            }}


            @Override
        public  java.util.Iterator<T>  iterator(){
            return new SetIterator(this);
        }


        public void print() {
            Node curr = head;
            while (curr != null) {
                if (curr.next.getReference() == null) {
                    System.out.print(curr.value + "\n");
                } else {
                    System.out.print(curr.value + " -> ");
                }
                curr = curr.next.getReference();
            }
        }
    }
    public static void main(String[] args){

    }
}