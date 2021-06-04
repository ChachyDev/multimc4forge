package club.chachy.multimc4forge.ui.filter.archive;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class JarFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".jar");
    }

    @Override
    public String getDescription() {
        return "Jar files (.jar)";
    }
}
