package sheba.backend.app.DTO;

import lombok.Data;

import java.util.List;
@Data
public class TaskDTO {
    private long taskID;
    private String name;
    private boolean withMsg;
    private List<String> taskFreeTexts;
    private QuestionTaskDTO questionTask;
    private List<MediaTaskDTO> mediaList;
}
