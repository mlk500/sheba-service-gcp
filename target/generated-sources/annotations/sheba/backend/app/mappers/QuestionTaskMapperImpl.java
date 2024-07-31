package sheba.backend.app.mappers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.QuestionTaskDTO;
import sheba.backend.app.entities.QuestionTask;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-07-30T23:36:36+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class QuestionTaskMapperImpl implements QuestionTaskMapper {

    @Override
    public QuestionTaskDTO questionTaskToQuestionTaskDTO(QuestionTask questionTask) {
        if ( questionTask == null ) {
            return null;
        }

        QuestionTaskDTO questionTaskDTO = new QuestionTaskDTO();

        questionTaskDTO.setQuestionTaskID( questionTask.getQuestionTaskID() );
        questionTaskDTO.setQuestion( questionTask.getQuestion() );
        List<String> list = questionTask.getAnswers();
        if ( list != null ) {
            questionTaskDTO.setAnswers( new ArrayList<String>( list ) );
        }
        questionTaskDTO.setCorrectAnswer( questionTask.getCorrectAnswer() );

        return questionTaskDTO;
    }

    @Override
    public QuestionTask questionTaskDTOToQuestionTask(QuestionTaskDTO questionTaskDTO) {
        if ( questionTaskDTO == null ) {
            return null;
        }

        QuestionTask questionTask = new QuestionTask();

        questionTask.setQuestionTaskID( questionTaskDTO.getQuestionTaskID() );
        questionTask.setQuestion( questionTaskDTO.getQuestion() );
        List<String> list = questionTaskDTO.getAnswers();
        if ( list != null ) {
            questionTask.setAnswers( new ArrayList<String>( list ) );
        }
        questionTask.setCorrectAnswer( questionTaskDTO.getCorrectAnswer() );

        return questionTask;
    }
}
