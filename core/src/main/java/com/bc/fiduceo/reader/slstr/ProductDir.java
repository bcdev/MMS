package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

abstract class ProductDir {

    static ProductDir create(File path, ReaderContext context) throws IOException {
        if (ReaderUtils.isCompressed(path)) {
            return new ZipArchive(path, context);
        } else if (path.isFile()) {
            return new FileSystem(path);
        }

        throw new IllegalStateException("Path is neither file nor zipped: " + path);
    }

    abstract void close() throws IOException;

    abstract File getFile(String fileName) throws IOException;

    static class FileSystem extends ProductDir {

        private File productDir;

        FileSystem(File path) {
            if (path.isDirectory()) {
                productDir = path;
            } else if (path.isFile()) {
                productDir = path.getParentFile();
            } else {
                throw new IllegalArgumentException("Input is neither file nor directory: " + path);
            }
        }

        @Override
        void close() {
            // nothing to do here tb 2020-10-23
        }

        @Override
        File getFile(String fileName) {
            final File requestedFile = new File(productDir, fileName);
            if (requestedFile.isFile()) {
                return requestedFile;
            }

            throw new IllegalArgumentException("Requested file does not exist: " + fileName);
        }
    }

    static class ZipArchive extends ProductDir {

        private final File extractionDir;
        private final ReaderContext context;
        private ZipFile zipFile;
        private String dirName;

        ZipArchive(File path, ReaderContext context) throws IOException {
            this.context = context;
            final String fileName = FileUtils.getFilenameWithoutExtension(path);
            final long millis = System.currentTimeMillis();
            extractionDir = context.createDirInTempDir(fileName + millis);

            zipFile = new ZipFile(path);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            dirName = "";
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    dirName = entry.getName();
                    break;
                }
            }
        }

        @Override
        void close() throws IOException {
            if (zipFile != null) {
                zipFile.close();
                zipFile = null;
            }
            context.deleteTempFile(extractionDir);
        }

        @Override
        File getFile(String fileName) throws IOException {
            final String entryName = dirName + fileName;
            final ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Zip entry not found: " + entryName);
            }

            final File targetFile = new File(extractionDir, fileName);
            if (targetFile.isFile()) {
                return targetFile;
            }

            if (!targetFile.createNewFile()) {
                throw new IOException("Unable to create file: " + targetFile);
            }

            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                    final byte[] buffer = new byte[4096];
                    int read;
                    while ((read = inputStream.read(buffer)) > 0) {
                        bos.write(buffer, 0, read);
                    }
                }
            }

            return targetFile;
        }
    }
}
