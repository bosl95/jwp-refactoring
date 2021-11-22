package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.KitchenPosTestFixture;
import kitchenpos.order.application.OrderService;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.ui.OrderRestController;
import kitchenpos.order.ui.dto.OrderLineItemRequest;
import kitchenpos.order.ui.dto.OrderRequest;
import kitchenpos.order.ui.dto.OrderResponse;
import kitchenpos.order.ui.dto.OrderStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = OrderRestController.class)
@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest extends KitchenPosTestFixture {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;

    private final OrderLineItem orderLineItem1 = 주문_항목을_저장한다(1L, 1L, 1L, 1000L);
    private final OrderLineItem orderLineItem2 = 주문_항목을_저장한다(2L, 2L, 2L, 1000L);

    private final Order firstOrder = 주문을_저장한다(
            1L,
            1L,
            OrderStatus.COMPLETION.name(),
            LocalDateTime.now(),
            Arrays.asList(orderLineItem1, orderLineItem2)
    );

    private final Order secondOrder = 주문을_저장한다(
            2L,
            2L,
            OrderStatus.COOKING.name(),
            LocalDateTime.now(),
            Collections.singletonList(orderLineItem2)
    );
    private final OrderLineItemRequest firstOrderLineItemRequest = 주문_항목을_요청한다(
            orderLineItem1.getMenuId(),
            orderLineItem1.getQuantity()
    );
    private final OrderRequest firstOrderRequest = 주문을_요청한다(
            firstOrder.getOrderTableId(),
            Collections.singletonList(firstOrderLineItemRequest)
    );

    private final OrderStatusRequest orderStatusRequest = new OrderStatusRequest(OrderStatus.COMPLETION.name());

    @Test
    void create() throws Exception {
        // given
        // when
        given(orderService.create(any(OrderRequest.class))).willReturn(OrderResponse.of(firstOrder));

        // then
        mvc.perform(post("/api/orders")
                        .content(objectMapper.writeValueAsString(firstOrderRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(firstOrder.getId().intValue())))
                .andExpect(jsonPath("$.orderTableId", is(firstOrder.getOrderTableId().intValue())))
                .andExpect(jsonPath("$.orderStatus", is(firstOrder.getOrderStatus())))
                .andExpect(jsonPath("$.orderedTime", is(표준_시간_형식을_적용한다(firstOrder.getOrderedTime()))))
                .andExpect(jsonPath("$.orderLineItems", hasSize(firstOrder.getOrderLineItems().size())));
    }

    @Test
    void list() throws Exception {
        // given
        List<Order> orders = Arrays.asList(firstOrder, secondOrder);

        // when
        given(orderService.list()).willReturn(OrderResponse.toList(orders));

        // then
        mvc.perform(get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstOrder.getId().intValue())))
                .andExpect(jsonPath("$[0].orderTableId", is(firstOrder.getOrderTableId().intValue())))
                .andExpect(jsonPath("$[0].orderStatus", is(OrderStatus.COMPLETION.name())))
                .andExpect(jsonPath("$[0].orderLineItems", hasSize(firstOrder.getOrderLineItems().size())))
                .andExpect(jsonPath("$[1].id", is(secondOrder.getId().intValue())))
                .andExpect(jsonPath("$[1].orderTableId", is(secondOrder.getOrderTableId().intValue())))
                .andExpect(jsonPath("$[1].orderStatus", is(secondOrder.getOrderStatus())))
                .andExpect(jsonPath("$[1].orderLineItems", hasSize(secondOrder.getOrderLineItems().size())));
    }

    @Test
    void changeOrderStatus() throws Exception {
        // given
        // when
        given(orderService.changeOrderStatus(any(Long.class), any(OrderStatusRequest.class))).willReturn(OrderResponse.of(secondOrder));

        // then
        mvc.perform(put("/api/orders/{orderId}/order-status", 1)
                        .content(objectMapper.writeValueAsString(orderStatusRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(secondOrder.getId().intValue())))
                .andExpect(jsonPath("$.orderTableId", is(secondOrder.getOrderTableId().intValue())))
                .andExpect(jsonPath("$.orderStatus", is(secondOrder.getOrderStatus())))
                .andExpect(jsonPath("$.orderedTime", is(표준_시간_형식을_적용한다(secondOrder.getOrderedTime()))))
                .andExpect(jsonPath("$.orderLineItems", hasSize(secondOrder.getOrderLineItems().size())));
    }
}