package com.jacky.imagecloud.FileStorage.FileService;

import com.jacky.imagecloud.data.LoggerHandle;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;

public interface FileUploader<T> {

    void init();

    T storage(MultipartFile file);

    Stream<Path> loadAll();

    Path load(String filePath);

    Resource loadAsResource(String filePath);

    void deleteAll();

    long delete(String fileName);

    static String formatSize(long bit){
        return formatSize(bit,0.9f,2);
    }

    /**
     * @param bit 要供转换的字节长度
     * @param edge 转换为下一单位阈值
     *             例如：edge=0.9
     *             bit=922时，将会转换为0.90 KiB 而不是922 Bit
     * @param varLevel 格式化字符串保留小数点
     * @return 格式化完成的文件大小
     */
    static String formatSize(long bit, float edge, int varLevel) {
        var formatString = "%." + varLevel + "f";
        var resultFormat = "%s %s";

        Function<Double, String> formatWorker = (f) -> String.format(formatString, f);

        var b2kb = bit / 1024.0;
        var kb2mb = b2kb / 1024.0;
        var mb2gb = kb2mb / 1024.0;
        var gb2tb = mb2gb / 1024.0;

        String formatValue;
        String formatSize;

        if (b2kb <= edge) {
            formatSize = "Bit";
            formatValue = formatWorker.apply((double) (bit));
        } else if (b2kb > edge && edge >= kb2mb) {
            formatSize = "KiB";
            formatValue = formatWorker.apply(b2kb);
        } else if (kb2mb > edge && edge >= mb2gb) {
            formatSize = "MiB";
            formatValue = formatWorker.apply(kb2mb);
        } else if (mb2gb > edge && edge >= gb2tb) {
            formatSize = "GiB";
            formatValue = formatWorker.apply(mb2gb);
        } else {
            formatSize = "TiB";
            formatValue = formatWorker.apply(gb2tb);
        }
        return String.format(resultFormat,formatValue,formatSize);
    }

}
