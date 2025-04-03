package com.example.chatapp.PrivateChatsFolder;

public class private_chatroom_model_class {
    private String chat_code;
    private String chat_user_email;

    public private_chatroom_model_class(String chat_code, String chat_user_email) {
        this.chat_code = chat_code;
        this.chat_user_email = chat_user_email;
    }

    public String getChat_code() {
        return chat_code;
    }

    public String getChat_user_email() {
        return chat_user_email;
    }
}
