package com.example.aly.firebaseapp;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener
{
    private ImageView sentPostImageView;
    private TextView txtDescription;
    private ListView postListView;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private ArrayList<DataSnapshot> dataSnapshots;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        sentPostImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.txtDescription);

        postListView = findViewById(R.id.postListView);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        postListView.setAdapter(adapter);

        dataSnapshots = new ArrayList<>();
        postListView.setOnItemClickListener(this);
        postListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users")
                .child(firebaseAuth.getCurrentUser().getUid()).child("received_posts")
                .addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                dataSnapshots.add(dataSnapshot);
                String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshots)
                {
                    if (snapshot.getKey().equals(dataSnapshot.getKey()))
                    {
                        dataSnapshots.remove(i);
                        usernames.remove(i);
                    }

                    i++;
                }

                adapter.notifyDataSetChanged();
                sentPostImageView.setImageResource(R.drawable.placeholder);
                txtDescription.setText("");

                Toast.makeText(ViewPostsActivity.this, "Post deletion successful",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        DataSnapshot myDataSnapShot = dataSnapshots.get(position);
        String downloadLink = (String) myDataSnapShot.child("imageLink").getValue();
        Picasso.get().load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String) myDataSnapShot.child("des").getValue());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        }

        else
        {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle("Delete post").setMessage("Confirm deletion?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // continue with delete
                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String) dataSnapshots.get(position)
                                .child("imageIdentifier").getValue()).delete();

                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser().getUid())
                                .child("received_posts")
                                .child(dataSnapshots.get(position).getKey()).removeValue();
                   }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();

        return false;
    }
}
