package com.cos407.cs407finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SlideActivity extends AppCompatActivity {
    private ImageView myProfilePhoto;
    private SlideMenu slideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);

        myProfilePhoto = findViewById(R.id.myProfilePhoto);
        slideMenu = findViewById(R.id.mySlideMenu);

        // Hamburger icon to switch
        myProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideMenu.switchMenu();
            }
        });

        // Account to jump to MePage
        findViewById(R.id.menuAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlideActivity.this, MePage.class));
            }
        });

        // Handle Menu Click
        findViewById(R.id.menuLanguage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlideActivity.this, LanguageActivity.class));
            }
        });

        findViewById(R.id.menuAppearance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlideActivity.this, AppearanceActivity.class));
            }
        });

        findViewById(R.id.menuReport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlideActivity.this, ReportActivity.class));
            }
        });

        findViewById(R.id.menuTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SlideActivity.this, TermsActivity.class));
            }
        });
    }
}
