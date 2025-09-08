package com.example.regulationsbatch.web;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentsJobRequest {

    @NotBlank
    private String docketId;

    private String startCommentId;
}
