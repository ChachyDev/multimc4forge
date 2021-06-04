package club.chachy.multimc4forge.cli;

import club.chachy.multimc4forge.installer.Installers;
import club.chachy.multimc4forge.legacy.LegacyInstaller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CLI {
    public static void main(String[] argsArray) {
        if (argsArray.length <= 0) {
            printUsage();
        }

        Installers.addInstaller(new LegacyInstaller());

        List<String> args = Arrays.asList(argsArray);

        int jarArgIndex = args.indexOf("--jar");

        int directoryArgIndex = args.indexOf("--directory");

        if (jarArgIndex == -1 || directoryArgIndex == -1) {
            printUsage();
        } else {
            try {
                File jar = new File(args.get(jarArgIndex + 1));

                StringBuilder directory = new StringBuilder(args.get(directoryArgIndex + 1).replace("/", File.separator));

                if (directory.toString().startsWith("\"")) {
                    int index = directoryArgIndex + 1;

                    do {
                        directory.append(args.get(index).replace("/", File.pathSeparator));
                        index++;
                    } while (!directory.toString().endsWith("\""));
                }

                File multiMcDirectory = new File(directory.toString());

                String name = jar.getName();

                if (!(name.endsWith(".jar") || name.endsWith(".zip"))) {
                    throw new IndexOutOfBoundsException();
                }

                if (!multiMcDirectory.isDirectory()) {
                    throw new IndexOutOfBoundsException();
                }

                File location = Installers.getInstaller(jar).install(multiMcDirectory, jar);
                System.out.println("Successfully installed Forge to " + location.getAbsolutePath() + "! Make sure to restart MultiMC if it was already opened");
            } catch (Exception e) {
                if (!(e instanceof IndexOutOfBoundsException)) {
                    e.printStackTrace();
                }
                printUsage();
            }
        }
    }

    private static void printUsage() {
        System.err.println("Incorrect usage of multimc4forge!");
        System.err.println("Example:");
        System.err.println("java -jar multimc4forge.jar --jar /path/to/forge/installer.jar --directory /path/to/multimc/directory");
        System.exit(1); // Failed
    }
}
