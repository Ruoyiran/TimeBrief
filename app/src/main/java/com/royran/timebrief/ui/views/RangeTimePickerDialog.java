package com.royran.timebrief.ui.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.royran.timebrief.R;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

public class RangeTimePickerDialog extends DialogFragment {
    private AlertDialog mAlertDialog;
    private boolean dialogDismissed;
    private TimePicker timePickerStart;
    private Button btnPositive, btnNegative;

    private int colorTextButton = R.color.Yellow;
    private int colorBackgroundTimePickerHeader = R.color.CyanWater;
    private boolean is24HourView = true;
    private String textBtnPositive = "确定";
    private String textBtnNegative = "取消";
    private int radiusDialog = 50; // Default 50
    private boolean isMinutesEnabled = true;
    private Calendar currentTime = Calendar.getInstance();
    private int initialStarHour = currentTime.get(Calendar.HOUR_OF_DAY);
    private int initialStartMinute = currentTime.get(Calendar.MINUTE);
    private boolean inputKeyboardAsDefault = false;

    public interface OnTimeSelectedListener {
        void onSelectedTime(int hour, int minute);
    }

    public RangeTimePickerDialog newInstance() {
        RangeTimePickerDialog f = new RangeTimePickerDialog();
        return f;
    }

    private OnTimeSelectedListener onTimeSelectedListener;

    public void setOnTimeSelectedListener(OnTimeSelectedListener onTimeSelectedListener) {
        this.onTimeSelectedListener = onTimeSelectedListener;
    }

    /**
     * Create a new instance with own attributes (All color MUST BE in this format "R.color.my_color")
     *
     * @param colorBackgroundHeader Color of Background header dialog and timePicker
     * @param colorTextButton       Text color of button
     * @param is24HourView          Indicates if the format should be 24 hours
     * @return
     */
    public RangeTimePickerDialog newInstance(int colorBackgroundHeader, int colorTextButton, boolean is24HourView) {
        RangeTimePickerDialog f = new RangeTimePickerDialog();
        this.colorBackgroundTimePickerHeader = colorBackgroundHeader;
        this.colorTextButton = colorTextButton;
        this.is24HourView = is24HourView;
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogView = inflater.inflate(R.layout.layout_custom_dialog, null);
        builder.setView(dialogView);
        timePickerStart = (TimePicker) dialogView.findViewById(R.id.timePickerStart);
        btnPositive = (Button) dialogView.findViewById(R.id.btnPositiveDialog);
        btnNegative = (Button) dialogView.findViewById(R.id.btnNegativeDialog);
        CardView cardView = (CardView) dialogView.findViewById(R.id.ly_root);

        // Set TimePicker header background color
        setTimePickerHeaderBackgroundColor(this, ContextCompat.getColor(getActivity(), colorBackgroundTimePickerHeader), "timePickerStart");
        setTimePickerHeaderBackgroundColor(this, ContextCompat.getColor(getActivity(), colorBackgroundTimePickerHeader), "timePickerEnd");

        // Set radius of dialog
        cardView.setRadius(radiusDialog);

        timePickerStart.setIs24HourView(is24HourView);

        // Set initial clock values
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerStart.setHour(initialStarHour);
            timePickerStart.setMinute(initialStartMinute);
        } else {
            timePickerStart.setCurrentHour(initialStarHour);
            timePickerStart.setCurrentMinute(initialStartMinute);
        }

        btnPositive.setTextColor(ContextCompat.getColor(getActivity(), colorTextButton));
        btnNegative.setTextColor(ContextCompat.getColor(getActivity(), colorTextButton));
        btnPositive.setText(textBtnPositive);
        btnNegative.setText(textBtnNegative);

