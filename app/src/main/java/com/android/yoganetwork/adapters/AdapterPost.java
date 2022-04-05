package com.android.yoganetwork.adapters;

import static android.text.TextUtils.isEmpty;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ablanco.zoomy.Zoomy;
import com.android.yoganetwork.AddPostActivity;
import com.android.yoganetwork.PostDetailActivity;
import com.android.yoganetwork.PostLikedByActivity;
import com.android.yoganetwork.R;
import com.android.yoganetwork.ThereProfileActivity;
import com.android.yoganetwork.VideoPlayerActivity;
import com.android.yoganetwork.models.ModelPost;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;


import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {


    private final DatabaseReference likesRef; //for likes database node
    private final DatabaseReference postsRef; //reference of posts
    Context context;
    List<ModelPost> postList;
    String myUid;
    boolean mProcessLike=false;
    RecyclerView recycler_view;
    int countLikes, countComments;
    ModelPost modelPost;


    public AdapterPost(Context context, List<ModelPost> postList, RecyclerView recycler_view) {
        this.context = context;
        this.postList = postList;
        this.recycler_view = recycler_view;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);
        return new MyHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, @SuppressLint("RecyclerView") int i) {
        //get data


/*

        this.recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {

                try {
                    Toast.makeText(context, "1", Toast.LENGTH_SHORT).show();
                    System.out.println("suka1");
                    mediaController.hide();


                } catch (NullPointerException e) {
                    Log.e("mediaController", String.valueOf(e));

                }

            }
        });


        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    mediaController.hide();
                    Toast.makeText(context, "2", Toast.LENGTH_SHORT).show();
                    System.out.println("suka2");

                } catch (NullPointerException e) {
                    Log.e("mediaController", String.valueOf(e));

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                try {
                    mediaController.hide();
                    Toast.makeText(context, "3", Toast.LENGTH_SHORT).show();
                    System.out.println("suka3");

                } catch (NullPointerException e) {
                    Log.e("mediaController", String.valueOf(e));

                }
            }
        });*/





        final String uid = postList.get(i).getUid();
        String uPseudonym = postList.get(i).getuPseudonym();
        String uPractic = postList.get(i).getuPractic();
        String uDp = postList.get(i).getuDp();
        final String pId = postList.get(i).getpId();
        String pTitle = postList.get(i).getpTitle();
        String pDescription = postList.get(i).getpDescr();
        countLikes = Integer.parseInt(postList.get(i).getpLikes());
        countComments = Integer.parseInt(postList.get(i).getpComments());

        String pImage = postList.get(i).getpImage();
        String pVideo = postList.get(i).getpVideo();
        if (pImage != null) {
            Picasso.get().load(pImage).fit().centerCrop().into(myHolder.pImageIv);
            myHolder.moreBtn.setOnClickListener(v -> showMoreOptions(myHolder.moreBtn, uid, myUid, pId, pImage, null));
            if (pImage.equals("noImage")) {
                myHolder.pImageIv.setVisibility(View.GONE);
                myHolder.zoomInBtn.setVisibility(View.GONE);
            }
        }
         modelPost = postList.get(i);
        String pTimeStamp = postList.get(i).getpTime();
        if (postList.get(i).getpVideo() != null) {
            myHolder.moreBtn.setOnClickListener(v -> showMoreOptions(myHolder.moreBtn, uid, myUid, pId, null, pVideo));
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            String videoUrl = modelPost.getpVideo();

            // holder.pImageIv.setVisibility(View.GONE);
            // holder.videoView.setVisibility(View.VISIBLE);

            //  Uri videoUri = Uri.parse(videoUrl);
            //    holder.videoView.setVideoURI(videoUri);
            //   holder.videoView.requestFocus();

            try {
                long thumb = myHolder.getLayoutPosition()* 1000L;
                RequestOptions options = new RequestOptions().frame(thumb);

                Glide.with(context).load(videoUrl).apply(options).fitCenter().centerCrop().into(myHolder.pImageIv);
                myHolder.playBtn.setVisibility(View.VISIBLE);

                myHolder.playBtn.setOnClickListener(view -> {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("videoUrl", videoUrl);
                    context.startActivity(intent);
                });

            }
            catch(NullPointerException e) {
                Log.e("null thumbnail", String.valueOf(e));
            }

        }
        String pLikes = postList.get(i).getpLikes(); //contains total number of likes for a post
        String pComments = postList.get(i).getpComments(); //contains total number of comments for a post


        //convert timestamp to dd/mm/yyyy HH:mm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();

        MyHolder holder1 = myHolder;
       // holder1.imageFull(postList.get(i).getpImage());
        MyHolder holder2 = myHolder;
        holder2.fullImage(postList.get(i).getpImage());
        //set data
        myHolder.uPseudonymTv.setText(uPseudonym);
        myHolder.uPracticTv.setText(uPractic);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pLikesTv.setText(pLikes +" Likes"); //i.e. 100 likes
        myHolder.pCommentsTv.setText(pComments +context.getString(R.string.comments)); //i.e. 100 comments
        //set likes for each post


      //  myHolder.pImageIv.setOnClickListener(new View.OnClickListener() {
     //       @Override
          //  public void onClick(View v) {

         //       if (v != null) {
        //            Zoomy.Builder builder = new Zoomy.Builder((Activity) context).target(v);
        //            builder.register();
       //         } else {
       //     }}
     //    });

        //set itemview cache size
        recycler_view.setItemViewCacheSize(20);


        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder.uPictureIv);
        }
        catch (Exception e) {
        }

        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView



        //handle button clicks
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });


        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                 public void onClick(View v) {
                   likePost(myHolder, i);



                                                }});


        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              some contain text and some text with images, we will handle both
                BitmapDrawable bitmapDrawable = (BitmapDrawable)myHolder.pImageIv.getDrawable();
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

        myHolder.profileLayout.setOnClickListener(v -> {
                    //click to go to ThereProfileActivity with uid, this uid is of clicked user
                    //which will be used to show user specifi data/posts
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                });

        //click like count to start PostLikedByActivity, and pass the post id
        myHolder.pLikesTv.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostLikedByActivity.class);
            intent.putExtra("postId", pId);
            context.startActivity(intent);
        });

        setLikes(myHolder, pId);




    }



    private void likePost(MyHolder myHolder, int i) {
        //example that for some reason reloads all the recycler views:
        /*

         */
        /*Get total number of likes for the post, whose like button clicked
         * if currently signed in user has not liked before
         * increase value by 1, otherwise decrease value by 1*//*

    int pLikes = Integer.parseInt(postList.get(i).getpLikes());
    mProcessLike = true;
    //get id of the post liked
    String postIde = postList.get(i).getpId();


                     likesRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (mProcessLike) {
                if (snapshot.child(postIde).hasChild(myUid)) {
                    //already liked, so remove like
                    postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                    likesRef.child(postIde).child(myUid).removeValue();
                    mProcessLike = false;
                }
                else {
                    //not liked, like it
                    postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                    likesRef.child(postIde).child(myUid).setValue("Liked"); //set any value
                    mProcessLike = false;

                    addToHisNotifications(""+uid, ""+pId, context.getString(R.string.liked));
                } }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
*/




//here is the solution
        //get the post id
        final String pId = postList.get(i).getpId();
        final String uid = postList.get(i).getUid();

        likesRef.child(pId).child(myUid).getKey();


        //add post id and uid in likes node
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                snapshot.child(pId).child(myUid).getRef().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            //already liked, so remove like
                            postsRef.child(pId).child("pLikes").setValue(""+(Integer.parseInt(postList.get(i).getpLikes())-1));
                            likesRef.child(pId).child(myUid).removeValue();
                        }
                        else {
                            //not liked, like it
                            postsRef.child(pId).child("pLikes").setValue(""+(Integer.parseInt(postList.get(i).getpLikes())+1));
                            likesRef.child(pId).child(myUid).setValue("Liked").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    addToHisNotifications(""+uid, ""+pId, context.getString(R.string.liked), i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

/*
        */
    }

   /* private void setVideoToVideoView(MyHolder holder, ModelPost postVideo) {
      //  DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
     //   float dpHeight = displayMetrics.heightPixels*3 / displayMetrics.density;

    //    ViewGroup.LayoutParams layoutParams = holder.videoView.getLayoutParams();
     //   layoutParams.height = (int) dpHeight/2;
     //   holder.videoView.setLayoutParams(layoutParams);

          videoUrl = postVideo.getpVideo();


       // holder.pImageIv.setVisibility(View.GONE);
       // holder.videoView.setVisibility(View.VISIBLE);

      //  Uri videoUri = Uri.parse(videoUrl);
    //    holder.videoView.setVideoURI(videoUri);
     //   holder.videoView.requestFocus();

        try {
            long thumb = holder.getLayoutPosition()* 1000L;
            RequestOptions options = new RequestOptions().frame(thumb);

            Glide.with(context).load(videoUrl).apply(options).fitCenter().centerCrop().into(holder.pImageIv);
            holder.playBtn.setVisibility(View.VISIBLE);

        }
        catch(NullPointerException e) {
            Log.e("null thumbnail", String.valueOf(e));
        }


       *//* holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        mediaController = new MediaController(context);
                        holder.videoView.setMediaController(mediaController);
                        mediaController.setAnchorView(holder.videoView);
                    }
                });
                holder.videoView.start();

            }
        });*//*

    }*/


    private void addToHisNotifications(String hisUid, String pId, String notification, int i) {
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
        sIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.enviadopor)); //in case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        context.startActivity(Intent.createChooser(sIntent, context.getString(R.string.compartirvia))); //message to show in share dialog
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
        sIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.sujeto)); //in case you share via email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        sIntent.setType("image/png"); //text to share
        context.startActivity(Intent.createChooser(sIntent, context.getString(R.string.compartirvia))); //message to show in share dialog

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdir(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.android.yoganetwork.fileprovider", file);
        }
        catch (Exception e) {
            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }


    //add a key named "pLikes" to each post and set its value to "0" manually in firebase

    private void setLikes(MyHolder holder, String postKey) {


        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    /*user has liked this post
                    *//*To indicate that the post is liked by this (SignedIn) user
                    * Change drawable left icon of like button
                    * Change text of like button from "like" to "liked"*/
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    /*user has noy liked this post
                    *//*To indicate that the post is not liked by this (SignedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button from "liked" to "like"*/
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.likeBtn.setText("LIKE");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage, String pVideo) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //add items in menu
        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, context.getString(R.string.eliminar));
            popupMenu.getMenu().add(Menu.NONE, 1, 0, context.getString(R.string.editar));

        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, context.getString(R.string.details));


        //item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id==0) {
                //delete is clicked
                beginDelete(pId, pImage, pVideo);
            }
            else   if (id==1) {
                //edit is clicked
                //start AddPostActivity with key "editPost" and the if of the post clicked
                Intent intent = new Intent(context, AddPostActivity.class);
                        intent.putExtra("key", "editPost");
                         intent.putExtra("editPostId", pId);
                         context.startActivity(intent);
            }
            else if (id==2) {
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }

            return false;
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage, String pVideo) {
        //post can be with or without image
        if (!isEmpty(pImage)) {
            if (pImage.equals("noImage")) {

                deleteWithoutImage(pId);
            }else {
                //post is with image
                deleteWithImageOrVideo(pId, pImage, pVideo);
            }
        } else {
            //post is with image
            deleteWithImageOrVideo(pId, pImage, pVideo);
        }


    }

    private void deleteWithImageOrVideo(String pId, String pImage, String pVideo) {
        //progress bar
        StorageReference picRef = null;
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.eliminando));
        /*Steps
        * 1)Delete image using url
        * 2)Delete from database using post id*/
        if (!isEmpty(pImage)) {
            picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        } else if (!isEmpty(pVideo)) {
            picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pVideo);
        } else {
            Toast.makeText(context, "Error uppon deleting video or image", Toast.LENGTH_SHORT).show();
        }
        assert picRef != null;
        picRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //image deleted, now delete database

                    Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                    fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                ds.getRef().removeValue(); //remove values from firebase where pId matches
                            }
                            //deleted
                            Toast.makeText(context, context.getString(R.string.eliminado), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

        }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.eliminando));

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pId matches
                }
                //deleted
                Toast.makeText(context, context.getString(R.string.eliminado), Toast.LENGTH_SHORT).show();
                pd.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }



    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //views from row_post.xml

        ImageView uPictureIv, pImageIv, imageView2, ic_arrow_left, ic_arrow_right;
        TextView uPseudonymTv, uPracticTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
        ImageButton moreBtn, zoomInBtn, zoomOutBtn;
        Button likeBtn, commentBtn, shareBtn,playBtn;
        LinearLayout profileLayout;
        VideoView videoView;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            videoView = itemView.findViewById(R.id.videoView);
            playBtn = itemView.findViewById(R.id.playBtn);


            ic_arrow_left = itemView.findViewById(R.id.ic_arrow_left);
            ic_arrow_right = itemView.findViewById(R.id.ic_arrow_right);
            imageView2 = itemView.findViewById(R.id.imageView2);
            uPseudonymTv = itemView.findViewById(R.id.uPseudonymTv);
            uPracticTv = itemView.findViewById(R.id.uPracticTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            zoomInBtn = itemView.findViewById(R.id.zoomInBtn);
            zoomOutBtn = itemView.findViewById(R.id.zoomOutBtn);


    }



        public void fullImage(String getpImage) {
            zoomInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pImage = getpImage;
                    if (pImage != null) {


                        imageView2.setVisibility(View.VISIBLE);
                        Picasso.get().load(pImage).into(imageView2, new Callback() {
                            @Override
                            public void onSuccess() {
                                pImageIv.setVisibility(View.GONE);
                                ivsizetofs();
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                        Zoomy.Builder builder = new Zoomy.Builder((Activity) context)
                                .target(imageView2);
                        builder.register();
                        zoomInBtn.setVisibility(View.GONE);
                        zoomOutBtn.setVisibility(View.VISIBLE);
                    }

                }
            });
            zoomOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pImage = getpImage;
                    if (pImage != null) {


                        pImageIv.setVisibility(View.VISIBLE);
                        imageView2.setVisibility(View.GONE);
                        Picasso.get().load(pImage).into(imageView2);
                        zoomInBtn.setVisibility(View.VISIBLE);
                        zoomOutBtn.setVisibility(View.GONE);
                    }

                }
            });
        }


        private void ivsizetofs() {
            ViewTreeObserver vto = imageView2.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    imageView2.getViewTreeObserver().removeOnPreDrawListener(this);
                    int finalHeight = imageView2.getMeasuredHeight()/2;
                    int finalWidth = imageView2.getMeasuredWidth()/2;
                    ic_arrow_left.getLayoutParams().width = imageView2.getMeasuredWidth();
                    ic_arrow_left.getLayoutParams().height = imageView2.getMeasuredHeight();
                    ic_arrow_right.getLayoutParams().width = imageView2.getMeasuredWidth();
                    ic_arrow_right.getLayoutParams().height = imageView2.getMeasuredHeight();

                    fullScreenAnimation(finalWidth, finalHeight);
                    return true;
                }
            });
        }

        private void fullScreenAnimation(int imageWidth, int imageHeight) {
                            ic_arrow_right.setVisibility(View.VISIBLE);
                            ViewPropertyAnimator animator2 = ic_arrow_right.animate()
                                    .translationX(imageWidth)
                                    .translationY(imageHeight)
                                    .setInterpolator(new AccelerateInterpolator())
                                    .setInterpolator(new BounceInterpolator())
                                    .setDuration(1000);
                            animator2
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            ic_arrow_right.setVisibility(View.GONE);
                                            ic_arrow_right.animate()
                                                    .translationX(0)
                                                    .translationY(0)
                                                    .setDuration(0)
                                                    .start();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    })
                                    .start();
            ic_arrow_left.setVisibility(View.VISIBLE);
            ViewPropertyAnimator animator1 = ic_arrow_left.animate()
                    .translationX(-imageWidth)
                    .translationY(-imageHeight)
                    .setInterpolator(new AccelerateInterpolator())
                    .setInterpolator(new BounceInterpolator())
                    .setDuration(1000);
            animator1
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ic_arrow_left.setVisibility(View.GONE);
                            ic_arrow_left.animate()
                                    .translationX(0)
                                    .translationY(0)
                                    .setDuration(0)
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();
                        }
        }

    }
