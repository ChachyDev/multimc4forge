package club.chachy.multimc4forge;

import club.chachy.multimc4forge.cli.CLI;
import club.chachy.multimc4forge.ui.UI;

import java.awt.*;

public class Main {
    public static void main(String[] args) throws Throwable {
        if (GraphicsEnvironment.isHeadless() || !GraphicsEnvironment.isHeadless() && args.length > 0) {
            CLI.main(args);
        } else {
            UI.main(new String[0]);
        }
    }
}
