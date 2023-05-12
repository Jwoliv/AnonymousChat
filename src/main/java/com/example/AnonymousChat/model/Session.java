package com.example.AnonymousChat.model;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@ToString
@Component
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Session implements BaseEntity {
    private User firstUser;
    private User secondUser;
}
