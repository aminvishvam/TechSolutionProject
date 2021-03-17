package com.example.techsolutionproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetUserFragment extends BottomSheetDialogFragment {

    String mTag;

    public static BottomSheetDialogFragment newInstance(String tag) {
        BottomSheetUserFragment f = new BottomSheetUserFragment();
        Bundle args = new Bundle();
        args.putString("TAG", tag);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString("TAG");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_user, container, false);
        return view;
    }
}
