package cli.command.files;

import java.io.File;

public class FileData {
     private File file;
     private boolean priv;

    public FileData(File file, boolean priv) {
        this.file = file;
        this.priv = priv;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isPriv() {
        return priv;
    }

    public void setPriv(boolean priv) {
        this.priv = priv;
    }

    @Override
    public String toString() {
        return file.toString() + " [" + isPriv() + "]";
    }
}
