package io.github.etases.edublock.rs.internal.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@UtilityClass
public class FileUtil {
    public static boolean createFile(File file) throws IOException {
        if (file.exists()) {
            return true;
        }
        return Optional.ofNullable(file.getParentFile()).map(dir -> dir.exists() || dir.mkdirs()).orElse(true)
                && file.createNewFile();
    }
}
