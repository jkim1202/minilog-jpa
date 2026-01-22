package com.asdf.minilog.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.asdf.minilog.dto.ArticleRequestDto;
import com.asdf.minilog.dto.ArticleResponseDto;
import com.asdf.minilog.entity.Article;
import com.asdf.minilog.entity.Follow;
import com.asdf.minilog.entity.User;
import com.asdf.minilog.repository.ArticleRepository;
import com.asdf.minilog.repository.FollowRepository;
import com.asdf.minilog.repository.UserRepository;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
// import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@ExtendWith(SpringExtension.class)
class ArticleServiceTest {
  @Container
  static final MySQLContainer mysqlContainer =
      new MySQLContainer(DockerImageName.parse("mysql:9"))
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Autowired ArticleRepository articleRepository;
  @Autowired UserRepository userRepository;
  @Autowired FollowRepository followRepository;

  User user1;
  Article article1;
  Follow follow1;
  ArticleService articleService;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
  }

  @BeforeEach
  @Transactional
  void setUp() {
    followRepository.deleteAll();
    articleRepository.deleteAll();
    userRepository.deleteAll();

    articleService = new ArticleService(articleRepository, userRepository);

    user1 = userRepository.save(User.builder().username("user1").password("user1").build());
    User user2 = userRepository.save(User.builder().username("user2").password("user2").build());

    article1 =
        articleRepository.save(
            Article.builder()
                .content("test article1")
                .author(userRepository.findById(user1.getId()).orElseThrow())
                .build());
    articleRepository.save(
        Article.builder()
            .content("test article2")
            .author(userRepository.findById(user2.getId()).orElseThrow())
            .build());

    follow1 = Follow.builder().follower(user2).followee(user1).build();
    followRepository.save(follow1);
  }

  @Test
  @Transactional
  void testCreateArticle() {
    ArticleRequestDto requestDto =
        ArticleRequestDto.builder().content("test3").authorId(user1.getId()).build();

    ArticleResponseDto article = articleService.createArticle(requestDto);

    assertThat(article.getContent()).isEqualTo("test3");
    assertThat(article.getAuthorId()).isEqualTo(user1.getId());
    assertThat(articleRepository.findAll()).hasSize(3);
  }

  @Test
  @Transactional
  void testGetArticleById() {
    ArticleResponseDto article = articleService.getArticleById(article1.getId());

    assertThat(article.getArticleId()).isEqualTo(article1.getId());
    assertThat(article.getContent()).isEqualTo(article1.getContent());
    assertThat(article.getAuthorId()).isEqualTo(article1.getAuthor().getId());
    assertThat(article.getAuthorName()).isEqualTo(article1.getAuthor().getUsername());
    assertThat(dateTimeFormatter.format(article.getCreatedAt()))
        .isEqualTo(dateTimeFormatter.format(article1.getCreatedAt()));
  }

  @Test
  @Transactional
  void testGetArticleListByUserId() {
    ArticleResponseDto article = articleService.getArticleListByUserId(user1.getId()).getFirst();

    assertThat(article.getArticleId()).isEqualTo(article1.getId());
    assertThat(article.getContent()).isEqualTo(article1.getContent());
    assertThat(article.getAuthorId()).isEqualTo(article1.getId());
    assertThat(article.getAuthorName()).isEqualTo(article1.getAuthor().getUsername());
    assertThat(dateTimeFormatter.format(article.getCreatedAt()))
        .isEqualTo(dateTimeFormatter.format(article1.getCreatedAt()));
  }

  @Test
  @Transactional
  void testGetFeedListByFollowerId() {
    ArticleResponseDto article =
        articleService.getFeedListByFollowerId(follow1.getFollower().getId()).getFirst();
    var target = articleService.getArticleListByUserId(article.getAuthorId()).getFirst();

    assertThat(article.getArticleId()).isEqualTo(target.getArticleId());
    assertThat(article.getContent()).isEqualTo(target.getContent());
    assertThat(article.getAuthorId()).isEqualTo(target.getAuthorId());
    assertThat(article.getAuthorName()).isEqualTo(target.getAuthorName());
    assertThat(dateTimeFormatter.format(article.getCreatedAt()))
        .isEqualTo(dateTimeFormatter.format(target.getCreatedAt()));
  }

  @Test
  @Transactional
  void testDeleteArticle() {
    assertThat(articleRepository.findAll()).hasSize(2);
    Long articleId = article1.getId(); // Adjusted to match the method signature
    articleService.deleteArticle(articleId);

    assertThat(articleRepository.findAll()).hasSize(1);
  }

  @Test
  @Transactional
  void testUpdateArticle() {
    Long articleId = article1.getId(); // Adjusted to match the method signature
    ArticleResponseDto article = articleService.updateArticle(articleId, "updated article 1");

    assertThat(article.getArticleId()).isEqualTo(article1.getId());
    assertThat(article.getContent()).isEqualTo("updated article 1");
    assertThat(article.getAuthorId()).isEqualTo(article1.getAuthor().getId());
    assertThat(article.getAuthorName()).isEqualTo(article1.getAuthor().getUsername());
    assertThat(dateTimeFormatter.format(article.getCreatedAt()))
        .isEqualTo(dateTimeFormatter.format(article1.getCreatedAt()));
  }
}
