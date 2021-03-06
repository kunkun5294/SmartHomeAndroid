package edu.temple.m.smarthomedroid.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

import edu.temple.m.smarthomedroid.R;

/**
 * Created by Matthew White on 3/10/2017.
 */

public class SignupDialogFragment extends DialogFragment {

    private final String TAG = "Signup Dialog";
    private EditText username, password, confirmPassword;
    SignupDialogListener mListener;

    /* The activity that creates instance of dialog fragment must implement
    *  this interface in order to recieve event callbacks
     */
    public interface SignupDialogListener{
        public void onSignupDialogPositiveClick(DialogFragment dialog);
        public void onSignupDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog);
    }
    //Override the Fragment.onAttach method to instantiate listener
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        //Verify that the host activity implements the callback interface
        try{
            //Instantiate listener so events can be sent to host
            mListener = (SignupDialogListener) activity;
        } catch(ClassCastException e){
            //Activity doesn't implement
            throw new ClassCastException(activity.toString() + " must implement SignupDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Builder Class for dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Get Layout inflater for custom layout
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        //Inflate dialog with custom layout
        //null for parent view as in dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_signup, null))
                .setPositiveButton("Sign up", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        //Send the positive button event back to host activity
                        mListener.onSignupDialogPositiveClick(SignupDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        //send the negative button event back to host activity
                        mListener.onSignupDialogNegativeClick(SignupDialogFragment.this);
                    }
                });
        //Create Dialog object and return it
        return builder.create();
    }


}
