package club.chachy.multimc4forge.legacy;

import club.chachy.multimc4forge.api.Installer;
import club.chachy.multimc4forge.utils.Utils;
import club.chachy.multimc4forge.utils.MojangFormatConverter;
import com.google.gson.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LegacyInstaller implements Installer {
    private static final int FORMAT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String INSTALL_PROFILE = "install_profile.json";
    private static final String FORGE_UNIVERSAL_JAR_REGEX = "^MinecraftForge-(?<mcver>[\\d.]+?)-(?<forgever>[\\d.]+?)-(?<branch>[\\S]+?)-universal\\.jar$";

    private final Pattern forgeUniversalJarPattern = Pattern.compile(FORGE_UNIVERSAL_JAR_REGEX);

    @Override
    public boolean detect(File forgeJar) {
        boolean isForgeJarPresent = false;
        boolean isVersionJsonPresent = false;

        try (ZipFile file = new ZipFile(forgeJar)) {
            Enumeration<? extends ZipEntry> entries = file.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().equals(INSTALL_PROFILE)) {
                    isVersionJsonPresent = true;

                    if (isForgeJarPresent) {
                        break;
                    }
                }

                if (entry.getName().matches(FORGE_UNIVERSAL_JAR_REGEX)) {
                    isForgeJarPresent = true;

                    if (isVersionJsonPresent) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isForgeJarPresent && isVersionJsonPresent;
    }

    @Override
    public File install(File installationDirectory, File forgeJar) throws IOException {
        ZipEntry versionJsonEntry = null;
        ZipEntry forgeJarEntry = null;


        String forgeVersion = null;
        String mcVersion = null;
        String branch = null;

        try (ZipFile file = new ZipFile(forgeJar)) {
            Enumeration<? extends ZipEntry> entries = file.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().equals(INSTALL_PROFILE)) {
                    versionJsonEntry = entry;
                }

                Matcher matcher = forgeUniversalJarPattern.matcher(entry.getName());

                if (matcher.matches()) {
                    forgeJarEntry = entry;

                    forgeVersion = matcher.group("forgever");
                    mcVersion = matcher.group("mcver");
                    branch = matcher.group("branch");
                }
            }


            if (versionJsonEntry == null | forgeJarEntry == null) {
                throw new IllegalStateException("The jar given is not a Legacy forge jar, but this should never happen...");
            }

            String version = forgeVersion(forgeVersion, mcVersion, branch);

            JsonObject installProfile = JsonParser.parseString(Utils.INSTANCE.readString(file.getInputStream(versionJsonEntry)))
                .getAsJsonObject()
                .get("versionInfo")
                .getAsJsonObject();

            JsonObject multiMcPatch = MojangFormatConverter.convert("net.minecraftforge", version, installProfile);

            return createFiles(
                installProfile.get("id").getAsString(),
                version,
                mcVersion,
                forgeVersion,
                installationDirectory,
                multiMcPatch,
                Utils.INSTANCE.readBytes(file.getInputStream(forgeJarEntry))
            );
        }
    }

    private File createFiles(String id, String version, String mcVersion, String forgeVersion, File installationDirectory, JsonObject patch, byte[] universalJar) throws IOException {
        File instanceDirectory = new File(installationDirectory, "instances/".concat(id));
        instanceDirectory.mkdirs();

        createInstanceConfig(id, instanceDirectory);
        createMMCPackJson(instanceDirectory, id, forgeVersion, mcVersion);
        createPatch("net.minecraftforge", patch, instanceDirectory);
        copyLibraries(instanceDirectory, universalJar, version);
        createDotMinecraft(instanceDirectory);

        return instanceDirectory;
    }

    private void createInstanceConfig(String id, File instanceDirectory) throws IOException {
        Map<String, String> instanceConfigMapping = new HashMap<>();
        instanceConfigMapping.put("%name%", id);

        File file = new File(instanceDirectory, "instance.cfg");
        file.createNewFile();

        InputStream legacyInstanceCfg = getClass().getResourceAsStream("/assets/legacy/base.instance.cfg");

        byte[] hoconFile = Utils.INSTANCE.remapHOCONFile(legacyInstanceCfg, instanceConfigMapping).getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(hoconFile, 0, hoconFile.length);
        }
    }

    private void createMMCPackJson(File instanceDirectory, String id, String forgeVersion, String mcVersion) throws IOException {
        File file = new File(instanceDirectory, "mmc-pack.json");
        file.createNewFile();

        JsonObject base = new JsonObject();
        JsonArray components = new JsonArray();

        components.add(createLWJGL());
        components.add(createMinecraft(mcVersion));
        components.add(createForge(id, "net.minecraftforge", forgeVersion, mcVersion, "net.minecraft"));

        base.add("components", components);
        base.addProperty("formatVersion", FORMAT_VERSION);

        byte[] json = base.toString().getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json, 0, json.length);
        }
    }

    private JsonObject createLWJGL() {
        JsonObject object = new JsonObject();

        String lwjglVersion = "2.9.4-nightly-20150209";

        object.addProperty("cachedName", "LWJGL 2");
        object.addProperty("cachedVersion", lwjglVersion);
        object.addProperty("cachedVolatile", true);
        object.addProperty("dependencyOnly", true);
        object.addProperty("uid", "org.lwjgl");
        object.addProperty("version", lwjglVersion);

        return object;
    }

    private JsonObject createMinecraft(String version) {
        JsonObject object = new JsonObject();

        object.addProperty("cachedName", "Minecraft");

        object.add("cachedRequires", createCachedRequires(true, "2.9.4-nightly-20150209", "org.lwjgl"));

        object.addProperty("cachedVersion", version);
        object.addProperty("important", true);
        object.addProperty("uid", "net.minecraft");
        object.addProperty("version", version);

        return object;
    }

    private JsonObject createForge(String id, String uid, String version, String minecraftVersion, String minecraftUid) {
        JsonObject base = new JsonObject();
        base.addProperty("cachedName", id);

        base.add("cachedRequires", createCachedRequires(false, minecraftVersion, minecraftUid));

        base.addProperty("cachedVersion", version);
        base.addProperty("uid", uid);
        base.addProperty("version", version);

        return base;
    }

    private JsonArray createCachedRequires(boolean suggests, String suggestsOrEquals, String uid) {
        JsonArray cachedRequires = new JsonArray();

        JsonObject lwjglObject = new JsonObject();
        lwjglObject.addProperty(suggests ? "suggests" : "equals", suggestsOrEquals);
        lwjglObject.addProperty("uid", uid);

        cachedRequires.add(lwjglObject);

        return cachedRequires;
    }

    private void createPatch(String uid, JsonObject patch, File instanceDirectory) throws IOException {
        File patchFolder = new File(instanceDirectory, "patches");
        patchFolder.mkdir();

        File patchFile = new File(patchFolder, uid.concat(".json"));
        patchFile.createNewFile();

        byte[] json = GSON.toJson(patch).getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fos = new FileOutputStream(patchFile)) {
            fos.write(json, 0, json.length);
        }
    }

    private void copyLibraries(File instanceDirectory, byte[] universalJar, String version) throws IOException {
        File librariesFolder = new File(instanceDirectory, "libraries");
        librariesFolder.mkdir();

        File forgeJar = new File(librariesFolder, "forge-".concat(version).concat(".jar"));
        forgeJar.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(forgeJar)) {
            fos.write(universalJar, 0, universalJar.length);
        }
    }

    private void createDotMinecraft(File instanceDirectory) {
        new File(instanceDirectory, ".minecraft").mkdir();
    }

    private String forgeVersion(String forgeVersion, String mcVersion, String branch) {
        return mcVersion.concat("-").concat(forgeVersion).concat("-").concat(branch);
    }
}
