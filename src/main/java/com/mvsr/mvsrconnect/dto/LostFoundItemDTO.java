package com.mvsr.mvsrconnect.dto;

import com.mvsr.mvsrconnect.model.LostFoundItem;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class LostFoundItemDTO {

    private final Long id;
    private final String type;
    private final String title;
    private final String description;
    private final String location;
    private final String category;
    private final String date;
    private final String authorName;
    private final Long authorId;
    private final boolean resolved;
    private final LocalDateTime createdAt;
    private final long responseCount;
    private final String mediaUrl;
    private final String mediaType;
    private final String mediaPublicId;

    private LostFoundItemDTO(LostFoundItem item, long responseCount) {
        this.id            = item.getId();
        this.type          = item.getType().name();
        this.title         = item.getTitle();
        this.description   = item.getDescription();
        this.location      = item.getLocation();
        this.category      = item.getCategory();
        this.date          = item.getDate();
        this.authorName    = item.getAuthorName();
        this.authorId      = item.getAuthorId();
        this.resolved      = item.isResolved();
        this.createdAt     = item.getCreatedAt();
        this.responseCount = responseCount;
        this.mediaUrl      = item.getMediaUrl();
        this.mediaType     = item.getMediaType();
        this.mediaPublicId = item.getMediaPublicId();
    }

    public static LostFoundItemDTO from(LostFoundItem item, long responseCount) {
        return new LostFoundItemDTO(item, responseCount);
    }
}