        // Set keyboard input as default
        if (inputKeyboardAsDefault) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setInputKeyboardAsDefault("timePickerStart");
                setInputKeyboardAsDefault("timePickerEnd");
            }
        }

        // Enable/Disable minutes
        if (!isMinutesEnabled) {
            setMinutesEnabled(this, isMinutesEnabled, "timePickerStart");
            setMinutesEnabled(this, isMinutesEnabled, "timePickerEnd");
        }

        // Create the AlertDialog object and return it
        mAlertDialog = builder.create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mAlertDialog.setOnShowListener(dialog -> {
            btnNegative.setOnClickListener(v -> dismiss());
            btnPositive.setOnClickListener(v -> {
                int hourStart, minuteStart;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hourStart = timePickerStart.getHour();
                    minuteStart = timePickerStart.getMinute();
                } else {
                    hourStart = timePickerStart.getCurrentHour();
                    minuteStart = timePickerStart.getCurrentMinute();
                }
                // Return value to activity
                if (onTimeSelectedListener != null) {
                    onTimeSelectedListener.onSelectedTime(hourStart, minuteStart);
                }
                dismiss();
            });
        });
        return mAlertDialog;
    }

    /*returns difference in minutes*/
    private long getDifferenceInMinutes(Date dateStart, Date dateEnd) {
        long diff = dateEnd.getTime() - dateStart.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        return minutes;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        dialogDismissed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogDismissed && mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    /**
     * Set button text color
     *
     * @param colorTextButton (eg. R.color.my_color)
     */
    public void setColorTextButton(int colorTextButton) {
        this.colorTextButton = colorTextButton;
    }

    /**
     * Set true if you want see time into 24 hour format
     *
     * @param is24HourView true = 24 hour format, false = am/pm format
     */
    public void setIs24HourView(boolean is24HourView) {
        this.is24HourView = is24HourView;
    }

    /**
     * Set positive button text
     *
     * @param textBtnPositive (eg. Ok or Accept)
     */
    public void setTextBtnPositive(String textBtnPositive) {
        this.textBtnPositive = textBtnPositive;
    }

    /**
     * Set negative button text
     *
     * @param textBtnNegative (eg. Cancel or Close)
     */
    public void setTextBtnNegative(String textBtnNegative) {
        this.textBtnNegative = textBtnNegative;
    }

    /**
     * Set dialog radius (default is 50)
     *
     * @param radiusDialog Set to 0 if you want remove radius
     */
    public void setRadiusDialog(int radiusDialog) {
        this.radiusDialog = radiusDialog;
    }

    /**
     * Set background color of header timePicker
     *
     * @param colorBackgroundTimePickerHeader (eg. R.color.my_color)
     */
    public void setColorBackgroundTimePickerHeader(int colorBackgroundTimePickerHeader) {
        this.colorBackgroundTimePickerHeader = colorBackgroundTimePickerHeader;
    }

    /**
     * Set color of timePicker'header
     *
     * @param rangeTimePickerDialog Dialog where is located the timePicker
     * @param color                 Color to set
     * @param nameTimePicker        id of timePicker declared into xml (eg. my_time_picker [android:id="@+id/my_time_picker"])
     */
    private void setTimePickerHeaderBackgroundColor(RangeTimePickerDialog rangeTimePickerDialog, int color, String nameTimePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Field mTimePickerField;
                mTimePickerField = RangeTimePickerDialog.class.getDeclaredField(nameTimePicker);
                mTimePickerField.setAccessible(true);
                final TimePicker mTimePicker = (TimePicker) mTimePickerField.get(rangeTimePickerDialog);
                int headerId = Resources.getSystem().getIdentifier("time_header", "id", "android");
                final View header = mTimePicker.findViewById(headerId);
                header.setBackgroundColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int headerTextId = Resources.getSystem().getIdentifier("input_header", "id", "android");
                    final View headerText = mTimePicker.findViewById(headerTextId);
                    headerText.setBackgroundColor(color);
                    headerText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to enable/disable minutes into range time dialog
     *
     * @param value true = minutes enabled; false = minutes disabled
     */
    public void enableMinutes(boolean value) {
        isMinutesEnabled = value;
    }

    private void setMinutesEnabled(RangeTimePickerDialog rangeTimePickerDialog, boolean value, String nameTimePicker) {
        try {
            Field mTimePickerField;
            mTimePickerField = RangeTimePickerDialog.class.getDeclaredField(nameTimePicker);
            mTimePickerField.setAccessible(true);
            final TimePicker mTimePicker = (TimePicker) mTimePickerField.get(rangeTimePickerDialog);
            int minutesId, hoursId;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                minutesId = Resources.getSystem().getIdentifier("minutes", "id", "android");
                hoursId = Resources.getSystem().getIdentifier("hours", "id", "android");
            } else {
                minutesId = Resources.getSystem().getIdentifier("minute", "id", "android");
                hoursId = Resources.getSystem().getIdentifier("hour", "id", "android");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final int toggleModeId = Resources.getSystem().getIdentifier("toggle_mode", "id", "android");
                final View toggleModeView = mTimePicker.findViewById(toggleModeId);
                toggleModeView.callOnClick();
                toggleModeView.setVisibility(View.GONE);
            }
            final View minutesView = mTimePicker.findViewById(minutesId);
            final View hoursView = mTimePicker.findViewById(hoursId);
            minutesView.setEnabled(value);
            mTimePicker.setCurrentMinute(0);

            mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    mTimePicker.setCurrentMinute(0);
                    hoursView.setSoundEffectsEnabled(false);
                    hoursView.performClick();
                    hoursView.setSoundEffectsEnabled(true);
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to set initial start clock
     *
     * @param hour   Initial hour
     * @param minute Initial minute
     */
    public void setInitialStartClock(int hour, int minute) {
        initialStarHour = hour;
        initialStartMinute = minute;
    }

    /**
     * Method to set keyboard input as default (Only on Oreo device)
     *
     * @param inputKeyboardAsDefault true = keyboard set as default, false: otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setInputKeyboardAsDefault(boolean inputKeyboardAsDefault) {
        this.inputKeyboardAsDefault = inputKeyboardAsDefault;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setInputKeyboardAsDefault(String timePickerName) {
        Field mTimePickerField;
        try {
            mTimePickerField = RangeTimePickerDialog.class.getDeclaredField(timePickerName);
            mTimePickerField.setAccessible(true);
            final TimePicker mTimePicker = (TimePicker) mTimePickerField.get(RangeTimePickerDialog.this);
            final int toggleModeId = Resources.getSystem().getIdentifier("toggle_mode", "id", "android");
            final View toggleModeView = mTimePicker.findViewById(toggleModeId);
            toggleModeView.callOnClick();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
