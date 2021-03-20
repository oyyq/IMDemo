package net.oyyq.dbflowdemo.db.model;

import com.raizlabs.android.dbflow.structure.BaseModel;

import net.oyyq.common.adapterUtil.DiffUiDataCallback;

import java.util.Set;

public abstract class BaseDbModel<Model> extends BaseModel implements DiffUiDataCallback.UiDataDiffer<Model>{

    /**
     * 创建/更新 某表更新时需要级联更新的表(若级联表不存在),  被本表调用, 在事务内调用,  晚于本表更新
     */
    public void createCASCADEmodelsIfNeed() {};


    /**
     * 拿到某表更新时需要级联更新的表,  被本表调用, 晚于本表更新
     * @return
     */
    public Set<BaseDbModel> getCASCADEUpdatemodels() {return null; }

    /**
     * 级联表的更新操作, 被级联表调用, 在事务内调用; 本表更新后, 级联表根据本地数据库内容更新自己, 并存储到数据库
     * @reutrn  此级联更新是否要回调到外层, 通知界面刷新; True: 要回调到Repository更新APP缓存
     */
    public boolean cascadeUpdate() { return false; }

    /**
     * 拿到某表记录删除时, 需要级联删除的别表记录,  被本表调用, 晚于本表删除
     * @return
     */
    public Set<BaseDbModel> getCASCADEDeletemodels() { return null; }

    /**
     * 某表记录删除时, 将级联删除的别表记录一并删除,  被本表调用, 在事务内调用
     */
    public boolean cascadeDelete() {
        return false;
    }


}



