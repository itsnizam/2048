package com.presto.p2048.rating;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.presto.p2048.R;
import com.presto.p2048.firebase.FireBaseHelper;
import com.presto.p2048.task.FeedbackSenderTask;

public class RatingDialog extends Dialog implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {

    private static final String SESSION_COUNT = "session_count";
    private static final String SHOW_NEVER = "show_never";
    private String MyPrefs = "RatingDialog";
    private SharedPreferences sharedpreferences;

    private Context context;
    private Builder builder;
    private TextView tvTitle, tvNegative, tvPositive, tvFeedback, tvSubmit, tvCancel;
    private RatingBar ratingBar;
    private ImageView ivIcon;
    private EditText etFeedback;
    private LinearLayout ratingButtons, feedbackButtons;

    private String defaultTitle = "How would you like to rate this app?";
    private String defaultPositiveText = ""; //"Maybe Later";
    private String defaultNegativeText = "";
    private String defaultFormTitle = "Feedback";
    private String defaultSubmitText = "Submit";
    private String defaultCancelText = "Cancel";
    private String defaultHint = "Suggest us what went wrong and we'll work on it.";

    private float threshold;
    private int session;
    private boolean thresholdPassed = true;

    public RatingDialog(Context context, Builder builder) {
        super(context);
        this.context = context;
        this.builder = builder;
        this.session = builder.session;
        this.threshold = builder.threshold;
    }

    public interface RatingDialogListener {
        void onRatingSelected(float rating, boolean thresholdCleared);
    }

    public interface RatingDialogFormListener {
        void onFormSubmitted(String feedback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FireBaseHelper.logEvent("rating", "on_create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_rating);

        tvTitle = (TextView) findViewById(R.id.dialog_rating_title);
        tvNegative = (TextView) findViewById(R.id.dialog_rating_button_negative);
        tvPositive = (TextView) findViewById(R.id.dialog_rating_button_positive);
        tvFeedback = (TextView) findViewById(R.id.dialog_rating_feedback_title);
        tvSubmit = (TextView) findViewById(R.id.dialog_rating_button_feedback_submit);
        tvCancel = (TextView) findViewById(R.id.dialog_rating_button_feedback_cancel);
        ratingBar = (RatingBar) findViewById(R.id.dialog_rating_rating_bar);
        ivIcon = (ImageView) findViewById(R.id.dialog_rating_icon);
        etFeedback = (EditText) findViewById(R.id.dialog_rating_feedback);
        ratingButtons = (LinearLayout) findViewById(R.id.dialog_rating_buttons);
        feedbackButtons = (LinearLayout) findViewById(R.id.dialog_rating_feedback_buttons);

        init();
    }

    private void init() {

        tvTitle.setText(TextUtils.isEmpty(builder.title) ? defaultTitle : builder.title);
        tvPositive.setText(TextUtils.isEmpty(builder.positiveText) ? defaultPositiveText : builder.positiveText);
        tvNegative.setText(TextUtils.isEmpty(builder.negativeText) ? defaultNegativeText : builder.negativeText);

        tvFeedback.setText(TextUtils.isEmpty(builder.formTitle) ? defaultFormTitle : builder.formTitle);
        tvSubmit.setText(TextUtils.isEmpty(builder.submitText) ? defaultSubmitText : builder.submitText);
        tvCancel.setText(TextUtils.isEmpty(builder.cancelText) ? defaultCancelText : builder.cancelText);
        etFeedback.setHint(TextUtils.isEmpty(builder.feedbackFormHint) ? defaultHint : builder.feedbackFormHint);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;

        tvTitle.setTextColor(builder.titleTextColor != 0 ? ContextCompat.getColor(context, builder.titleTextColor) : ContextCompat.getColor(context, R.color.black));
        tvPositive.setTextColor(builder.positiveTextColor != 0 ? ContextCompat.getColor(context, builder.positiveTextColor) : color);
        tvNegative.setTextColor(builder.negativeTextColor != 0 ? ContextCompat.getColor(context, builder.negativeTextColor) : ContextCompat.getColor(context, R.color.grey_500));

        tvFeedback.setTextColor(builder.titleTextColor != 0 ? ContextCompat.getColor(context, builder.titleTextColor) : ContextCompat.getColor(context, R.color.black));
        //tvSubmit.setTextColor(builder.positiveTextColor != 0 ? ContextCompat.getColor(context, builder.positiveTextColor) : color);
        tvCancel.setTextColor(builder.negativeTextColor != 0 ? ContextCompat.getColor(context, builder.negativeTextColor) : ContextCompat.getColor(context, R.color.grey_500));

        if (builder.positiveBackgroundColor != 0) {
            tvPositive.setBackgroundResource(builder.positiveBackgroundColor);
            tvSubmit.setBackgroundResource(builder.positiveBackgroundColor);

        }
        if (builder.negativeBackgroundColor != 0) {
            tvNegative.setBackgroundResource(builder.negativeBackgroundColor);
            tvCancel.setBackgroundResource(builder.negativeBackgroundColor);
        }

        if (builder.ratingBarColor != 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
                stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, builder.ratingBarColor), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(1).setColorFilter(ContextCompat.getColor(context, builder.ratingBarColor), PorterDuff.Mode.SRC_ATOP);
                stars.getDrawable(0).setColorFilter(ContextCompat.getColor(context, R.color.grey_200), PorterDuff.Mode.SRC_ATOP);
            } else {
                Drawable stars = ratingBar.getProgressDrawable();
                DrawableCompat.setTint(stars, ContextCompat.getColor(context, builder.ratingBarColor));
            }
        }

