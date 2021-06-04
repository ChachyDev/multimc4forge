package club.chachy.multimc4forge.utils;

import com.google.gson.*;

public class MojangFormatConverter {
    public static JsonObject convert(String uid, String version, JsonObject mojangJson) {
        JsonObject multiMcJson = new JsonObject();

        JsonArray array = new JsonArray();

        String[] minecraftArgs = mojangJson.get("minecraftArguments").getAsString().split(" ");

        int index = 0;
        for (String minecraftArg : minecraftArgs) {
            if (minecraftArg.equals("--tweakClass")) {
                array.add(minecraftArgs[index + 1]);
            }
            index++;
        }

        if (array.size() > 0) {
            multiMcJson.add("+tweakers", array);
        }

        multiMcJson.addProperty("formatVersion", 1);

        JsonArray libraries = new JsonArray();

        for (JsonElement element : mojangJson.get("libraries").getAsJsonArray()) {
            JsonObject libraryObject = new JsonObject();
            if (!(element instanceof JsonObject)) {
                throw new IllegalStateException("The given Mojang version.json is not supported...");
            }

            JsonObject obj = element.getAsJsonObject();

            String name = obj.get("name").getAsString();

            if (name.startsWith("net.minecraftforge:forge:")) {
                libraryObject.addProperty("name", name);
                libraryObject.addProperty("MMC-hint", "local");
            } else {
                libraryObject.addProperty("name", name);

                if (obj.has("url")) {
                    libraryObject.addProperty("url", obj.get("url").getAsString());
                }
            }

            libraries.add(libraryObject);
        }

        multiMcJson.add("libraries", libraries);

        multiMcJson.addProperty("name", mojangJson.get("id").getAsString());
        multiMcJson.addProperty("releaseTime", mojangJson.get("releaseTime").getAsString());

        JsonArray requiresArray = new JsonArray();

        if (mojangJson.has("inheritsFrom") || mojangJson.has("jar")) {
            String gameVersion = (mojangJson.has("inheritsFrom") ? mojangJson.get("inheritsFrom") : mojangJson.get("jar")).getAsString();
            JsonObject requiredObject = new JsonObject();

            requiredObject.addProperty("equals", gameVersion);
            requiredObject.addProperty("uid", "net.minecraft"); // We know it's the game already soooo

            requiresArray.add(requiredObject);
        }

        if (requiresArray.size() > 0) {
            multiMcJson.add("requires", requiresArray);
        }

        multiMcJson.addProperty("mainClass", mojangJson.get("mainClass").getAsString());
        multiMcJson.addProperty("uid", uid);
        multiMcJson.addProperty("version", version);

        return multiMcJson;
    }
}
