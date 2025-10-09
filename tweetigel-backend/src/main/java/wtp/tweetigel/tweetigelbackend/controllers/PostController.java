package wtp.tweetigel.tweetigelbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.annotations.Comments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import wtp.tweetigel.tweetigelbackend.dtos.*;
import wtp.tweetigel.tweetigelbackend.entities.User;
import wtp.tweetigel.tweetigelbackend.services.AuthService;
import wtp.tweetigel.tweetigelbackend.services.PostService;

import java.util.List;

@RestController
public class PostController {

    private PostService postService;
    private AuthService authService;

    @Autowired
    public PostController(PostService postService, AuthService authService){
        this.postService = postService;
        this.authService = authService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/post",
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createPost(HttpServletRequest request,
                           @RequestBody PostCreateDto postCreateDto){
        this.postService.createPost(request, postCreateDto.content());
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/post/{id}")
    public void deletePost(HttpServletRequest request,
                           @PathVariable("id") Long postId){
        this.postService.deletePost(request, postId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/post/{id}/like")
    public void likePost(HttpServletRequest request,
                         @PathVariable("id") long postId){
        User user = authService.getAuthenticatedUser(request);
        postService.likePost(user, postId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/post/{id}/unlike")
    public void unlikePost(HttpServletRequest request,
                         @PathVariable("id") long postId){
        User user = authService.getAuthenticatedUser(request);
        postService.unlikePost(user, postId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/posts/{author}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostDto> getPostsForProfile(HttpServletRequest request,
                                      @PathVariable("author") String username,
                                      @RequestParam("page") int num){
        return postService.getPostsForProfile(request, username, num);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/postsCount/{user}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public int getPostsCountForProfile(@PathVariable("user") String username){
        return postService.getPostsCountForProfile(username);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/posts",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostDto> getPostsFeed(HttpServletRequest request,
                                      @RequestParam("page") int num){
        return postService.getPostsFeed(request, num);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/postsCount",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public int getPostsCountForFeed(HttpServletRequest request){
        return postService.getPostsCountForFeed(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/post/{id}/comment",
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addComment(@PathVariable("id") Long postId,
                            HttpServletRequest request,
                           @RequestBody CommentCreateDto commentCreateDto){
        postService.addComment(postId, request, commentCreateDto.content());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/post/{id}/comments")
    public List<CommentDto> getCommentsOfPost(@PathVariable("id") Long postId){
        return postService.getCommentsOfPost(postId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/hashtag/{name}")
    public List<PostDto> getHashTag(HttpServletRequest request,
                                    @PathVariable("name") String hashtagName,
                                    @RequestParam("page") int num){
        return postService.getHashTagPosts(request, hashtagName, num);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/hashtag/{name}/postsCount",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public int getPostsCountForFeed(@PathVariable("name") String hashtagName){
        return postService.getPostsCountForHashtags(hashtagName);
    }

    @GetMapping(
            value="/post/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<PostDto> searchPosts(HttpServletRequest request,
                                     @RequestParam("term") String term,
                                     @RequestParam("page") int num){
        return postService.searchPosts(request, term, num);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/post/search/postCount",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public int getPostsCountForSearch(@RequestParam("term") String term){
        return postService.getPostsCountForSearch(term);
    }

}