        Drawable d = context.getPackageManager().getApplicationIcon(context.getApplicationInfo());
        ivIcon.setImageDrawable(builder.drawable != null ? builder.drawable : d);

        ratingBar.setOnRatingBarChangeListener(this);
        tvPositive.setOnClickListener(this);
        tvNegative.setOnClickListener(this);
        tvSubmit.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //TODO cleanup remove dialog_rating_button_negative and dialog_rating_button_positive
        if (view.getId() == R.id.dialog_rating_button_negative) {
            FireBaseHelper.logEvent("rating_button_clicked", "dialog_rating_button_negative", "impossile");
            //do nothing
//            dismiss();
//            showNever();

        } else if (view.getId() == R.id.dialog_rating_button_positive) {
            FireBaseHelper.logEvent("rating_button_clicked", "dialog_rating_button_positive", "impossible");
            //do nothing
            //dismiss();
        } else if (view.getId() == R.id.dialog_rating_button_feedback_submit) {
            String feedback = etFeedback.getText().toString().trim();
            FireBaseHelper.logEvent("rating_button_clicked", "dialog_rating_button_feedback_submit");
            if (TextUtils.isEmpty(feedback)) {
                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                etFeedback.startAnimation(shake);
                return;
            }
            if (builder.ratingDialogFormListener != null) {
                FireBaseHelper.logEvent("rating_button_clicked", "feedback_submit", "impossible");
                builder.ratingDialogFormListener.onFormSubmitted(feedback);
            } else {
                FireBaseHelper.logEvent("rating_button_clicked", "email");
                sendEmail(feedback);
            }

            dismiss();
            showNever();
        } else if (view.getId() == R.id.dialog_rating_button_feedback_cancel) {
            FireBaseHelper.logEvent("rating_button_clicked", "dismiss");
            dismiss();
        }
    }

