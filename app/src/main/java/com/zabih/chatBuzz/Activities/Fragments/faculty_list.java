package com.zabih.chatBuzz.Activities.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zabih.chatBuzz.Activities.Models.BlockedModel;
import com.zabih.chatBuzz.Activities.Models.UserModel;
import com.zabih.chatBuzz.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class faculty_list extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference("users");
    private Button mSearchBtn;
    EditText mSearchBar;

    private FirebaseRecyclerAdapter<UserModel, UserViewHolder> mAdapter;
    private RecyclerView recyclerView;

    public faculty_list() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faculty_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializations();
        getFacultyList();

        mSearchBar = getActivity().findViewById(R.id.chat_tab2_searchBar);
        mSearchBtn = getActivity().findViewById(R.id.chat_tab2_searchBtn);
        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserfromFb(mSearchBar.getText().toString());
            }
        });
    }

    private void searchUserfromFb(String name) {
        Query query = userRef.orderByChild("username").startAt(name).endAt(name + "\\uf8ff");
        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(query, UserModel.class)//Query(streamsDbRef, liveStreamModel.class)
                        .build();
        mAdapter = new FirebaseRecyclerAdapter<UserModel, faculty_list.UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull faculty_list.UserViewHolder holder, int position, @NonNull final UserModel model) {
                holder.setDetails(model.getUsername(), model.getEmail(), model.getImage_url(),getContext());
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final CharSequence[] items = {"Change role to HOD", "Block User","Unblock User"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Options");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    changeRoleToHOD(model.getUserID());
                                } else if (which == 1) {
                                    blockUser(model.getUserID());
                                }
                                else if(which==2){
                                    unblockUser(model.getUserID());
                                }
                            }
                        });
                        builder.show();
                        //changeRoleToHOD(model.getUserID());
                        return false;
                    }
                });

            }

            @NonNull
            @Override
            public faculty_list.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_row, parent, false);

                return new faculty_list.UserViewHolder(view);
            }
        };
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();


    }

    public void getFacultyList() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = usersRef.orderByChild("role").equalTo("faculty");
        FirebaseRecyclerOptions<UserModel> options =
                new FirebaseRecyclerOptions.Builder<UserModel>()
                        .setQuery(query, UserModel.class)//Query(streamsDbRef, liveStreamModel.class)
                        .build();
        mAdapter = new FirebaseRecyclerAdapter<UserModel, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull final UserModel model) {
                holder.setDetails(model.getUsername(), model.getEmail(), model.getImage_url(), getContext());
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final CharSequence[] items = {"Change role to HOD", "Block User","Unblock User"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Options");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    changeRoleToHOD(model.getUserID());
                                } else if (which == 1) {
                                    blockUser(model.getUserID());
                                }
                                else if(which==2){
                                    unblockUser(model.getUserID());
                                }
                            }
                        });
                        builder.show();
                        //changeRoleToHOD(model.getUserID());
                        return false;
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_row, parent, false);

                return new UserViewHolder(view);
            }
        };
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    private void unblockUser(String userID) {
        DatabaseReference blockRef=FirebaseDatabase.getInstance().getReference("blockedUsers");
        Query query=blockRef.orderByChild("id").equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0)
                {
                    //is in blocked list
                    for(DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        ds.getRef().removeValue();
                    }
                    Toast.makeText(getContext(), "User un-blocked!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "User is not blocked!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void blockUser(String userID) {
        DatabaseReference blockRef=FirebaseDatabase.getInstance().getReference("blockedUsers");
        BlockedModel bm=new BlockedModel();
        bm.setId(userID);
        blockRef.push().setValue(bm);
        Toast.makeText(getContext(), "User Blocked!", Toast.LENGTH_SHORT).show();
    }

    private void changeRoleToHOD(String userID) {
        DatabaseReference facultyRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
        facultyRef.child("role").setValue("hod");
        Toast.makeText(getContext(), "Faculty appointed as HOD", Toast.LENGTH_SHORT).show();
    }

    private void initializations() {
        recyclerView = getActivity().findViewById(R.id.faculty_list_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null)
            mAdapter.stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null)
            mAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null)
            mAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setDetails(String name, String email, String image_url, Context context) {
            CircleImageView userImage = mView.findViewById(R.id.user_row_image);
            TextView userName = mView.findViewById(R.id.user_row_name);
            TextView userEmail = mView.findViewById(R.id.user_row_email);

            if (!image_url.equals(""))
                Glide.with(context).load(image_url).into(userImage);
            userName.setText(name);
            userEmail.setText(email);
        }
    }

}
