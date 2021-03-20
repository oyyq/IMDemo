package net.oyyq.dbflowdemo.db.center;

import net.oyyq.dbflowdemo.db.Card.Card;
import net.oyyq.dbflowdemo.db.model.BaseDbModel;


/**
 * 某一个实体类的卡片
 * @param <Model>  实体类
 * @param <card>  卡片
 */
public interface CardCenter<Model extends BaseDbModel, card extends Card<Model>> {
    /**
     * 分发卡片, 存储到数据表
     * @param cards  利用cards, 给Model数据表增加新记录 / 更新现有记录, 不删除记录 !
     */
     void dispatch(card... cards);
}
