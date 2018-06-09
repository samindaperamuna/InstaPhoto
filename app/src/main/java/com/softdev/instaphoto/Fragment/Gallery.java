package com.softdev.instaphoto.Fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.softdev.instaphoto.Activity.Publish;
import com.softdev.instaphoto.R;

import static android.app.Activity.RESULT_OK;

public class Gallery extends Fragment {

    Button btnGallery;
    Button btnCamera;

    public Gallery() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        btnGallery = (Button) v.findViewById(R.id.btnGallery);
        btnCamera = (Button) v.findViewById(R.id.btnCamera);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoCaptureIntent, 2);
            }
        });
        return v;
    }

    private void showFileChooser() {
        if (grantPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Intent publishIntent = new Intent(getActivity(), Publish.class);
            publishIntent.putExtra("photoUri", filePath.toString());
            startActivity(publishIntent);
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            Intent publishIntent = new Intent(getActivity(), Publish.class);
            publishIntent.putExtra("photoUri", filePath.toString());
            startActivity(publishIntent);
        }
    }

    private boolean grantPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getActivity(), "Permission denied for certain usages within the app. The app will close now.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
