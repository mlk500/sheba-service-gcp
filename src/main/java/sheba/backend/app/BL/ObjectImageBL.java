package sheba.backend.app.BL;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.entities.ObjectImage;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.repositories.ObjectLocationRepository;
import sheba.backend.app.repositories.ObjectImageRepository;
import sheba.backend.app.util.StoragePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObjectImageBL {
    private final ObjectImageRepository objectImageRepository;
    private final ObjectLocationRepository locationObjectRepository;
    private final GcsBL gcsBL;

    public ObjectImageBL(ObjectImageRepository objectImageRepository,
                         ObjectLocationRepository locationObjectRepository,
                         GcsBL gcsBL) {
        this.objectImageRepository = objectImageRepository;
        this.locationObjectRepository = locationObjectRepository;
        this.gcsBL = gcsBL;
    }

    public List<ObjectImage> addObjectImage(Long objectId, List<MultipartFile> images) throws IOException {
        ObjectLocation object = locationObjectRepository.findById(objectId)
                .orElseThrow(() -> new EntityNotFoundException("LocationObject not found with ID: " + objectId));

        List<ObjectImage> savedImages = new ArrayList<>();
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }
            try {
                String folderPath = StoragePath.OBJECTS_IMAGES_PATH + "/" + object.getName();
                String gcsPath = gcsBL.bucketUpload(image, folderPath);
                String publicUrl = gcsBL.getPublicUrl(gcsPath);

                ObjectImage objectImage = new ObjectImage();
                objectImage.setName(image.getOriginalFilename());
//                System.out.println("image path "+ gcsPath);
//                System.out.println("image url "+ publicUrl);
                objectImage.setImagePath(gcsPath);
                objectImage.setImageUrl(publicUrl);
                objectImage.setObject(object);
                savedImages.add(objectImageRepository.save(objectImage));
            } catch (IOException e) {
                throw new MediaUploadFailed("Failed to upload image for object: " + object.getObjectID(), e);
            }
        }
        return savedImages;
    }

    public void deleteObjectImage(ObjectImage img){
        if(img != null && img.getImagePath() != null){
            System.out.println("in delete obj img " +  img.getImagePath() );
            gcsBL.bucketDelete(img.getImagePath());
            objectImageRepository.delete(img);
        }
    }

    //    public void deleteMedia(MediaTask mediaTask) throws IOException {
//        if (mediaTask != null && mediaTask.getMediaPath() != null) {
//            gcsBL.bucketDelete(mediaTask.getMediaPath());
//            mediaTaskRepository.delete(mediaTask);
//        }
//    }
}