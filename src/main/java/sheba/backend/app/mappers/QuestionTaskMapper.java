package sheba.backend.app.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sheba.backend.app.DTO.QuestionTaskDTO;
import sheba.backend.app.entities.QuestionTask;

@Mapper(componentModel = "spring")
public interface QuestionTaskMapper {
    @Mapping(target = "questionTaskID", source = "questionTaskID")
    @Mapping(target = "question", source = "question")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "correctAnswer", source = "correctAnswer")
    QuestionTaskDTO questionTaskToQuestionTaskDTO(QuestionTask questionTask);

    QuestionTask questionTaskDTOToQuestionTask(QuestionTaskDTO questionTaskDTO);
}
