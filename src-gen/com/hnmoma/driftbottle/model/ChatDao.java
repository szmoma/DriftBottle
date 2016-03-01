package com.hnmoma.driftbottle.model;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.hnmoma.driftbottle.model.Chat;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table CHAT.
*/
public class ChatDao extends AbstractDao<Chat, Long> {

    public static final String TABLENAME = "CHAT";

    /**
     * Properties of entity Chat.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserId = new Property(1, String.class, "userId", false, "USER_ID");
        public final static Property Content = new Property(2, String.class, "content", false, "CONTENT");
        public final static Property ContentType = new Property(3, String.class, "contentType", false, "CONTENT_TYPE");
        public final static Property NickName = new Property(4, String.class, "nickName", false, "NICK_NAME");
        public final static Property IdentityType = new Property(5, String.class, "identityType", false, "IDENTITY_TYPE");
        public final static Property HeadImg = new Property(6, String.class, "headImg", false, "HEAD_IMG");
        public final static Property Province = new Property(7, String.class, "province", false, "PROVINCE");
        public final static Property City = new Property(8, String.class, "city", false, "CITY");
        public final static Property IsContent = new Property(9, Boolean.class, "isContent", false, "IS_CONTENT");
        public final static Property IsComMsg = new Property(10, boolean.class, "isComMsg", false, "IS_COM_MSG");
        public final static Property ChatTime = new Property(11, java.util.Date.class, "chatTime", false, "CHAT_TIME");
        public final static Property BottleIdPk = new Property(12, long.class, "bottleIdPk", false, "BOTTLE_ID_PK");
    };

    private Query<Chat> bottle_BottleToChatsQuery;

    public ChatDao(DaoConfig config) {
        super(config);
    }
    
    public ChatDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'CHAT' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'USER_ID' TEXT NOT NULL ," + // 1: userId
                "'CONTENT' TEXT," + // 2: content
                "'CONTENT_TYPE' TEXT NOT NULL ," + // 3: contentType
                "'NICK_NAME' TEXT NOT NULL ," + // 4: nickName
                "'IDENTITY_TYPE' TEXT," + // 5: identityType
                "'HEAD_IMG' TEXT," + // 6: headImg
                "'PROVINCE' TEXT," + // 7: province
                "'CITY' TEXT," + // 8: city
                "'IS_CONTENT' INTEGER," + // 9: isContent
                "'IS_COM_MSG' INTEGER NOT NULL ," + // 10: isComMsg
                "'CHAT_TIME' INTEGER," + // 11: chatTime
                "'BOTTLE_ID_PK' INTEGER NOT NULL );"); // 12: bottleIdPk
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'CHAT'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Chat entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getUserId());
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(3, content);
        }
        stmt.bindString(4, entity.getContentType());
        stmt.bindString(5, entity.getNickName());
 
        String identityType = entity.getIdentityType();
        if (identityType != null) {
            stmt.bindString(6, identityType);
        }
 
        String headImg = entity.getHeadImg();
        if (headImg != null) {
            stmt.bindString(7, headImg);
        }
 
        String province = entity.getProvince();
        if (province != null) {
            stmt.bindString(8, province);
        }
 
        String city = entity.getCity();
        if (city != null) {
            stmt.bindString(9, city);
        }
 
        Boolean isContent = entity.getIsContent();
        if (isContent != null) {
            stmt.bindLong(10, isContent ? 1l: 0l);
        }
        stmt.bindLong(11, entity.getIsComMsg() ? 1l: 0l);
 
        java.util.Date chatTime = entity.getChatTime();
        if (chatTime != null) {
            stmt.bindLong(12, chatTime.getTime());
        }
        stmt.bindLong(13, entity.getBottleIdPk());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Chat readEntity(Cursor cursor, int offset) {
        Chat entity = new Chat( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // userId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // content
            cursor.getString(offset + 3), // contentType
            cursor.getString(offset + 4), // nickName
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // identityType
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // headImg
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // province
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // city
            cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0, // isContent
            cursor.getShort(offset + 10) != 0, // isComMsg
            cursor.isNull(offset + 11) ? null : new java.util.Date(cursor.getLong(offset + 11)), // chatTime
            cursor.getLong(offset + 12) // bottleIdPk
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Chat entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserId(cursor.getString(offset + 1));
        entity.setContent(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setContentType(cursor.getString(offset + 3));
        entity.setNickName(cursor.getString(offset + 4));
        entity.setIdentityType(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setHeadImg(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setProvince(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setCity(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setIsContent(cursor.isNull(offset + 9) ? null : cursor.getShort(offset + 9) != 0);
        entity.setIsComMsg(cursor.getShort(offset + 10) != 0);
        entity.setChatTime(cursor.isNull(offset + 11) ? null : new java.util.Date(cursor.getLong(offset + 11)));
        entity.setBottleIdPk(cursor.getLong(offset + 12));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Chat entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Chat entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "bottleToChats" to-many relationship of Bottle. */
    public List<Chat> _queryBottle_BottleToChats(long bottleIdPk) {
        synchronized (this) {
            if (bottle_BottleToChatsQuery == null) {
                QueryBuilder<Chat> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.BottleIdPk.eq(null));
                queryBuilder.orderRaw("CHAT_TIME DESC");
                bottle_BottleToChatsQuery = queryBuilder.build();
            }
        }
        Query<Chat> query = bottle_BottleToChatsQuery.forCurrentThread();
        query.setParameter(0, bottleIdPk);
        return query.list();
    }

}