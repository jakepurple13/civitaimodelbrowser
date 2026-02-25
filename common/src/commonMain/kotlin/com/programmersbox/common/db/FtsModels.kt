package com.programmersbox.common.db

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Entity(tableName = "FavoriteRoomFts")
@Fts4(contentEntity = FavoriteRoom::class)
data class FavoriteRoomFts(
    val name: String,
    val description: String?,
    val creatorName: String?,
    val type: String
)

val MIGRATION_FAVORITES_FTS = object : Migration(12, 13) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. Create the FTS table
        // Note: Room generates a specific schema for contentEntity FTS tables.
        // We recreate that schema here.
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `FavoriteRoomFts` 
            USING FTS4(
                `name`, 
                `description`, 
                `creatorName`, 
                content=`favorite_table`
            )
        """
        )

        // 2. Backfill existing data
        connection.execSQL(
            """
            INSERT INTO FavoriteRoomFts(rowid, name, description, creatorName)
            SELECT id, name, description, creatorName FROM favorite_table
        """
        )
    }
}

val MIGRATION_FAVORITES_FTS_2 = object : Migration(13, 14) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. Drop the old FTS table (It's missing the 'type' column)
        connection.execSQL("DROP TABLE IF EXISTS `FavoriteRoomFts`")

        // 2. Recreate with the new schema (including 'type')
        // Note: contentEntity tables strictly follow this syntax
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `FavoriteRoomFts` 
            USING FTS4(
                `name`, 
                `description`, 
                `creatorName`, 
                `type`, 
                content=`favorite_table`
            )
        """
        )

        // 3. Re-Backfill data (Critical step!)
        connection.execSQL(
            """
            INSERT INTO FavoriteRoomFts(rowid, name, description, creatorName, type)
            SELECT id, name, description, creatorName, type FROM favorite_table
        """
        )
    }
}


// Shadow table for the Parent Item
@Entity(tableName = "CustomListItemFts")
@Fts4 // No contentEntity due to String PK
data class CustomListItemFts(
    val uuid: String, // The ID to link back to the real table
    val name: String,
    val description: String? = null, // Added description field for FTS
)

// Shadow table for the Child Info
@Entity(tableName = "CustomListInfoFts")
@Fts4
data class CustomListInfoFts(
    val parentUuid: String, // We need this to find the parent CustomListItem
    val name: String,
    val description: String?
)

fun getFtsCallback(): RoomDatabase.Callback {
    return object : RoomDatabase.Callback() {
        // Note: The argument is SQLiteConnection, not SupportSQLiteDatabase
        override fun onCreate(connection: SQLiteConnection) {
            super.onCreate(connection)

            // --- Triggers for CustomListItem ---
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS insert_item_fts AFTER INSERT ON CustomListItem
                BEGIN
                    INSERT INTO CustomListItemFts(uuid, name, description) 
                    VALUES (new.uuid, new.name, new.description);
                END;
            """
            )
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS delete_item_fts BEFORE DELETE ON CustomListItem
                BEGIN
                    DELETE FROM CustomListItemFts WHERE uuid = old.uuid;
                END;
            """
            )
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS update_item_fts AFTER UPDATE ON CustomListItem
                BEGIN
                    UPDATE CustomListItemFts 
                    SET name = new.name, description = new.description 
                    WHERE uuid = new.uuid;
                END;
            """
            )

            // --- Triggers for CustomListInfo ---
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS insert_info_fts AFTER INSERT ON CustomListInfo
                BEGIN
                    INSERT INTO CustomListInfoFts(parentUuid, name, description) 
                    VALUES (new.uuid, new.name, new.description);
                END;
            """
            )
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS delete_info_fts BEFORE DELETE ON CustomListInfo
                BEGIN
                    DELETE FROM CustomListInfoFts WHERE rowid = old.rowid; 
                END;
            """
            )
            connection.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS update_info_fts AFTER UPDATE ON CustomListInfo
                BEGIN
                    UPDATE CustomListInfoFts 
                    SET name = new.name, description = new.description 
                    WHERE parentUuid = new.uuid AND name = old.name; 
                END;
            """
            )
        }
    }
}

// Replace '1' and '2' with your actual start and end versions
val MIGRATION_FTS = object : Migration(11, 12) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. Create the FTS Table for Parents
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `CustomListItemFts` 
            USING FTS4(`uuid`, `name`)
        """
        )

        // 2. Create the FTS Table for Children
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `CustomListInfoFts` 
            USING FTS4(`parentUuid`, `name`, `description`)
        """
        )

        // 3. Backfill existing data into FTS tables (Crucial for existing users!)
        connection.execSQL(
            """
            INSERT INTO CustomListItemFts(uuid, name) 
            SELECT uuid, name FROM CustomListItem
        """
        )

        connection.execSQL(
            """
            INSERT INTO CustomListInfoFts(parentUuid, name, description) 
            SELECT uuid, name, description FROM CustomListInfo
        """
        )

        // 4. Create Triggers for CustomListItem (Sync Logic)
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS insert_item_fts AFTER INSERT ON CustomListItem
            BEGIN
                INSERT INTO CustomListItemFts(uuid, name) VALUES (new.uuid, new.name);
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS delete_item_fts BEFORE DELETE ON CustomListItem
            BEGIN
                DELETE FROM CustomListItemFts WHERE uuid = old.uuid;
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS update_item_fts AFTER UPDATE ON CustomListItem
            BEGIN
                UPDATE CustomListItemFts SET name = new.name WHERE uuid = new.uuid;
            END;
        """
        )

        // 5. Create Triggers for CustomListInfo (Sync Logic)
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS insert_info_fts AFTER INSERT ON CustomListInfo
            BEGIN
                INSERT INTO CustomListInfoFts(parentUuid, name, description) 
                VALUES (new.uuid, new.name, new.description);
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS delete_info_fts BEFORE DELETE ON CustomListInfo
            BEGIN
                DELETE FROM CustomListInfoFts WHERE rowid = old.rowid; 
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS update_info_fts AFTER UPDATE ON CustomListInfo
            BEGIN
                UPDATE CustomListInfoFts 
                SET name = new.name, description = new.description 
                WHERE parentUuid = new.uuid AND name = old.name; 
            END;
        """
        )
    }
}

// Migration to add description field to CustomListItemFts
val MIGRATION_FTS_CUSTOM_LIST_ITEM_DESCRIPTION = object : Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. Drop the old FTS table for CustomListItem (missing 'description' column)
        connection.execSQL("DROP TABLE IF EXISTS `CustomListItemFts`")

        // 2. Recreate with the new schema (including 'description')
        connection.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS `CustomListItemFts` 
            USING FTS4(`uuid`, `name`, `description`)
        """
        )

        // 3. Backfill data from CustomListItem (including description)
        connection.execSQL(
            """
            INSERT INTO CustomListItemFts(uuid, name, description) 
            SELECT uuid, name, description FROM CustomListItem
        """
        )

        // 4. Recreate triggers for CustomListItem with description support
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS insert_item_fts AFTER INSERT ON CustomListItem
            BEGIN
                INSERT INTO CustomListItemFts(uuid, name, description) 
                VALUES (new.uuid, new.name, new.description);
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS delete_item_fts BEFORE DELETE ON CustomListItem
            BEGIN
                DELETE FROM CustomListItemFts WHERE uuid = old.uuid;
            END;
        """
        )
        connection.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS update_item_fts AFTER UPDATE ON CustomListItem
            BEGIN
                UPDATE CustomListItemFts 
                SET name = new.name, description = new.description 
                WHERE uuid = new.uuid;
            END;
        """
        )
    }
}