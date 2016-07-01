package com.example.rafa.liquidgalaxypoiscontroller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;


/**
 * Created by lgwork on 29/06/16.
 */
public class TerminalFragment extends Fragment{

    ScrollView scrollView;
    TextView textView;
    EditText editText;
    ImageButton sendCommandButton;

    String commandResult;

    Session session;
    ChannelShell channelShell;

    public static TerminalFragment newInstance() {

        Bundle args = new Bundle();

        TerminalFragment fragment = new TerminalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.console_layout,container,false);

        commandResult = new String();

        scrollView = (ScrollView)view.findViewById(R.id.terminalScrollView);
        textView = (TextView)view.findViewById(R.id.terminalTextView);
        editText = (EditText)view.findViewById(R.id.terminalTextEdit);
        sendCommandButton = (ImageButton)view.findViewById(R.id.terminalSendCommand);

        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = editText.getText().toString();
                String previousString = textView.getText().toString();
                String nextString = previousString + "\n" + "$"+command;
                textView.setText(nextString);
                editText.getText().clear();
                scrollView.fullScroll(View.FOCUS_DOWN);

                SendCommandTask sendCommand = new SendCommandTask(command,session);
                sendCommand.execute();

                View focusView = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }
            }
        });
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

            GetLGConnectionTask getLGConnectionTask = new GetLGConnectionTask();
            getLGConnectionTask.execute();

    }

    private class SendCommandTask extends AsyncTask<Void, Void, String> {

        String command;
        Session session;
        private ProgressDialog dialog;

        public SendCommandTask(String command, Session session) {
            this.command = command;
            this.session = session;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.sending_command));
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                dialog.show();
            }
        }



        @Override
        protected String doInBackground(Void... params) {
            try {
                return sendCommandToLG(command);
            } catch (JSchException e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                Toast.makeText(getActivity(), getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
                return null;
            } catch (IOException e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                cancel(true);
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String returnedString) {
            super.onPostExecute(returnedString);
            if (returnedString!=null) {
                commandResult = returnedString;

                String previousString = textView.getText().toString();
                String nextString = previousString + "\n" + commandResult;
                textView.setText(nextString);

                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
        }

        private String sendCommandToLG(String command) throws Exception {


            if(this.session==null){

                GetLGConnectionTask getLGConnectionTask = new GetLGConnectionTask();
                getLGConnectionTask.execute();
            }

            if(!this.session.isConnected()){
                this.session.connect();
            }


            if(channelShell==null) {
                channelShell = (ChannelShell) this.session.openChannel("shell");
            }

            InputStream in = new PipedInputStream();
            PipedOutputStream pin = new PipedOutputStream((PipedInputStream) in);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            channelShell.setOutputStream(baos);

            channelShell.setInputStream(in);

            if(!channelShell.isConnected()){

                channelShell.connect();
            }

            //channelShell.connect();
            pin.write(command.getBytes());
//
//            baos.flush();
//
            channelShell.disconnect();

            return baos.toString();
        }

    }




    private class GetLGConnectionTask extends AsyncTask<Void, Void, Session> {

        private ProgressDialog dialog;

        public GetLGConnectionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.getting_connection));
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                dialog.show();
            }
        }



        @Override
        protected Session doInBackground(Void... params) {
            try {
                return setConnectionWithLiquidGalaxy();
            } catch (JSchException e) {
                cancel(true);
                Toast.makeText(getActivity(), getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Session pSession) {
            super.onPostExecute(pSession);
            if (pSession!=null) {
                session = pSession;

                SendCommandTask sendCommand = new SendCommandTask("",session);
                sendCommand.execute();

                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
        }

        private Session setConnectionWithLiquidGalaxy() throws JSchException, IOException {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String user = prefs.getString("User", "lg");
            String password = prefs.getString("Password", "lqgalaxy");
            String hostname = prefs.getString("HostName", "172.26.17.21");
            int port = Integer.parseInt(prefs.getString("Port", "22"));

            JSch jsch = new JSch();

            Session session = jsch.getSession(user, hostname, port);
            session.setPassword(password);

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect(1000000000);

            return session;
        }

    }
}
