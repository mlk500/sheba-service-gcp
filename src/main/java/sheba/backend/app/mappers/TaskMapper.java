package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.TaskDTO;
import sheba.backend.app.entities.Task;

@Mapper(componentModel = "spring", uses = {QuestionTaskMapper.class, MediaTaskMapper.class})
public interface TaskMapper {

    @Mapping(source = "questionTask", target = "questionTask")
    @Mapping(source = "mediaList", target = "mediaList")
    TaskDTO taskToTaskDTO(Task task);

//    Task taskDTOToTask(TaskDTO taskDTO);
}
