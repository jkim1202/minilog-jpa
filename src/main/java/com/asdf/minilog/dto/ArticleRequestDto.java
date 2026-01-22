package com.asdf.minilog.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ArticleRequestDto {
  @NonNull private String content;
  @NonNull private Long authorId;
}
