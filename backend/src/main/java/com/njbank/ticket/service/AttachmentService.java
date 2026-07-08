package com.njbank.ticket.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AttachmentService {
    void uploadAttachment(Long ticketId, MultipartFile file) throws IOException;
}