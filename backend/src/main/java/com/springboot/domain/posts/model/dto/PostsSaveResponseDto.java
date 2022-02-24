package com.springboot.domain.posts.model.dto;


import com.springboot.domain.member.model.Dto.MemberBasicInfoDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostsSaveResponseDto {

    // Posts Info
    private Long postsId;

    private String ref;

    private String content;

    private String imageUrl;

    private LocalDateTime createdDate;

    private MemberBasicInfoDto authorInfo;

    // Author Info
//    private Long authorId; //Author id
//
//    private String authorEmail; //Author email
//
//    private String authorNickname; // Author nickname
//
//    private String authorProfileUrl;
//
//    private boolean alreadyLike;
}
