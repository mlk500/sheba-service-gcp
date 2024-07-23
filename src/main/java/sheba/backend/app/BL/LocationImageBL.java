package sheba.backend.app.BL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.LocationImage;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.repositories.LocationImageRepository;
import sheba.backend.app.util.StoragePath;

import java.io.IOException;
import java.util.Optional;

@Service
public class LocationImageBL {
    private final LocationImageRepository locationImageRepository;
    private final GcsBL gcsBL;
    private static final Logger logger = LoggerFactory.getLogger(LocationImageBL.class);


    public LocationImageBL(LocationImageRepository locationImageRepository, GcsBL gcsBL) {
        this.locationImageRepository = locationImageRepository;
        this.gcsBL = gcsBL;
    }

    public LocationImage uploadImageToGCS(MultipartFile file, Location location) throws IOException {
        try {
            String folderName = StoragePath.LOCATION_IMAGE_PATH;
            String objectName = gcsBL.bucketUpload(file, folderName);
            String publicUrl = gcsBL.getPublicUrl(objectName);
            LocationImage locationImage = new LocationImage();
            locationImage.setName(file.getOriginalFilename());
            locationImage.setType(file.getContentType());
            locationImage.setGcsObjectName(objectName);
            locationImage.setPublicUrl(publicUrl);
            locationImage.setLocation(location);

            LocationImage savedImage = locationImageRepository.save(locationImage);
            System.out.println("LocationImageBL: LocationImage saved to database. ID: " + savedImage.getLocationImgID());
            return savedImage;
        } catch (Exception e) {
            System.out.println("LocationImageBL: Failed to upload image to GCS: " + e.getMessage());
            e.printStackTrace();
            throw new MediaUploadFailed("Failed to upload image to GCS", e);
        }
    }

    public void deleteImageFromGCS(LocationImage locationImage) {
        try {
            gcsBL.bucketDelete(locationImage.getGcsObjectName());
            locationImageRepository.delete(locationImage);
        } catch (Exception e) {
            throw new MediaUploadFailed("Failed to delete image from GCS", e);
        }
    }
}