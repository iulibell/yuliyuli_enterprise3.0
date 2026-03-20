package com.yuliyuli.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 转换工具类 */
@Component
@Slf4j
public class TransferUtil {

  public String uploadVideoCover(MultipartFile file, String uploadPath) throws IOException {
    // 检查文件是否为空
    if (file.isEmpty()) {
      return "上传文件不能为空";
    }

    // 创建存储目录（如果不存在）
    Path uploadDir = Paths.get(uploadPath);
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    // 生成唯一文件名，避免文件名冲突
    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

    // 保存文件到指定路径
    Path filePath = uploadDir.resolve(uniqueFilename);
    try (var inputStream = file.getInputStream()) {
      Files.copy(inputStream, filePath);
    }

    // 返回/static/coverUrl/xxx.jpg格式的路径，用于数据库存储
    return "/static/coverUrl/" + uniqueFilename;
  }

  // 上传视频处理
  public String uploadVideo(MultipartFile file, String uploadPath) throws IOException {
    // 检查文件是否为空
    if (file.isEmpty()) {
      return "上传文件不能为空";
    }

    // 创建存储目录（如果不存在）
    Path uploadDir = Paths.get(uploadPath);
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    // 生成唯一文件名，避免文件名冲突
    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

    // 保存文件到指定路径
    Path filePath = uploadDir.resolve(uniqueFilename);
    try (var inputStream = file.getInputStream()) {
      Files.copy(inputStream, filePath);
    }

    // 返回/static/videoUrl/xxx.mp4格式的路径，用于数据库存储
    return "/static/videoUrl/" + uniqueFilename;
  }

  // 上传头像文件处理
  public String uploadAvatar(MultipartFile file, String uploadPath) throws IOException {
    // 检查文件是否为空
    if (file.isEmpty()) {
      return "上传文件不能为空";
    }

    // 创建存储目录（如果不存在）
    Path uploadDir = Paths.get(uploadPath);
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    // 生成唯一文件名，避免文件名冲突
    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

    // 保存文件到指定路径
    Path filePath = uploadDir.resolve(uniqueFilename);
    try (var inputStream = file.getInputStream()) {
      Files.copy(inputStream, filePath);
    }

    // 返回/static/avatarUrl/xxx.jpg格式的路径，用于数据库存储
    return "/static/avatarUrl/" + uniqueFilename;
  }
}
