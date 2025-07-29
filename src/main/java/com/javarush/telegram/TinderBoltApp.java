package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = System.getenv("TELEGRAM_BOT_JAVA_RUSH_NAME");
    public static final String TELEGRAM_BOT_TOKEN = System.getenv("TELEGRAM_BOT_JAVA_RUSH_TOKEN");
    public static final String OPEN_AI_TOKEN = System.getenv("OPEN_AI_JAVA_RUSH_TOKEN");
    private final ChatGPTService chatGptService = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode dialogMode;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        if (message.equals("/start")) {
            dialogMode = DialogMode.MAIN;
            sendPhotoMessage("avatar_main.jpg");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }

        if (message.equals("/gpt")) {
            dialogMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (dialogMode == DialogMode.GPT) {
            String answer = chatGptService.sendMessage("Ответь на вопрос: ", message);
            sendSafeMessage(answer);
            return;
        }
        sendTextMessage("Вы написали: " + message);

        sendTextButtonsMessage("Твикс", "правая", "right", "левая", "left");

    }

    private void sendSafeMessage(String text) {
        int maxLength = 4096;
        if (text == null || text.isEmpty()) return;

        while (text.length() > 0) {
            int end = Math.min(text.length(), maxLength);

            int splitIndex = end;
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(" ", end);
                if (lastSpace > 0) {
                    splitIndex = lastSpace;
                }
            }
            String part = text.substring(0, splitIndex).trim();
            sendTextMessage(part);

            text = text.substring(splitIndex).trim();
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
