package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

@Data
public class CatReq {
    private String name;
    private Long parentId;
}