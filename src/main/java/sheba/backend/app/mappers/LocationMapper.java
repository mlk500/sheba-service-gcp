package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.LocationDTO;
import sheba.backend.app.entities.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO locationToLocationDTO(Location location);
}
