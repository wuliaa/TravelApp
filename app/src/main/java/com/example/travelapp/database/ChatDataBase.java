package com.example.travelapp.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.travelapp.bean.Message;


@Database(entities = {Message.class},version = 1,exportSchema = false)
public abstract class ChatDataBase extends RoomDatabase {
    private static ChatDataBase INSTANCE;
    public static synchronized ChatDataBase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    ChatDataBase.class,"chat_database")
                    .build();
        }
        return INSTANCE;
    }
    public abstract ChatDao getChatDao();
}
