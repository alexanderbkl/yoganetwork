package com.amit.yoganet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ablanco.zoomy.Zoomy;
import com.amit.yoganet.adapters.AdapterComments;
import com.amit.yoganet.models.ModelComment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class PostDetailActivity extends AppCompatActivity {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference, userDbRef;
    //storage
    StorageReference storageReference;


    //to get detail of user and post
    String hisUid, myUid, myPractic, myPseudonym, myDp,
    postId, pLikes, hisDp, hisPractic, hisPseudonym, pImage, pVideo, youtubeUrl;
    String pseudonyn, uid;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //progress bar
    ProgressDialog pd;

    //views
    ImageView uPictureIv, pImageIv;
    TextView uPseudonymTv, uPracticTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn, playBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //add comments views
    EditText commentEt, commentEdit;
    ImageButton sendBtn, editBtn;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        //actionbar and its properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar()
                .setTitle(R.string.detallespst);
        getSupportActionBar()
                .setDisplayShowHomeEnabled(true);
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(true);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        uid = intent.getStringExtra("uid");
        //init views
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        playBtn = findViewById(R.id.playBtn);
        uPseudonymTv = findViewById(R.id.uPseudonymTv);
        uPracticTv = findViewById(R.id.uPracticTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        moreBtn = findViewById(R.id.moreBtn);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        commentEdit = findViewById(R.id.commentEdit);
        sendBtn = findViewById(R.id.sendBtn);
        editBtn = findViewById(R.id.editBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        commentEdit.setVisibility(View.GONE);
        editBtn.setVisibility(View.GONE);
        playBtn.setVisibility(View.GONE);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        loadComments();


        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //click to go to ThereProfileActivity with uid, this uid is of clicked user
                //which will be used to show user specifi data/posts
                Intent intent = new Intent(PostDetailActivity.this, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        //like button click handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();

            }
        });
            //more button click handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMoreOptions();
                }
            });

            //share button click handle
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pTitle = pTitleTv.getText().toString().trim();
                    String pDescription = pDescriptionTv.getText().toString().trim();

                    BitmapDrawable bitmapDrawable = (BitmapDrawable)pImageIv.getDrawable();
                    if (bitmapDrawable == null) {
                        //post without image
                        shareTextOnly(pTitle, pDescription);
                    }
                    else {
                        //post with image

                        //convert image to bitmap
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        shareImageAndText(pTitle, pDescription, bitmap);

                    }
                }
            });
        //click like count to start PostLikedByActivity, and pass the post id
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });


        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-message".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String value = intent.getStringExtra("value");
            String cid = intent.getStringExtra("cid");
            commentEdit.setText(value);
            commentEt.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);
            commentEdit.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.VISIBLE);

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //input text from edittext
                    String updatedComment = commentEdit.getText().toString().trim();
                    if (!TextUtils.isEmpty(updatedComment)){
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("comment", updatedComment);
                        result.put("edited", "edited");
                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
                        ref.child("Comments").child(cid).updateChildren(result);
                        sendBtn.setVisibility(View.VISIBLE);
                        commentEt.setVisibility(View.VISIBLE);
                        commentEdit.setVisibility(View.GONE);
                        editBtn.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    private void addToHisNotifications(String hisUid, String pId, String notification) {
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

        private void shareTextOnly(String pTitle, String pDescription) {
            //concatenate title and description to share
            String shareBody = pTitle +"\n"+ pDescription;

            //share intent
            Intent sIntent = new Intent(Intent.ACTION_SEND);
            sIntent.setType("text/plain");
            sIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.enviadopor1)); //in case you share via email app
            sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
            startActivity(Intent.createChooser(sIntent, getString(R.string.compartirvia1))); //message to show in share dialog
        }

        private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
            //concatenate title and description to share
            String shareBody = pTitle +"\n"+ pDescription;

            //first we will save this image in cache, get the saved image uri
            Uri uri = saveImageToShare(bitmap);
            //share intent
            Intent sIntent = new Intent(Intent.ACTION_SEND);
            sIntent.setType("text/plain");
            sIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sujetoaqui)); //in case you share via email app
            sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
            sIntent.setType("image/jpeg"); //text to share
            startActivity(Intent.createChooser(sIntent, getString(R.string.compartirvia1))); //message to show in share dialog

        }

        private Uri saveImageToShare(Bitmap bitmap) {
            File imageFolder = new File(getCacheDir(), "images");
            Uri uri = null;
            try {
                imageFolder.mkdir(); //create if not exists
                File file = new File(imageFolder, "shared_image.jpeg");

                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
                stream.flush();
                stream.close();
                uri = FileProvider.getUriForFile(this, "com.amit.yoganet.provider", file);
            }
            catch (Exception e) {
                Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return uri;
        }





    private void loadComments() {
        //layout(Linear) for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init comments list
        commentList = new ArrayList<>();

        //path of the post, to get its comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
           for (DataSnapshot ds: snapshot.getChildren()) {
               ModelComment modelComment = ds.getValue(ModelComment.class);

               commentList.add(modelComment);

               //pass myUid and postId as parameter of constructor of Comment Adapter

               //setup adapter
               adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
               //set adapter
               recyclerView.setAdapter(adapterComments);

           }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //add items in menu
        if (hisUid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.eliminar);
            popupMenu.getMenu().add(Menu.NONE, 1, 0, R.string.editar);

        }
        else {
            moreBtn.setVisibility(View.GONE);
        }


        //item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id==0) {
                //delete is clicked
                beginDelete();
            }
            else   if (id==1) {
                //edit is clicked
                //start AddPostActivity with key "editPost" and the if of the post clicked
                Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                intent.putExtra("key", "editPost");
                intent.putExtra("editPostId", postId);
                startActivity(intent);
            }

            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image

        if (pImage.equals("noImage")) {

            deleteWithoutImage();
        }
        else {
            //post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //progress bar
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.eliminando));
        /*Steps
         * 1)Delete image using url
         * 2)Delete from database using post id*/
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //image deleted, now delete database

                    Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                    fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ds.getRef().removeValue(); //remove values from firebase where pId matches
                            }
                            //deleted
                            Toast.makeText(PostDetailActivity.this, R.string.eliminado, Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                })
                .addOnFailureListener(e -> {
                    //failed, can't go further
                    pd.dismiss();
                    Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.eliminando));

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pId matches
                }
                //deleted
                Toast.makeText(PostDetailActivity.this, R.string.eliminado, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        //when details of post are loading, also check if current user has liked it or not
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "like" to "liked"*/
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText("Liked");
                }
                else {
                    //user has noy liked this post
                    /*To indicate that the post is not liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "liked" to "like"*/
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        /*Get total number of likes for the post, whose like button clicked
         * if currently signed in user has not liked before
         * increase value by 1, otherwise decrease value by 1*/
        mProcessLike = true;
        //get id of the post liked
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        final long currentDate = System.currentTimeMillis();

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike) {
                    if (snapshot.child(postId).hasChild(myUid)) {
                        //already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    }
                    else {
                        //not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //set any value
                        mProcessLike = false;
                        String hotScore = String.valueOf(hot(Long.parseLong(postId), Long.parseLong(pLikes)+1,currentDate));
                        postsRef.child(postId).child("hotScore").setValue(hotScore);
                        addToHisNotifications(""+hisUid, ""+postId, getString(R.string.hadadolike));

                    } } }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private long hot(long postDate, long likes, long currentDate) {
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeZone(TimeZone.getTimeZone("GMT"));
        int year = cal2.get(Calendar.YEAR);
        int yearNumber = (Math.abs(year) % 10)*10000;

        return (((currentDate - postDate) / 86400000) * 1000 - score(likes) - yearNumber * 10000)*(-1);
    }

    private long score(long likes) {
        return likes * 1000;
    }



    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.añadiocomment));
        //get data from comment edit text
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)) {
            //no value is entered
            Toast.makeText(this, R.string.comentvacio, Toast.LENGTH_SHORT).show();
            return;

        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        //each post will have a child "Comments" that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uPseudonym", myPseudonym);
        hashMap.put("uDp", myDp);
        hashMap.put("uPractic", myPractic);
        hashMap.put("cLikes", "0");


        //put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Added
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, R.string.comentanadido, Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                        addToHisNotifications(""+hisUid, ""+postId, getString(R.string.hacomentado));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, not added
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateCommentCount() {
        //whenever user adds comment increase the comment count as we did for like count
        mProcessComment = true;
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessComment) {
                            String comments = ""+ snapshot.child("pComments").getValue();
                            int newCommentVal = Integer.parseInt(comments) +1;
                            ref.child("pComments").setValue(""+newCommentVal);
                            mProcessComment = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadUserInfo() {
        //get current info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    myPseudonym = ""+ds.child("pseudonym").getValue();
                    myPractic = ""+ds.child("practic").getValue();
                    myDp = ""+ds.child("image").getValue();
                    System.out.println(pseudonyn);
                    //set data
                    try {
                        //if image is received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void loadPostInfo() {
        //get post using the id of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the posts until the required post
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                     pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    pVideo = ""+ds.child("pVideo").getValue();
                    youtubeUrl = ""+ds.child("youtubeUrl").getValue();
                    hisPractic = ""+ds.child("uPractic").getValue();
                    hisPseudonym = ""+ds.child("uPseudonym").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();
                    //conver timestamp to dd/mm/yyyy HH:mm
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+" Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount+getString(R.string.comentarios1));

                    uPseudonymTv.setText(hisPseudonym);
                    uPracticTv.setText(hisPractic);
                    //set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if(pImage.equals("noImage")) {
                        //hide imageview
                        pImageIv.setVisibility(View.GONE);

                    } else if (!Objects.equals(pVideo, "") && !Objects.equals(pVideo, "null") || !Objects.equals(youtubeUrl, "") && !Objects.equals(youtubeUrl, "null")) {

                        playBtn.setVisibility(View.VISIBLE);

                        if (!Objects.equals(pVideo, "") && !Objects.equals(pVideo, "null")) {
                            try {
                                long thumb = 1000L;
                                RequestOptions options = new RequestOptions().frame(thumb);
                                Glide.with(getApplicationContext()).load(pVideo).apply(options).fitCenter().centerCrop().into(pImageIv);
                                playBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(PostDetailActivity.this, VideoPlayerActivity.class);
                                        intent.putExtra("videoUrl", pVideo);
                                        PostDetailActivity.this.startActivity(intent);
                                    }
                                });
                            }
                            catch(NullPointerException e) {
                                Log.e("null thumbnail", String.valueOf(e));
                            }
                        } else if (!Objects.equals(youtubeUrl, "") && !Objects.equals(youtubeUrl, "null")) {
                            Glide.with(getApplicationContext()).load(pImage).into(pImageIv);
                            playBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    playBtn.setOnClickListener(view -> {
                                        Intent intent = new Intent(PostDetailActivity.this, YouTubePlayerActivity.class);
                                        intent.putExtra("youtubeUrl", youtubeUrl);
                                        PostDetailActivity.this.startActivity(intent);
                                    });

                                }
                            });
                        }

                    }
                    else {
                        //show imageview
                       pImageIv.setVisibility(View.VISIBLE);


                             try {
                                 Glide.with(getApplicationContext()).load(pImage).into(pImageIv);
                                 Zoomy.Builder builder = new Zoomy.Builder(PostDetailActivity.this)
                                         .target(pImageIv);
                                 builder.register();

                        }
                      catch (Exception ignored) {

                        }

                    }
                    //set user image in comment part
                    try {
                        Glide.with(getApplicationContext()).load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    }
                    catch (Exception e) {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_default_img).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null) {
            //user is signed in
            myUid = user.getUid();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
           checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}