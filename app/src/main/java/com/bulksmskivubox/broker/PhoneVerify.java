package com.bulksmskivubox.broker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class PhoneVerify extends AppCompatActivity {

    CountryCodePicker ccp;
    EditText phone;
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verify);

        ccp = findViewById(R.id.ccp);
        phone = findViewById(R.id.phone);
        send = findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ccp.getSelectedCountryCodeWithPlus().isEmpty()){
                    Toast.makeText(PhoneVerify.this, "A country code is needed", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(phone.getText().toString().isEmpty()){
                    phone.setError("The telephone is required");
                    return;
                }

                String telephone = ccp.getSelectedCountryCodeWithPlus()+phone.getText();

                startActivity(new Intent(PhoneVerify.this,CodeVerify.class).putExtra("telephone",telephone));

            }
        });


    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
