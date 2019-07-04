package com.crackdress.wordgrab.repository;/*
 * Copyright 2017, The Android Open Source Project
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


import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.migration.Migration;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.util.Log;

import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;


@Database(entities = {Recording.class, Contact.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public static final String TAG = AppDatabase.class.getSimpleName();

    private static AppDatabase INSTANCE;

    public abstract LiveRecordingsDao recordingModel();
    public abstract LiveContactsDao   contactsModel();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "recordings")
                    .allowMainThreadQueries()
                    .build();
//            Log.i(TAG, "getDatabase: database opened");
//          INSTANCE = Room.inMemoryDatabaseBuilder(context.getApplicationContext(), AppDatabase.class, "name")
//                            // To simplify the codelab, allow queries on the main thread.
//                            // Don't do this on a real app! See PersistenceBasicSample for an example.
//                            .allowMainThreadQueries()
//                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: 1-2" );
        }
    };

    static final Migration MIGRATION_2_1= new Migration(2, 1) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: 2-1" );
        }
    };

    static final Migration MIGRATION_3_2= new Migration(3, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: 3-2" );
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: 2-3" );
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.i(TAG, "migrate: 3-4" );
        }
    };
}