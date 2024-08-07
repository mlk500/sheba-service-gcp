package sheba.backend.app.mappers;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.ObjectImageModelDTO;
import sheba.backend.app.entities.Location;
import sheba.backend.app.entities.ObjectLocation;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-08-07T13:42:40+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class ObjectImageModelMapperImpl implements ObjectImageModelMapper {

    @Override
    public ObjectImageModelDTO objectImgToObjectImgDTO(ObjectLocation objectLocation) {
        if ( objectLocation == null ) {
            return null;
        }

        ObjectImageModelDTO objectImageModelDTO = new ObjectImageModelDTO();

        objectImageModelDTO.setLocationID( objectLocationLocationLocationID( objectLocation ) );
        objectImageModelDTO.setObjectImgsUrls( imagesToUrls( objectLocation.getObjectImages() ) );
        objectImageModelDTO.setObjectID( objectLocation.getObjectID() );
        objectImageModelDTO.setName( objectLocation.getName() );

        return objectImageModelDTO;
    }

    private long objectLocationLocationLocationID(ObjectLocation objectLocation) {
        if ( objectLocation == null ) {
            return 0L;
        }
        Location location = objectLocation.getLocation();
        if ( location == null ) {
            return 0L;
        }
        long locationID = location.getLocationID();
        return locationID;
    }
}
