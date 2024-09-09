package sheba.backend.app.mappers;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sheba.backend.app.DTO.MediaTaskDTO;
import sheba.backend.app.entities.MediaTask;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-03T19:57:19+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.7 (Oracle Corporation)"
)
@Component
public class MediaTaskMapperImpl implements MediaTaskMapper {

    @Override
    public MediaTaskDTO mediaTaskToMediaTaskDTO(MediaTask mediaTask) {
        if ( mediaTask == null ) {
            return null;
        }

        MediaTaskDTO mediaTaskDTO = new MediaTaskDTO();

        mediaTaskDTO.setMediaTaskID( mediaTask.getMediaTaskID() );
        mediaTaskDTO.setMediaType( mediaTask.getMediaType() );
        mediaTaskDTO.setMediaUrl( mediaTask.getMediaUrl() );

        return mediaTaskDTO;
    }

    @Override
    public MediaTask mediaTaskDTOToMediaTask(MediaTaskDTO mediaTaskDTO) {
        if ( mediaTaskDTO == null ) {
            return null;
        }

        MediaTask mediaTask = new MediaTask();

        mediaTask.setMediaTaskID( mediaTaskDTO.getMediaTaskID() );
        mediaTask.setMediaType( mediaTaskDTO.getMediaType() );
        mediaTask.setMediaUrl( mediaTaskDTO.getMediaUrl() );

        return mediaTask;
    }
}
