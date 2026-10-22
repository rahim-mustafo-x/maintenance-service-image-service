package org.safa.maintenanceservice.fileSystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ImageFileSystemServiceImpl implements ImageFileSystemService {

    public ImageFileSystemServiceImpl() {
        var file = new File(STORAGE_PATH);
        if (!file.exists()){
            //this line is the result of watching Rust code
            var _ = file.mkdirs();
        }
    }

    @Override
    public boolean saveImage(byte[] fileInBytes, String path) {
        try {
            Path target = Paths.get(path);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);   // creates the folder if missing
            }
            Files.write(target, fileInBytes);      // creates or overwrites, closes the stream
            return true;
        } catch (IOException e) {
            log.error("Failed to save image to {}", path, e);   // never swallow silently
            return false;
        }
    }
    @Override
    public boolean deleteImage(String path) {
        var result = false;
        var file = new File(path);
        if (file.exists()){
            var _ = file.delete();
            result = true;
        }
        return result;
    }

    @Override
    public byte[] getImage(String path) {
        var result = new byte[0];
        var file = new File(path);
        if (file.exists()){
            try {
                var fileInputStream = new FileInputStream(file);
                result = fileInputStream.readAllBytes();
                fileInputStream.close();
            } catch (IOException _) {}
        }
        return result;
    }

    @Override
    public boolean exists(String path) {
        var file = new File(path);
        return file.exists();
    }
}
