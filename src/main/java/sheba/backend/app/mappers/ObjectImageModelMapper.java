package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sheba.backend.app.DTO.ObjectImageModelDTO;
import sheba.backend.app.entities.ObjectImage;
import sheba.backend.app.entities.ObjectLocation;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ObjectImageModelMapper {
    @Mapping(target = "locationID", source = "location.locationID")
    @Mapping(target = "objectImgsUrls", source = "objectImages", qualifiedByName = "imagesToUrls")
    ObjectImageModelDTO objectImgToObjectImgDTO(ObjectLocation objectLocation);

    @Named("imagesToUrls")
    default List<String> imagesToUrls(List<ObjectImage> objectImages) {
        if (objectImages == null) {
            return null;
        }
        return objectImages.stream()
                .map(ObjectImage::getImageUrl)
                .collect(Collectors.toList());
    }

}
