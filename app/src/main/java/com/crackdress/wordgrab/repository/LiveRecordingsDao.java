package com.crackdress.wordgrab.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.crackdress.wordgrab.model.Recording;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface LiveRecordingsDao {

    @Query("select * from recordings where id = :id")
    Recording getRecordingById(long id);

    @Query("select * from recordings order by id desc")
    LiveData<List<Recording>> queryAll();

    @Insert
    void insert(Recording ... recordings);

    @Delete
    void delete(Recording...recordings);

    @Update (onConflict = REPLACE)
    void update(Recording recording);
}
