package com.psr.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Post {
    private Integer userId;
    private Integer id;
    private String title;
    private String body;
}
