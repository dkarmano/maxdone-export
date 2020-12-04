package test_pom;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaxDoneDecomp {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Context {
        @SerializedName("id")
        private String id;

        @SerializedName("ct")
        private LocalDateTime ct;

        @SerializedName("mt")
        private LocalDateTime mt;

        @SerializedName("cb")
        private String cb;

        @SerializedName("owners")
        private List<String> owners;

        @SerializedName("title")
        private String title;

        @SerializedName("userId")
        private String userId;

        @SerializedName("archived")
        private Boolean archived;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Project {

        @SerializedName("id")
        private String id;

        @SerializedName("ct")
        private LocalDateTime ct;

        @SerializedName("mt")
        private LocalDateTime mt;

        @SerializedName("cb")
        private String cb;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("status")
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Task {

        @SerializedName("id")
        private String id;

        @SerializedName("ct")
        private LocalDateTime ct;

        @SerializedName("mt")
        private LocalDateTime mt;

        @SerializedName("dueDate")
        private LocalDateTime dueDate;

        @SerializedName("startDatetime")
        private LocalDateTime startDatetime;

        @SerializedName("completionDate")
        private LocalDateTime completionDate;

        @SerializedName("path")
        private String categoryId;

        @SerializedName("title")
        private String title;

        @SerializedName("cb")
        private String cb;
        
        @SerializedName("userId")
        private String userId;

        @SerializedName("goalId")
        private String goalId;

        @SerializedName("contextId")
        private String contextId;

        @SerializedName("taskType")
        private String taskType;

        @SerializedName("allDay")
        private String allDay;

        @SerializedName("done")
        private Boolean done;

        @SerializedName("priority")
        private Double priority;

        @SerializedName("notes")
        private String notes;

        @SerializedName("checklistItems")
        private List<ChecklistItem> checklistItems;

        @SerializedName("objectType")
        private String objectType;

        @SerializedName("project")
        private Boolean project;

        @SerializedName("archived")
        private Boolean archived;
        

        transient private Context contextObject;
        
        transient private Project projectObject;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ChecklistItem {

        @SerializedName("title")
        private String title;

        @SerializedName("done")
        private Boolean done;

        @SerializedName("sortOrder")
        private Integer sortOrder;
    }
    

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class TaskOutputLine {

        private String createTime;
        
        private String modifyTime;

        private String startTime;
        
        private String dueTime;

        private String completionDate;

        private String category;

        private String title;

        private String project;
        
        private String projectDescription;
        
        private String projectStatus;

        private String taskType;

        private String done;

        private String priority;

        private String notes;

        private String checklist;

        private String archived;
    }
    
    private List<String> buildTaskRow(Task task) {
        List<String> out = new ArrayList<>();
        
        TaskOutputLine taskOutputLine = interactTask(task);
        out.add(taskOutputLine.getCreateTime());
        out.add(taskOutputLine.getCategory());
        out.add(taskOutputLine.getTitle());
        out.add(taskOutputLine.getTaskType());
        out.add(taskOutputLine.getPriority());
        out.add(taskOutputLine.getDone());
        out.add(taskOutputLine.getModifyTime());
        out.add(taskOutputLine.getStartTime());
        out.add(taskOutputLine.getDueTime());
        out.add(taskOutputLine.getCompletionDate());
        out.add(taskOutputLine.getNotes());
        out.add(taskOutputLine.getChecklist());
        out.add(taskOutputLine.getProject());
        out.add(taskOutputLine.getProjectDescription());
        out.add(taskOutputLine.getProjectStatus());

        return out;
    }
    
    private TaskOutputLine interactTask(Task task) {
        TaskOutputLine taskOutputLine = new TaskOutputLine();
        
        taskOutputLine.setCreateTime(parseDateTime(task.getCt()));      
        taskOutputLine.setModifyTime(parseDateTime(task.getMt()));
        taskOutputLine.setStartTime(parseDateTime(task.getStartDatetime()));
        taskOutputLine.setDueTime(parseDateTime(task.getDueDate()));
        taskOutputLine.setCompletionDate(parseDateTime(task.getCompletionDate()));
        
        taskOutputLine.setCategory(Optional.ofNullable(task.getContextObject()).map(m -> m.getTitle()).orElse(""));
        taskOutputLine.setTitle(task.getTitle());
        taskOutputLine.setProject(Optional.ofNullable(task.getProjectObject()).map(m -> m.getTitle()).orElse(""));
        taskOutputLine.setProjectDescription(formatNotes(Optional.ofNullable(task.getProjectObject()).map(m -> m.getDescription()).orElse("")));
        taskOutputLine.setProjectStatus(Optional.ofNullable(task.getProjectObject()).map(m -> m.getStatus()).orElse(""));
        
        taskOutputLine.setTaskType(task.getTaskType());
        
        taskOutputLine.setDone(String.valueOf(task.getDone()));
        taskOutputLine.setPriority(String.valueOf(task.getPriority()));
        
        taskOutputLine.setNotes(formatNotes(task.getNotes()));
        taskOutputLine.setChecklist(formatCheckList(task.getChecklistItems()));
        
        return taskOutputLine;
    }

    private String parseDateTime(LocalDateTime dt) {
        return Optional.ofNullable(dt).map(m -> READABLE_DATE_TIME_FORMATTER.format(m)).orElse("");
    }

    private String formatCheckList(List<ChecklistItem> checklistItems) {
        String out = "";
        if(checklistItems == null) {
            return out;
        }
        checklistItems.sort(Comparator.comparingInt(ChecklistItem::getSortOrder));
        for(ChecklistItem item: checklistItems) {
            if(!out.isEmpty()) {
                out += "\n";
            }
            out += (item.getDone() ? "> + " : "> - ") + item.getTitle().replace("\"", "''");
        }        
        
        return out;
    }

    private String formatNotes(String notes) {
        Pattern p = Pattern.compile("<a target(.*?)>");
        Matcher m = p.matcher(notes);
        notes = m.replaceAll("");

        return notes
                .replace("</div>", "")
                .replace("<div>", "")                
                .replace("\"", "''")
                .replace("</a>", "")
                .replace("<p>", "")
                .replace("<br />", "\n")
                .replace("</p>", "\n\n");
    }





    public static void start() {
        new MaxDoneDecomp().process();
    }

    private void process() {

        String fileContexts = "F:\\Work\\__Sripts\\maxdone\\contexts.json";
        String fileProjects = "F:\\Work\\__Sripts\\maxdone\\projects.json";
        String fileTasks = "F:\\Work\\__Sripts\\maxdone\\tasks.json";
        String fileInbox = "F:\\Work\\__Sripts\\maxdone\\inbox.json";
        String fileCompleted = "F:\\Work\\__Sripts\\maxdone\\completed.json";
        
        List<Context> contextsList = Collections.emptyList();
        List<Project> projectsList = Collections.emptyList();
        List<Task> tasksList = Collections.emptyList();
        List<Task> inboxList = Collections.emptyList();
        List<Task> completedList = Collections.emptyList();
        
        try {
            contextsList = desJson(fileContexts, new TypeToken<List<Context>>() {}).orElseThrow( () -> new FileNotFoundException());
            projectsList = desJson(fileProjects, new TypeToken<List<Project>>() {}).orElseThrow( () -> new FileNotFoundException());
            tasksList = desJson(fileTasks, new TypeToken<List<Task>>() {}).orElseThrow( () -> new FileNotFoundException());
            inboxList = desJson(fileInbox, new TypeToken<List<Task>>() {}).orElseThrow( () -> new FileNotFoundException());
            completedList = desJson(fileCompleted, new TypeToken<List<Task>>() {}).orElseThrow( () -> new FileNotFoundException());
            
            tasksList = updateTaskList(tasksList, contextsList, projectsList);
            inboxList = updateTaskList(inboxList, contextsList, projectsList);
            completedList = updateTaskList(completedList, contextsList, projectsList);
            
            List<String> rows = new ArrayList<>();
            rows.add(titleRow());
            rows.addAll(outputTaskTable(inboxList));
            rows.addAll(outputTaskTable(tasksList));
            rows.addAll(outputTaskTable(completedList));
            
            wtiteOutputFile(rows, fileTasks);
            
        } catch (Exception e) {
            log.error("process ERROR: ", e);
        }
    }

    private String titleRow() {
        List<String> tableRows = Arrays.asList(
                "Дата создания",
                "Категория",
                "Заголовок",
                "Тип",
                "Приоритет",
                "Завершён",
                "Дата изменения",
                "Дата начала",
                "Срок",
                "Дата выполнения",
                "Заметки",
                "Список",
                "Проект",
                "Проект - Описание",
                "Проект - Статус"
                );
        return outputTaskRow(tableRows);
    }

    private List<String> outputTaskTable(List<Task> tasksList) {
        log.info("outputTaskTable START {}", tasksList.size());
        List<String> tableRows = new ArrayList<>();
        try {
            for(Task task: tasksList) {
                String row = outputTaskRow(buildTaskRow(task));
                tableRows.add(row);
            }

        } catch (Exception e) {
            log.error("outputTaskTable ERROR: ", e);
        }
        log.info("outputTaskTable DONE {}", tasksList.size());
        return tableRows;
    }
    


    private String outputTaskRow(List<String> cells) {
        // log.info("outputTaskRow START {}", cells.size());

        String row = "";
        for (String cell : cells) {
            if (!row.isEmpty()) {
                row += ";";
            }
            row += "\"" + cell + "\"";
        }
        // log.info("outputTaskRow DONE {}", cells.size());
        return row;
    }
    
    
    private void wtiteOutputFile(Iterable<? extends CharSequence> lines, String fileTasks) {
        log.info("wtiteOutputFile START {}", fileTasks);
        try {            
            Files.write(Paths.get(fileTasks + ".csv"), lines, Charset.forName("Windows-1251"), 
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
                    );
        } catch (Exception e) {
            log.error("wtiteOutputFile ERROR: ", e);
        }
        log.info("wtiteOutputFile DONE {}", fileTasks);
    }

    private List<Task> updateTaskList(List<Task> tasksList, List<Context> contextsList, List<Project> projectsList) {
        Map<String,Context> contextMap = contextsList.stream().collect(Collectors.toMap( context -> context.getId(), c -> c));
        Map<String, Project> projectMap = projectsList.stream().collect(Collectors.toMap( roject -> roject.getId(), c -> c));
        
        for(Task task: tasksList) {
            task.setContextObject(contextMap.get(task.getContextId()));
            task.setProjectObject(projectMap.get(task.getGoalId()));
        }
        return tasksList;
    }

    public <T> Optional<T> desJson(String file, TypeToken<T> type) {
        log.warn("desJson START for file  {}", file);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, OFSET_DESERIALIZER);        
        
        Gson gson = builder.create();
        Optional<T> resObj = Optional.empty();
        try (Reader reader = new BufferedReader(new FileReader(file))) {

            resObj = Optional.ofNullable(gson.fromJson(reader, type.getType()));

        } catch (Exception e) {
            log.error("desJson Reader ERROR: ", e);
        }

        log.info("desJson succseed = {}\n\n", resObj);
        return resObj;
    }

    
    public static final DateTimeFormatter EXT_ISO_OFFSET_DATE_TIME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .parseLenient()
            .appendLiteral("+0000")
            .parseStrict().toFormatter();
    
    public static final JsonSerializer<LocalDateTime> OFSET_SERIALIZER = (src, typeOfSrc, context) -> {
        return src == null ? null : new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    };
    
    public static final JsonDeserializer<LocalDateTime> OFSET_DESERIALIZER = (json, typeOfT, context) -> {
        JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();

        try {
            if (jsonPrimitive.isString()) {
                return LocalDateTime.parse(jsonPrimitive.getAsString(), EXT_ISO_OFFSET_DATE_TIME);
            }

            if (jsonPrimitive.isNumber()) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(jsonPrimitive.getAsLong()), ZoneId.systemDefault());
            }
        } catch (RuntimeException var5) {
            throw new JsonParseException("Unable to parse LocalDateTime", var5);
        }

        throw new JsonParseException("Unable to parse LocalDateTime");
    };    

    public static final DateTimeFormatter READABLE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"));
    
}
