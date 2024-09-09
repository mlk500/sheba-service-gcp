package sheba.backend.app.mappers;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.MediaTaskDTO;
import sheba.backend.app.DTO.TaskDTO;
import sheba.backend.app.entities.MediaTask;
import sheba.backend.app.entities.Task;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-03T19:57:19+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Autowired
    private QuestionTaskMapper questionTaskMapper;
    @Autowired
    private MediaTaskMapper mediaTaskMapper;

    @Override
    public TaskDTO taskToTaskDTO(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskDTO taskDTO = new TaskDTO();

        taskDTO.setQuestionTask( questionTaskMapper.questionTaskToQuestionTaskDTO( task.getQuestionTask() ) );
        taskDTO.setMediaList( mediaTaskListToMediaTaskDTOList( task.getMediaList() ) );
        taskDTO.setTaskID( task.getTaskID() );
        taskDTO.setName( task.getName() );
        taskDTO.setWithMsg( task.isWithMsg() );
        List<String> list1 = task.getTaskFreeTexts();
        if ( list1 != null ) {
            taskDTO.setTaskFreeTexts( new ArrayList<String>( list1 ) );
        }

        return taskDTO;
    }

    protected List<MediaTaskDTO> mediaTaskListToMediaTaskDTOList(List<MediaTask> list) {
        if ( list == null ) {
            return null;
        }

        List<MediaTaskDTO> list1 = new ArrayList<MediaTaskDTO>( list.size() );
        for ( MediaTask mediaTask : list ) {
            list1.add( mediaTaskMapper.mediaTaskToMediaTaskDTO( mediaTask ) );
        }

        return list1;
    }
}
