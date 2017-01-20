package com.presto.p2048;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;

import com.presto.p2048.firebase.FireBaseHelper;
import com.presto.p2048.util.Util;

public class InputListener implements View.OnTouchListener {

    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 25;
    private static final int MOVE_THRESHOLD = 250;
    private static final int RESET_STARTING = 10;

    private float x;
    private float y;
    private float lastdx;
    private float lastdy;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private int previousDirection = 1;
    private int veryLastDirection = 1;
    private boolean hasMoved = false;

    MainView mView;

    public InputListener(MainView view) {
        super();
        this.mView = view;
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastdx = 0;
                lastdy = 0;
                hasMoved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                if (mView.game.isActive()) {
                    float dx = x - previousX;
                    if (Math.abs(lastdx + dx) < Math.abs(lastdx) + Math.abs(dx)
                            && Math.abs(dx) > RESET_STARTING
                            && Math.abs(x - startingX) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastdx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastdx == 0) {
                        lastdx = dx;
                    }
                    float dy = y - previousY;
                    if (Math.abs(lastdy + dy) < Math.abs(lastdy) + Math.abs(dy)
                            && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastdy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastdy == 0) {
                        lastdy = dy;
                    }
                    if (pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE) {
                        boolean moved = false;
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || y
                                - startingY >= MOVE_THRESHOLD)
                                && previousDirection % 2 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            mView.game.move(2);
                        } else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || y
                                - startingY <= -MOVE_THRESHOLD)
                                && previousDirection % 3 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            mView.game.move(0);
                        } else if (((dx >= SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || x
                                - startingX >= MOVE_THRESHOLD)
                                && previousDirection % 5 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            mView.game.move(1);
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && previousDirection == 1) || x
                                - startingX <= -MOVE_THRESHOLD)
                                && previousDirection % 7 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            mView.game.move(3);
                        }
                        if (moved) {
                            hasMoved = true;
                            startingX = x;
                            startingY = y;
                        }
                    }
                }
                previousX = x;
                previousY = y;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;
                // "Menu" inputs
                if (!hasMoved) {
                    if (iconPressed(mView.sXNewGame, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_new_game", "new_game");
                        handleNewGameIconPress();
                    } else if (iconPressed(mView.sXUndo, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_undo", "undo");
                        mView.game.revertUndoState();
                    } else if (iconPressed(mView.sXSound, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_sound", "sound");
                        mView.game.toggleSound();
                    } else if (iconPressed(mView.sXLeaderboard, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_leaderboard", "leaderboard");
                        mView.game.showLeaderBoard();
                    } else if (iconPressed(mView.sXFeedback, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_rating", "rating");
                        mView.game.showRateDialog();
                    } else if (iconPressed(mView.sXShare, mView.sYIcons)) {
                        FireBaseHelper.logEvent("button_clicked_share", "share");
                        mView.shareScore();
                    } else if (isTap(2)
                            && inRange(mView.startingX, x, mView.endingX)
                            && inRange(mView.startingY, x, mView.endingY)
                            && mView.continueButtonEnabled) {
                        mView.game.setEndlessMode();
                    }
                }
        }
        return true;
    }

    private void handleNewGameIconPress() {
        AlertDialog.Builder alert = new AlertDialog.Builder(ContextHolder.getMainActivity());
        alert.setTitle("sure?");
        alert.setMessage("Are you sure you want to start new game?");
        alert.setCancelable(false);
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mView.game.newGame();
            }
        });
        AlertDialog d = alert.show();
        //d.getWindow().setBackgroundDrawableResource(android.R.color.background_light);
    }

    private float pathMoved() {
        return (x - startingX) * (x - startingX) + (y - startingY)
                * (y - startingY);
    }

    private boolean iconPressed(int sx, int sy) {
        return isTap(1) && inRange(sx, x, sx + mView.iconSize)
                && inRange(sy, y, sy + mView.iconSize);
    }

    private boolean inRange(float starting, float check, float ending) {
        return (starting <= check && check <= ending);
    }

    private boolean isTap(int factor) {
        return pathMoved() <= mView.iconSize * factor;
    }

}
