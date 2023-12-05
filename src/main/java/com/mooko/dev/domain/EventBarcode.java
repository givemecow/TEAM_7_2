package com.mooko.dev.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "event_barcode")
public class EventBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_barcode_id_seq")
    @SequenceGenerator(name = "event_barcode_idseq", sequenceName = "event_barcode_id_seq")
    @Column(name = "event_barcode_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barcode_id")
    private Barcode barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_photo_id")
    private EventBarcode eventBarcode;

}