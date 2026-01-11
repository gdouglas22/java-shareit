package ru.practicum.shareit.item.policy;

import ru.practicum.shareit.item.model.Item;

public interface ItemAccessPolicy {
    void checkOwner(Item item, Long ownerId);
}
