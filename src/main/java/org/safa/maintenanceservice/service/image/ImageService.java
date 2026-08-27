package org.safa.maintenanceservice.service.image;

import lombok.RequiredArgsConstructor;
import org.safa.maintenanceservice.feignClient.UserFeignClient;
import org.safa.maintenanceservice.model.dto.ApiResponse;
import org.safa.maintenanceservice.model.dto.UserResponse;
import org.safa.maintenanceservice.models.dto.image.ImageByteResponse;
import org.safa.maintenanceservice.models.dto.image.ImageResponse;
import org.safa.maintenanceservice.model.entity.ImageEntity;
import org.safa.maintenanceservice.model.exceptions.NotFoundException;
import org.safa.maintenanceservice.model.ImageType;
import org.safa.maintenanceservice.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final UserFeignClient userFeignClient;

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
        ImageEntity save = imageRepository.save(new ImageEntity(imageId, file.getBytes(), file.getContentType(), file.getOriginalFilename(), imageType, ownerId));
        return new ImageResponse(save.getId(), save.getContentType(), save.getFileName(), save.getOwnerId(), save.getImageType());
    }

    public ImageByteResponse getImageAsByteArray(UUID id) {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isPresent()) {
            var image = imageEntity.get();
            return new ImageByteResponse(image.getData(), image.getContentType());
        }
        throw new NotFoundException("Image not found");
    }

    public ImageResponse getImageResponse(UUID id) {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isPresent()) {
            var image = imageEntity.get();
            return new ImageResponse(image.getId(), image.getContentType(), image.getFileName(), image.getOwnerId(), image.getImageType());
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
        image.setData(file.getBytes());
        image.setContentType(file.getContentType());
        image.setFileName(file.getOriginalFilename());
        ImageEntity save = imageRepository.save(image);
        return new ImageResponse(save.getId(), save.getContentType(), save.getFileName(), image.getOwnerId(), image.getImageType());
    }

    public boolean deleteImage(UUID id) {
        Optional<ImageEntity> imageEntity = imageRepository.findById(id);
        if (imageEntity.isEmpty()) {
            throw new NotFoundException("Image not found");
        }
        imageRepository.delete(imageEntity.get());
        return true;
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
}