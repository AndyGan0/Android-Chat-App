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

public class PublicChat extends AppCompatActivity {

    TextView textView2,allMessages;
    EditText message;
    String nickname;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_chat);
        textView2 = findViewById(R.id.textView2);
        nickname = getIntent().getStringExtra("nickname");
        textView2.setText("Hello "+nickname);
        allMessages = findViewById(R.id.textView3);
        allMessages.setText("");
        message = findViewById(R.id.editTextText3);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Public_Chat/message");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String previousMessages = allMessages.getText().toString();
                if (snapshot.getValue()!=null)
                    allMessages.setText(previousMessages+"\n"+snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void send(View view){
        if(!message.getText().toString().trim().isEmpty()){
            reference.setValue(nickname+":"+message.getText().toString());
            message.setText("");
        }else {
            showMessage("Error","Please write a message first!..");
        }
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }


}