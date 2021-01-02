package ru.wildkarm.maxdone.export.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskOutputLine {

    private String createTime;
    
    private String modifyTime;

    private String startTime;
    
    private String dueTime;

    private String completionDate;

    private String context;

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