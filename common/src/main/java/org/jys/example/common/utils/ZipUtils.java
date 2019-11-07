package org.jys.example.common.utils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author jiangyueosng
 * @date 2019/1/12
 * @description <p> </p>
 */
public class ZipUtils {

    private static final Logger logger = LoggerFactory.getLogger(ZipUtils.class);

    public static byte[] unzip(byte[] data) {
        byte[] b = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            try (ZipInputStream zip = new ZipInputStream(bis)) {
                while (zip.getNextEntry() != null) {
                    byte[] buffer = new byte[1024];
                    int num;
                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        while ((num = zip.read(buffer, 0, buffer.length)) != -1) {
                            outputStream.write(buffer, 0, num);
                        }
                        b = outputStream.toByteArray();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("error when unzip data");
            logger.error(e.getMessage(), e);
        }

        return b;
    }

    public static byte[] zip(byte[] data) {
        byte[] b = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                ZipEntry entry = new ZipEntry("zip");
                entry.setSize(data.length);
                zip.putNextEntry(entry);
                zip.write(data);
                zip.closeEntry();
                b = bos.toByteArray();
            }
        } catch (IOException e) {
            logger.error("error when zip data");
            logger.error(e.getMessage(), e);
        }
        return b;
    }

    /**
     * unzip file
     *
     * @param file     file
     * @param savePath unzip save folder
     */
    public static void unip(File file, String savePath) throws IOException {
        File saveFolder = Paths.get(savePath).toFile();
        if (!saveFolder.exists()) {
            boolean createSuccess = saveFolder.mkdirs();
            if (!createSuccess) {
                throw new IOException();
            }
        }

        try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(FileUtils.openInputStream(file), "UTF-8", false, true)) {
            ArchiveEntry archiveEntry = zipInputStream.getNextEntry();
            while (archiveEntry != null) {
                String entryFileName = archiveEntry.getName();
                File entryFile = Paths.get(savePath, entryFileName).toFile();
                if (archiveEntry.isDirectory()) {
                    System.out.println(entryFile.exists());
                    boolean createSuccess = entryFile.exists() || entryFile.mkdirs();
                    if (!createSuccess) {
                        throw new IOException();
                    }
                } else {
                    try (BufferedOutputStream stream = new BufferedOutputStream(FileUtils.openOutputStream(entryFile))) {
                        IOUtils.copy(zipInputStream, stream);
                    }
                }
                archiveEntry = zipInputStream.getNextEntry();
            }
        }
    }
}