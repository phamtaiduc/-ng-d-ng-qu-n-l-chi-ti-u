// src/com/app_mobile/campusexpensemanager/ExpenseDetailActivity.java
package com.app_mobile.campusexpensemanager;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.app_mobile.campusexpensemanager.SQLite.ExpenseDb;

import java.util.Calendar;

public class ExpenseDetailActivity extends AppCompatActivity {

    private EditText descriptionEditText, dateEditText, amountEditText;
    private Spinner categorySpinner;
    private Button saveButton, deleteButton;
    private ExpenseDb expenseDb;
    private long expenseId;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);


        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);

        expenseDb = new ExpenseDb(this);

        // Get the expense ID from the Intent
        expenseId = getIntent().getLongExtra("expense_id", -1);
        if (expenseId == -1) {
            finish(); // Close the activity if no ID is passed
            return;
        }

        // Load the categories into the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Load expense details
        loadExpenseDetails(expenseId);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpenseDetails();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteExpense();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to the previous activity
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        ExpenseDetailActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                dateEditText.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

    }

    private void loadExpenseDetails(long expenseId) {
        Cursor cursor = expenseDb.getExpenseById(expenseId);
        if (cursor != null && cursor.moveToFirst()) {
            descriptionEditText.setText(cursor.getString(cursor.getColumnIndex("description")));
            dateEditText.setText(cursor.getString(cursor.getColumnIndex("date")));
            amountEditText.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndex("amount"))));

            // Set the category in the Spinner
            String category = cursor.getString(cursor.getColumnIndex("category"));
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
            int position = adapter.getPosition(category);
            categorySpinner.setSelection(position);

            cursor.close();
        }
    }

    private void saveExpenseDetails() {
        ContentValues values = new ContentValues();
        values.put("description", descriptionEditText.getText().toString());
        values.put("date", dateEditText.getText().toString());
        values.put("amount", Double.parseDouble(amountEditText.getText().toString()));
        values.put("category", categorySpinner.getSelectedItem().toString());

        int rowsUpdated = expenseDb.updateExpense(expenseId, values);
        if (rowsUpdated > 0) {
            setResult(RESULT_OK);  // Indicate that the expense was updated
        } else {
            setResult(RESULT_CANCELED);  // Indicate that the update failed
        }
        finish();
    }


    private void deleteExpense() {
        int rowsDeleted = expenseDb.deleteExpense(expenseId);
        if (rowsDeleted > 0) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
