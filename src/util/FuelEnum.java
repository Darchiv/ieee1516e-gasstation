package util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FuelEnum {
    private String value;

    public FuelEnum(String value) {
        if(!value.equals("diesel") && !value.equals("petrol")){
            throw new IllegalArgumentException("Value " + value + " not compliant with enumerator");
        }
        this.value=value;
    }

    public String getValue() { return this.value; }

    public byte[] getByteArray() {
        // UTF-8 Encode
        final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
        return value.getBytes(UTF8_CHARSET);
    }
}
