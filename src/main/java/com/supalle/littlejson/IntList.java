package com.supalle.littlejson;

public class IntList {

    private int[] elements = new int[32];
    private int size = 0;

    public void add(int aInt) {
        int[] elements = this.elements;
        int length = elements.length;
        if (size >= length) {
            int[] newElements = new int[size + 32];
            System.arraycopy(elements, 0, newElements, 0, length);
            this.elements = elements = newElements;
        }
        elements[size++] = aInt;
    }

    public int[] getElements() {
        return this.elements;
    }

}