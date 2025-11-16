# 数据库

```sql
-- auto-generated definition
create table chat_messages
(
    id         bigint auto_increment
        primary key,
    session_id bigint                             not null,
    role       varchar(32)                        not null,
    content    text                               not null,
    created_at datetime default CURRENT_TIMESTAMP null
)
    comment 'AI聊天消息';

create index idx_chat_messages_session
    on chat_messages (session_id);
```



```sql
-- auto-generated definition
create table chat_sessions
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                                not null,
    title      varchar(255)                          not null,
    mode       varchar(32) default 'default'         null,
    created_at datetime    default CURRENT_TIMESTAMP null,
    updated_at datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment 'AI聊天会话';

create index idx_chat_sessions_user
    on chat_sessions (user_id);
```



```sql
-- auto-generated definition
create table flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);
```



```sql
-- auto-generated definition
create table link_categories
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    user_id    bigint        not null comment '用户ID',
    name       varchar(64)   not null comment '分类名称',
    parent_id  bigint        null comment '父分类ID',
    sort_order int default 0 null comment '排序',
    icon       varchar(128)  null comment '图标'
)
    comment '个人链接分类表';
```



```sql
-- auto-generated definition
create table links
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint                             not null comment '用户ID',
    category_id bigint                             null comment '分类ID',
    title       varchar(255)                       not null comment '标题',
    url         varchar(512)                       not null comment '链接地址',
    remark      varchar(512)                       null comment '备注',
    icon        varchar(128)                       null comment '图标',
    order_index int      default 0                 null comment '排序',
    created_at  datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '个人链接表';
```



```sql
-- auto-generated definition
create table mindmap_categories
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    user_id          bigint                                not null comment '用户ID',
    name             varchar(64)                           not null comment '分类名称',
    parent_id        bigint                                null comment '父分类ID',
    sort_order       int         default 0                 null comment '排序',
    icon             varchar(128)                          null comment '图标',
    created_at       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    cover_key        varchar(255)                          null comment '封面图片OSS Key',
    description      text                                  null comment '分类描述',
    visibility       varchar(20) default 'private'         null comment '可见性: private/public/enterprise',
    item_count       int         default 0                 null comment '分类下项目数量',
    background_style varchar(100)                          null comment '背景样式: gradient color',
    badge_text       varchar(50)                           null comment '徽章文字'
)
    comment '思维导图分类表';

create index idx_mindmap_categories_user_visibility
    on mindmap_categories (user_id, visibility);

create index idx_mindmap_categories_visibility
    on mindmap_categories (visibility);
```



```sql
-- auto-generated definition
create table mindmap_resources
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                             not null comment '用户ID',
    mindmap_id bigint                             not null comment '思维导图ID',
    node_id    varchar(100)                       not null comment '思维导图节点ID',
    oss_key    varchar(255)                       not null comment 'OSS文件Key',
    file_name  varchar(255)                       null comment '原始文件名',
    file_size  bigint                             null comment '文件大小(字节)',
    mime_type  varchar(100)                       null comment '文件类型',
    width      int                                null comment '图片宽度',
    height     int                                null comment '图片高度',
    sort_order int      default 0                 null comment '排序',
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint mindmap_resources_ibfk_1
        foreign key (mindmap_id) references mindmaps (id)
            on delete cascade
)
    comment '思维导图资源文件表';

create index idx_mindmap_node
    on mindmap_resources (mindmap_id, node_id);

create index idx_user
    on mindmap_resources (user_id);
```



```sql
-- auto-generated definition
create table mindmap_tag_relations
(
    id         bigint auto_increment
        primary key,
    mindmap_id bigint                             not null comment '思维导图ID',
    tag_id     bigint                             not null comment '标签ID',
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_mindmap_tag
        unique (mindmap_id, tag_id),
    constraint mindmap_tag_relations_ibfk_1
        foreign key (mindmap_id) references mindmaps (id)
            on delete cascade,
    constraint mindmap_tag_relations_ibfk_2
        foreign key (tag_id) references mindmap_tags (id)
            on delete cascade
)
    comment '思维导图标签关联表';

create index idx_mindmap
    on mindmap_tag_relations (mindmap_id);

create index idx_tag
    on mindmap_tag_relations (tag_id);
```



```sql
-- auto-generated definition
create table mindmap_tags
(
    id         bigint auto_increment
        primary key,
    user_id    bigint                                not null comment '用户ID',
    name       varchar(50)                           not null comment '标签名称',
    color      varchar(20) default '#1890ff'         null comment '标签颜色',
    created_at datetime    default CURRENT_TIMESTAMP null,
    constraint uk_user_tag
        unique (user_id, name)
)
    comment '思维导图标签表';

create index idx_user
    on mindmap_tags (user_id);
```



