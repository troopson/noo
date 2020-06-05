package noo.util.codec;

import java.nio.charset.StandardCharsets;

public final class CodeUtils {

    private CodeUtils() {
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        String sTmp;
        for (byte b : data) {
            sTmp = Integer.toHexString(0xFF & b);
            if (sTmp.length() < 2) sb.append(0);
            sb.append(sTmp.toUpperCase());
        }
        return sb.toString();
    }

    public static String toHex(String src) {

        return toHex(src.getBytes(StandardCharsets.UTF_8));
    }
}
