package com.hello.minio.service;

import com.hello.minio.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void initBucket() throws Exception {
        // 自动创建 Bucket
        createBucketIfAbsent(minioConfig.getBucketName());

        // 设置公开读策略
        String policy = String.format("""
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }
            """, minioConfig.getBucketName());

        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioConfig.getBucketName()).config(policy).build());
    }

    // ==================== Bucket 操作 ====================

    /**
     * 创建 Bucket（不存在时才创建）
     */
    public void createBucketIfAbsent(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    // ==================== 文件上传 ====================

    /**
     * 上传 MultipartFile，返回文件在 MinIO 中的 objectName
     */
    public String upload(MultipartFile file, String directory) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String ext =
            (originalFilename != null && originalFilename.contains(".")) ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        // 生成唯一文件名，避免覆盖
        String objectName = directory + "/" + UUID.randomUUID() + ext;

        minioClient.putObject(
            PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType()).build());

        return objectName;
    }

    /**
     * 上传输入流
     */
    public void uploadStream(String objectName, InputStream inputStream, long size, String contentType) throws Exception {
        minioClient.putObject(
            PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).stream(inputStream, size, -1).contentType(contentType)
                .build());
    }

    // ==================== 文件下载 ====================

    /**
     * 获取文件输入流（供下载使用）
     */
    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
    }

    // ==================== 预签名 URL ====================

    /**
     * 生成预签名下载 URL
     * **7 天有效期是什么意思：**
     *
     * MinIO 默认文件是私有的，不能直接通过 URL 访问。预签名 URL 相当于生成一个**带有临时授权信息的链接**，在有效期内任何人拿到这个链接都可以直接访问该文件，过期后链接自动失效。
     * ```
     * # 生成的 URL 大概长这样，里面包含签名和过期时间
     * http://192.168.1.100:9000/test/avatar/uuid.jpg
     *     ?X-Amz-Algorithm=AWS4-HMAC-SHA256
     *     &X-Amz-Credential=minioadmin/20241201/us-east-1/s3/aws4_request
     *     &X-Amz-Date=20241201T000000Z
     *     &X-Amz-Expires=604800      # 604800 秒 = 7天
     *     &X-Amz-SignedHeaders=host
     *     &X-Amz-Signature=xxxxxxxx  # 签名，篡改任何参数都会失效
     *
     */
    public String getPresignedUrl(String objectName, int expireDays) throws Exception {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(minioConfig.getBucketName()).object(objectName)
                .expiry(expireDays, TimeUnit.DAYS).build());
    }



    // ==================== 文件删除 ====================

    /**
     * 删除单个文件
     */
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
    }

    // ==================== 文件查询 ====================

    /**
     * 列出指定前缀下的所有文件名
     */
    public List<String> listObjects(String prefix) throws Exception {
        List<String> names = new ArrayList<>();
        Iterable<Result<Item>> results =
            minioClient.listObjects(ListObjectsArgs.builder().bucket(minioConfig.getBucketName()).prefix(prefix).recursive(true).build());
        for (Result<Item> result : results) {
            names.add(result.get().objectName());
        }
        return names;
    }

    /**
     * 判断文件是否存在
     */
    public boolean exists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}