```sql
-- auto-generated definition
create table mindmaps
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint                                not null comment '用户ID',
    category_id bigint                                null comment '分类ID',
    title       varchar(255)                          not null comment '标题',
    description varchar(512)                          null comment '描述',
    content     json                                  null comment '思维导图节点数据(JSON格式)',
    oss_key     varchar(255)                          not null comment 'OSS文件Key',
    format      varchar(16)                           not null comment '文件格式',
    version     varchar(32) default '1.0'             null comment '版本号',
    visibility  varchar(16) default 'private'         null comment '可见性',
    status      varchar(20) default 'active'          null comment '状态: active/archived/deleted',
    cover_key   varchar(255)                          null comment '封面OSS Key',
    likes       int         default 0                 null comment '点赞数',
    views       int         default 0                 null comment '浏览量',
    node_count  int         default 0                 null comment '节点数量',
    created_at  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '思维导图表';

create index idx_mindmaps_category
    on mindmaps (category_id);
```



```sql
-- auto-generated definition
create table note_categories
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    user_id          bigint                                not null comment '用户ID',
    name             varchar(64)                           not null comment '分类名称',
    parent_id        bigint                                null comment '父分类ID',
    sort_order       int         default 0                 null comment '排序',
    icon             varchar(128)                          null comment '图标',
    created_at       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    cover_key        varchar(255)                          null comment '封面图片OSS Key',
    description      text                                  null comment '分类描述',
    visibility       varchar(20) default 'private'         null comment '可见性: private/public/enterprise',
    item_count       int         default 0                 null comment '分类下项目数量',
    background_style varchar(100)                          null comment '背景样式: gradient color',
    badge_text       varchar(50)                           null comment '徽章文字'
)
    comment '笔记分类表';

create index idx_note_categories_user_visibility
    on note_categories (user_id, visibility);

create index idx_note_categories_visibility
    on note_categories (visibility);
```



```sql
-- auto-generated definition
create table notes
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint                                not null comment '用户ID',
    category_id bigint                                null comment '分类ID',
    title       varchar(255)                          not null comment '标题',
    description varchar(512)                          null comment '描述',
    oss_key     varchar(255)                          not null comment 'OSS文件Key',
    format      varchar(16)                           not null comment '文件格式',
    visibility  varchar(16) default 'private'         null comment '可见性',
    tags        varchar(255)                          null comment '标签逗号分隔',
    cover_key   varchar(255)                          null comment '封面OSS Key',
    likes       int         default 0                 null comment '点赞数',
    views       int         default 0                 null comment '浏览量',
    word_count  int         default 0                 null comment '字数',
    created_at  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    content     longtext                              null comment '笔记原始内容'
)
    comment '笔记表';

create index idx_notes_category
    on notes (category_id);
```



```sql
-- auto-generated definition
create table profile_items
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    profile_id  bigint        not null comment '简介ID',
    type        varchar(32)   not null comment '类型:教育/工作/荣誉等',
    title       varchar(255)  not null comment '标题',
    content     varchar(2048) null comment '内容',
    start_date  date          null comment '开始日期',
    end_date    date          null comment '结束日期',
    order_index int default 0 null comment '排序'
)
    comment '个人简介项目表';
```



```sql
-- auto-generated definition
create table profiles
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    user_id    bigint        not null comment '用户ID',
    name       varchar(64)   null comment '姓名',
    contact    varchar(128)  null comment '联系方式',
    bio        varchar(1024) null comment '个人简介',
    avatar_key varchar(255)  null comment '头像OSS Key',
    location   varchar(128)  null comment '所在城市',
    job_title  varchar(128)  null comment '职位',
    website    varchar(255)  null comment '个人网站'
)
    comment '个人简介表';
```





```sql
-- auto-generated definition
create table users
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    username      varchar(64)                           not null comment '用户名',
    password_hash varchar(255)                          not null comment '密码哈希',
    nickname      varchar(64)                           null comment '昵称',
    role          varchar(32) default 'USER'            null comment '角色',
    status        tinyint     default 1                 null comment '状态:1正常,0停用',
    email         varchar(128)                          null comment '邮箱',
    phone         varchar(32)                           null comment '手机号',
    avatar_url    varchar(256)                          null comment '头像URL',
    last_login_at datetime                              null comment '最近登录时间',
    created_at    datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint username
        unique (username)
)
    comment '用户表';
```