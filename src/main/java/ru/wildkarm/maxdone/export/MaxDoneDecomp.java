package ru.wildkarm.maxdone.export;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import ru.wildkarm.maxdone.export.model.ChecklistItem;
import ru.wildkarm.maxdone.export.model.Context;
import ru.wildkarm.maxdone.export.model.Project;
import ru.wildkarm.maxdone.export.model.Task;
import ru.wildkarm.maxdone.export.model.TaskOutputLine;

@Slf4j
public class MaxDoneDecomp {

    private String inputPath;

    public MaxDoneDecomp(String inputPath) {
        this.inputPath = inputPath + "\\";
        if (!this.inputPath.endsWith("\\")) {
            this.inputPath += "\\";
        }
    }

    public static void start(String inputPath) {
        new MaxDoneDecomp(inputPath).process();
    }

    public void process() {
        
        log.info("inputPath: {}", Path.of(inputPath).toFile().getAbsolutePath());
        
        String fileContexts = inputPath + "contexts.json";
        String fileCategories = inputPath + "categories.json";
        String fileProjects = inputPath + "projects.json";
        String fileTasks = inputPath + "tasks.json";
        String fileInbox = inputPath + "inbox.json";
        String fileCompleted = inputPath + "completed.json";

        List<Context> contextsList = Collections.emptyList();
        List<Context> categoriesList = Collections.emptyList();
        List<Project> projectsList = Collections.emptyList();
        List<Task> tasksList = Collections.emptyList();
        List<Task> inboxList = Collections.emptyList();
        List<Task> completedList = Collections.emptyList();

        try {
            contextsList = JsonParser.desJson(fileContexts, List.class, Context.class);
            categoriesList = JsonParser.desJson(fileCategories, List.class, Context.class);
            projectsList = JsonParser.desJson(fileProjects, List.class, Project.class);
            tasksList = JsonParser.desJson(fileTasks, List.class, Task.class);
            inboxList = JsonParser.desJson(fileInbox, List.class, Task.class);
            completedList = JsonParser.desJson(fileCompleted, List.class, Task.class);

            tasksList = updateTaskList(tasksList, contextsList, projectsList, categoriesList);
            inboxList = updateTaskList(inboxList, contextsList, projectsList, categoriesList);
            completedList = updateTaskList(completedList, contextsList, projectsList, categoriesList);

            List<String> rows = new ArrayList<>();
            rows.add(titleRow());
            rows.addAll(outputTaskTable(inboxList));
            rows.addAll(outputTaskTable(tasksList));
            rows.addAll(outputTaskTable(completedList));

            wtiteOutputFile(rows, inputPath);

        } catch (Exception e) {
            log.error("process ERROR: ", e);
        }
    }

    private String titleRow() {
        List<String> tableRows = Arrays.asList(
                "Дата создания",
                "Контекст",
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
                "Проект - Статус");
        return outputTaskRow(tableRows);
    }

    private List<String> outputTaskTable(List<Task> tasksList) {
        log.info("outputTaskTable START {}", tasksList.size());
        List<String> tableRows = new ArrayList<>();
        try {
            for (Task task : tasksList) {
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

    private void wtiteOutputFile(Iterable<? extends CharSequence> lines, String inputPath) {
        log.info("wtiteOutputFile START {}", inputPath);
        try {

            Path parentPath = Path.of(
                    Main.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent();

            DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.forLanguageTag("ru-RU"));
            String dateSuffix = LocalDateTime.now().format(fileFormatter);
            Path filePath = Path.of(parentPath.toAbsolutePath().toString(), "output_table_" + dateSuffix + ".csv");
            log.info("wtiteOutputFile OutputFile path = " + filePath.toFile().getAbsolutePath());

            Files.write(filePath, lines, Charset.forName("Windows-1251"),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        } catch (Exception e) {
            log.error("wtiteOutputFile ERROR: ", e);
        }
        log.info("wtiteOutputFile DONE {}", inputPath);
    }

    private List<Task> updateTaskList(
            List<Task> tasksList,
            List<Context> contextsList,
            List<Project> projectsList,
            List<Context> categoriesList) {

        Map<String, Context> contextMap = contextsList.stream().collect(Collectors.toMap(context -> context.getId(), c -> c));
        Map<String, Project> projectMap = projectsList.stream().collect(Collectors.toMap(roject -> roject.getId(), c -> c));
        Map<String, Context> categoriesMap = categoriesList.stream().collect(Collectors.toMap(category -> category.getId(), c -> c));

        for (Task task : tasksList) {
            task.setContextObject(contextMap.get(task.getContextId()));
            task.setProjectObject(projectMap.get(task.getGoalId()));
            if (task.getCategoryId() != null && !task.getCategoryId().isBlank()) {
                String st1 = task.getCategoryId();
                String st2 = task.getRealCategoty();
                Context st3 = categoriesMap.get(task.getRealCategoty());
                log.info("updateTaskList getCategoryId: {} / {} / {}", st1, st2);

                if (st3 != null) {
                    task.setCategoryObject(categoriesMap.get(task.getRealCategoty()));
                    log.info("updateTaskList getCategoryId: {}", st3.toString());
                }
            }
        }
        return tasksList;
    }

    private List<String> buildTaskRow(Task task) {
        List<String> out = new ArrayList<>();

        TaskOutputLine taskOutputLine = interactTask(task);
        out.add(taskOutputLine.getCreateTime());
        out.add(taskOutputLine.getContext());
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

        taskOutputLine.setCreateTime(formatDateTime(task.getCt()));
        taskOutputLine.setModifyTime(formatDateTime(task.getMt()));
        taskOutputLine.setStartTime(formatDateTime(task.getStartDatetime()));
        taskOutputLine.setDueTime(formatDateTime(task.getDueDate()));
        taskOutputLine.setCompletionDate(formatDateTime(task.getCompletionDate()));

        taskOutputLine.setContext(Optional.ofNullable(task.getContextObject()).map(m -> m.getTitle()).orElse(""));
        taskOutputLine.setCategory(Optional.ofNullable(task.getCategoryObject()).map(m -> m.getTitle()).orElse(""));
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

    private String formatDateTime(LocalDateTime dt) {
        return Optional.ofNullable(dt).map(m -> JsonParser.READABLE_DATE_TIME_FORMATTER.format(m)).orElse("");
    }

    private String formatCheckList(List<ChecklistItem> checklistItems) {
        String out = "";
        if (checklistItems == null) {
            return out;
        }
        checklistItems.sort(Comparator.comparingInt(ChecklistItem::getSortOrder));
        for (ChecklistItem item : checklistItems) {
            if (!out.isEmpty()) {
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

}
