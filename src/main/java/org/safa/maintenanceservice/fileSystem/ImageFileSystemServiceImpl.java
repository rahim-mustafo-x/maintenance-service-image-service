package org.safa.maintenanceservice.fileSystem;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
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
        var result = false;
        var file = new File(path);
        if (!file.exists()){
            try {
                var fileOutputStream = new FileOutputStream(file, false);
                fileOutputStream.write(fileInBytes);
                fileOutputStream.close();
                result = true;
            } catch (IOException _) {}
        }
        return result;
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
