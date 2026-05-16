package com.total.overiden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BSPAirCoverDialogFragment extends DialogFragment {
    private final List<BSPStrike> strikes;
    private final List<BSPStrike> cover;
    public BSPAirCoverDialogFragment(List<BSPStrike> strikes, List<BSPStrike> cover) {
        super();
        this.strikes = strikes;
        this.cover = cover;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater.
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
        View view = inflater.inflate(R.layout.fragment_bsp_air_cover_dialog, null);
        RecyclerView cards = view.findViewById(R.id.strike_cards);
        cards.setAdapter(new BspCounterAdapter(strikes, cover));
        cards.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));

        builder.setView(view).setPositiveButton("Lock", (dialog, id) -> {
            // need to go through each card and assign the air cover for each one
            BspCounterAdapter adapter = (BspCounterAdapter) cards.getAdapter();
            if (adapter!=null)adapter.complete();

            getParentFragmentManager().setFragmentResult("counter",new Bundle());
        });
        return builder.create();
    }

}
