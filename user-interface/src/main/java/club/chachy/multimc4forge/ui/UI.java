package club.chachy.multimc4forge.ui;

import club.chachy.multimc4forge.installer.Installers;
import club.chachy.multimc4forge.ui.filter.archive.JarFilter;
import club.chachy.multimc4forge.ui.filter.archive.ZipFilter;
import club.chachy.multimc4forge.ui.filter.directory.DirectoryFilter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class UI {
    public static void main(String[] args) throws Throwable {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        File forgeJar = getFile(new JFileChooser(), "Please select a Forge INSTALLER jar", "You MUST provide a Forge Installer jar...", new JarFilter(), new ZipFilter());

        if (forgeJar != null) {
            File multiMcDirectory = getFile(new JFileChooser(), "Please provide your Multi", "You MUST provide a MultiMC install directory", new DirectoryFilter());

            if (multiMcDirectory != null) {
                File location = Installers.getInstaller(forgeJar).install(multiMcDirectory, forgeJar);
                JOptionPane.showMessageDialog(null, "Successfully installed Forge to " + location.getAbsolutePath() + "! Make sure to restart MultiMC if it was already opened", "Success! :)", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    private static File getFile(JFileChooser chooser, String title, String errorMessage, FileFilter... fileFilters) {
        if (fileFilters.length > 0) {
            chooser.setFileFilter(fileFilters[0]);
            for (FileFilter filter : fileFilters) {
                chooser.addChoosableFileFilter(filter);
            }

        }

        if (fileFilters.length == 1 && fileFilters[0] instanceof DirectoryFilter) {
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new DirectoryFilter());
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        chooser.setDialogTitle(title);
        chooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Downloads"));
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            JOptionPane.showMessageDialog(null, errorMessage, "Something went wrong :(", JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }
}
