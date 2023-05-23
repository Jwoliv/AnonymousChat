package com.example.AnonymousChat.model.user;

import com.example.AnonymousChat.model.BaseEntity;
import com.example.AnonymousChat.util.AesCrypt;
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
    @ElementCollection()
    private List<Report> reports = new ArrayList<>();
    private String opponentChatId;
    private String previousChatId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> blockUsers = new ArrayList<>();


    public Long decryptOpponentChatId() {
        var strOpponentChatId = this.getOpponentChatId();
        if (strOpponentChatId != null) {
            return Long.parseLong(AesCrypt.decrypt(strOpponentChatId));
        }
        return null;
    }

    public Long decryptPreviousChatId() {
        var previousChatId = this.getPreviousChatId();
        if (previousChatId != null) {
            return Long.parseLong(AesCrypt.decrypt(previousChatId));
        }
        return null;
    }

    public String encryptOwnChatId() {
        var chatId = this.getChatId();
        return AesCrypt.encrypt(String.valueOf(chatId));
    }
}
