package ru.wildkarm.maxdone.export.model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItem {

    @SerializedName("title")
    private String title;

    @SerializedName("done")
    private Boolean done;

    @SerializedName("sortOrder")
    private Integer sortOrder;
}