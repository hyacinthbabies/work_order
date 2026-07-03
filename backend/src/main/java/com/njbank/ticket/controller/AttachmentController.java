package com.njbank.ticket.controller;

import com.njbank.ticket.dto.ApiResponse;
import com.njbank.ticket.entity.TicketAttachment;
import com.njbank.ticket.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {
    
    private final AttachmentService attachmentService;
    
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }
    
    @PostMapping("/upload/{ticketId}")
    public ApiResponse<TicketAttachment> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {
        TicketAttachment attachment = attachmentService.uploadAttachment(ticketId, file);
        return ApiResponse.success("上传成功", attachment);
    }
    
    @GetMapping("/ticket/{ticketId}")
    public ApiResponse<List<TicketAttachment>> getAttachmentsByTicketId(@PathVariable Long ticketId) {
        List<TicketAttachment> attachments = attachmentService.getAttachmentsByTicketId(ticketId);
        return ApiResponse.success(attachments);
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) throws IOException {
        byte[] content = attachmentService.getAttachmentContent(id);
        TicketAttachment attachment = attachmentService.getAttachmentById(id);
        
        String fileName = attachment != null ? attachment.getFileName() : "download";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(content);
    }
    
    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewAttachment(@PathVariable Long id) throws IOException {
        byte[] content = attachmentService.getAttachmentContent(id);
        TicketAttachment attachment = attachmentService.getAttachmentById(id);
        
        String contentType = attachment != null ? attachment.getContentType() : "application/octet-stream";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(content);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ApiResponse.success("删除成功", null);
    }
}