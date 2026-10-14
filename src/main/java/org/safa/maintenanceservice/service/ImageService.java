package org.safa.maintenanceservice.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.safa.maintenanceservice.feignClient.UserFeignClient;
import org.safa.maintenanceservice.fileSystem.ImageFileSystemService;
import org.safa.maintenanceservice.model.dto.ApiResponse;
import org.safa.maintenanceservice.model.dto.UserResponse;
import org.safa.maintenanceservice.model.dto.ImageByteResponse;
import org.safa.maintenanceservice.model.dto.ImageResponse;
import org.safa.maintenanceservice.model.entity.ImageEntity;
import org.safa.maintenanceservice.model.exceptions.NotFoundException;
import org.safa.maintenanceservice.model.ImageType;
import org.safa.maintenanceservice.repository.ImageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserFeignClient userFeignClient;
    private final ImageFileSystemService imageFileSystemService;

    public ImageResponse save(MultipartFile file, ImageType imageType, long ownerId,@Nullable String accessToken) throws IOException {
        if (notExistsByOwnerId(ownerId, accessToken)){
            throw new NotFoundException("Owner id not found");
        }
        UUID imageId;
        if (imageType == ImageType.PROFILE_PICTURE){
            imageId = imageRepository.findIdByOwnerIdAndImageType(ownerId, imageType).orElse(UUID.randomUUID());
        }else {
            imageId = UUID.randomUUID();
        }
        var imagePath = pathGenerator();
        if (imageFileSystemService.saveImage(file.getBytes(), imagePath)){
            ImageEntity save = imageRepository.save(new ImageEntity(imageId, file.getContentType(), imagePath, imageType, ownerId));
            return new ImageResponse(save.getId(), save.getContentType(), save.getOwnerId(), save.getImageType());
        }
        return null;
    }

    public ImageByteResponse getImageAsByteArray(UUID id) {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isPresent()) {
            var image = imageEntity.get();
            var imagePath = image.getFilePath();
            if (imageFileSystemService.exists(imagePath)){
                return new ImageByteResponse(imageFileSystemService.getImage(imagePath), image.getContentType());
            }
        }
        throw new NotFoundException("Image not found");
    }

    public ImageResponse getImageResponse(UUID id) {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isPresent()) {
            var image = imageEntity.get();
            return new ImageResponse(image.getId(), image.getContentType(), image.getOwnerId(), image.getImageType());
        }
        throw new NotFoundException("Image not found");
    }

    public ImageResponse updateImage(MultipartFile file, UUID id, long ownerId, @Nullable String accessToken) throws IOException {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isEmpty()) {
            throw new NotFoundException("Image not found");
        }
        if (notExistsByOwnerId(ownerId, accessToken)){
            throw new NotFoundException("Owner id not found");
        }
        var image = imageEntity.get();
        imageFileSystemService.saveImage(file.getBytes(), image.getFilePath());
        image.setContentType(file.getContentType());
        ImageEntity save = imageRepository.save(image);
        return new ImageResponse(save.getId(), save.getContentType(), image.getOwnerId(), image.getImageType());
    }

    public boolean deleteImage(UUID id) {
        var result = false;
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isEmpty()) {
            throw new NotFoundException("Image not found");
        }
        if (imageFileSystemService.deleteImage(imageEntity.get().getFilePath())) {
            imageRepository.delete(imageEntity.get());
            result = true;
        }
        return result;
    }
    private boolean notExistsByOwnerId(long ownerId, String accessToken) {
        //check if ownerId not exists
//        return userRepository.notExistsById(ownerId);
        if (accessToken != null){
            ResponseEntity<ApiResponse<UserResponse>> currentUser = userFeignClient.getCurrentUser(accessToken);
            UserResponse data = currentUser.getBody() != null ? currentUser.getBody().getData() : null;
            if (data == null) {
                return true;
            }
            return ownerId!=data.id();
        }
        return false;
    }
    private @NonNull String pathGenerator(){
        var filename = String.valueOf(System.currentTimeMillis());
        return ImageFileSystemService.STORAGE_PATH+ File.separator +filename;
    }
}