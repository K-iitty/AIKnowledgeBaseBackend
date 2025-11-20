package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.Profile;
import com.fanfan.aiknowledgebasebackend.domain.entity.ProfileItem;

import java.time.LocalDate;
import java.util.List;

public interface ProfileService {
    Profile getOrCreate(Long userId);
    Profile update(Profile profile);
    ProfileItem addItem(Long profileId, String type, String title, String content, LocalDate startDate, LocalDate endDate, Integer orderIndex);
    ProfileItem updateItem(Long id, String type, String title, String content, LocalDate startDate, LocalDate endDate, Integer orderIndex);
    void deleteItem(Long id);
    List<ProfileItem> listItems(Long profileId);
}

