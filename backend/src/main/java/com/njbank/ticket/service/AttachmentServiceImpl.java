package com.njbank.ticket.service;

import com.njbank.ticket.entity.Attachment;
import com.njbank.ticket.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void uploadAttachment(Long ticketId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadDir, newFilename);

        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String username = "system";
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null 
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : null;
        if (principal != null && !"anonymousUser".equals(principal.toString())) {
            username = principal.toString();
        }

        Attachment attachment = new Attachment(
                ticketId,
                originalFilename,
                filePath.toString(),
                file.getSize(),
                file.getContentType(),
                username
        );
        attachmentRepository.save(attachment);
    }
}