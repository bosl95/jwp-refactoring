package kitchenpos;

import kitchenpos.domain.*;
import kitchenpos.ui.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class KitchenPosTestFixture {

    public static final List<String> COOKING_OR_MEAL_STATUS = Arrays.asList(
            OrderStatus.COOKING.name(), OrderStatus.MEAL.name()
    );

    public static String 표준_시간_형식을_적용한다(LocalDateTime localTime) {
        return localTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public static ProductRequest 상품을_요청한다(String name, BigDecimal price) {
        return new ProductRequest(name, price);
    }

    public static Product 상품을_저장한다(Long id, String name, BigDecimal price) {
        return new Product(id, name, price);
    }

    public static Product 상품을_저장한다(Long id, ProductRequest productRequest) {
        return 상품을_저장한다(id, productRequest.getName(), productRequest.getPrice());
    }

    public static Menu 메뉴를_저장한다(Long id, String name, BigDecimal price, Long menuGroupId, List<MenuProduct> menuProducts) {
        Menu savedMenu = new Menu();
        savedMenu.setId(id);
        savedMenu.setName(name);
        savedMenu.setPrice(price);
        savedMenu.setMenuGroupId(menuGroupId);
        savedMenu.setMenuProducts(menuProducts);
        return savedMenu;
    }

    public static Menu 메뉴를_저장한다(Long id, Menu menu) {
        return 메뉴를_저장한다(id, menu.getName(), menu.getPrice(), menu.getMenuGroupId(), menu.getMenuProducts());
    }

    public static MenuGroup 메뉴_그룹을_저장한다(Long id, String groupName) {
        return new MenuGroup(id, groupName);
    }

    public static MenuGroupRequest 메뉴_그룹을_요청한다(String groupName) {
        return new MenuGroupRequest(groupName);
    }

    public static MenuGroupResponse 메뉴_그룹을_응답한다(Long id, String groupName) {
        return new MenuGroupResponse(id, groupName);
    }

    public static OrderTableRequest 주문_테이블을_요청한다(int numberOfGuests, boolean empty) {
        return new OrderTableRequest(numberOfGuests, empty);
    }

    public static OrderTableRequest 주문_테이블을_요청한다(Long orderTableId) {
        return new OrderTableRequest(orderTableId);
    }

    public static OrderTable 주문_테이블을_저장한다(Long id, Long tableGroupId, int numberOfGuests, boolean empty) {
        return new OrderTable(id, tableGroupId, numberOfGuests, empty);
    }

    public static OrderTable 주문_테이블을_저장한다(Long id, Long tableGroupId, OrderTableRequest orderTableRequest) {
        return 주문_테이블을_저장한다(id, tableGroupId, orderTableRequest.getNumberOfGuests(), orderTableRequest.isEmpty());
    }

    public static OrderTable 주문_테이블을_저장한다(OrderTable orderTable, OrderTableRequest orderTableRequest) {
        return 주문_테이블을_저장한다(orderTable.getId(), orderTable.getTableGroupId(), orderTableRequest.getNumberOfGuests(), orderTable.isEmpty());
    }

    public static TableGroup 테이블_그룹을_저장한다(Long id, LocalDateTime createdDate, List<OrderTable> orderTables) {
        TableGroup tableGroup = new TableGroup();
        tableGroup.setId(id);
        tableGroup.setCreatedDate(createdDate);
        tableGroup.setOrderTables(orderTables);
        return tableGroup;
    }

    public static TableGroupRequest 테이블_그룹을_요청한다(LocalDateTime createdDate, List<OrderTableRequest> orderTables) {
        TableGroupRequest tableGroupRequest = new TableGroupRequest(orderTables);
        tableGroupRequest.setCreatedDate(createdDate);
        return tableGroupRequest;
    }

    public static MenuProduct 메뉴_상품을_저장한다(Long seq, Long menuId, Long productId, long quantity) {
        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setSeq(seq);
        menuProduct.setMenuId(menuId);
        menuProduct.setProductId(productId);
        menuProduct.setQuantity(quantity);
        return menuProduct;
    }

    public static Order 주문을_저장한다(Long id, Long orderTableId, String orderStatus, LocalDateTime orderedTime, List<OrderLineItem> orderLineItems) {
        Order order = new Order();
        order.setId(id);
        order.setOrderTableId(orderTableId);
        order.setOrderStatus(orderStatus);
        order.setOrderedTime(orderedTime);
        order.setOrderLineItems(orderLineItems);
        return order;
    }

    public static Order 주문을_저장한다(Long id, Order order) {
        return 주문을_저장한다(id, order.getOrderTableId(), order.getOrderStatus(), order.getOrderedTime(), order.getOrderLineItems());
    }

    public static OrderLineItem 주문_항목을_저장한다(Long id, Long orderId, Long menuId, Long quantity) {
        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setSeq(id);
        orderLineItem.setOrderId(orderId);
        orderLineItem.setMenuId(menuId);
        orderLineItem.setQuantity(quantity);
        return orderLineItem;
    }
}
