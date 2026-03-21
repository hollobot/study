package com.hello.minio.controller;

import com.hello.minio.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "文件管理", description = "MinIO 文件上传、下载、删除、预签名 URL")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final MinioService minioService;

    @Operation(summary = "上传文件", description = "上传文件到 MinIO，返回 objectName 和 7 天有效期的预签名访问 URL")
    @ApiResponse(responseCode = "200", description = "上传成功", content = @Content(schema = @Schema(example = """
            {
              "objectName": "avatar/uuid.jpg",
              "url": "http://xxx:9000/my-bucket/avatar/uuid.jpg?..."
            }
        """)))
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
        @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
        @Parameter(description = "存储目录，默认 uploads", example = "avatar") @RequestParam(value = "dir", defaultValue = "uploads") String dir)
        throws Exception {

        String objectName = minioService.upload(file, dir);
        String url = minioService.getPresignedUrl(objectName, 7);

        Map<String, String> result = new HashMap<>();
        result.put("objectName", objectName);
        result.put("url", url);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "下载文件", description = "根据 objectName 下载文件，以附件形式返回")
    @GetMapping("/download")
    public void download(@Parameter(description = "文件路径，例如 uploads/xxx.jpg", required = true) @RequestParam("objectName") String objectName,
        HttpServletResponse response) throws Exception {
        try (InputStream is = minioService.download(objectName)) {
            String filename = objectName.substring(objectName.lastIndexOf("/") + 1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            is.transferTo(response.getOutputStream());
        }
    }

    @Operation(summary = "删除文件", description = "根据 objectName 从 MinIO 中删除文件")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(example = "删除成功")))
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(
        @Parameter(description = "文件路径，例如 uploads/xxx.jpg", required = true) @RequestParam("objectName") String objectName) throws Exception {
        minioService.delete(objectName);
        return ResponseEntity.ok("删除成功");
    }

    @Operation(summary = "获取预签名访问 URL", description = "生成指定有效期的临时访问 URL，默认 7 天")
    @ApiResponse(responseCode = "200", description = "返回预签名 URL",
        content = @Content(schema = @Schema(example = "http://xxx:9000/my-bucket/avatar/uuid.jpg?X-Amz-...")))
    @GetMapping("/presigned-url")
    public ResponseEntity<String> presignedUrl(
        @Parameter(description = "文件路径，例如 uploads/xxx.jpg", required = true) @RequestParam("objectName") String objectName,
        @Parameter(description = "URL 有效天数，默认 7 天", example = "7") @RequestParam(value = "days", defaultValue = "7") int days)
        throws Exception {
        return ResponseEntity.ok(minioService.getPresignedUrl(objectName, days));
    }
}