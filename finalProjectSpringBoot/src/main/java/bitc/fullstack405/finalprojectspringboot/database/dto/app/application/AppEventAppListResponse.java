package bitc.fullstack405.finalprojectspringboot.database.dto.app.application;

import bitc.fullstack405.finalprojectspringboot.database.entity.EventAppEntity;
import bitc.fullstack405.finalprojectspringboot.database.entity.EventEntity;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class AppEventAppListResponse {

    // 앱 - 신청 내역 전체 목록
    private final Long eventId; // 행사 id
    private final String eventTitle; // 제목
    private final String appDate; // 신청일
    private final Character eventComp; // 수료 여부

    public AppEventAppListResponse(EventEntity event, EventAppEntity app) {
        this.eventId = event.getEventId();
        this.eventTitle = event.getEventTitle();
        this.eventComp = app.getEventComp();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        this.appDate = app.getAppDate().format(formatter);
    }
}