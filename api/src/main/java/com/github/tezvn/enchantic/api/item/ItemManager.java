package com.github.tezvn.enchantic.api.item;

import javax.annotation.Nullable;
import java.util.List;

public interface ItemManager {

    List<EnchanticItem> getItems();

    @Nullable
    EnchanticItem getItem(String id);


}
