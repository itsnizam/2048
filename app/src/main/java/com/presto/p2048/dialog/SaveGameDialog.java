package com.presto.p2048.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.presto.p2048.ContextHolder;
import com.presto.p2048.MainGame;
import com.presto.p2048.R;
import com.presto.p2048.firebase.TelemetryHelper;
import com.presto.p2048.modal.SavedGameEntity;
import com.presto.p2048.util.SavedGameHelper;

import java.util.Date;

public class SaveGameDialog extends Dialog implements View.OnClickListener {
    private static final int MAX_SAVED_COUNT = 20;
    public static boolean isVisible = false;
    private boolean mSaving = false;
    private MainGame mGame;
    private ProgressDialog mProgressDialog;
    private EditText gameNameTextInput;
    private TextView errorText;

    public SaveGameDialog(Context context, MainGame game) {
        super(context);
        this.mGame = game;
    }

    protected void onCreate(Bundle savedInstanceState) {
        TelemetryHelper.logEvent("SaveGameDialog", "on_create");
        isVisible = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_save_game);
        initViews();
        addListeners();
    }

    private void initViews() {
        gameNameTextInput = (EditText) findViewById(R.id.dialog_save_text_name);
        errorText = (TextView) findViewById(R.id.dialog_save_errorText);
        errorText.setVisibility(View.GONE);
    }

    private void addListeners() {
        ((TextView) findViewById(R.id.dialog_save_button_save)).setOnClickListener(this);
        ((TextView) findViewById(R.id.dialog_save_button_cancel)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.dialog_save_button_save) {
            TelemetryHelper.logEvent("dialog_save_game_positive");
            onSaveCurrentGameButtonClicked();
        } else if (v.getId() == R.id.dialog_save_button_cancel) {
            TelemetryHelper.logEvent("dialog_save_game_negative");
            dismiss();
        }
    }

    private void onSaveCurrentGameButtonClicked() {
        if (this.mSaving)
            return;

        SavedGameHelper sgm = new SavedGameHelper(ContextHolder.getMainActivity());
        String name = "" + gameNameTextInput.getText();

        if (sgm.getTotalSavedGameCount() >= MAX_SAVED_COUNT) {
            String countLimitString = String.format(getContext().getString(R.string.dialog_save_game_error_count_limit), "" + MAX_SAVED_COUNT);
            errorText.setText(countLimitString);
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        if (name.length() == 0) {
            errorText.setText(getContext().getString(R.string.dialog_save_game_error_empty_name));
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        if (!SavedGameEntity.isValidName(name)) {
            errorText.setText(getContext().getString(R.string.dialog_save_game_error_invalid_name));
            errorText.setVisibility(View.VISIBLE);
            return;
        }
        errorText.setVisibility(View.GONE);
        this.mSaving = true;
        saveCurrentGame(name, sgm);
        dismiss();
    }

    private void saveCurrentGame(String gameHandle, SavedGameHelper sgm) {
        String prefName = "" + new Date().getTime();
        sgm.saveGame(mGame, gameHandle, prefName);
    }

    public void dismiss() {
        isVisible = false;
        super.dismiss();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null)
            initProgressbar();
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void initProgressbar() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getContext().getString(R.string.dialog_save_game_loader_message));
        mProgressDialog.setCancelable(false);
    }


}