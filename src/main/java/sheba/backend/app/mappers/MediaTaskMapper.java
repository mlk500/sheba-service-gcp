package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.MediaTaskDTO;
import sheba.backend.app.entities.MediaTask;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MediaTaskMapper {
    @Mapping(target = "mediaTaskID", source = "mediaTaskID")
    @Mapping(target = "mediaType", source = "mediaType")
    @Mapping(target = "mediaUrl", source = "mediaUrl")
    MediaTaskDTO mediaTaskToMediaTaskDTO(MediaTask mediaTask);

    MediaTask mediaTaskDTOToMediaTask(MediaTaskDTO mediaTaskDTO);
}
