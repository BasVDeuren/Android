package com.gunit.spacecrack.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.interfacerequest.IVerifyCallBack;
import com.gunit.spacecrack.task.VerifyTask;

/**
 * Created by Tim on 20/03/14.
 */
public class VerifyFragment extends Fragment implements IVerifyCallBack{

    private Activity context;
    private EditText edtVerificationCode;
    private Button btnVerify;
    private IVerifyCallBack verifyCallBack = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify, container, false);

        context = getActivity();

        edtVerificationCode = (EditText) view.findViewById(R.id.edt_verify_verificationCode);

        btnVerify = (Button) view.findViewById(R.id.btn_verify_verify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtVerificationCode.getText().toString().equals("")) {

                    new VerifyTask(edtVerificationCode.getText().toString(), verifyCallBack).execute();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    @Override
    public void verifyCallback(Integer statusCode) {

        if(statusCode == 200)
        {
            getActivity().getFragmentManager().beginTransaction()
                    .replace(R.id.container, new LoginFragment(), "Login")
                    .addToBackStack("VerifyFragment")
                    .commit();
        }else if(statusCode == 406){
            Toast.makeText(context,getString(R.string.invalid_verification_token), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
        }

    }
}
