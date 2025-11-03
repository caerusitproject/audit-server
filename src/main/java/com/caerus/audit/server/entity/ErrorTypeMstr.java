package com.caerus.audit.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "error_type_mstr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorTypeMstr {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "error_type_id")
  private Byte errorTypeID;

  @Column(nullable = false, unique = true)
  private Byte errorTypeCd;

  @Column(nullable = false, length = 40)
  private String errorTypeMne;

  @Column(length = 254)
  private String errorTypeDesc;
}
