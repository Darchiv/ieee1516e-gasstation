package util;

public class Uint32 {
    private int value;

    public Uint32() {
        this(0);
    }

    public Uint32(int value) {
        this.value = value;
    }

    public Uint32(byte[] arr) {
        if (arr.length != 4) {
            throw new IllegalArgumentException("Not a uint32 byte array: length=" + arr.length);
        }

        this.value = (arr[0] << 24) + (arr[1] << 16) + (arr[2] << 8) + arr[3];
    }

    public int getValue() {
        return this.value;
    }

    public byte[] getByteArray() {
        // Encode as a big-endian 32-bit integer
        byte[] arr = new byte[4];
        arr[0] = (byte) (this.value >>> 24);
        arr[1] = (byte) (this.value >>> 16);
        arr[2] = (byte) (this.value >>> 8);
        arr[3] = (byte) this.value;

        return arr;
    }
}
