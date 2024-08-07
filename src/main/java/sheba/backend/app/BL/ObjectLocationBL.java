package sheba.backend.app.BL;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.ObjectLocation;
import sheba.backend.app.entities.ObjectImage;
import sheba.backend.app.exceptions.ObjectIsPartOfUnit;
import sheba.backend.app.exceptions.ObjectNameMustBeUnique;
import sheba.backend.app.repositories.ObjectLocationRepository;
import sheba.backend.app.repositories.LocationRepository;
import sheba.backend.app.repositories.ObjectImageRepository;
import sheba.backend.app.repositories.UnitRepository;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
public class ObjectLocationBL {
    private final ObjectLocationRepository locationObjectRepository;
    private final LocationRepository locationRepository;
    private final ObjectImageBL objectImageBL;
    private final UnitRepository unitRepository;


    public ObjectLocationBL(ObjectLocationRepository locationObjectRepository,
                            LocationRepository locationRepository
            , ObjectImageBL objectImageBL, UnitRepository unitRepository) {
        this.locationObjectRepository = locationObjectRepository;
        this.locationRepository = locationRepository;
        this.objectImageBL = objectImageBL;
        this.unitRepository = unitRepository;
    }

    public ObjectLocation createLocationObject(Long locationId, ObjectLocation locationObject, List<MultipartFile> images) throws IOException, ObjectNameMustBeUnique {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found with ID: " + locationId));
        if(!isNameUnique(locationObject.getName())){
            throw new ObjectNameMustBeUnique("Object Name Must Be Unique");
        }
        locationObject.setLocation(location);
        ObjectLocation savedObject = locationObjectRepository.save(locationObject);

        if (images != null && !images.isEmpty()) {
            List<ObjectImage> objectImages = objectImageBL.addObjectImage(savedObject.getObjectID(), images);
            savedObject.setObjectImages(objectImages);
        }
        return savedObject;
    }

    public void deleteObject(ObjectLocation objectLocation) throws Exception {
        if(objectLocation != null){
            if (!objectLocation.getObjectImages().isEmpty()){
                for(ObjectImage img : objectLocation.getObjectImages()){
                    try{
                        objectImageBL.deleteObjectImage(img);
                    }
                    catch (Exception e){
                        throw new Exception("Error deleting images for object");
                    }
                }
            }
        }
    }

    public void deleteObject(long objectID) throws Exception {
        ObjectLocation objectLocation = locationObjectRepository.findById(objectID).orElseThrow(() ->
                new EntityNotFoundException("Object was not found with ID: " + objectID));
        if(objectLocation != null){
            if (isPartOfAGame(objectLocation)){
                throw new ObjectIsPartOfUnit("Object is part of a game");
            }
            if (!objectLocation.getObjectImages().isEmpty()){
                for(ObjectImage img : objectLocation.getObjectImages()){
                    try{
                        objectImageBL.deleteObjectImage(img);
                    }
                    catch (Exception e){
                        throw new Exception("Error deleting images for object");
                    }
                }
            }
        }
    }

    private boolean isNameUnique(String name){
        ObjectLocation foundObject = locationObjectRepository.findObjectLocationByName(name);
        return foundObject == null;
    }

    private boolean isPartOfAGame(ObjectLocation checkObject){
        return unitRepository.findByObject(checkObject) != null && !unitRepository.findByObject(checkObject).isEmpty();
    }

    public List<ObjectLocation> getAllObjects(){
        List<ObjectLocation> objects = locationObjectRepository.findAll();
        for(ObjectLocation obj : objects){
            if(obj.getObjectImages() == null || obj.getObjectImages().isEmpty()){
                objects.remove(obj);
            }
        }
        return objects;
    }




}