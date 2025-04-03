package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.chatapp.PrivateChatsFolder.RecyclerViewAdapter;
import com.example.chatapp.PrivateChatsFolder.RecyclerViewInterface;
import com.example.chatapp.PrivateChatsFolder.private_chatroom_model_class;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrivateChats extends AppCompatActivity implements RecyclerViewInterface {

    String email, nickname;

    EditText editText4;

    List<private_chatroom_model_class> Private_chats;

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;


    FirebaseDatabase database;
    DatabaseReference reference;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chats);

        email = getIntent().getStringExtra("email");
        nickname = getIntent().getStringExtra("nickname");

        editText4 = findViewById(R.id.editTextText4);

        recyclerView = findViewById(R.id.mRecyclerView);


        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Private_Chats/");

        //  Reading pre-existing chats with other people
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                List<private_chatroom_model_class> Private_Chat = new ArrayList<private_chatroom_model_class>();


                for (DataSnapshot snapshot2 : snapshot.getChildren()){

                    String user1_email_t = Objects.requireNonNull(snapshot2.child("user1_email").getValue()).toString();
                    String user2_email_t = Objects.requireNonNull(snapshot2.child("user2_email").getValue()).toString();


                    if ( !(user1_email_t.equals(email)) && !(user2_email_t.equals(email)) ){
                        //  Both users in the conversation have different email that current user
                        //  This chat doesn't belong current user
                        continue;
                    }



                    String chat_code = snapshot2.getKey() ;
                    String chat_user_email;

                    if ( user1_email_t.equals(email) ){
                        chat_user_email = user2_email_t ;
                    }
                    else{
                        chat_user_email = user1_email_t ;
                    }

                    private_chatroom_model_class temp = new private_chatroom_model_class(chat_code , chat_user_email);
                    Private_Chat.add(temp);


                }

                Private_chats = Private_Chat;

                drawUiChats();  //  Adding those chats using this function




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public void addChatWithEmail(View view) {

        if (editText4.getText().toString().isEmpty()) {
            showMessage("Error","You have to add another user's email");
            return;
        }

        String user1_email = email;
        String user2_email = editText4.getText().toString();

        //  Checking if user with this email exists
        reference = database.getReference("Users/");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                reference = database.getReference("Private_Chats/");
                boolean found = false;

                for (DataSnapshot snapshot2 : snapshot.getChildren()){
                    if ( Objects.requireNonNull(snapshot2.getValue()).toString().equals(user2_email) ) {
                        //  Email exists
                        found = true;
                        break;
                    }
                }

                if (!found){
                    showMessage("Error", "User with this email doesn't exist");
                }
                else {
                    //  Checking if chat with this email already exists
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot snapshot2 : snapshot.getChildren()){

                                String user1_email_t = Objects.requireNonNull(snapshot2.child("user1_email").getValue()).toString();
                                String user2_email_t = Objects.requireNonNull(snapshot2.child("user2_email").getValue()).toString();

                                if ( (user1_email.equals(user1_email_t) && user2_email.equals(user2_email_t)) || (user1_email.equals(user2_email_t) && user2_email.equals(user1_email_t)) ) {
                                    //  there is a private conversation with the same participants in the server
                                    showMessage("Error", "You already have a conversation with this user. Please open that conversation.");
                                    return;
                                }

                            }

                            //  If no previous conversation was found, create a new one
                            reference = reference.push();

                            Map<String, Object> Chat_Database_Children = new HashMap<String, Object>();
                            Chat_Database_Children.put("user1_email" , email);
                            Chat_Database_Children.put("user2_email" , editText4.getText().toString());
                            Chat_Database_Children.put("message", "");

                            reference.updateChildren(Chat_Database_Children).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    private_chatroom_model_class temp = new private_chatroom_model_class(reference.getKey(), user2_email );
                                    Private_chats.add(0 , temp);

                                    reference = database.getReference("Private_Chats/");

                                    adapter.notifyDataSetChanged();

                                    onItemClick(0);

                                }
                            });





                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }



    private void drawUiChats(){
        adapter = new RecyclerViewAdapter(this, Private_chats, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent( getApplicationContext() , ShowPrivateConversationActivity.class );
        intent.putExtra("CurrentUserNickname",nickname);
        intent.putExtra("OtherUserEmail",Private_chats.get(position).getChat_user_email());
        intent.putExtra("ChatCode", Private_chats.get(position).getChat_code());
        startActivity(intent);
    }
}