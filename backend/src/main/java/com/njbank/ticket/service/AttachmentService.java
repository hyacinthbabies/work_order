package com.njbank.ticket.service;

import com.njbank.ticket.entity.TicketAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {
    TicketAttachment uploadAttachment(Long ticketId, MultipartFile file) throws IOException;
    List<TicketAttachment> getAttachmentsByTicketId(Long ticketId);
    TicketAttachment getAttachmentById(Long id);
    void deleteAttachment(Long id);
    void deleteAttachmentsByTicketId(Long ticketId);
    byte[] getAttachmentContent(Long id) throws IOException;
}