package ru.practicum.shareit.item.policy;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;

@Component
public class DefaultItemAccessPolicy implements ItemAccessPolicy {

    @Override
    public void checkOwner(Item item, Long ownerId) {
        if (item.getOwner() == null || !item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("только владелец может менять вещь");
        }
    }
}
