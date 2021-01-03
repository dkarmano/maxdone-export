package ru.wildkarm.maxdone.export.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

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
    
    transient private Context categoryObject;
    
    public String getRealCategory() {
        return Optional.ofNullable(categoryId).orElse("")
                .strip()
                .replace(",", "")
                .replace("\r", "")
                .replace("\t", "")
                .replace("\n", "");
    }
}

