package sheba.backend.app.BL;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sheba.backend.app.entities.Admin;
import sheba.backend.app.entities.MediaTask;
import sheba.backend.app.entities.QuestionTask;
import sheba.backend.app.entities.Task;
import sheba.backend.app.exceptions.AdminNotFound;
import sheba.backend.app.exceptions.MediaUploadFailed;
import sheba.backend.app.exceptions.TaskCannotBeEmpty;
import sheba.backend.app.exceptions.TaskIsPartOfUnit;
import sheba.backend.app.repositories.AdminRepository;
import sheba.backend.app.repositories.TaskRepository;
import sheba.backend.app.repositories.UnitRepository;
import sheba.backend.app.security.CustomAdminDetails;
import sheba.backend.app.util.StoragePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class TaskBL {

    private final TaskRepository taskRepository;
    private final QuestionTaskBL questionTaskBL;
    private final MediaTaskBL mediaTaskBL;
    private final AdminRepository adminRepository;
    private final UnitRepository unitRepository;
    private final GcsBL gcsBL;


    public TaskBL(TaskRepository taskRepository, QuestionTaskBL questionTaskBL, MediaTaskBL mediaTaskBL, AdminRepository adminRepository, UnitRepository unitRepository, GcsBL gcsBL) {
        this.taskRepository = taskRepository;
        this.questionTaskBL = questionTaskBL;
        this.mediaTaskBL = mediaTaskBL;
        this.adminRepository = adminRepository;
        this.unitRepository = unitRepository;
        this.gcsBL = gcsBL;
    }

    @Transactional
    // use when adding task items
    public Task createTask(Task task, QuestionTask questionTask, List<MultipartFile> media, String adminSector) throws TaskCannotBeEmpty, IOException, AdminNotFound, MediaUploadFailed {
        Admin admin;
        if(adminSector!= null && SecurityContextHolder.getContext().getAuthentication() != null){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomAdminDetails adminDetails = (CustomAdminDetails) authentication.getPrincipal();
        admin = adminRepository.findAdminByUsername(adminDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        task.setAdmin(admin);
        }
        else{
            admin = adminRepository.findAdminBySector(adminSector).orElseThrow(() -> new AdminNotFound("Admin not found with " +adminSector+ " sector"));
            task.setAdmin(admin);
        }
//
        task.setAdminIDAPI(admin.getAdminID());
        if (questionTask == null && media == null && task.getTaskFreeTexts().isEmpty() && task.getMediaList() == null) {
            throw new TaskCannotBeEmpty("Task must contain at least one item.");
        }

        task = taskRepository.save(task);

        try {
            createTaskItems(task, questionTask, media);
        } catch (MediaUploadFailed e) {
            // If media upload fails, we roll back the transaction
            throw new MediaUploadFailed("Failed to create task due to media upload failure", e);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long taskID, Task newTask, QuestionTask questionTask, List<MultipartFile> newMedia,
                           String adminSector, List<Long> toBeDeletedMediaIds, Long tbdQuestion)
            throws TaskCannotBeEmpty, AdminNotFound, MediaUploadFailed {
        Task task = taskRepository.findById(taskID)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskID));

        if (questionTask == null
                && newMedia == null
                && task.getQuestionTask() == null
                && tbdQuestion != null
                && task.getTaskFreeTexts().isEmpty() &&
                newTask.getTaskFreeTexts().isEmpty() &&
                task.getMediaList() == null &&
                (task.getMediaList().size() - toBeDeletedMediaIds.size()) <= 0) {
            throw new TaskCannotBeEmpty("Task must contain at least one item.");
        }

        updateTaskFields(task, newTask);

        if (toBeDeletedMediaIds != null) {
            for (Long mediaId : toBeDeletedMediaIds) {
                removeMediaFromTask(taskID, mediaId);
            }
        }

        if (tbdQuestion != null) {
            removeQuestionFromTask(taskID);
        }

        if (questionTask != null) {
            if (task.getQuestionTask() != null) {
                updateTaskQuestion(task.getTaskID(), task.getQuestionTask().getQuestionTaskID(), questionTask);
            } else {
                questionTask.setTask(task);
                task.setQuestionTask(questionTaskBL.createQuestionTask(questionTask));
            }
        }

        if (newMedia != null && !newMedia.isEmpty()) {
            for (MultipartFile file : newMedia) {
                try {
                    MediaTask savedMedia = mediaTaskBL.createMedia(task, file);
                    task.getMediaList().add(savedMedia);
                } catch (MediaUploadFailed e) {
                    throw new MediaUploadFailed("Failed to upload new media during task update", e);
                }
            }
        }

        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) throws TaskIsPartOfUnit, MediaUploadFailed {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        if (unitRepository.findByTask(task) != null && !unitRepository.findByTask(task).isEmpty()) {
            throw new TaskIsPartOfUnit("Task is part of an existing game");
        }

        try {
            for (MediaTask mediaTask : task.getMediaList()) {
                gcsBL.bucketDelete(mediaTask.getMediaPath());
            }
            boolean res = gcsBL.deleteFolder(StoragePath.MEDIA_TASK_PATH+"/task"+taskId);
            System.out.println("res is "+ res);
        } catch (Exception e) {
            throw new MediaUploadFailed("Failed to delete media from cloud storage", e);
        }

        if (task.getQuestionTask() != null) {
            questionTaskBL.deleteQuestionTask(task.getQuestionTask());
        }

        taskRepository.delete(task);
    }

    @Transactional
    public Task removeMediaFromTask(Long taskId, Long mediaId) throws TaskCannotBeEmpty, IllegalArgumentException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        MediaTask mediaToRemove = task.getMediaList().stream()
                .filter(media -> media.getMediaTaskID() == mediaId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Media not found with id: " + mediaId));

        task.getMediaList().remove(mediaToRemove);
        try {
            gcsBL.bucketDelete(mediaToRemove.getMediaPath());
            mediaTaskBL.deleteMedia(mediaToRemove);
        } catch (Exception e) {
            throw new MediaUploadFailed("Failed to delete media from cloud storage", e);
        }

        if (task.getQuestionTask() == null && task.getMediaList().isEmpty() && task.getTaskFreeTexts().isEmpty()) {
            throw new TaskCannotBeEmpty("Task must contain at least one item after media removal.");
        }

        return taskRepository.save(task);
    }

//    public Task updateTask(Long taskID, Task newTask, QuestionTask questionTask, List<MultipartFile> media, String adminSector, List<Long> toBeDeletedMediaIds, Long tbdQuestion) throws TaskCannotBeEmpty, IOException, IllegalArgumentException, AdminNotFound {
//        Task task = taskRepository.findById(taskID)
//                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskID));
//
//        if (questionTask == null
//                && media == null
//                && task.getQuestionTask() == null
//                && tbdQuestion != null
//                && task.getTaskFreeTexts().isEmpty() &&
//                newTask.getTaskFreeTexts().isEmpty() &&
//                task.getMediaList() == null &&
//                (task.getMediaList().size() - toBeDeletedMediaIds.size()) <= 0) {
//            throw new TaskCannotBeEmpty("Task must contain at least one item.");
//        }
//
//        if (newTask != null) {
//            updateTaskFields(task, newTask);
//        }
//        if (toBeDeletedMediaIds != null) {
//            for (Long mediaId : toBeDeletedMediaIds) {
//                removeMediaFromTask(taskID, mediaId);
//            }
//        }
//        if (tbdQuestion != null) {
//            removeQuestionFromTask(taskID);
//        }
//        createTask(task, questionTask, media, adminSector);
//
//        return taskRepository.save(task);
//    }


    private void updateTaskFields(Task task, Task newTask) {
        task.setName(newTask.getName());
        task.setTaskFreeTexts(newTask.getTaskFreeTexts());
        task.setDescription(newTask.getDescription());
        task.setWithMsg(newTask.isWithMsg());
    }

    public QuestionTask updateTaskQuestion(Long taskId, Long questionTaskId, QuestionTask questionTask) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        return questionTaskBL.updateQuestionTask(taskId, questionTaskId, questionTask);
    }


