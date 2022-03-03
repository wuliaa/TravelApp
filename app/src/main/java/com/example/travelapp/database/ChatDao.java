package com.example.travelapp.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.travelapp.bean.Message;
import com.example.travelapp.viewmodel.SingleLiveEvent;

import java.util.List;

@Dao
public interface ChatDao {

    @Query("SELECT * FROM chat_table where " +
            "from_user = :fromUser and to_user = :toUser " +
            "or from_user = :toUser order by id desc limit 10")
    LiveData<List<Message>> getLastFive(String fromUser,String toUser);


    @Query("SELECT * FROM chat_table where " +
            "from_user = :fromUser and to_user = :toUser " +
            "or from_user = :toUser order by id desc limit :page*5,5")
    List<Message> getMoreFive(String fromUser,String toUser,Integer page);

    @Insert
    void insert(Message mess);

    @Delete
    void delete(Message mess);

    @Query("SELECT * FROM chat_table WHERE id =:ID")
    Message hasItem(int ID);
}
