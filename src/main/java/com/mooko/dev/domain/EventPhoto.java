package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "event_photo")
public class EventPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_photo_id_seq")
    @SequenceGenerator(name = "event_photo_id_seq", sequenceName = "event_photo_id_seq")
    @Column(name = "event_photo_id")
    private Long id;

    private String url;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

}
