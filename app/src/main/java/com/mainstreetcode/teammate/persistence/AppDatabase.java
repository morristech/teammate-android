package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.support.v4.util.Pair;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.BuildConfig;
import com.mainstreetcode.teammate.model.Chat;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.persistence.entity.EventEntity;
import com.mainstreetcode.teammate.persistence.entity.GuestEntity;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.persistence.entity.RoleEntity;
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;
import com.mainstreetcode.teammate.persistence.entity.UserEntity;
import com.mainstreetcode.teammate.persistence.migrations.Migration1To2;
import com.mainstreetcode.teammate.persistence.migrations.Migration2To3;
import com.mainstreetcode.teammate.persistence.typeconverters.CharSequenceConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.DateTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.EventTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.LatLngTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.PositionTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.SportTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.TeamTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.UserTypeConverter;
import com.mainstreetcode.teammate.persistence.typeconverters.VisibilityTypeConverter;
import com.mainstreetcode.teammate.util.Logger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

/**
 * App Database
 */

@Database(entities = {UserEntity.class, TeamEntity.class, EventEntity.class,
        RoleEntity.class, JoinRequestEntity.class, GuestEntity.class,
        Chat.class, Media.class}, version = 3)

@TypeConverters({LatLngTypeConverter.class, DateTypeConverter.class, CharSequenceConverter.class,
        UserTypeConverter.class, TeamTypeConverter.class, EventTypeConverter.class,
        SportTypeConverter.class, PositionTypeConverter.class, VisibilityTypeConverter.class})

public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String PROD_DB = "database-name";
    private static final String DEV_DB = "teammate-dev-db";
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(App.getInstance(), AppDatabase.class, BuildConfig.DEV ? DEV_DB : PROD_DB)
                    .addMigrations(new Migration1To2())
                    .addMigrations(new Migration2To3())
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

    public abstract TeamDao teamDao();

    public abstract RoleDao roleDao();

    public abstract EventDao eventDao();

    public abstract MediaDao mediaDao();

    public abstract GuestDao guestDao();

    public abstract ChatDao teamChatDao();

    public abstract JoinRequestDao joinRequestDao();

    public DeviceDao deviceDao() {return new DeviceDao();}

    public ConfigDao configDao() {return new ConfigDao();}

    public TeamMemberDao teamMemberDao() {return new TeamMemberDao();}

    public Single<List<Pair<String, Integer>>> clearTables() {
        final List<Single<Pair<String, Integer>>> singles = new ArrayList<>();
        final List<Pair<String, Integer>> collector = new ArrayList<>();

        singles.add(clearTable(teamChatDao()));
        singles.add(clearTable(joinRequestDao()));
        singles.add(clearTable(guestDao()));
        singles.add(clearTable(eventDao()));
        singles.add(clearTable(mediaDao()));
        singles.add(clearTable(roleDao()));
        singles.add(clearTable(teamDao()));
        singles.add(clearTable(userDao()));
        singles.add(clearTable(deviceDao()));
        singles.add(clearTable(configDao()));

        return Single.concat(singles).collectInto(collector, List::add);
    }

    private Single<Pair<String, Integer>> clearTable(EntityDao<?> entityDao) {
        final String tableName = entityDao.getTableName();

        return entityDao.deleteAll()
                .map(rowsDeleted -> new Pair<>(tableName, rowsDeleted))
                .onErrorResumeNext(throwable -> {
                    Logger.log(TAG, "Error clearing table: " + tableName, throwable);
                    return Single.just(new Pair<>(tableName, 0));
                });
    }
}
