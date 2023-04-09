package com.github.tezvn.enchantic.api.item;

public interface ItemEnchantment {

    String getName();

    double getSuccessRate();

    double getFailedRate();

    int getUpgradeLevel();

    int getDowngradeLevelOnFailed();

}
