package com.njbank.ticket.service;

import com.njbank.ticket.entity.TicketAttachment;
import com.njbank.ticket.repository.TicketAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    
    private final TicketAttachmentRepository attachmentRepository;
    private Path uploadPath;
    
    public AttachmentServiceImpl(TicketAttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }
    
    @PostConstruct
    public void init() throws IOException {
        uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }
    
    @Override
    public TicketAttachment uploadAttachment(Long ticketId, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFilename);
        
        Files.copy(file.getInputStream(), filePath);
        
        return attachmentRepository.save(TicketAttachment.builder()
                .ticketId(ticketId)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build());
    }
    
    @Override
    public List<TicketAttachment> getAttachmentsByTicketId(Long ticketId) {
        return attachmentRepository.findByTicketId(ticketId);
    }
    
    @Override
    public TicketAttachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }
    
    @Override
    public void deleteAttachment(Long id) {
        TicketAttachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment != null) {
            try {
                Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            attachmentRepository.delete(attachment);
        }
    }
    
    @Override
    public void deleteAttachmentsByTicketId(Long ticketId) {
        List<TicketAttachment> attachments = attachmentRepository.findByTicketId(ticketId);
        for (TicketAttachment attachment : attachments) {
            try {
                Files.deleteIfExists(Paths.get(attachment.getFilePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        attachmentRepository.deleteByTicketId(ticketId);
    }
    
    @Override
    public byte[] getAttachmentContent(Long id) throws IOException {
        TicketAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
        return Files.readAllBytes(Paths.get(attachment.getFilePath()));
    }
}