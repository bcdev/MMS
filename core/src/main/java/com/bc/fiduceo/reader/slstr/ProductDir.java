package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.util.io.FileUtils;

import java.io.File;
import java.io.IOException;

abstract class ProductDir {

    static ProductDir create(File path, ReaderContext context) throws IOException {
        if (ReaderUtils.isCompressed(path)) {
            return new ZipFile(path, context);
        } else if (path.isFile()) {
            return new FileSystem(path);
        }

        throw new IllegalStateException("Path is neither file nor zipped: " + path);
    }

    abstract void close();
    abstract File getFile(String fileName);

    static class FileSystem extends ProductDir {

        FileSystem(File path) {
            throw new IllegalStateException("not implemented");
        }

        @Override
        void close() {
            throw new IllegalStateException("not implemented");
        }

        @Override
        File getFile(String fileName) {
            throw new IllegalStateException("not implemented");
        }
    }

    static class ZipFile extends ProductDir {

        private final File extractionDir;
        private final ReaderContext context;

        ZipFile(File path, ReaderContext context) throws IOException {
            this.context = context;
            final String fileName = FileUtils.getFilenameWithoutExtension(path);
            final long millis = System.currentTimeMillis();
            extractionDir = context.createDirInTempDir(fileName + millis);

        }

        @Override
        void close() {
            context.deleteTempFile(extractionDir);
        }

        @Override
        File getFile(String fileName) {
            throw new IllegalStateException("not implemented");
        }
    }
}