//    public Task removeMediaFromTask(Long taskId, Long mediaId) throws TaskCannotBeEmpty, IOException, IllegalArgumentException {
//        Task task = taskRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
//
////        if (task.getMediaList().size() < 2 && task.getTaskFreeTexts().isEmpty() && task.getQuestionTask() == null) {
////            throw new TaskCannotBeEmpty("Removing this item leads to an empty task, delete task instead or add at least one item");
////        }
//
//        MediaTask mediaToRemove = task.getMediaList().stream()
//                .filter(media -> media.getMediaTaskID() == mediaId)
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Media not found with id: " + mediaId));
//
//        task.getMediaList().remove(mediaToRemove);
//        mediaTaskBL.deleteMedia(mediaToRemove);
//
////        if (task.getQuestionTask() == null && task.getMediaList().isEmpty()) {
////            throw new TaskCannotBeEmpty("Task must contain at least one item after media removal.");
////        }
//        return taskRepository.save(task);
//    }


    public Task removeQuestionFromTask(Long taskId) throws TaskCannotBeEmpty, IllegalArgumentException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

//        if (task.getMediaList().isEmpty() || task.getTaskFreeTexts().isEmpty()) {
//            throw new TaskCannotBeEmpty("Removing this item leads to an empty task, delete task instead or add at least one item");
//        }
        if (task.getQuestionTask() != null) {
            questionTaskBL.deleteQuestionTask(task.getQuestionTask());
            task.setQuestionTask(null);
        }
        return taskRepository.save(task);
    }

