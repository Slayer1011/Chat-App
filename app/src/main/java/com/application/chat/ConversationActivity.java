package com.application.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.application.chat.Models.Chat;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversationActivity extends AppCompatActivity {
    TextInputEditText messagetext;
    TextView username;
    ImageView image;
    ImageView sendBtn;
    RecyclerView recyclerMsg;
    MessageAdapter messageAdapter;
    FirebaseUser fUser;
    Intent infoIntent;
    ExecutorService exe;
    List<Chat> list;
    ImageView onlineDot;
    FirebaseUser currentUser;
    SeenIndicatorListener indicatorListener;
    String remoteUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        this.currentUser=FirebaseAuth.getInstance().getCurrentUser();
        this.exe= Executors.newFixedThreadPool(2);
        this.username=findViewById(R.id.nameText);
        this.image=findViewById(R.id.userProfile);
        this.fUser= FirebaseAuth.getInstance().getCurrentUser();
        this.messagetext=findViewById(R.id.messageInput);
        this.onlineDot=findViewById(R.id.onlineStatus);
        indicatorListener=InstanceHelper.getSeenIndicatorListener();
        loadEverything();
        this.sendBtn=findViewById(R.id.sendButton);
        sendBtn.setOnClickListener(view -> {
            String msg=messagetext.getText().toString();
            DateFormat dateFormat=new SimpleDateFormat("hh.mm aa");
            if(!msg.equals("")){
                String timeStamp=dateFormat.format(new Date()).toString();
                sendMsg(fUser.getUid(),infoIntent.getStringExtra("uid"),msg,timeStamp);
            }
            messagetext.setText("");
        });
        ImageView backbtn=findViewById(R.id.backPress);
        backbtn.setOnClickListener(v->onBackPressed());
    }
    public void dropMenu(View view){
        PopupMenu popupMenu=new PopupMenu(this,view);
        MenuInflater menuInflater=popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_bar,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId=item.getItemId();
            if(itemId==R.id.background){
                return true;
            } else if(itemId==R.id.setting){
                return true;
            }
            else {
                return false;
            }
        });
        popupMenu.show();
    }
    public void loadEverything(){
        this.infoIntent=getIntent();
        this.remoteUserId=infoIntent.getStringExtra("uid");
        username.setText(infoIntent.getStringExtra("name"));
        Glide.with(getApplicationContext()).load(Uri.parse(infoIntent.getStringExtra("image")))
                        .placeholder(R.drawable.default_pic)
                                .error(R.drawable.default_pic)
                                        .into(image);

        loadOnlineStatus(infoIntent.getStringExtra("uid"),onlineDot);
        buildChatList();
    }
    public void sendMsg(String sender, String reciver, String msg,String timeStamp){
        exe.execute(()->{
            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chats");
            String nodeId=ref.push().getKey();
            Chat chat=new Chat(sender,reciver,msg,timeStamp);
            chat.setSeen(false);
            chat.setMesssageId(nodeId);
            ref.child(nodeId).setValue(chat, ((error, ref1) -> {
                if(error!=null) {
                    Log.e("FirebaseInsertError",error.getMessage());
                }
                pushNotification(currentUser.getDisplayName(),msg,currentUser.getUid(),currentUser.getPhotoUrl().toString());
            }));
        });
    }
    public void pushNotification(String tiltle,String content,String sender,String image){
        HashMap<String,String> data=new HashMap<>();
        String recipientoken=infoIntent.getStringExtra("token");
        data.put("title",tiltle);
        data.put("body",content);
        data.put("sender",sender);
        data.put("imaage",image);
        data.put("token",recipientoken);
       // FCManager.sendRemoteNotification(recipientoken,data);
    }
    public void buildChatList(){
        exe.execute(()->{
            recyclerMsg=findViewById(R.id.messageRecylerView);
            recyclerMsg.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setStackFromEnd(true);
            recyclerMsg.setLayoutManager(linearLayoutManager);
            loadMessages();
        });
    }
    public void loadMessages(){
        list=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.hasChildren() && snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null && fUser != null && chat.getReciver() != null && chat.getSender() != null) {
                            if (!chat.isSeen() && chat.getReciver().equals(fUser.getUid()) && chat.getSender().equals(remoteUserId)){
                                ref.child(chat.getMesssageId()).child("seen").setValue(true);
                                refreshUserAdapter(true,remoteUserId);
                            }
                            if (chat.getReciver().equals(fUser.getUid()) && chat.getSender().equals(remoteUserId) || chat.
                                    getSender().equals(fUser.getUid()) && chat.getReciver().equals(remoteUserId)) {
                                list.add(chat);
                            }
                            messageAdapter = new MessageAdapter(list, getApplicationContext());
                            recyclerMsg.setAdapter(messageAdapter);
                        } else {
                            Log.e("NullReference", "chat is null object at loadMessage()");
                        }
                    }
                } else {
                    Log.e("Snapshot", "has no children at loadMessage()");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void refreshUserAdapter(boolean seen,String sender){
        Intent intent=new Intent("com.application.chat.ACTION_REFRESH");
        intent.putExtra("seen",seen);
        intent.putExtra("sender",sender);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    public void notifySeen(String nodeId){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(nodeId).child("seen").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nodeId=snapshot.getKey();
                int pos=getPositionById(nodeId);
                boolean seen=snapshot.getValue(Boolean.class);
                messageAdapter.updateSeen(pos,seen);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseSeenFail",error.getMessage());
            }
        });
    }
    public int getPositionById(String target){
        for(int i=0;i<list.size();i++){
            if(list.get(i).getMesssageId().equals(target)){
                return i;
            }
        }
        return -1;
    }
    public void loadOnlineStatus(String uid,ImageView onlineStatus){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("isOnline").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isUserOnline=snapshot.getValue(Boolean.class);
                if (isUserOnline){
                    onlineStatus.setVisibility(View.VISIBLE);
                }else {
                    onlineStatus.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CurrentUserStatus",error.getMessage());
            }
        });
    }
}
interface SeenIndicatorListener{
    void onSeenIndicatorChange(boolean seen,String sender,String reciver,String remoteUserId);
}
interface OnlineIndicatorListener{
    void listenOnline(boolean status);
}