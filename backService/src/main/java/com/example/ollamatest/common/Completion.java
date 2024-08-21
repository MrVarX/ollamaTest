package com.example.ollamatest.common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * 存入上下文信息，并调用接口进行聊天
 */
@Component
public class Completion {
    @Resource
    private OllamaChatModel aiClient;

    /**
     * 最大消息记录数
     */
    private final static Integer MAX_SIZE = 10;

    /**
     * 消息记录
     */
    private List<Message> messages = new ArrayList<>();


    /**
     * 初始化存入系统消息
     */
    @PostConstruct
    private void addSystemMessage() {
        String message = "李白（701年2月28日—762年12月），字太白，号青莲居士，出生于蜀郡绵州昌隆县（今四川省绵阳市江油市青莲镇），一说出生于西域碎叶 ，祖籍陇西成纪（今甘肃省秦安县）。唐朝伟大的浪漫主义诗人，凉武昭王李暠九世孙 。" +
                "为人爽朗大方，乐于交友，爱好饮酒作诗，名列“酒中八仙” 。曾经得到唐玄宗李隆基赏识，担任翰林供奉，赐金放还后，游历全国，先后迎娶宰相许圉师、宗楚客的孙女。唐肃宗李亨即位后，卷入永王之乱，流放夜郎，辗转到达当涂县令李阳冰家。上元二年，去世，时年六十二 。" +
                "著有《李太白集》，代表作有《望庐山瀑布》《行路难》《蜀道难》《将进酒》《早发白帝城》《黄鹤楼送孟浩然之广陵》等。李白所作词赋，就其开创意义及艺术成就而言，享有极为崇高的地位，后世誉为“诗仙”，与诗圣杜甫并称“李杜”。";
        Message systemMessage = new SystemMessage(message);
        messages.add(systemMessage);
    }

    /**
     * 存储用户发送的消息
     */
    private void addUserMessage(String message) {
        Message userMessage = new UserMessage(message);
        messages.add(userMessage);
    }

    /**
     * 存储AI回复的消息
     */
    private void addAssistantMessage(String message) {
        Message assistantMessage = new AssistantMessage(message);
        messages.add(assistantMessage);
    }

    /**
     * 聊天接口
     */
    public String chat(String message) {
        addUserMessage(message);
        String result = aiClient.call(new Prompt(messages)).getResult().getOutput().getContent();
        addAssistantMessage(result);
        update();
        return result;
    }

    /**
     * 流式聊天接口
     */
    public Flux<String> chatStream(String message) {
        addUserMessage(message);

        StringBuffer fullReply = new StringBuffer();

        Flux<String> fluxResult = aiClient.stream(new Prompt(messages))
                .flatMap(response -> {
                    String reply = response.getResult().getOutput().getContent();

                    //拼接回复内容
                    fullReply.append(reply);

                    return Flux.just(reply);
                })
                .doOnComplete(() -> {
                    //监听流式响应完成，完整回复存入消息记录
                    System.out.println(fullReply);
                    addAssistantMessage(String.valueOf(fullReply));
                });

        update();
        return fluxResult;
    }

    /**
     * 更新消息记录
     */
    private void update() {
        if (messages.size() > MAX_SIZE) {
            messages = messages.subList(messages.size() - MAX_SIZE, messages.size());
        }
    }
}
