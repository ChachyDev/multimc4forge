package club.chachy.multimc4forge.api;

import java.io.File;
import java.io.IOException;

public interface Installer {
    boolean detect(File forgeJar);

    void install(File installationDirectory, File forgeJar) throws IOException;
}
