package sheba.backend.app.BL;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.entities.ObjectImage;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.repositories.ObjectLocationRepository;
import sheba.backend.app.repositories.LocationRepository;
import sheba.backend.app.repositories.ObjectImageRepository;
import sheba.backend.app.util.StoragePath;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObjectLocationBL {
    private final ObjectLocationRepository locationObjectRepository;
    private final LocationRepository locationRepository;
    private final ObjectImageRepository objectImageRepository;
    private final GcsBL gcsBL;

    public ObjectLocationBL(ObjectLocationRepository locationObjectRepository,
                            LocationRepository locationRepository,
                            ObjectImageRepository objectImageRepository,
                            GcsBL gcsBL) {
        this.locationObjectRepository = locationObjectRepository;
        this.locationRepository = locationRepository;
        this.objectImageRepository = objectImageRepository;
        this.gcsBL = gcsBL;
    }

    public ObjectLocation createLocationObject(Long locationId, ObjectLocation locationObject, List<MultipartFile> images) throws IOException {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + locationId));
        locationObject.setLocation(location);
        ObjectLocation savedObject = locationObjectRepository.save(locationObject);

        if (images != null && !images.isEmpty()) {
            List<ObjectImage> objectImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String folderPath = StoragePath.OBJECTS_IMAGES_PATH + "/" + savedObject.getName();
                        System.out.println("Folder name: "+ folderPath);
                        String objectName = gcsBL.bucketUpload(image, folderPath);
                        String publicUrl = gcsBL.getPublicUrl(objectName);

                        ObjectImage objectImage = new ObjectImage();
                        objectImage.setName(image.getOriginalFilename());
                        objectImage.setImagePath(publicUrl);
                        objectImage.setObject(savedObject);
                        objectImages.add(objectImageRepository.save(objectImage));
                    } catch (IOException e) {
                        throw new MediaUploadFailed("Failed to upload image for object: " + savedObject.getObjectID(), e);
                    }
                }
            }
            savedObject.setObjectImages(objectImages);
        }
        return savedObject;
    }
}