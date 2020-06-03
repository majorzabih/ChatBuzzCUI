package com.zabih.chatBuzz.Activities.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zabih.chatBuzz.Activities.Admin;
import com.zabih.chatBuzz.Activities.AdminChatActivity;
import com.zabih.chatBuzz.Activities.Chat;
import com.zabih.chatBuzz.Activities.EventData;
import com.zabih.chatBuzz.Activities.EventsShow;
import com.zabih.chatBuzz.Activities.FacultyListActivity;
import com.zabih.chatBuzz.Activities.LostFounDViewpager;
import com.zabih.chatBuzz.Activities.Models.UserModel;
import com.zabih.chatBuzz.Activities.NavDrawer;
import com.zabih.chatBuzz.Activities.SignIn;
import com.zabih.chatBuzz.R;

import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button mLogout;
    private FirebaseAuth mAuth;
    int flag=0;
    CircleImageView profile_img;

    private CardView mNotifications, mChats, mEvents, mLost, mAdmin, mAdmin2;
    private UserModel myUserInfo = null;
    private TextView toolbarText, mAdminTextView, mAdminTextView2, mWelcomeText,mEventsText;
    UserModel adminInfo = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializations();
        getUserInfo();
        mChats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUserInfo != null) {
                    if (!myUserInfo.getRole().equals("admin")) {
                        startActivity(new Intent(getActivity(), Chat.class).putExtra("myUserInfo", (Serializable) myUserInfo));
                    }else {
                        startActivity(new Intent(getActivity(),AdminChatActivity.class));
                    }
                }
            }
        });
        mEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getActivity(), EventsShow.class).putExtra("info", myUserInfo));

            }
        });
        mNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EventData.class).putExtra("info", myUserInfo));

            }
        });


        mLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getActivity(), LostFounDViewpager.class));

            }
        });
        mAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FacultyListActivity.class));
            }
        });
        mAdmin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Admin.class));
            }
        });

        if (adminInfo == null) {


            loadUserImage();
        }
    }

    private void getUserInfo() {

        final String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(myID);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myUserInfo = dataSnapshot.getValue(UserModel.class);
                if (myUserInfo != null) {

                    mWelcomeText.setText("Welcome: "+myUserInfo.getUsername());
                    if (myUserInfo.getRole().equals("hod")) {
                        mAdmin2.setVisibility(View.VISIBLE);
                        mAdminTextView2.setVisibility(View.VISIBLE);
                    }
                    if (myUserInfo.getRole().equals("admin"))
                    {
                        mAdmin2.setVisibility(View.VISIBLE);
                     //   mAdminTextView2.setVisibility(View.VISIBLE);
                        mAdmin.setVisibility(View.VISIBLE);
                     //   mAdminTextView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserImage() {

        String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(myID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = new UserModel();
                user = dataSnapshot.getValue(UserModel.class);


                if (user != null) {
                    checkBlocked(user);
                    if (!user.getImage_url().equals(""))
                        Glide.with(HomeFragment.this).load(user.getImage_url()).into(profile_img);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkBlocked(UserModel myUserInfo) {
        DatabaseReference blockRef=FirebaseDatabase.getInstance().getReference("blockedUsers");
        Query query=blockRef.orderByChild("id").equalTo(myUserInfo.getUserID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0)
                {//is in blocked list
                    Toast.makeText(getActivity(), "User is blocked by Admin", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                   // finish();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initializations() {

        mChats = getActivity().findViewById(R.id.dashboard_Chats);
        mEvents = getActivity().findViewById(R.id.dashboard_Events);
        mNotifications = getActivity().findViewById(R.id.dashboard_notifications);
        mAuth = FirebaseAuth.getInstance();
        mLost = getActivity().findViewById(R.id.mLost);
        mAdmin = getActivity().findViewById(R.id.dashboard_admin);
        mAdminTextView = getActivity().findViewById(R.id.dashboard_adminTextView);
        mAdmin2 = getActivity().findViewById(R.id.dashboard_admin2);
        mAdminTextView2 = getActivity().findViewById(R.id.dashboard_adminTextView2);
        mWelcomeText = getActivity().findViewById(R.id.dashboard_welcomtext);
        mEventsText=getActivity().findViewById(R.id.mEventsText);
        profile_img=getActivity().findViewById(R.id.db_img_view);

    }


}