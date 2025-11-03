package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_type_mstr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTypeMstr {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "event_type_id")
  private Byte eventTypeID;

  @Column(nullable = false, unique = true)
  private Byte eventTypeCd;

  @Column(nullable = false, length = 40)
  private String eventTypeMne;

  @Column(length = 254)
  private String eventTypeDesc;
}
