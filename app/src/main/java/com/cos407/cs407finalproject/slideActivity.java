package com.cos407.cs407finalproject;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class slideActivity extends AppCompatActivity {
    private ImageView myProfilePhoto;
    private slideMenu slideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);


        myProfilePhoto = findViewById(R.id.myProfilePhoto);
        slideMenu = findViewById(R.id.mySlideMenu);

        //implement a slide
        myProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideMenu.switchMenu();
            }
        });
    }

}
