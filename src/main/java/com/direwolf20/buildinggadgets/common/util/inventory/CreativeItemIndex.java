package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.Iterator;

public final class CreativeItemIndex implements IItemIndex {
    @Override
    public void insert(Multiset<UniqueItem> items) {

    }

    @Override
    public void reIndex() {

    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        Iterator<ImmutableMultiset<UniqueItem>> it = list.iterator();
        ImmutableMultiset<UniqueItem> chosen = it.hasNext() ? it.next() : ImmutableMultiset.of();
        return MatchResult.success(list, chosen, chosen);
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        return result.isSuccess();
    }
}
