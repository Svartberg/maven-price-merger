package com.svartberg.service;

import com.svartberg.model.Price;

import java.util.List;

public interface PriceMerger {

    List<Price> merge(List<Price> currentPrice, List<Price> newPrice);

}
