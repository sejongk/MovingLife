package com.example.tb990.shortpathalarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Permission extends AppCompatActivity{
   String[] permission_list = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),1);
        }else{
            Intent intent = new Intent(this, setVelocity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(checkPermission()){
            Intent intent = new Intent(this, setVelocity.class);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(this, "앱 이용을 위해 모든 관리권한을 활성화 해주세요", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private boolean checkPermission(){
        boolean check = true;
        for (String permission : permission_list){
            int chk = checkCallingOrSelfPermission(permission);
            if (chk == PackageManager.PERMISSION_DENIED) check = false;
        }
        return check;
    }
}
