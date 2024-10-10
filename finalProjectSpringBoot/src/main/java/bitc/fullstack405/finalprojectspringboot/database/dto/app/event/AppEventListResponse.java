package bitc.fullstack405.finalprojectspringboot.database.dto.app.event;

import bitc.fullstack405.finalprojectspringboot.database.entity.EventEntity;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AppEventListResponse {

    // 앱 - 행사 목록
    private final Long eventId; // 행사 id
    private final String eventTitle; // 제목
    private final String visibleDate; // 게시일
    private final Character isRegistrationOpen; // 신청 마감 여부

    public AppEventListResponse(EventEntity event) {
        this.eventId = event.getEventId();
        this.eventTitle = event.getEventTitle();
        this.isRegistrationOpen = event.getIsRegistrationOpen();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        this.visibleDate = event.getVisibleDate().format(formatter);
    }
}
