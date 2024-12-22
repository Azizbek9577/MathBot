package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MathBot extends TelegramLongPollingBot {
    private final Map<Long, String> userStates = new HashMap<>();
    private final Map<Long, Integer> firstNumbers = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "proektcha_bot";
    }

    @Override
    public String getBotToken() {
        return "8084387852:AAFMVAAl6nBMiPnEfkRA98K0BzsUd35F6NQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            try {
                if (messageText.equals("/start")) {
                    String userName = update.getMessage().getFrom().getFirstName();
                    sendTextWithButtons(chatId,
                            "Assalomu alaykum, " + userName + "! Men quyidagi amallarni bajara olaman:",
                            List.of("Qo'shish", "Ayirish", "Ko'paytirish", "Bo'lish", "Darajani hisoblash")
                    );
                } else if (userStates.containsKey(chatId)) {
                    handleUserState(chatId, messageText);
                } else {
                    handleMainCommands(chatId, messageText);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMainCommands(Long chatId, String messageText) throws TelegramApiException {
        switch (messageText) {
            case "Qo'shish" -> {
                userStates.put(chatId, "ADD");
                sendText(chatId, "Qo'shish uchun birinchi sonni kiriting:");
            }
            case "Ayirish" -> {
                userStates.put(chatId, "SUBTRACT");
                sendText(chatId, "Ayirish uchun birinchi sonni kiriting:");
            }
            case "Ko'paytirish" -> {
                userStates.put(chatId, "MULTIPLY");
                sendText(chatId, "Ko'paytirish uchun birinchi sonni kiriting:");
            }
            case "Bo'lish" -> {
                userStates.put(chatId, "DIVIDE");
                sendText(chatId, "Bo'lish uchun birinchi sonni kiriting:");
            }
            case "Darajani hisoblash" -> {
                userStates.put(chatId, "POWER");
                sendText(chatId, "Iltimos, son kiriting:");
            }
            default -> sendText(chatId, "Noto'g'ri amal tanlandi. /start ni bosib qayta boshlang.");
        }
    }

    private void handleUserState(Long chatId, String messageText) throws TelegramApiException {
        String state = userStates.get(chatId);

        try {
            int number = Integer.parseInt(messageText);

            switch (state) {
                case "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE" -> {
                    if (!firstNumbers.containsKey(chatId)) {
                        firstNumbers.put(chatId, number);
                        sendText(chatId, "Iltimos, ikkinchi sonni kiriting:");
                    } else {
                        int firstNumber = firstNumbers.remove(chatId);
                        int result = switch (state) {
                            case "ADD" -> firstNumber + number;
                            case "SUBTRACT" -> firstNumber - number;
                            case "MULTIPLY" -> firstNumber * number;
                            case "DIVIDE" -> firstNumber / number;
                            default -> 0;
                        };
                        sendText(chatId, "Natija: " + result);
                        userStates.remove(chatId);
                    }
                }
                case "POWER" -> {
                    sendText(chatId, "Natija: " + (number * number));
                    userStates.remove(chatId);
                }
                default -> sendText(chatId, "Noto'g'ri amal. /start ni bosib qayta boshlang.");
            }
        } catch (NumberFormatException e) {
            sendText(chatId, "Iltimos, to'g'ri son kiriting.");
        } catch (ArithmeticException e) {
            sendText(chatId, "Bo'lishda xato: nolga bo'lib bo'lmaydi.");
        }
    }

    private void sendText(Long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        execute(message);
    }

    private void sendTextWithButtons(Long chatId, String text, List<String> buttonLabels) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        for (String label : buttonLabels) {
            row.add(new KeyboardButton(label));
            if (row.size() == 2) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        execute(message);
    }
}
