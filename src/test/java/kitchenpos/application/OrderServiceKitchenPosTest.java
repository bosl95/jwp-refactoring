package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.menu.dao.MenuDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.dao.OrderDao;
import kitchenpos.order.dao.OrderLineItemDao;
import kitchenpos.order.dao.OrderTableDao;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.ui.dto.OrderLineItemRequest;
import kitchenpos.order.ui.dto.OrderRequest;
import kitchenpos.order.ui.dto.OrderResponse;
import kitchenpos.order.ui.dto.OrderStatusRequest;
import kitchenpos.product.domain.Product;
import kitchenpos.table.domain.OrderTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceKitchenPosTest extends KitchenPosTestFixture {

    @Mock
    private MenuDao menuDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderLineItemDao orderLineItemDao;
    @Mock
    private OrderTableDao orderTableDao;
    @InjectMocks
    private OrderService orderService;

    public static final long MENU_ID = 1L;

    private final Product product = 상품을_저장한다(
            1L,
            "강정치킨",
            BigDecimal.valueOf(17000)
    );
    private final MenuProduct menuProduct = 메뉴_상품을_저장한다(
            1L,
            MENU_ID,
            product.getId(),
            2L
    );
    private final Menu menu = 메뉴를_저장한다(
            MENU_ID,
            "후라이드+후라이드",
            BigDecimal.valueOf(2000),
            null,
            Collections.singletonList(menuProduct)
    );

    private final OrderTable orderTable = 주문_테이블을_저장한다(
            1L,
            null,
            2,
            false
    );

    private final OrderLineItem orderLineItem = 주문_항목을_저장한다(
            1L,
            orderTable.getId(),
            menu.getId(),
            menuProduct.getQuantity()
    );

    List<OrderLineItem> orderLineItems = Collections.singletonList(orderLineItem);

    private final Order completionStatusOrder = 주문을_저장한다(
            1L,
            orderTable.getId(),
            OrderStatus.COMPLETION.name(),
            LocalDateTime.now(),
            orderLineItems
    );

    private final Order mealStatusOrder = 주문을_저장한다(
            2L,
            orderTable.getId(),
            OrderStatus.MEAL.name(),
            LocalDateTime.now(),
            orderLineItems
    );

    @DisplayName("메뉴를 주문할 수 있다.")
    @Test
    void create() {
        // given
        OrderLineItemRequest orderLineItemRequest = 주문_항목을_요청한다(
                orderLineItem.getMenuId(),
                orderLineItem.getQuantity()
        );

        OrderRequest orderRequest = 주문을_요청한다(
                orderTable.getId(),
                Collections.singletonList(orderLineItemRequest)
        );

        given(menuDao.countByIdIn(Collections.singletonList(menu.getId()))).willReturn(1L);
        given(orderTableDao.findById(completionStatusOrder.getOrderTableId())).willReturn(Optional.of(orderTable));
        given(orderDao.save(any(Order.class))).willReturn(completionStatusOrder);
        given(orderLineItemDao.save(any(OrderLineItem.class))).willReturn(orderLineItem);

        // when
        OrderResponse savedOrder = orderService.create(orderRequest);

        // then
        assertThat(savedOrder).usingRecursiveComparison().isEqualTo(OrderResponse.of(completionStatusOrder));

        verify(menuDao, times(1)).countByIdIn(Collections.singletonList(menuProduct.getProductId()));
        verify(orderTableDao, times(1)).findById(completionStatusOrder.getOrderTableId());
        verify(orderLineItemDao, times(1)).save(any(OrderLineItem.class));
        verify(orderDao, times(1)).save(any(Order.class));
    }

    @DisplayName("주문 내역을 조회할 수 있다.")
    @Test
    void list() {
        // given
        List<Order> orders = Arrays.asList(
                주문을_저장한다(1L, completionStatusOrder),
                주문을_저장한다(2L, mealStatusOrder)
        );

        given(orderDao.findAll()).willReturn(orders);
        given(orderLineItemDao.findAllByOrderId(any(Long.class))).willReturn(orderLineItems);

        // when
        List<OrderResponse> results = orderService.list();

        // then
        assertThat(results).usingRecursiveComparison().isEqualTo(orders);

        verify(orderDao, times(1)).findAll();
        verify(orderLineItemDao, times(2)).findAllByOrderId(any(Long.class));
    }

    @DisplayName("주문 상태를 변경할 수 있다.")
    @Test
    void changeOrderStatus() {
        // given
        given(orderDao.findById(mealStatusOrder.getId())).willReturn(Optional.of(mealStatusOrder));
        given(orderLineItemDao.findAllByOrderId(mealStatusOrder.getId())).willReturn(mealStatusOrder.getOrderLineItems());
        OrderStatusRequest orderStatusRequest = new OrderStatusRequest(OrderStatus.COOKING.name());

        // when
        OrderResponse changedOrder = orderService.changeOrderStatus(mealStatusOrder.getId(), orderStatusRequest);

        // then
        assertThat(changedOrder).usingRecursiveComparison().isEqualTo(OrderResponse.of(mealStatusOrder));

        verify(orderDao, times(1)).findById(mealStatusOrder.getId());
        verify(orderDao, times(1)).save(mealStatusOrder);
        verify(orderLineItemDao, times(1)).findAllByOrderId(mealStatusOrder.getId());
    }
}