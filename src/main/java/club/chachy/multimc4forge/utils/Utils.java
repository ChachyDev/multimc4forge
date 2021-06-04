package club.chachy.multimc4forge.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Utils {
    public static Utils INSTANCE = new Utils();

    public String readString(InputStream stream) {
        byte[] bytes = readBytes(stream);

        return new String(bytes, 0, bytes.length);
    }

    public byte[] readBytes(InputStream s) {
        try (InputStream stream = s) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int read;

                while ((read = stream.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, read);
                }

                return bos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public Map<String, String> readHOCONtoMap(InputStream stream) {
        return readHOCONtoMap(stream, new HashMap<>());
    }

    public Map<String, String> readHOCONtoMap(InputStream stream, Map<String, String> mappings) {
        Map<String, String> map = new HashMap<>();

        String hocon = readString(stream);

        for (String line : hocon.split("\n")) {
            String[] keyValue = line.split("=");

            String key = keyValue[0].replace("\r", "")
                .replace("\n", "");
            String value = keyValue[1].replace("\r", "")
                .replace("\n", "");

            if (!mappings.isEmpty()) {
                if (mappings.containsKey(value)) {
                    value = mappings.get(value);
                }
            }

            map.put(key, value);
        }

        return map;
    }

    public String buildMapToHOCON(Map<String, String> hoconMap) {
        StringBuilder builder = new StringBuilder();

        Set<Map.Entry<String, String>> entries = hoconMap.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            builder.append(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
        }

        return builder.toString();
    }

    public String remapHOCONFile(InputStream stream, Map<String, String> mappings) {
        return buildMapToHOCON(Utils.INSTANCE.readHOCONtoMap(stream, mappings));
    }
}
