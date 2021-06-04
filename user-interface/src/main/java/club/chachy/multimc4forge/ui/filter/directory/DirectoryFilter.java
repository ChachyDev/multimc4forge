package club.chachy.multimc4forge.ui.filter.directory;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class DirectoryFilter extends FileFilter {
    @Override
    public boolean accept(File f) {
        // This gets overwritten by UI#L40
        return true;
    }

    @Override
    public String getDescription() {
        return "Folder";
    }
}
