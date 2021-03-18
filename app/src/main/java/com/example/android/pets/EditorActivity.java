/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /* Specific pet uri to update */
    private Uri mPetUriForUpdate;

    /* Cursor Loader id for querying the pet */
    private static final int CURSOR_lOADER_ID = 2;

    /* Activity mode whether it's edit mode or add mode*/
    private int mActivityMode;

    /*Edit Mode of Activity*/
    private static final int EDIT_MODE = 1;

    /*ADD Mode of Activity*/
    private static final int ADD_MODE = 0;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        setupSpinner();

        Uri petUriForUpdate = getIntent().getData();
        if(petUriForUpdate != null){
            mActivityMode = 1;
            this.setTitle("Edit Pet");
            Log.d("PetUriEditorActivity","uri :" + petUriForUpdate);
            mPetUriForUpdate = petUriForUpdate;
            getLoaderManager().initLoader(CURSOR_lOADER_ID,null,this).forceLoad();

        }else{
            this.setTitle("Add Pet");
            mActivityMode = 0;
        }

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                long result = savePet(mActivityMode);
                getBack();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deletePet();
                getBack();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private long savePet(int activityMode){

        long result = -1;

        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);

        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        int petGender = mGender;
        int petWeight = Integer.parseInt(mWeightEditText.getText().toString().trim());



        ContentValues petData = new ContentValues();
        petData.put(PetEntry.COLUMN_PET_NAME,petName);
        petData.put(PetEntry.COLUMN_PET_WEIGHT,petWeight);
        petData.put(PetEntry.COLUMN_PET_GENDER,petGender);
        petData.put(PetEntry.COLUMN_PET_BREED,petBreed);

        switch (activityMode){
            case EDIT_MODE:
                int rowNumber = getContentResolver().update(mPetUriForUpdate,petData,null,null);
                return rowNumber;
            case ADD_MODE:
                Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI,petData);
                result = ContentUris.parseId(uri);
        }
        return  result;

    }

    private void getBack(){
        Intent intent = new Intent(this,CatalogActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case CURSOR_lOADER_ID:
                return new CursorLoader(this,mPetUriForUpdate,new String[]{PetEntry.COLUMN_PET_NAME,PetEntry.COLUMN_PET_WEIGHT,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_BREED},null,null,null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            data.moveToFirst();
            mGender = data.getInt(data.getColumnIndex(PetEntry.COLUMN_PET_GENDER));
            mNameEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
            mWeightEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)));
            mBreedEditText.setText(data.getString(data.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
            mGenderSpinner.setSelection(mGender);
            
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void deletePet(){
        Log.d("DeletingPet", "deletePet: " + mPetUriForUpdate);
        getContentResolver().delete(mPetUriForUpdate,null,null);
    }
}