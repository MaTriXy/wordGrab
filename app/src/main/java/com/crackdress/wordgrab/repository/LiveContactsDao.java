package com.crackdress.wordgrab.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;


@Dao
public interface LiveContactsDao {

    @Query("select * from contacts where id = :id")
    Contact getContactById(long id);

    @Query("select * from contacts order by id desc")
    LiveData<List<Contact>> queryAll();

    @Insert (onConflict = IGNORE)
    void insert(Contact ... contacts);

    @Delete
    void delete(Contact...contacts);

    @Update(onConflict = REPLACE)
    void update(Contact contact);

}
