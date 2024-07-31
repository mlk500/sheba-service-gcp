package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.ObjectLocationDTO;
import sheba.backend.app.entities.ObjectLocation;

@Mapper(componentModel = "spring")
public interface ObjectLocationMapper {
//    @Mapping(source = "objectID", target = "objectID")
//    @Mapping(source = "name", target = "name")
    ObjectLocationDTO objectToObjectDTO(ObjectLocation objectLocation);
    Object objectDTOToObject(ObjectLocationDTO objectDTO);
}
