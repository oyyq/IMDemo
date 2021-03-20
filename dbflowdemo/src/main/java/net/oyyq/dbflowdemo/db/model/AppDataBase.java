package net.oyyq.dbflowdemo.db.model;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = AppDataBase.NAME, version = AppDataBase.VERSION,
        foreignKeyConstraintsEnforced = true,
        insertConflict = ConflictAction.IGNORE,
        updateConflict = ConflictAction.IGNORE)
public class AppDataBase {

    public static final String NAME = "AppDatabase";
    public static final int VERSION = 1;

}
