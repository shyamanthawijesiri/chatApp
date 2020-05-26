package com.abc.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mInput;
    private Button mSavebtn;

    //firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //prograess dialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar)findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //get user id
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mInput = (TextInputLayout)findViewById(R.id.status_input_txt);
        mSavebtn = (Button)findViewById(R.id.status_save_btn);

        String status_val = getIntent().getStringExtra("status_val");
        mInput.getEditText().setText(status_val);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("saving changes");
                mProgressDialog.setMessage("Plese Wait Until Saving is Finished!!!");
                mProgressDialog.show();

                String status = mInput.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                        }else {
                            Toast.makeText(StatusActivity.this,"Saving is Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });



    }
}
