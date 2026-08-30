package com.railway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastUpdate(Long trainRunId, Map<String, Object> trackingData) {
        messagingTemplate.convertAndSend("/topic/train-run/" + trainRunId, trackingData);
    }
}
