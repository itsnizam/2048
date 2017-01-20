package com.presto.p2048.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.presto.p2048.MainGame;
import com.presto.p2048.R;
import com.presto.p2048.firebase.FireBaseHelper;

public class GameOverDialog extends Dialog implements View.OnClickListener {
    MainGame mGame;
    public static boolean isVisible = false;

    public GameOverDialog(Context context, MainGame game) {
        super(context);
        this.mGame = game;
    }

    protected void onCreate(Bundle savedInstanceState) {
        FireBaseHelper.logEvent("GameOverDialog", "on_create");
        isVisible = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_game_over);
        initViews();
    }

    private void initViews() {
        TextView scoreText = (TextView) findViewById(R.id.scoreText);
        TextView highScoreText = (TextView) findViewById(R.id.highScoreText);
        ImageView leaderBoardButt = (ImageView) findViewById(R.id.leaderBoardButt);
        ImageView shareButt = (ImageView) findViewById(R.id.shareButt);
        ImageView newGameButt = (ImageView) findViewById(R.id.newGameButt);
        ImageView feedbackButt = (ImageView) findViewById(R.id.feedbackButt);

        scoreText.setText(getContext().getString(R.string.dialog_score) + " " + mGame.score);
        highScoreText.setText(getContext().getString(R.string.dialog_high_score) + " " + mGame.highScore);
        newGameButt.setOnClickListener(this);
        shareButt.setOnClickListener(this);
        leaderBoardButt.setOnClickListener(this);
        feedbackButt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FireBaseHelper.logEvent("GameOverDialog", "onClick", "" + v.getId());
        if (v.getId() == R.id.leaderBoardButt) {
            mGame.showLeaderBoard();
        } else if (v.getId() == R.id.feedbackButt) {
            mGame.showRateDialog();
        } else if (v.getId() == R.id.newGameButt) {
            dismiss();
            mGame.newGame();
        } else if (v.getId() == R.id.shareButt) {
            shareScore();
        }
    }

    public void dismiss() {
        isVisible = false;
        super.dismiss();
    }

    private void shareScore() {
        FireBaseHelper.logEvent("GameOverDialog", "shareScore");
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBody = String.format(getContext().getString(R.string.shared_body_text), "" + mGame.score);
        shareBody += " https://play.google.com/store/apps/details?id=com.presto.p2048";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "2048 offline");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        getContext().startActivity(Intent.createChooser(sharingIntent, "Share via 2048 offline"));
    }
}