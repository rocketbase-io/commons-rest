package io.rocketbase.commons.openapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Writes TypeScript client files directly to the filesystem.
 * This replaces the ZIP-based approach with direct file system writes.
 */
@Slf4j
@RequiredArgsConstructor
public class FileSystemClientWriter {

    private final Path outputDirectory;

    /**
     * Writes a file to the output directory.
     *
     * @param relativePath The relative path within the output directory (e.g., "src/clients/activity-api.ts")
     * @param content The file content
     */
    public void writeFile(String relativePath, String content) {
        Path targetFile = outputDirectory.resolve(relativePath);

        try {
            // Create parent directories if they don't exist
            Files.createDirectories(targetFile.getParent());

            // Write content to file
            Files.writeString(targetFile, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

            log.debug("Written file: {}", relativePath);
        } catch (IOException e) {
            log.error("Failed to write file: {}", relativePath, e);
            throw new RuntimeException("Failed to write file: " + relativePath, e);
        }
    }

    /**
     * Writes multiple files from a map of relative paths to content.
     *
     * @param files Map of relative path to content
     */
    public void writeFiles(java.util.Map<String, String> files) {
        files.forEach(this::writeFile);
        log.info("Written {} files to {}", files.size(), outputDirectory);
    }

    /**
     * Creates a directory if it doesn't exist.
     *
     * @param relativePath The relative path within the output directory
     */
    public void createDirectory(String relativePath) {
        Path targetDir = outputDirectory.resolve(relativePath);

        try {
            Files.createDirectories(targetDir);
            log.debug("Created directory: {}", relativePath);
        } catch (IOException e) {
            log.error("Failed to create directory: {}", relativePath, e);
            throw new RuntimeException("Failed to create directory: " + relativePath, e);
        }
    }

    /**
     * Cleans the output directory by deleting all existing files.
     */
    public void cleanOutputDirectory() {
        try {
            if (Files.exists(outputDirectory)) {
                log.info("Cleaning output directory: {}", outputDirectory);
                deleteRecursively(outputDirectory);
            }
            Files.createDirectories(outputDirectory);
            log.info("Output directory ready: {}", outputDirectory);
        } catch (IOException e) {
            log.error("Failed to clean output directory: {}", outputDirectory, e);
            throw new RuntimeException("Failed to clean output directory", e);
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     */
    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                stream.forEach(child -> {
                    try {
                        deleteRecursively(child);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        Files.deleteIfExists(path);
    }

    /**
     * Gets the absolute path of the output directory.
     */
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Gets the absolute path for a relative file path.
     */
    public Path getAbsolutePath(String relativePath) {
        return outputDirectory.resolve(relativePath);
    }

    /**
     * Checks if a file exists.
     */
    public boolean fileExists(String relativePath) {
        return Files.exists(outputDirectory.resolve(relativePath));
    }
}
