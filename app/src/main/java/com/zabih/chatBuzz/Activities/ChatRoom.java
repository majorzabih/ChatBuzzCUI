package com.zabih.chatBuzz.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zabih.chatBuzz.Activities.Adapters.MessageAdapter;
import com.zabih.chatBuzz.Activities.Models.MessageModel;
import com.zabih.chatBuzz.Activities.Models.UserModel;
import com.zabih.chatBuzz.Activities.Notifications.APIService;
import com.zabih.chatBuzz.Activities.Notifications.Client;
import com.zabih.chatBuzz.Activities.Notifications.Data;
import com.zabih.chatBuzz.Activities.Notifications.MyResponse;
import com.zabih.chatBuzz.Activities.Notifications.Sender;
import com.zabih.chatBuzz.Activities.Notifications.Token;
import com.zabih.chatBuzz.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoom extends AppCompatActivity {
    Toolbar toolbar;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference chatRef = database.getReference("chatRef");
    List<MessageModel> mChat;
    MessageAdapter mAdapter;
    ValueEventListener chatListener;
    String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private ImageView mSendBtn,mImageBtn;
    private EditText mMessage;
    private RecyclerView recyclerView;
    private TextView mToolbarText;
    private UserModel otherUser;
    private Uri image_uri,finalUri=null;
    private static final int IMAGE_PICK_CODE = 123;
    String userid;
    FirebaseUser fuser;
    APIService apiService;
    boolean notify=false;
    Intent intent;
    String message;
    DatabaseReference reference;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        initializations();

        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                message= mMessage.getText().toString();
                if(!(mMessage.getText().toString().equals("") && image_uri==null)) {
                    String s = mMessage.getText().toString();
                    List<String> words = Arrays.asList("fuck","asshole","cunt","dick","penis","gandu","bhenchod","dalla","chutiya","chutia","harami","phudda","madrchod","shit");
                    for (String word : words) {
                        Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
                        s = rx.matcher(s).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
                    }

// s: i will hit your ** right *

                    String BadWordStarred=s;
                    sendMessageToFb(BadWordStarred);
                }else{
                    Toast.makeText(ChatRoom.this, "Nothing to send!", Toast.LENGTH_SHORT).show();

                }

//                final String msg= message;
//                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("users").child(myID);
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        UserModel userModel =dataSnapshot.getValue(UserModel.class);
//                        if(notify) {
//                            sendNotification(otherUser.getUserID(), userModel.getUsername(),msg);
//                        }
//                        notify=false;
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
            }
        });


        readMessages(myID, otherUser.getUserID());
        seenMessage(otherUser.getUserID());

    }

    private void seenMessage(final String userid){
        seenListener=chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    MessageModel messageModel=snapshot.getValue(MessageModel.class);
                    if(messageModel.getReceiver().equals(myID) && messageModel.getSender().equals(userid)){

//                        MessageModel mssg = new MessageModel();
//                        mssg.setSeen(true);
//                        chatRef.push().setValue(mssg);
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message){
        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token=snapshot.getValue(Token.class);
//                    intent=getIntent();
//                    String userid=intent.getStringExtra("otherUser");
                    Data data=new Data(myID,R.mipmap.ic_launcher,username+": "+ message,"New Message",
                            otherUser.getUserID());

                    Sender sender=new Sender(data,token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(response.body().success!=1){
                                            Toast.makeText(ChatRoom.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            image_uri = data.getData();
            uploadImageToStorage();
        }
    }
    private void uploadImageToStorage() {
        final StorageReference picRef = FirebaseStorage.getInstance().getReference("messageImages");
        UploadTask uploadTask = picRef.putFile(image_uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return picRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    finalUri= task.getResult();
                    sendMessageToFb(mMessage.getText().toString());
                    readMessages(myID,otherUser.getUserID());
                } else {
                    Toast.makeText(ChatRoom.this, "could not produce Url", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoom.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void readMessages(final String myID, final String otheruserID) {
        mChat = new ArrayList<>();

        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MessageModel message = ds.getValue(MessageModel.class);
                    if (message.getReceiver().equals(myID) && message.getSender().equals(otheruserID) ||
                            message.getReceiver().equals(otheruserID) && message.getSender().equals(myID)) {
                        mChat.add(message);
                    }
                    mAdapter = new MessageAdapter(mChat, myID, otheruserID, ChatRoom.this);
                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        chatRef.addValueEventListener(chatListener);

    }

    private void sendMessageToFb(final String message) {
        String currentDate = new SimpleDateFormat("dd MMMM", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm aa", Locale.getDefault()).format(new Date());
//        MessageModel mssg = new MessageModel();
//        mssg.setMessage(message);
//        mssg.setTime(currentTime);
//        mssg.setDate(currentDate);
//        mssg.setReceiver(otherUser.getUserID());
//        mssg.setSender(myID);
//        if (finalUri!=null) {
//            mssg.setImage_url(finalUri.toString());
//        } else {
//            mssg.setImage_url("");
//        }
//        if (mMessage.getText().toString().equals(""))
//            mssg.setMessage("");

//        chatRef.push().setValue(mssg);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myID);
        hashMap.put("time", currentTime);
        hashMap.put("date", currentDate);
        hashMap.put("receiver", otherUser.getUserID());
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        if (finalUri!=null) {
            hashMap.put("image_url",finalUri.toString());
        } else {
            hashMap.put("image_url","");
        }
        if (mMessage.getText().toString().equals(""))
            hashMap.put("message"," ");

        chatRef.push().setValue(hashMap);
        mMessage.setText("");
        finalUri=null;
        image_uri=null;

        final String msg= message;
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("users").child(myID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel userModel =dataSnapshot.getValue(UserModel.class);
                if(notify) {
                    sendNotification(otherUser.getUserID(), userModel.getUsername(),msg);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initializations() {
        mMessage = findViewById(R.id.chatRoom_searchBar);
        mSendBtn = findViewById(R.id.chatRoom_searchBtn);
        mToolbarText = findViewById(R.id.chatRoom_toolbar_text);
        mImageBtn=findViewById(R.id.chatRoom_imageBtn);


        recyclerView = findViewById(R.id.chatRoom_recycler);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setHasFixedSize(true);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        otherUser = (UserModel) getIntent().getSerializableExtra("otherUser");
        mToolbarText.setText(otherUser.getUsername());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatListener != null)
            chatRef.removeEventListener(chatListener);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (chatListener != null){
            chatRef.removeEventListener(chatListener);
        }
        chatRef.removeEventListener(seenListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null)
            chatRef.removeEventListener(chatListener);
    }
}
