package club.chachy.multimc4forge.installer;

import club.chachy.multimc4forge.api.Installer;
import club.chachy.multimc4forge.legacy.LegacyInstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Installers {
    private static final List<Installer> installerList = new ArrayList<>();

    public static Installer getInstaller(File forgeJar) {
        Installer installer = null;

        if (installerList.isEmpty()) {
            // Add the defaults
            installerList.add(new LegacyInstaller());
        }

        for (Installer i : installerList) {
            if (i.detect(forgeJar)) {
                installer = i;
                break;
            }
        }

        if (installer == null) {
            throw new IllegalStateException("There are no installers currently available for the provided jar, is it a Forge jar...?");
        }

        return installer;
    }

    public static void addInstaller(Installer installer) {
        installerList.add(installer);
    }
}
