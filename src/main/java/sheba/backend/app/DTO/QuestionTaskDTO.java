package sheba.backend.app.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuestionTaskDTO {
    private long questionTaskID;
    private String question;
    private List<String> answers;
    private Integer correctAnswer;
}
