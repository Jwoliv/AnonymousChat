package com.example.AnonymousChat.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@ToString
@Component
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_user")
public class User implements BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private Long reputation;
    @ElementCollection
    private List<Report> reports = new ArrayList<>();
    @Transient
    private Long opponentChatId;
}
