package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%')))")
    List<Item> searchAvailable(@Param("text") String text);

    List<Item> findByOwner_Id(Long ownerId, Sort sort);

    List<Item> findByRequestId(Long requestId, Sort sort);

    List<Item> findByRequestIdIn(Collection<Long> requestIds, Sort sort);
}
