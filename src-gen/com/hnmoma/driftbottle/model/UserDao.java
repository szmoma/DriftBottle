package com.hnmoma.driftbottle.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.hnmoma.driftbottle.model.User;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table USER.
*/
public class UserDao extends AbstractDao<User, String> {

    public static final String TABLENAME = "USER";

    /**
     * Properties of entity User.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property UserId = new Property(0, String.class, "userId", true, "USER_ID");
        public final static Property Province = new Property(1, String.class, "province", false, "PROVINCE");
        public final static Property City = new Property(2, String.class, "city", false, "CITY");
        public final static Property NickName = new Property(3, String.class, "nickName", false, "NICK_NAME");
        public final static Property IdentityType = new Property(4, String.class, "identityType", false, "IDENTITY_TYPE");
        public final static Property Descript = new Property(5, String.class, "descript", false, "DESCRIPT");
        public final static Property HeadImg = new Property(6, String.class, "headImg", false, "HEAD_IMG");
        public final static Property Age = new Property(7, Integer.class, "age", false, "AGE");
        public final static Property Job = new Property(8, String.class, "job", false, "JOB");
        public final static Property Hobby = new Property(9, String.class, "hobby", false, "HOBBY");
        public final static Property Constell = new Property(10, String.class, "constell", false, "CONSTELL");
        public final static Property Birthday = new Property(11, String.class, "birthday", false, "BIRTHDAY");
        public final static Property IsVIP = new Property(12, Integer.class, "isVIP", false, "IS_VIP");
        public final static Property TempHeadImg = new Property(13, String.class, "tempHeadImg", false, "TEMP_HEAD_IMG");
    };


    public UserDao(DaoConfig config) {
        super(config);
    }
    
    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'USER' (" + //
                "'USER_ID' TEXT PRIMARY KEY NOT NULL ," + // 0: userId
                "'PROVINCE' TEXT," + // 1: province
                "'CITY' TEXT," + // 2: city
                "'NICK_NAME' TEXT," + // 3: nickName
                "'IDENTITY_TYPE' TEXT," + // 4: identityType
                "'DESCRIPT' TEXT," + // 5: descript
                "'HEAD_IMG' TEXT," + // 6: headImg
                "'AGE' INTEGER," + // 7: age
                "'JOB' TEXT," + // 8: job
                "'HOBBY' TEXT," + // 9: hobby
                "'CONSTELL' TEXT," + // 10: constell
                "'BIRTHDAY' TEXT," + // 11: birthday
                "'IS_VIP' INTEGER," + // 12: isVIP
                "'TEMP_HEAD_IMG' TEXT);"); // 13: tempHeadImg
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'USER'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(1, userId);
        }
 
        String province = entity.getProvince();
        if (province != null) {
            stmt.bindString(2, province);
        }
 
        String city = entity.getCity();
        if (city != null) {
            stmt.bindString(3, city);
        }
 
        String nickName = entity.getNickName();
        if (nickName != null) {
            stmt.bindString(4, nickName);
        }
 
        String identityType = entity.getIdentityType();
        if (identityType != null) {
            stmt.bindString(5, identityType);
        }
 
        String descript = entity.getDescript();
        if (descript != null) {
            stmt.bindString(6, descript);
        }
 
        String headImg = entity.getHeadImg();
        if (headImg != null) {
            stmt.bindString(7, headImg);
        }
 
        Integer age = entity.getAge();
        if (age != null) {
            stmt.bindLong(8, age);
        }
 
        String job = entity.getJob();
        if (job != null) {
            stmt.bindString(9, job);
        }
 
        String hobby = entity.getHobby();
        if (hobby != null) {
            stmt.bindString(10, hobby);
        }
 
        String constell = entity.getConstell();
        if (constell != null) {
            stmt.bindString(11, constell);
        }
 
        String birthday = entity.getBirthday();
        if (birthday != null) {
            stmt.bindString(12, birthday);
        }
 
        Integer isVIP = entity.getIsVIP();
        if (isVIP != null) {
            stmt.bindLong(13, isVIP);
        }
 
        String tempHeadImg = entity.getTempHeadImg();
        if (tempHeadImg != null) {
            stmt.bindString(14, tempHeadImg);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public User readEntity(Cursor cursor, int offset) {
        User entity = new User( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // userId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // province
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // city
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // nickName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // identityType
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // descript
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // headImg
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // age
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // job
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // hobby
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // constell
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // birthday
            cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12), // isVIP
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13) // tempHeadImg
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setUserId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setProvince(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCity(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setNickName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setIdentityType(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setDescript(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setHeadImg(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setAge(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setJob(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setHobby(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setConstell(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
        entity.setBirthday(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setIsVIP(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
        entity.setTempHeadImg(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(User entity, long rowId) {
        return entity.getUserId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(User entity) {
        if(entity != null) {
            return entity.getUserId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
