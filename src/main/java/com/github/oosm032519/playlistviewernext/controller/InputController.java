package com.github.oosm032519.playlistviewernext.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*") // 全てのオリジンからのリクエストを許可
@RestController
@RequestMapping("/api")
public class InputController {

    @PostMapping("/input")
    public String handleInput(@RequestBody String inputText) {
        System.out.println("Received input: " + inputText);
        return "Input received: " + inputText;
    }
}
