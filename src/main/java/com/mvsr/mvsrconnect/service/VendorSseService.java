package com.mvsr.mvsrconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class VendorSseService {

    private final Map<Long, List<SseEmitter>> vendorEmitters = new ConcurrentHashMap<>();
    private final Map<Long, List<SseEmitter>> orderEmitters = new ConcurrentHashMap<>();

    // Vendor dashboard subscribes here
    public SseEmitter registerVendor(Long stallId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        vendorEmitters.computeIfAbsent(stallId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeVendor(stallId, emitter));
        emitter.onTimeout(() -> removeVendor(stallId, emitter));
        return emitter;
    }

    // Student order status page subscribes here
    public SseEmitter registerOrder(Long orderId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        orderEmitters.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeOrder(orderId, emitter));
        emitter.onTimeout(() -> removeOrder(orderId, emitter));
        return emitter;
    }

    // Called when payment confirmed — pushes to vendor dashboard
    public void pushOrderToVendor(Long stallId, Object orderData) {
        List<SseEmitter> list = vendorEmitters.getOrDefault(stallId, List.of());
        list.removeIf(e -> {
            try {
                e.send(SseEmitter.event().name("new-order").data(orderData));
                return false;
            } catch (Exception ex) {
                return true;
            }
        });
    }

    // Called when vendor updates order status — pushes to student
    public void pushStatusToStudent(Long orderId, String status) {
        List<SseEmitter> list = orderEmitters.getOrDefault(orderId, List.of());
        list.removeIf(e -> {
            try {
                e.send(SseEmitter.event().name("status").data(status));
                if ("READY".equals(status) || "CANCELLED".equals(status)) {
                    e.complete();
                    return true;
                }
                return false;
            } catch (Exception ex) {
                return true;
            }
        });
    }

    private void removeVendor(Long stallId, SseEmitter emitter) {
        List<SseEmitter> list = vendorEmitters.get(stallId);
        if (list != null) list.remove(emitter);
    }

    private void removeOrder(Long orderId, SseEmitter emitter) {
        List<SseEmitter> list = orderEmitters.get(orderId);
        if (list != null) list.remove(emitter);
    }
}
