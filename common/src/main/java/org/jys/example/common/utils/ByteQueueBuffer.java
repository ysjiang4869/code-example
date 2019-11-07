package org.jys.example.common.utils;

/**
 * @author YueSong Jiang
 * @date 2018/4/5
 * @description <p> </p>
 */
public class ByteQueueBuffer {

    private byte[] array;
    private int head;
    private int tail;
    private int size;

    private int growFactor = 200;

    private final int minimumGrow = 16;

    public ByteQueueBuffer() {
        this(1024);
    }

    public ByteQueueBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        array = new byte[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public ByteQueueBuffer duplicate() {

        ByteQueueBuffer buf = new ByteQueueBuffer(size);
        buf.size = size;
        buf.array = this.peek(size);
        return buf;
    }

    public void clear() {
        head = 0;
        tail = 0;
        size = 0;
    }

    public void push(byte value) {
        if (size == array.length) {
            int newCapacity = (int) (array.length * (long) growFactor / 100);
            if (newCapacity < array.length + minimumGrow) {
                newCapacity = array.length + minimumGrow;
            }
            setCapacity(newCapacity);
        }
        array[tail] = value;
        tail = (tail + 1) % array.length;
        size++;
    }

    public void push(byte[] value) {
        if (size + value.length > array.length) {
            int adjust = Math.max(value.length, array.length);
            int newCapacity = (int) (adjust * (long) growFactor / 100);

            if (newCapacity < array.length + minimumGrow) {
                newCapacity = array.length + minimumGrow;
            }
            setCapacity(newCapacity);
        }

        int numToCopy = value.length;
        int firstPart = (array.length - tail < numToCopy) ? array.length - tail : numToCopy;
        System.arraycopy(value, 0, array, tail, firstPart);
        numToCopy -= firstPart;
        if (numToCopy > 0)
            System.arraycopy(value, array.length - tail, array, 0, numToCopy);

        tail = (tail + value.length) % array.length;
        size += value.length;
    }

    public byte peek() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("no data in buf");
        }
        return array[head];
    }

    public byte[] peek(int len) {
        if (len > size) {
            throw new IndexOutOfBoundsException("no enough data in buf");
        }

        byte[] data = new byte[len];
        int numToCopy = len;
        int firstPart = (array.length - head < numToCopy) ? array.length - head : numToCopy;
        System.arraycopy(array, head, data, 0, firstPart);
        numToCopy -= firstPart;
        if (numToCopy > 0) {
            System.arraycopy(array, 0, data, array.length - head, numToCopy);
        }
        return data;
    }

    /**
     * retun the first value of queue
     *
     * @return queue head value
     */
    public byte pop() {
        if (size == 0) {
            throw new IndexOutOfBoundsException("no data in buf");
        }
        byte removed = array[head];
        //array[head] = 0; no need to release
        head = (head + 1) % array.length;
        size--;
        return removed;
    }

    public byte[] pop(int len) {
        byte[] data = this.peek(len);
        head = (head + len) % array.length;
        size -= len;
        return data;
    }


    public void append(ByteQueueBuffer buf) {
        byte[] data = buf.peek(buf.size);
        this.push(data);
    }

    private void setCapacity(int capacity) {
        byte[] newArray = new byte[capacity];
        if (size > 0) {
            if (head < tail) {
                System.arraycopy(array, head, newArray, 0, size);
            } else {
                System.arraycopy(array, head, newArray, 0, array.length - head);
                System.arraycopy(array, 0, newArray, array.length - head, tail);
            }
        }

        array = newArray;
        head = 0;
        tail = (size == capacity) ? 0 : size;
    }
}
