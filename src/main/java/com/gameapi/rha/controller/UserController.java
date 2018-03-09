package com.gameapi.rha.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameapi.rha.models.Message;
import com.gameapi.rha.models.User;
import com.gameapi.rha.services.UserService;
//import jdk.incubator.http.HttpResponse;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.io.IOException;

@RestController
@CrossOrigin(origins = {"*", "http://bf-balance.herokuapp.com", "localhost:3000"})
@RequestMapping("/users")
public class UserController {

    static ObjectMapper mapper = new ObjectMapper();

    private enum UserStatus {
        SUCCESSFULLY_REGISTERED,
        SUCCESSFULLY_AUTHED,
        SUCCESSFULLY_LOGGED_OUT,
        SUCCESSFULLY_CHANGED,
        ACCESS_ERROR,
        WRONG_CREDENTIALS,
        NOT_UNIQUE_USERNAME,
        ALREADY_AUTHENTICATED,
        UNEXPECTED_ERROR
    }

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity create(@RequestBody User user, HttpSession session) throws JsonProcessingException {

        // Аутентифицированный пользователь не может зарегистрироваться
        if (session.getAttribute("user") != null)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ALREADY_AUTHENTICATED));


        if (UserService.putInMap(user) != null) {
            user.saltHash();
            sessionAuth(session, user);
            return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_REGISTERED));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.NOT_UNIQUE_USERNAME));
        }
    }

    @PostMapping(path = "/auth")
    public ResponseEntity auth(@RequestBody User user, HttpSession session, HttpServletResponse response)  {
        ResponseEntity respond;
        // Мы не можем дважды аутентицифироваться
        if (session.getAttribute("user") != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ALREADY_AUTHENTICATED));
        }

        // Если неверные учетные данные
        if (!UserService.check(user.getUsername(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.WRONG_CREDENTIALS));
        }

        sessionAuth(session, user);
        Cookie userCook = new Cookie("user", user.getUsername());
        userCook.setMaxAge(30*60);
        response.addCookie(userCook);
        try {
        response.sendRedirect("/info");
    } catch (IOException ex){
        return null;
    }
        return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_AUTHED));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity logout(HttpSession session) {

        // Мы не можем выйти, не войдя
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ACCESS_ERROR));
        }

        session.setAttribute("user", null);
        session.invalidate();
        return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_LOGGED_OUT));
    }

    @GetMapping(path = "/info")
    public ResponseEntity info(HttpSession session) {

        // Если пользователь не аутертифицирован, то у него нет доступа к информации о текущей сессии
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(UserStatus.ACCESS_ERROR));
        }

        final User result = UserService.userInfo((String) session.getAttribute("user"));
        if (result == null) {
            // Этого быть не может
            return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.UNEXPECTED_ERROR));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Message(result));
    }

    @PostMapping(path = "/change")
    public ResponseEntity change(@RequestBody User user, HttpSession session) {

        // Без аутентификации нет доступа к изменению данных
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(UserStatus.ACCESS_ERROR));
        }

        UserService.changeUser((String) session.getAttribute("user"), user);

        return ResponseEntity.status(HttpStatus.OK).body(new Message(UserStatus.SUCCESSFULLY_CHANGED));
    }


    private static void sessionAuth(HttpSession session, User user)
    {
        session.setAttribute("user", user.getUsername());
        session.setMaxInactiveInterval(30*60);
    }
}
