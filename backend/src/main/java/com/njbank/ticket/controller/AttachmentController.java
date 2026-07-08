package com.njbank.ticket.controller;

import com.njbank.ticket.entity.Attachment;
import com.njbank.ticket.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload/{ticketId}")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (file.isEmpty()) {
            response.put("code", 400);
            response.put("message", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, newFilename);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            String username = principal != null ? principal.getName() : "system";
            Attachment attachment = new Attachment(
                    ticketId,
                    originalFilename,
                    filePath.toString(),
                    file.getSize(),
                    file.getContentType(),
                    username
            );
            attachmentRepository.save(attachment);

            response.put("code", 200);
            response.put("message", "上传成功");
            response.put("data", Map.of(
                    "id", attachment.getId(),
                    "fileName", originalFilename,
                    "fileSize", file.getSize(),
                    "contentType", file.getContentType()
            ));
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("code", 500);
            response.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            File file = new File(attachment.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = attachment.getContentType();
            if (contentType == null) {
                contentType = Files.probeContentType(file.toPath());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            File file = new File(attachment.getFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = attachment.getContentType();
            if (contentType == null) {
                contentType = Files.probeContentType(file.toPath());
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<Map<String, Object>> getAttachmentsByTicket(@PathVariable Long ticketId) {
        Map<String, Object> response = new HashMap<>();
        List<Attachment> attachments = attachmentRepository.findByTicketId(ticketId);
        
        List<Map<String, Object>> attachmentList = attachments.stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "fileName", a.getFileName(),
                        "fileSize", a.getFileSize(),
                        "contentType", a.getContentType(),
                        "createTime", a.getCreateTime()
                ))
                .toList();

        response.put("code", 200);
        response.put("data", attachmentList);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAttachment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        
        if (attachment == null) {
            response.put("code", 404);
            response.put("message", "附件不存在");
            return ResponseEntity.notFound().build();
        }

        try {
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            attachmentRepository.delete(attachment);

            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}