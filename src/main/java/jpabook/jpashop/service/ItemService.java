package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    // 변경감지 (원하는 속성만 선택해서 변경 가능)
    @Transactional
//    public void updateItem(Long itemId, Book bookParam) {
    public void updateItem(Long itemId, int price, String name, int stockQuantity) {
        Item foundItem = itemRepository.findOne(itemId);
        foundItem.setPrice(price);
        foundItem.setName(name);
        foundItem.setStockQuantity(stockQuantity);
        // foundItem.change(price, name, stockQuantity) : 의미 있는 method 따로 관리

        // commit
        // JPA flush(변경감지 set)
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
