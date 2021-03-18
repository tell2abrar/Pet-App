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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetContract.PetEntry;

import java.util.List;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int mPetLoader = 1;
    private ListView mPetListView = null;
    private PetCursorAdapter mPetCursorAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        Log.d("DATABASE_QUERY_RESULT","THE VALUE OF RESULT : " + result);
        if(result != null){
            if (!result.contentEquals("-1")){
                Toast.makeText(this,"Pet is saved with id " + result , Toast.LENGTH_SHORT).show();
            }else
            {
                Toast.makeText(this,"Error with Saving pet "  , Toast.LENGTH_SHORT).show();
            }
        }


        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        mPetListView = (ListView) findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        mPetListView.setEmptyView(emptyView);

        //Initialize the cursor loader to query data in worker thread
        getLoaderManager().initLoader(mPetLoader,null,this).forceLoad();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllpets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void insertPet(){

        ContentValues dummyData = new ContentValues();
        dummyData.put(PetEntry.COLUMN_PET_NAME,"jimmy");
        dummyData.put(PetEntry.COLUMN_PET_BREED,"german shephered");
        dummyData.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        dummyData.put(PetEntry.COLUMN_PET_WEIGHT,5);

        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI,dummyData);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case mPetLoader:
                return new CursorLoader(this,PetEntry.CONTENT_URI,new String[]{PetEntry._ID,PetEntry.COLUMN_PET_NAME,PetEntry.COLUMN_PET_BREED,PetEntry.COLUMN_PET_GENDER,PetEntry.COLUMN_PET_WEIGHT},null,null,null);
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mPetCursorAdapter = new PetCursorAdapter(this, data);
        mPetListView.setAdapter(mPetCursorAdapter);

        mPetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor petDataAtCurrentPosition = (Cursor) mPetCursorAdapter.getItem(position);
                int _id = petDataAtCurrentPosition.getInt(petDataAtCurrentPosition.getColumnIndex(PetEntry._ID));
                Log.d("PetAdapter","Value :" + _id);
                Uri petUriforUpdate = ContentUris.withAppendedId(PetEntry.CONTENT_URI,_id);

                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
                intent.setData(petUriforUpdate);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mPetCursorAdapter.swapCursor(null);

    }

    private void deleteAllpets(){

        getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
    }



}
