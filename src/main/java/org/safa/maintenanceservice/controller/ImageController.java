package org.safa.maintenanceservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.safa.maintenanceservice.model.ImageType;
import org.safa.maintenanceservice.model.dto.ApiResponse;
import org.safa.maintenanceservice.models.dto.image.ImageByteResponse;
import org.safa.maintenanceservice.model.exceptions.NotFoundException;
import org.safa.maintenanceservice.service.image.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/v1/image")
@Slf4j
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    /**
     * When on the parts of setting a profile image it is optional to set one.
     * <pre>
     *  (------------)
     *  (    /\   () )
     *  (   /  \/\   )
     *  (  /    \ \  )
     *  (____________)
     * </pre>
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> saveImage(@RequestPart MultipartFile file, @RequestParam long ownerId, @RequestParam ImageType imageType, HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.CREATED.value())
                            .data(imageService.save(file, imageType, ownerId, accessToken))
                            .message(null)
                            .build());
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .message(e.getMessage())
                            .build());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data(null)
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID imageId) {
        try {
            ImageByteResponse imageAsByteArray = imageService.getImageAsByteArray(imageId);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType(imageAsByteArray.contentType()))
                    .body(imageAsByteArray.data());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA).body(null);
        }
    }

    @GetMapping("/data/{imageId}")
    public ResponseEntity<ApiResponse<?>> getImageData(@PathVariable UUID imageId) {
        try {
            var data = imageService.getImageResponse(imageId);
            log.debug(data.toString());
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .data(data)
                            .build());
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build());
        }
    }
    @PutMapping
    public ResponseEntity<ApiResponse<?>> updateImage(@RequestPart MultipartFile file, @RequestParam UUID imageId, @RequestParam long ownerId, HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.ACCEPTED.value())
                            .data(imageService.updateImage(file, imageId, ownerId, accessToken))
                            .build());
        }catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.MULTIPART_FORM_DATA).body(ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .build());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(ApiResponse.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .build());
        }
    }
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable UUID imageId) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.OK.value())
                            .data(imageService.deleteImage(imageId))
                            .build());
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ApiResponse.builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .build());
        }
    }
}
