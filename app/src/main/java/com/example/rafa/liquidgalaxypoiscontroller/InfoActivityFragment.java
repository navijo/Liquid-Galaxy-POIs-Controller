package com.example.rafa.liquidgalaxypoiscontroller;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class InfoActivityFragment extends Fragment {

    public InfoActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help, container, false);
        TextView inst = (TextView) view.findViewById(R.id.import_inst);
        TextView introduction = (TextView) view.findViewById(R.id.import_introduction);
        TextView complete_inf = (TextView) view.findViewById(R.id.complete_information);
        inst.setMovementMethod(LinkMovementMethod.getInstance());
        introduction.setMovementMethod(LinkMovementMethod.getInstance());
        complete_inf.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