    private void sendEmail(String feedback) {
        new FeedbackSenderTask(feedback).execute();
    }


    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
        FireBaseHelper.logEvent("rating_button_clicked_onRatingChanged", "onRatingChanged", "rating:" + ratingBar.getRating());
        if (ratingBar.getRating() >= threshold) {
            thresholdPassed = true;
            openPlaystore(context);
            dismiss();
        } else {
            thresholdPassed = false;
            openForm();
        }
        if (builder.ratingDialogListener != null) {
            builder.ratingDialogListener.onRatingSelected(ratingBar.getRating(), thresholdPassed);
        }
        showNever();
    }

    private void openForm() {
        tvFeedback.setVisibility(View.VISIBLE);
        etFeedback.setVisibility(View.VISIBLE);
        feedbackButtons.setVisibility(View.VISIBLE);
        ratingButtons.setVisibility(View.GONE);
        ivIcon.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);
        ratingBar.setVisibility(View.GONE);
    }

    private void openPlaystore(Context context) {
        try {
            final Uri marketUri = Uri.parse("market://details?id=" + context.getPackageName());
            context.startActivity(new Intent(Intent.ACTION_VIEW, marketUri));
        } catch (Exception e) {
            FireBaseHelper.logEvent("Exception", "open_playstore", e.getMessage());
        }

    }

    public TextView getTitleTextView() {
        return tvTitle;
    }

    public TextView getPositiveButtonTextView() {
        return tvPositive;
    }

    public TextView getNegativeButtonTextView() {
        return tvNegative;
    }

    public TextView getFormTitleTextView() {
        return tvFeedback;
    }

    public TextView getFormSumbitTextView() {
        return tvSubmit;
    }

    public TextView getFormCancelTextView() {
        return tvCancel;
    }

    public ImageView getIconImageView() {
        return ivIcon;
    }

    public RatingBar getRatingBarView() {
        return ratingBar;
    }

    @Override
    public void show() {
        super.show();
        int i = 1;
        if (i == 1)
            return;
        if (checkIfSessionMatches(session)) {
            super.show();
        }
    }

    private boolean checkIfSessionMatches(int session) {

        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        if (sharedpreferences.getBoolean(SHOW_NEVER, false)) {
            return false;
        }

        int count = sharedpreferences.getInt(SESSION_COUNT, 1);

        if (session == count) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, 1);
            editor.commit();
            return true;
        } else {
            count++;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, count);
            editor.commit();
            return false;
        }
    }

    private void showNever() {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(SHOW_NEVER, true);
        editor.commit();
    }

    public static class Builder {

        private final Context context;
        private String title, positiveText, negativeText;
        private String formTitle, submitText, cancelText, feedbackFormHint;
        private int positiveTextColor, negativeTextColor, titleTextColor, ratingBarColor;
        private int positiveBackgroundColor, negativeBackgroundColor;
        private RatingDialogListener ratingDialogListener;
        private RatingDialogFormListener ratingDialogFormListener;
        private Drawable drawable;

        private int session = 1;
        private float threshold = 1;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder session(int session) {
            this.session = session;
            return this;
        }

        public Builder threshold(float threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /*public Builder icon(int icon) {
            this.icon = icon;
            return this;
        }*/

        public Builder icon(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        public Builder positiveButtonText(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder negativeButtonText(String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public Builder titleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public Builder positiveButtonTextColor(int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
        }

        public Builder negativeButtonTextColor(int negativeTextColor) {
            this.negativeTextColor = negativeTextColor;
            return this;
        }

        public Builder positiveButtonBackgroundColor(int positiveBackgroundColor) {
            this.positiveBackgroundColor = positiveBackgroundColor;
            return this;
        }

        public Builder negativeButtonBackgroundColor(int negativeBackgroundColor) {
            this.negativeBackgroundColor = negativeBackgroundColor;
            return this;
        }


        public Builder onRatingChanged(RatingDialogListener ratingDialogListener) {
            this.ratingDialogListener = ratingDialogListener;
            return this;
        }

        public Builder onRatingBarFormSumbit(RatingDialogFormListener ratingDialogFormListener) {
            this.ratingDialogFormListener = ratingDialogFormListener;
            return this;
        }

        public Builder formTitle(String formTitle) {
            this.formTitle = formTitle;
            return this;
        }

        public Builder formHint(String formHint) {
            this.feedbackFormHint = formHint;
            return this;
        }

        public Builder formSubmitText(String submitText) {
            this.submitText = submitText;
            return this;
        }

        public Builder formCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public Builder ratingBarColor(int ratingBarColor) {
            this.ratingBarColor = ratingBarColor;
            return this;
        }

        public RatingDialog build() {
            return new RatingDialog(context, this);
        }
    }
}
