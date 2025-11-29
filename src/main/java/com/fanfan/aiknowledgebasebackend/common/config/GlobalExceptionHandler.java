/**
 * 全局异常处理器
 * 用于统一处理应用程序中的各种异常
 * 包括参数验证异常、运行时异常、安全异常等
 * 返回格式统一的错误响应给前端
 */
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

    /**
     * 处理参数验证异常
     * 当请求参数不符合验证规则时触发
     * @param ex MethodArgumentNotValidException异常实例
     * @return 包含错误信息的Map对象
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream().findFirst().map(e -> e.getDefaultMessage()).orElse("参数错误");
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", msg);
        return m;
    }

    /**
     * 处理运行时异常
     * 当程序中抛出RuntimeException及其子类异常时触发
     * @param ex RuntimeException异常实例
     * @return 包含错误信息的Map对象
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntime(RuntimeException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", ex.getMessage());
        return m;
    }

    /**
     * 处理空指针异常
     * 通常是由于未登录或token无效导致
     * @param ex NullPointerException异常实例
     * @return 包含错误信息的Map对象
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleNullPointer(NullPointerException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 401);
        m.put("message", "未登录或令牌无效，请重新登录");
        return m;
    }
    
    /**
     * 处理访问拒绝异常
     * 当用户尝试访问没有权限的资源时触发
     * @param ex AccessDeniedException异常实例
     * @return 包含错误信息的Map对象
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 403);
        m.put("message", "权限不足，无法访问该资源");
        return m;
    }
    
    /**
     * 处理文件上传大小超限异常
     * 当上传的文件超过最大限制时触发
     * @param ex MaxUploadSizeExceededException异常实例
     * @return 包含错误信息的Map对象
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", 400);
        m.put("message", "文件大小超出限制，请上传小于10MB的文件");
        return m;
    }
    
    /**
     * 处理通用异常
     * 处理其他未被专门捕获的异常
     * @param ex Exception异常实例
     * @return 包含错误信息的Map对象
     */
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