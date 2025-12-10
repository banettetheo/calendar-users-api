package com.calendar.users.application;

import com.calendar.users.domain.models.BusinessUser;
import com.calendar.users.domain.models.UserWithStatusDTO;
import com.calendar.users.domain.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<UserWithStatusDTO> readUsersWithRelationshipStatus(@AuthenticationPrincipal Jwt jwt) {
        return userService.readUsersWithRelationshipStatus(jwt);
    }

    @GetMapping("me")
    public Mono<ResponseEntity<BusinessUser>> readProfile(
            @AuthenticationPrincipal Jwt jwt) {
        return userService.readProfile(jwt).map(ResponseEntity::ok);
    }

//    @GetMapping("{id}/friends")
//    public Flux<ResponseEntity<BusinessUser>> readFriends(
//            @RequestParam Long id
//    ) {
//        return userService.
//    }

    @PostMapping("me/profilePicture")
    public Mono<ResponseEntity<String>> updateProfilePicture(
            @AuthenticationPrincipal Jwt jwt, @RequestPart("file") Mono<FilePart> filePartMono) {
        return userService.updateProfilePicture(jwt, filePartMono).map(ResponseEntity::ok);
    }

}
