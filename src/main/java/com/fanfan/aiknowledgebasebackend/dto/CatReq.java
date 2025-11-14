package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

@Data
public class CatReq {
    private String name;
    private Long parentId;
}