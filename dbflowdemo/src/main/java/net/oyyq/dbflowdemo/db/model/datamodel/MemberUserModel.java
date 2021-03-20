package net.oyyq.dbflowdemo.db.model.datamodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;

import net.oyyq.dbflowdemo.db.model.AppDataBase;

/**
 * 群成员对应的用户的简单信息表
 *
 */
@QueryModel(database = AppDataBase.class)
public class MemberUserModel {

    @Column
    public String userId; // User-id/Member-userId
    @Column
    public String name; // User-name
    @Column
    public String portrait; // User-portrait

}
