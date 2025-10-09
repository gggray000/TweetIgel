package wtp.tweetigel.tweetigelbackend.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wtp.tweetigel.tweetigelbackend.dtos.CommentDto;
import wtp.tweetigel.tweetigelbackend.dtos.PostDto;
import wtp.tweetigel.tweetigelbackend.entities.Comment;
import wtp.tweetigel.tweetigelbackend.entities.HashTag;
import wtp.tweetigel.tweetigelbackend.entities.Post;
import wtp.tweetigel.tweetigelbackend.entities.User;
import wtp.tweetigel.tweetigelbackend.exceptions.ClientErrors;
import wtp.tweetigel.tweetigelbackend.repositories.CommentRepository;
import wtp.tweetigel.tweetigelbackend.repositories.HashTagRepository;
import wtp.tweetigel.tweetigelbackend.repositories.PostRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final HashTagRepository hashTagRepository;
    private final AuthService authService;
    private final UserService userService;
    private final DateTimeFormatter timeFormatter;


    @Autowired
    public PostService(PostRepository postRepository,
                       CommentRepository commentRepository,
                       HashTagRepository hashTagRepository,
                       AuthService authService,
                       UserService userService){
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.hashTagRepository = hashTagRepository;
        this.authService = authService;
        this.userService = userService;
        this.timeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.systemDefault());
    }

    private PostDto toDto(Post post, User user){
        return new PostDto(
                post.getId(),
                post.getContent(),
                userService.toUsernameDto(post.getAuthor()),
                timeFormatter.format(post.getTimestamp()),
                post.getLikedList().size(),
                post.getLikedList().contains(user)? Boolean.FALSE
                        : post.getAuthor().equals(user)? null
                            : Boolean.TRUE,
                post.getComments().size()
        );
    }

    private CommentDto toCommentDto(Comment comment){
        return new CommentDto(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                timeFormatter.format(comment.getTimestamp())
        );
    }

    public void createPost(HttpServletRequest request, String content){
        User user = authService.getAuthenticatedUser(request);
        Post newPost = new Post(content, user);
        extractAndSaveHashtags(newPost);
        postRepository.save(newPost);
    }

    // Reference: https://regex101.com/codegen?language=java
    public void extractAndSaveHashtags(Post post){

        final String regex = "#(\\w+)";
        final String string = post.getContent();

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            System.out.println("Match: " + matcher.group(0));
            savePostToHashTag(post, matcher.group(1));
        }
    }

    public void savePostToHashTag(Post post, String hashtag){
       if(hashTagRepository.existsHashTagByName(hashtag)){
           HashTag hashTag = hashTagRepository.findHashTagByName(hashtag).orElseThrow();
           hashTag.getPosts().add(post);
           hashTagRepository.save(hashTag);
           post.getHashtags().add(hashTag);
       } else {
           HashTag newHashTag =  new HashTag(hashtag);
           newHashTag.getPosts().add(post);
           hashTagRepository.save(newHashTag);
           post.getHashtags().add(newHashTag);
       }
    }

    @Transactional // Without this, "TransactionRequiredException" will be thrown.
    public void deletePost(HttpServletRequest request, Long postId){
        User user = authService.getAuthenticatedUser(request);
        for (Post post: user.getPosts()){
            if(Objects.equals(post.getId(), postId)){
                postRepository.deletePostById(postId);
                user.getPosts().remove(post);
                return;
            }
        }
        throw ClientErrors.invalidDeleteRequest();
    }

    public void likePost(User user, long id) {
        Post post = postRepository.findById(id).orElseThrow(ClientErrors::postNotFound);
        if(post.getAuthor().equals(user) || post.getLikedList().contains(user)){
            throw ClientErrors.notLikable();
        }
        post.getLikedList().add(user);
        postRepository.save(post);
    }

    public void unlikePost(User user, long id) {
        Post post = postRepository.findById(id).orElseThrow(ClientErrors::postNotFound);
        if(post.getAuthor().equals(user) || !post.getLikedList().contains(user)){
            throw ClientErrors.notUnlikable();
        }
        post.getLikedList().remove(user);
        postRepository.save(post);
    }

    @Transactional
    public List<PostDto> getPostsForProfile(HttpServletRequest request, String username, int num){
        User user = authService.getAuthenticatedUser(request);
        User toBeViewed = userService.getUser(username);
        Pageable mostRecentTwentyPosts = PageRequest.of(num,20);
        Page<Post> postsPage = postRepository.findPostsByAuthorOrderByTimestampDesc(toBeViewed, mostRecentTwentyPosts);
        return postsPage
                .get()
                .map(post -> this.toDto(post, user))
                .toList();
    }

    public int getPostsCountForProfile(String username){
        User toBeViewed = userService.getUser(username);
        return postRepository.countPostByAuthor(toBeViewed);
    }

    // Reference: https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html
    @Transactional
    public List<PostDto> getPostsFeed(HttpServletRequest request, int num){
        User user = authService.getAuthenticatedUser(request);
        List<User> usersInFeed = new ArrayList<>(user.getFollowed());
        usersInFeed.add(user);
        Pageable pageWithTwentyPosts = PageRequest.of(num, 20, Sort.by("timestamp").descending());
        Page<Post> postPage = postRepository.findPostByAuthorIsIn(usersInFeed, pageWithTwentyPosts);
        return postPage
                .get()
                .map(post -> this.toDto(post, user))
                .toList();
    }

    public int getPostsCountForFeed(HttpServletRequest request){
        User user = authService.getAuthenticatedUser(request);
        Set<User> authors = new HashSet<>(user.getFollowed());
        authors.add(user);
        return postRepository.countPostByAuthorIsIn(authors);
    }

    @Transactional
    public void addComment(long postId, HttpServletRequest request, String content){
        Post post = postRepository.findById(postId).orElseThrow(ClientErrors::postNotFound);
        User user = authService.getAuthenticatedUser(request);
        Comment comment = new Comment(post, user, content);
        commentRepository.save(comment);
        post.getComments().add(comment);
        postRepository.save(post);
    }

    @Transactional
    public List<CommentDto> getCommentsOfPost(long postId){
        Post post = postRepository.findById(postId).orElseThrow(ClientErrors::postNotFound);
        return post.getComments().stream()
                .sorted(Comparator.comparing(Comment::getTimestamp).reversed())
                .map(this::toCommentDto)
                .toList();
    }

    @Transactional
    public List<PostDto> getHashTagPosts(HttpServletRequest request, String hashtagName, int num){
        User user = authService.getAuthenticatedUser(request);
        HashTag hashTag = hashTagRepository.findHashTagByName(hashtagName).orElseThrow(ClientErrors::hashtagNotFound);
        Pageable pageWithTwentyPosts = PageRequest.of(num, 20, Sort.by("timestamp").descending());
        Page<Post> postPage = postRepository.findPostsByHashtagsContains(hashTag, pageWithTwentyPosts);
        return postPage
                .get()
                .map(post -> this.toDto(post, user))
                .toList();
    }

    public int getPostsCountForHashtags(String hashtagName){
        HashTag hashTag = hashTagRepository.findHashTagByName(hashtagName).orElseThrow(ClientErrors::hashtagNotFound);
        return postRepository.countPostByHashtagsContains(hashTag);
    }

    @Transactional
    public List<PostDto> searchPosts(HttpServletRequest request, String term, int num){
        User user = authService.getAuthenticatedUser(request);
        Pageable pageWithTwentyPosts = PageRequest.of(num, 20, Sort.by("timestamp").descending());
        Page<Post> postPage = postRepository.findPostsByContentContainingIgnoreCase(term, pageWithTwentyPosts);
        return postPage
                .get()
                .map(post -> this.toDto(post, user))
                .toList();
    }

    public int getPostsCountForSearch(String term){
        return postRepository.countPostByContentContains(term);
    }
}
