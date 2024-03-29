package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final EventService eventService;

    @Autowired
    public UserController(UserService userService, EventService eventService) {
        this.userService = userService;
        this.eventService = eventService;
    }

    @GetMapping
    public List<User> getUser() {
        return userService.getUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public User deleteUser(@PathVariable long userId) {
        return userService.deleteUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addToFriends(@PathVariable long id,
                             @PathVariable long friendId) {
        return userService.addToFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFromFriends(@PathVariable long id,
                                  @PathVariable long friendId) {
        return userService.deleteFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsListForUser(@PathVariable long id) {
        return userService.getFriendsListForUser(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getMutualFriends(@PathVariable long id,
                                       @PathVariable long otherId) {
        return userService.getMutualFriends(id, otherId);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}/feed")
    public Collection<Event> getFeed(@PathVariable long id) {
        return eventService.getEventOfUser(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendationsByUserId(@PathVariable long id) {
        return userService.getRecommendationsByUserId(id);
    }
}
