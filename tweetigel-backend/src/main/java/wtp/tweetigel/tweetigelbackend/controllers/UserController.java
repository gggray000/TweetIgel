package wtp.tweetigel.tweetigelbackend.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import wtp.tweetigel.tweetigelbackend.dtos.*;
import wtp.tweetigel.tweetigelbackend.entities.User;
import wtp.tweetigel.tweetigelbackend.services.AuthService;
import wtp.tweetigel.tweetigelbackend.services.UserService;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public UserController(UserService userService, AuthService authService){
        this.userService = userService;
        this.authService = authService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(
            value="/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserInfoConfirmDto registerNewUser(@RequestBody UserCreateDto userCreateDto){
        return userService.register(userCreateDto);
    }

    @SecurityRequirement(name = "basicAuth")
    @PostMapping(value="/user/login")
    public UserInfoConfirmDto login(HttpServletRequest request){
        User user = authService.logIn(request);
        return userService.getUserInfoConfirmDto(user.getUsername());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/user/logout")
    public void logOut(HttpServletRequest request) {
        authService.logOut(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(
            value="/user/follow",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void follow(HttpServletRequest request,
                       @RequestBody UsernameDto usernameDto){
        userService.follow(request, usernameDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(
            value="/user/unfollow",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void unfollow(HttpServletRequest request,
                         @RequestBody UsernameDto usernameDto){
        userService.unfollow(request, usernameDto);
    }

    @GetMapping(
            value="/user/followedList",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<UsernameDto> getFollowedList(@RequestBody UsernameDto usernameDto){
        return userService.getFollowedList(usernameDto);
    }

    @GetMapping(
            value="/user/followers",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<UsernameDto> getFollowers(@RequestBody UsernameDto usernameDto){
        return userService.getFollowers(usernameDto);
    }

    @GetMapping(
            value="/user/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<UserSearchResultDto> searchUser(HttpServletRequest request,
                                                @RequestParam("term") String term){
        return userService.searchUsers(request, term);
    }

    // Need to be changed to use session
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(
            value="/password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserInfoConfirmDto changePassword(@RequestBody UserCreateDto userCreateDto){
        return userService.changePassword(userCreateDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            value="/profile/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserProfileDto getUserProfile(HttpServletRequest request,
                                         @PathVariable("username") String username){
        return userService.getUserProfile(request, username);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(
            value="/profile",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public UserProfileDto updateUserProfile(HttpServletRequest request,
                                            @RequestBody UserProfileUpdateDto userProfileUpdateDto){
        return userService.updateUserProfile(request, userProfileUpdateDto);
    }

}

