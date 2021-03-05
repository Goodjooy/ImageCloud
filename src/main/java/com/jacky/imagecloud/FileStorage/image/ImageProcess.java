package com.jacky.imagecloud.FileStorage.image;

import at.dhyan.open_imaging.GifDecoder;
import com.jacky.imagecloud.err.file.FileFormatNotSupportException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class ImageProcess {

    public final static String[] supportFileFormat = ImageIO.getReaderFormatNames();


    public static boolean checkSupportFileFormat(String filename, boolean errThrow) {
        var fileFormat = filename.substring(filename.lastIndexOf(".") + 1);
        var support = List.of(supportFileFormat).contains(fileFormat);
        if (!support && errThrow)
            throw new FileFormatNotSupportException(String.format("file format name<%s> not support", fileFormat));
        return support;
    }

    public static String getFileFormat(Path filename) {
        return getFileFormat(filename.getFileName().toFile().getName());
    }

    public static String getFileFormat(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static BufferedImage transformImage(File file, int maxSize) throws IOException {
        checkSupportFileFormat(file.getName(), true);

        var rawImage = ImageIO.read(file);
        return transformImage(rawImage, maxSize);
    }

    public static BufferedImage transformImage(InputStream inputStream, String filename, int maxSize) throws IOException {
        checkSupportFileFormat(filename, true);

        var rawImage = ImageReader(inputStream, filename);
        return transformImage(rawImage, maxSize);
    }

    public static BufferedImage transformImage(BufferedImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        //图像压缩比计算
        float consult = (maxSize * 1.0f) / (Math.max(width, height));
        consult = consult - 1 > 0 ? 1 : consult;

        return transformImageFromBufferedImage(image, consult);
    }


    public static BufferedImage transformImage(InputStream inputStream, String filename, float scale) throws IOException {
        checkSupportFileFormat(filename, true);

        var RawImage = ImageReader(inputStream, filename);
        return transformImageFromBufferedImage(RawImage, scale);
    }

    public static BufferedImage transformImageFromMultiPartFile(MultipartFile file, float scale) throws IOException {
        return transformImage(file.getInputStream(), file.getOriginalFilename(), scale);
    }

    public static BufferedImage transformImageFromFile(File file, float scale) throws IOException {
        checkSupportFileFormat(file.getName(), true);
        return transformImageFromBufferedImage(ImageReader(file), scale);
    }

    public static BufferedImage transformImageFromBufferedImage(BufferedImage RawImage, float scale) {
        int width = RawImage.getWidth();
        int height = RawImage.getHeight();
        int flag = RawImage.getType();

        int TWidth = Math.round(width * scale);
        int THeight = Math.round(height * scale);
        return transformImageFromBufferedImage(RawImage, TWidth, THeight);
    }

    public static BufferedImage transformImageIntoSquareFromBufferedImage(BufferedImage RawImage, int size) {
        return transformImageFromBufferedImage(RawImage, size, size);
    }

    public static BufferedImage transformImageFromBufferedImage(BufferedImage RawImage, int width, int height) {
        var ThumbnailImageMid = RawImage.getScaledInstance(width, height,
                Image.SCALE_SMOOTH);
        var thumbnailImage = new BufferedImage(width, height, RawImage.getType());

        thumbnailImage.getGraphics().drawImage(ThumbnailImageMid, 0, 0, null);

        return thumbnailImage;
    }

    public static ByteArrayOutputStream BufferImageToOutputStream(BufferedImage image, String fileFormat) throws IOException {
        var Output = new ByteArrayOutputStream();
        ImageIO.write(image, fileFormat, Output);
        return Output;
    }

    public static boolean BufferImageToFile(BufferedImage image, String fileFormat, File file) throws IOException {
        return ImageIO.write(image, fileFormat, file);
    }

    public static BufferedImage ImageReader(InputStream stream, String fileName) throws IOException {
        BufferedImage img;
        if (fileName.toLowerCase().endsWith(".gif")) {
            var Gif = GifDecoder.read(stream);
            img = Gif.getFrame(0);
        } else {
            img = ImageIO.read(stream);
        }
        return img;
    }

    public static BufferedImage ImageReader(File file) throws IOException {
        return ImageReader(new FileInputStream(file), file.getName());
    }


}
