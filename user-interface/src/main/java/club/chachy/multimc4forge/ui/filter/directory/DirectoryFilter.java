package club.chachy.multimc4forge.ui.filter.directory;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class DirectoryFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        // Return true the configured setting will wipe out all other file types anyway...
        return true;
    }

    @Override
    public String getDescription() {
        return "Folder";
    }
}
