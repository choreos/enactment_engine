package org.ow2.choreos.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

public class FileLoader {

    private static Logger logger = Logger.getLogger(FileLoader.class);

    private String fileName;

    public FileLoader(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() throws IOException {
        if (isAbsolutPath())
            return getFileWithAbsolutPath();
        else
            return getFileFromClassPath();
    }

    private boolean isAbsolutPath() {
        if (isUnixAbsolutPath() || isWIndowsAbsolutPath())
            return true;
        else
            return false;
    }

    private boolean isUnixAbsolutPath() {
        return fileName.startsWith("/");
    }

    private boolean isWIndowsAbsolutPath() {
        final String regex = "[A-Z]:\\\\.*";
        return fileName.matches(regex);
    }

    private File getFileWithAbsolutPath() throws IOException {
        File file = new File(fileName);
        if (!file.exists())
            fail();
        return file;
    }

    private File getFileFromClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource(fileName);
        if (url == null)
            fail();
        File file = new File(url.getFile());
        return file;
    }

    private void fail() throws IOException {
        String msg = fileName + " does not exist.";
        logger.error(msg);
        throw new IOException(msg);
    }

}
