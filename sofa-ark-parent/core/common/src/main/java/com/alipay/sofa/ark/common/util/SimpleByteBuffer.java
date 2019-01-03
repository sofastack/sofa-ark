package com.alipay.sofa.ark.common.util;

/**
 * A helper class, which buffers one line of input. It provides for simple line editing, e.g.
 * insertion, deletion, backspace, left and right movement
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class SimpleByteBuffer {

    private final static int BUFFER_CHUNK = 20;

    private byte[]           buffer;

    private int              pos          = 0;

    private int              size         = 0;

    public SimpleByteBuffer() {
        buffer = new byte[BUFFER_CHUNK];
    }

    private void resize() {
        final byte[] next = new byte[buffer.length << 1];
        System.arraycopy(buffer, 0, next, 0, buffer.length);
        buffer = next;
    }

    public void add(byte b) {
        if (size >= buffer.length) {
            resize();
        }
        buffer[size++] = b;
    }

    public void insert(byte b) {
        if (size >= buffer.length) {
            resize();
        }
        final int gap = size - pos;
        if (gap > 0) {
            System.arraycopy(buffer, pos, buffer, pos + 1, gap);
        }
        buffer[pos++] = b;
        size++;
    }

    public byte goRight() {
        if (pos < size) {
            return buffer[pos++];
        }
        return -1;
    }

    public boolean goLeft() {
        if (pos > 0) {
            pos--;
            return true;
        }
        return false;
    }

    public void backSpace() {
        if (pos > 0) {
            System.arraycopy(buffer, pos, buffer, pos - 1, size - pos);
            pos--;
            size--;
        }
    }

    public void delete() {
        if (pos < size) {
            System.arraycopy(buffer, pos + 1, buffer, pos, size - pos);
            size--;
        }
    }

    public byte[] getBuffer() {
        byte[] data = new byte[size];
        System.arraycopy(buffer, 0, data, 0, size);
        return data;
    }

    public byte[] getAndClearBuffer() {
        byte[] data = new byte[size];
        System.arraycopy(buffer, 0, data, 0, size);
        size = 0;
        pos = 0;
        return data;
    }

    public int getPos() {
        return pos;
    }

    public int getSize() {
        return size;
    }

    public int getGap() {
        return size - pos;
    }

}