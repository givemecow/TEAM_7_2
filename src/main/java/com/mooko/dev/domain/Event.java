package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_id_seq")
    @SequenceGenerator(name = "event_id_seq", sequenceName = "event_id_seq")
    @Column(name = "event_id")
    private Long id;

    private String title;

    private String startDate;
    private String endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private List<EventPhoto> eventPhotos = new ArrayList<>();

    private LocalDateTime createdAt;

    public void updateEventName(String eventName) {
        this.title = eventName;
    }

    public void updateEventDate(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateEventPhoto(List<EventPhoto> eventPhotos) {
        this.eventPhotos = eventPhotos;
    }

}
