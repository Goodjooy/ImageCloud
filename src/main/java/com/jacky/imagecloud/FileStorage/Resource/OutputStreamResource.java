package com.jacky.imagecloud.FileStorage.Resource;

import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

public class OutputStreamResource extends ByteArrayResource {
    private final String filename;

    public OutputStreamResource(ByteArrayOutputStream stream, Path filePath) {
        super(stream.toByteArray());
        filename = filePath.toFile().getName();
    }

    /**
     * Return a description for this resource,
     * to be used for error output when working with the resource.
     * <p>Implementations are also encouraged to return this value
     * from their {@code toString} method.
     *
     * @see Object#toString()
     */
    @Override
    public String getDescription() {
        return "User head Image Generator Resource";
    }

    @Override
    public String getFilename() {
        return filename;
    }
}
