package com.fanfan.aiknowledgebasebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * XMind文件解析服务
 * XMind文件是ZIP格式，包含content.xml等文件
 */
@Service
public class XMindParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析XMind文件，返回JSON格式的思维导图数据
     */
    public Map<String, Object> parseXMindFile(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            
            ZipEntry entry;
            byte[] contentXml = null;
            
            // 遍历ZIP文件中的所有条目
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                // XMind 8/2020/2021使用content.xml
                if (entryName.equals("content.xml") || entryName.equals("content.json")) {
                    contentXml = zipInputStream.readAllBytes();
                    break;
                }
            }
            
            if (contentXml == null) {
                throw new RuntimeException("无效的XMind文件：未找到content.xml");
            }
            
            // 解析XML内容
            return parseContentXml(contentXml);
            
        } catch (Exception e) {
            throw new RuntimeException("解析XMind文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析content.xml文件
     */
    private Map<String, Object> parseContentXml(byte[] xmlData) throws Exception {
        System.out.println("开始解析XMind XML内容，大小: " + xmlData.length + " 字节");
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(new ByteArrayInputStream(xmlData));
        Element root = document.getRootElement();
        
        System.out.println("XML根元素: " + root.getName());
        
        // XMind文件结构: xmap-content > sheet > topic (root topic)
        Element sheet = root.element("sheet");
        if (sheet == null) {
            System.err.println("未找到sheet元素");
            throw new RuntimeException("无效的XMind文件结构：未找到sheet元素");
        }
        
        Element rootTopic = sheet.element("topic");
        if (rootTopic == null) {
            System.err.println("未找到根topic元素");
            throw new RuntimeException("无效的XMind文件结构：未找到根topic元素");
        }
        
        System.out.println("找到根主题节点");
        Map<String, Object> result = new HashMap<>();
        result.put("root", parseNode(rootTopic));
        
        System.out.println("解析完成，节点数: " + countNodes(result));
        return result;
    }

    /**
     * 递归解析节点
     */
    private Map<String, Object> parseNode(Element element) {
        Map<String, Object> node = new HashMap<>();
        
        // 获取节点ID
        String id = element.attributeValue("id");
        if (id != null) {
            node.put("id", id);
        } else {
            node.put("id", UUID.randomUUID().toString());
        }
        
        // 获取节点标题
        Element titleElement = element.element("title");
        if (titleElement != null) {
            String title = titleElement.getTextTrim();
            node.put("topic", title);
            System.out.println("  解析节点: " + title);
        } else {
            node.put("topic", "");
        }
        
        // 获取子节点 - XMind结构: topic > children > topics > topic
        Element childrenElement = element.element("children");
        if (childrenElement != null) {
            List<Map<String, Object>> children = new ArrayList<>();
            
            // 获取所有topics元素
            List<Element> topicsElements = childrenElement.elements("topics");
            
            for (Element topicsElement : topicsElements) {
                // 获取topics下的所有topic元素
                List<Element> topicElements = topicsElement.elements("topic");
                for (Element topicElement : topicElements) {
                    children.add(parseNode(topicElement));
                }
            }
            
            if (!children.isEmpty()) {
                node.put("children", children);
                System.out.println("    子节点数: " + children.size());
            }
        }
        
        // 获取备注
        Element notesElement = element.element("notes");
        if (notesElement != null) {
            Element plainElement = notesElement.element("plain");
            if (plainElement != null) {
                node.put("note", plainElement.getTextTrim());
            }
        }
        
        // 获取标签
        Element labelsElement = element.element("labels");
        if (labelsElement != null) {
            List<String> labels = new ArrayList<>();
            List<Element> labelElements = labelsElement.elements("label");
            for (Element labelElement : labelElements) {
                labels.add(labelElement.getTextTrim());
            }
            if (!labels.isEmpty()) {
                node.put("labels", labels);
            }
        }
        
        return node;
    }

    /**
     * 统计节点数量
     */
    public int countNodes(Map<String, Object> mindmapData) {
        if (mindmapData == null) return 0;
        
        int count = 1; // 当前节点
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) mindmapData.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                count += countNodes(child);
            }
        }
        
        return count;
    }

    /**
     * 提取思维导图的描述信息
     */
    public String extractDescription(Map<String, Object> mindmapData) {
        if (mindmapData == null) return "";
        
        Object rootObj = mindmapData.get("root");
        if (rootObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) rootObj;
            String topic = (String) root.get("topic");
            if (topic != null && !topic.isEmpty()) {
                return topic.length() > 100 ? topic.substring(0, 100) + "..." : topic;
            }
        }
        
        return "思维导图";
    }
}
