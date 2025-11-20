package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.Profile;
import com.fanfan.aiknowledgebasebackend.domain.entity.ProfileItem;
import com.fanfan.aiknowledgebasebackend.mapper.ProfileItemMapper;
import com.fanfan.aiknowledgebasebackend.mapper.ProfileMapper;
import com.fanfan.aiknowledgebasebackend.service.ProfileService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;
    private final ProfileItemMapper profileItemMapper;

    public ProfileServiceImpl(ProfileMapper profileMapper, ProfileItemMapper profileItemMapper) {
        this.profileMapper = profileMapper;
        this.profileItemMapper = profileItemMapper;
    }

    @Override
    public Profile getOrCreate(Long userId) {
        Profile p = profileMapper.selectOne(new LambdaQueryWrapper<Profile>().eq(Profile::getUserId, userId));
        if (p == null) {
            p = new Profile();
            p.setUserId(userId);
            p.setName("");
            p.setContact("");
            p.setBio("");
            profileMapper.insert(p);
        }
        return p;
    }

    @Override
    public Profile update(Profile profile) {
        profileMapper.updateById(profile);
        return profileMapper.selectById(profile.getId());
    }

    @Override
    public ProfileItem addItem(Long profileId, String type, String title, String content, LocalDate startDate, LocalDate endDate, Integer orderIndex) {
        ProfileItem item = new ProfileItem();
        item.setProfileId(profileId);
        item.setType(type);
        item.setTitle(title);
        item.setContent(content);
        item.setStartDate(startDate);
        item.setEndDate(endDate);
        item.setOrderIndex(orderIndex != null ? orderIndex : 0);
        profileItemMapper.insert(item);
        return item;
    }
    
    @Override
    public ProfileItem updateItem(Long id, String type, String title, String content, LocalDate startDate, LocalDate endDate, Integer orderIndex) {
        ProfileItem item = profileItemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("个人简介项目不存在");
        }
        if (type != null) item.setType(type);
        if (title != null) item.setTitle(title);
        if (content != null) item.setContent(content);
        if (startDate != null) item.setStartDate(startDate);
        if (endDate != null) item.setEndDate(endDate);
        if (orderIndex != null) item.setOrderIndex(orderIndex);
        profileItemMapper.updateById(item);
        return item;
    }

    @Override
    public void deleteItem(Long id) {
        profileItemMapper.deleteById(id);
    }

    @Override
    public List<ProfileItem> listItems(Long profileId) {
        return profileItemMapper.selectList(new LambdaQueryWrapper<ProfileItem>()
                .eq(ProfileItem::getProfileId, profileId)
                .orderByAsc(ProfileItem::getOrderIndex));
    }
}

