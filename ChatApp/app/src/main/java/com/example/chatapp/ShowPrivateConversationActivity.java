package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ShowPrivateConversationActivity extends AppCompatActivity {


    String CurrentUserNickname, OtherUserEmail, ChatCode;

    TextView textView2,allMessages;
    EditText NextMessage;

    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_private_conversation);

        CurrentUserNickname = getIntent().getStringExtra("CurrentUserNickname");
        OtherUserEmail = getIntent().getStringExtra("OtherUserEmail");
        ChatCode = getIntent().getStringExtra("ChatCode");

        textView2 = findViewById(R.id.textView2);
        textView2.setText(OtherUserEmail);

        allMessages = findViewById(R.id.textView3);
        allMessages.setText("");

        NextMessage = findViewById(R.id.editTextText3);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Private_Chats/" + ChatCode + "/message");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    String MessageHistory = snapshot.getValue().toString();
                    allMessages.setText(MessageHistory);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void send_private(View view){
        if(!NextMessage.getText().toString().trim().isEmpty()){

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String MessageHistory;
                    if (snapshot.getValue() == null){
                        MessageHistory = "";
                    }
                    else{
                        MessageHistory= Objects.requireNonNull(snapshot.getValue()).toString();
                    }

                    MessageHistory = MessageHistory + CurrentUserNickname +":"+ NextMessage.getText().toString() + '\n';

                    reference.setValue(MessageHistory).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            NextMessage.setText("");
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else {
            showMessage("Error","Please write a message first!..");
        }
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

}