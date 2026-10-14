package org.safa.maintenanceservice.fileSystem;

import java.io.IOException;

public interface ImageFileSystemService {
    String STORAGE_PATH = System.getProperty("user.home")+"/images";
    boolean saveImage(byte[] fileInBytes, String path) throws IOException;
    boolean deleteImage(String path);
    byte[] getImage(String path);
    boolean exists(String path);
}
