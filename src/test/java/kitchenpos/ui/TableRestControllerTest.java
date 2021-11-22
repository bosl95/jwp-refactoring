package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.KitchenPosTestFixture;
import kitchenpos.table.application.TableService;
import kitchenpos.table.ui.TableRestController;
import kitchenpos.table.ui.dto.OrderTableRequest;
import kitchenpos.table.ui.dto.OrderTableResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = TableRestController.class)
@WebMvcTest(TableRestController.class)
class TableRestControllerTest extends KitchenPosTestFixture {

    private final OrderTableRequest firstOrderTableRequest = 주문_테이블을_요청한다(3, false);
    private final OrderTableResponse firstOrderTableResponse
            = OrderTableResponse.of(주문_테이블을_저장한다(1L, 1L, firstOrderTableRequest));
    private final OrderTableRequest secondOrderTableRequest = 주문_테이블을_요청한다(3, true);
    private final OrderTableResponse secondOrderTableResponse
            = OrderTableResponse.of(주문_테이블을_저장한다(2L, 1L, secondOrderTableRequest));
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TableService tableService;

    @Test
    void create() throws Exception {
        // given
        // when
        given(tableService.create(any(OrderTableRequest.class))).willReturn(firstOrderTableResponse);

        // then
        mvc.perform(post("/api/tables")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstOrderTableRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(firstOrderTableResponse.getId().intValue())))
                .andExpect(jsonPath("$.tableGroupId", is(firstOrderTableResponse.getTableGroupId().intValue())))
                .andExpect(jsonPath("$.numberOfGuests", is(firstOrderTableResponse.getNumberOfGuests())))
                .andExpect(jsonPath("$.empty", is(firstOrderTableResponse.isEmpty())));
    }

    @Test
    void list() throws Exception {
        // given
        List<OrderTableResponse> orderTableResponses = Arrays.asList(firstOrderTableResponse, secondOrderTableResponse);

        // when
        given(tableService.list()).willReturn(orderTableResponses);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/tables")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstOrderTableResponse.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(secondOrderTableResponse.getId().intValue())));
    }

    @Test
    void changeEmpty() throws Exception {
        // given
        OrderTableRequest changedOrderTableRequest = 주문_테이블을_요청한다(3, false);
        OrderTableResponse changedOrderTableResponse =
                OrderTableResponse.of(주문_테이블을_저장한다(2L, 1L, changedOrderTableRequest));

        // when
        given(tableService.changeEmpty(any(Long.class), any(OrderTableRequest.class))).willReturn(changedOrderTableResponse);

        // then
        mvc.perform(put("/api/tables/{orderTableId}/empty", 1)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changedOrderTableRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(changedOrderTableResponse.getId().intValue())))
                .andExpect(jsonPath("$.tableGroupId", is(changedOrderTableResponse.getTableGroupId().intValue())))
                .andExpect(jsonPath("$.numberOfGuests", is(changedOrderTableResponse.getNumberOfGuests())))
                .andExpect(jsonPath("$.empty", is(changedOrderTableResponse.isEmpty())));
    }

    @Test
    void changeNumberOfGuests() throws Exception {
        // given
        OrderTableRequest changedOrderTableRequest = 주문_테이블을_요청한다(7, false);
        OrderTableResponse changedOrderTableResponse =
                OrderTableResponse.of(주문_테이블을_저장한다(1L, 1L, changedOrderTableRequest));

        // when
        given(tableService.changeNumberOfGuests(any(Long.class), any(OrderTableRequest.class))).willReturn(changedOrderTableResponse);

        // then
        mvc.perform(put("/api/tables/{orderTableId}/number-of-guests", 1L)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changedOrderTableRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(changedOrderTableResponse.getId().intValue())))
                .andExpect(jsonPath("$.tableGroupId", is(changedOrderTableResponse.getTableGroupId().intValue())))
                .andExpect(jsonPath("$.numberOfGuests", is(changedOrderTableResponse.getNumberOfGuests())))
                .andExpect(jsonPath("$.empty", is(changedOrderTableResponse.isEmpty())));
    }
}