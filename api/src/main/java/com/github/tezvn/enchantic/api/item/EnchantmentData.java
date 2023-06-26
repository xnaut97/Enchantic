package com.github.tezvn.enchantic.api.item;

public interface EnchantmentData {

    EnchantmentType getType();

    String getName();

    double getSuccessRate();

    double getFailureRate();

    int getLevel(LevelType level);

}
