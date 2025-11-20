package com.fanfan.aiknowledgebasebackend.common.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream().findFirst().map(e -> e.getDefaultMessage()).orElse("参数错误");
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", msg);
        return m;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntime(RuntimeException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", ex.getMessage());
        return m;
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleNullPointer(NullPointerException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 401);
        m.put("message", "未登录或令牌无效，请重新登录");
        return m;
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 403);
        m.put("message", "权限不足，无法访问该资源");
        return m;
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", "文件大小超出限制，请上传小于10MB的文件");
        return m;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneral(Exception ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 500);
        m.put("message", "服务器内部错误，请稍后重试");
        m.put("details", ex.getMessage());
        return m;
    }
}
