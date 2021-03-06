package edu.temple.m.smarthomedroid.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.m.smarthomedroid.Handlers.HttpHandler;
import edu.temple.m.smarthomedroid.Handlers.refresh;
import edu.temple.m.smarthomedroid.R;

import static edu.temple.m.smarthomedroid.SystemSettingsFragment.mpass2;
import static java.lang.Thread.sleep;

/**
 * Created by Matthew White on 3/17/2017.
 */

public class RemoveBoardDialog extends DialogFragment {
    private String board,sessionID,housename;
    private String error= " ";
    private final String TAG = "ChangeBoardDialog";
    private EditText username, password;
    private refresh update;
    private int done=0;

    private synchronized void setdone(int n){
        this.done=n;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        update = (refresh) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Builder Class for dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        board = getArguments().getString("OldBoard");
        sessionID = getArguments().getString("SessionToken");
        housename = getArguments().getString("HouseName");
        //Get a Layout Inflater for custom layout
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setMessage("Are you sure that you want to remove this board?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        new remove().execute();
                        while(done==0){
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        setdone(0);
                        if(error.equals("no")){
                            mpass2.msg2("Removed Board Successfully");
                            RemoveBoardDialog.this.getDialog().cancel();
                            update.config_update();
                        }else{
                            RemoveBoardDialog.this.getDialog().cancel();
                            mpass2.msg2(error);
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        RemoveBoardDialog.this.getDialog().cancel();
                    }
                });
        //Create Dialog object and return it
        return builder.create();
    }

    private class remove extends AsyncTask<Void, Void, Void> {
        JSONObject jsonObject = new JSONObject();
        String session = sessionID;
        String housena = housename;
        String bname = board;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try{
                jsonObject.put("SessionToken", session)
                    .put("HouseName",housena)
                    .put("BoardName",bname);
            } catch(JSONException e){
                Log.e(TAG, "JSONException: " + e.getMessage());
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpHandler sh = new HttpHandler();
            //Make a request to url and get response
            Log.d(TAG, "object : " + jsonObject.toString());
                String resp = sh.makePostCall("https://zvgalu45ka.execute-api.us-east-1.amazonaws.com/prod/board/removeboard", jsonObject);
                if (resp != null) {
                    Log.d(TAG, "remove : " + resp);
                    if(resp.equals("\"0 No Errors\"")){
                        error="no";
                    }else{
                        error=resp;
                    }
                }else{
                    error="Failed, please try again";
                }
            setdone(1);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}
