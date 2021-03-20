package net.oyyq.dbflowdemo.db.Card;

import net.oyyq.dbflowdemo.db.model.BaseDbModel;

public interface Card<Model extends BaseDbModel> {
    String getId();
}
