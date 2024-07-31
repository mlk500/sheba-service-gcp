package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.UnitDTO;
import sheba.backend.app.entities.Unit;

@Mapper(componentModel = "spring", uses = {TaskMapper.class, LocationMapper.class, ObjectLocationMapper.class})
public interface UnitMapper {
    @Mapping(target = "taskDTO", source = "task")
    @Mapping(target = "locationDTO", source = "location")
    @Mapping(target = "objectDTO", source = "object")
    UnitDTO unitToUnitDTO(Unit unit);

}
