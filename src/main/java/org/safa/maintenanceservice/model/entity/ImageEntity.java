package org.safa.maintenanceservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.safa.maintenanceservice.model.ImageType;
import java.util.UUID;

@Entity
@Table(name = "image")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false, name = "fileName")
    private String filePath;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;
    /**
     * check this field manually every updating,inserting and deleting process of user as well as image*/
    private long ownerId;

}