//    public Optional<Task> getTask(Long id) {
//        return taskRepository.findById(id);
//    }

//    public void deleteTask(Long taskId) throws IOException, RuntimeException, TaskIsPartOfUnit {
//        Task task = taskRepository.findById(taskId)
//                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
//
//        if(unitRepository.findByTask(task) != null && !unitRepository.findByTask(task).isEmpty()){
//            unitRepository.findByTask(task);
//            throw new TaskIsPartOfUnit("Task is part of an existing game");
//        }
//
//        mediaTaskBL.deleteAllMediaForTask(taskId);
//        if (task.getQuestionTask() != null) {
//            questionTaskBL.deleteQuestionTask(task.getQuestionTask());
//        }
//
//        String baseDirectory = StoragePath.MEDIA_TASK_PATH;
//        Path taskDirectory = Paths.get(baseDirectory + File.separator + "task" + taskId);
//        if (Files.exists(taskDirectory)) {
//            try (Stream<Path> paths = Files.walk(taskDirectory)) {
//                paths.sorted(Comparator.reverseOrder())
//                        .map(Path::toFile)
//                        .forEach(file -> {
//                            if (!file.delete()) {
//                                throw new RuntimeException("Failed to delete file " + file);
//                            }
//                        });
//            }
//        }
//
//        taskRepository.delete(task);
//    }


    private void createTaskItems(Task task, QuestionTask questionTask, List<MultipartFile> media) throws MediaUploadFailed {
        if (questionTask != null) {
            if (task.getQuestionTask() != null) {
                updateTaskQuestion(task.getTaskID(), task.getQuestionTask().getQuestionTaskID(),
                        questionTask);
            } else {
                questionTask.setTask(task);
                task.setQuestionTask(questionTaskBL.createQuestionTask(questionTask));
            }
        }

        if (media != null && !media.isEmpty()) {
            for (MultipartFile file : media) {
                MediaTask savedMedia = mediaTaskBL.createMedia(task, file);
                task.getMediaList().add(savedMedia);
                savedMedia.setTask(task);
            }
        }
    }


//    public List<Task> getAllTasks() {
//        return taskRepository.findAll();
//    }

    public Optional<Task> getTask(Long id) {
        Optional<Task> taskOptional = taskRepository.findById(id);
        taskOptional.ifPresent(this::setMediaUrls);
        return taskOptional;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        tasks.forEach(this::setMediaUrls);
        return tasks;
    }

    private void setMediaUrls(Task task) {
        task.getMediaList().forEach(mediaTask -> {
            String url = gcsBL.getPublicUrl(mediaTask.getMediaPath());
            mediaTask.setMediaUrl(url);
        });
    }
}


