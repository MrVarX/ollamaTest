package com.example.ollamatest1.controller;

import com.example.ollamatest1.common.Completion;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    @Resource
    private OllamaChatModel ollamaChatModel;

    /**
     * 简单调用
     */
    @PostMapping(value = "/ai/ask")
    public Object ask(String msg) {
        String called = ollamaChatModel.call(msg);
        System.out.println(called);
        return called;
    }

    /***
     * 流式方式
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(String msg) {
        return ollamaChatModel.stream(msg).flatMapSequential(Flux::just);
    }

    private final Completion completion;

    public OllamaController(Completion completion) {
        this.completion = completion;
    }

    /**
     * 分析上下文聊天
     */
    @PostMapping("/chat")
    public String chat(String message) {
        return completion.chat(message);
    }

    /**
     * 流式上下文
     */
    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody String message) {
        return completion.chatStream(message);
    }
}
