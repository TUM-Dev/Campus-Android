package de.tum.in.tumcampusapp.component.tumui.calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;





class LimitPickerDialog extends Dialog {
    private Button cancelButton;
    private Button saveButton;
    private NumberPicker numberPickerMin;
    private NumberPicker numberPickerMax;
    protected Context context;
    private List<LimitPickerDialogListener> listeners = new ArrayList<LimitPickerDialogListener>();

    LimitPickerDialog(Context context) {
        super(context);
        this.context = context;
        this.setContentView(R.layout.dialog_calendar_filter_limits);
        this.initComponents();
    }

    public void addListener(LimitPickerDialogListener listener) {
        this.listeners.add(listener);
    }

    private void initComponents() {
        cancelButton = this.findViewById(R.id.calendar_hourlimit_cancel_button);
        saveButton = this.findViewById(R.id.calendar_hourlimit_save_button);
        numberPickerMin = this.findViewById(R.id.calendar_hourlimit_numberpicker_min);
        numberPickerMax = this.findViewById(R.id.calendar_hourlimit_numberpicker_max);

        int savedMinHour = Integer.parseInt(Utils.getSetting(context, Const.CALENDAR_FILTER_HOUR_LIMIT_MIN, "0"));
        int savedMaxHour = Integer.parseInt(Utils.getSetting(context, Const.CALENDAR_FILTER_HOUR_LIMIT_MAX, "24"));
        numberPickerMin.setMinValue(0);
        numberPickerMin.setMaxValue(23);
        numberPickerMin.setWrapSelectorWheel(false);
        numberPickerMax.setMinValue(1);
        numberPickerMax.setMaxValue(24);
        numberPickerMin.setValue(savedMinHour);
        numberPickerMax.setValue(savedMaxHour);
        numberPickerMax.setWrapSelectorWheel(false);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LimitPickerDialog.this.dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int minHour = numberPickerMin.getValue();
                int maxHour = numberPickerMax.getValue();
                if(minHour < maxHour) {
                    notifyListeners(minHour, maxHour);
                    LimitPickerDialog.this.dismiss();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle(R.string.error);
                    alertDialog.setMessage(context.getResources().getString(R.string.calendar_filter_hour_limit_error_message));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                                          context.getResources().getString(R.string.ok),
                                          new DialogInterface.OnClickListener() {
                                              public void onClick(DialogInterface dialog, int which) {
                                                  alertDialog.dismiss();
                                              }
                                          });
                    alertDialog.show();
                }
            }
        });
        numberPickerMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                numberPickerMax.setMinValue(newVal + 1);
            }
        });
        numberPickerMax.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                numberPickerMin.setMaxValue(newVal - 1);
            }
        });
    }

    private void notifyListeners(int min, int max) {
        for(LimitPickerDialogListener listener : this.listeners) {
            listener.onSelected(min, max);
        }
    }
}
