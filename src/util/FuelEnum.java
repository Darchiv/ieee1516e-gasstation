package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FuelEnum {
    private String value;

    final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;

    public FuelEnum(String value) {
        if (!value.equals("diesel") && !value.equals("petrol")) {
            throw new IllegalArgumentException("Value " + value + " not compliant with enumerator");
        }
        this.value = value;
    }

    public FuelEnum(byte[] arr) {
        this(new String(arr, UTF8_CHARSET));
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getByteArray() {
        // UTF-8 Encode
        return value.getBytes(UTF8_CHARSET);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuelEnum fuelEnum = (FuelEnum) o;
        return value.equals(fuelEnum.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
