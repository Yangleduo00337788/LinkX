package com.linkx.server.module.chat.ws;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatWebSocketTicketService {

    private static final long TICKET_TTL_MILLIS = 60_000L;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TicketEntry> tickets = new ConcurrentHashMap<>();

    public String createTicket(Long userId) {
        cleanupExpiredTickets();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        tickets.put(ticket, new TicketEntry(userId, Instant.now().toEpochMilli() + TICKET_TTL_MILLIS));
        return ticket;
    }

    public Long consumeTicket(String ticket) {
        cleanupExpiredTickets();
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        TicketEntry entry = tickets.remove(ticket.trim());
        if (entry == null || entry.expireAt() < Instant.now().toEpochMilli()) {
            return null;
        }
        return entry.userId();
    }

    private void cleanupExpiredTickets() {
        long now = Instant.now().toEpochMilli();
        tickets.entrySet().removeIf(entry -> entry.getValue().expireAt() < now);
    }

    private record TicketEntry(Long userId, long expireAt) {
    }
}
