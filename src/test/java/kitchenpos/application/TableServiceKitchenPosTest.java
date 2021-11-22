package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderTable;
import kitchenpos.ui.dto.OrderTableRequest;
import kitchenpos.ui.dto.OrderTableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TableServiceKitchenPosTest extends KitchenPosTestFixture {

    private final OrderTable firstOrderTable = 주문_테이블을_저장한다(1L, null, 2, true);
    private final OrderTable secondOrderTable = 주문_테이블을_저장한다(2L, null, 3, false);
    private final OrderTableRequest orderTableRequest = 주문_테이블을_요청한다(0, false);
    private final OrderTable savedOrderTable = 주문_테이블을_저장한다(1L, null, orderTableRequest);
    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderTableDao orderTableDao;
    @InjectMocks
    private TableService tableService;

    @DisplayName("테이블을 등록할 수 있다.")
    @Test
    void create() {
        // given
        OrderTable savedOrderTable = 주문_테이블을_저장한다(1L, null, orderTableRequest);
        given(orderTableDao.save(any(OrderTable.class))).willReturn(savedOrderTable);

        // when
        OrderTableResponse result = tableService.create(orderTableRequest);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(OrderTableResponse.of(savedOrderTable));

        verify(orderTableDao, times(1)).save(any(OrderTable.class));
    }

    @DisplayName("전체 테이블을 조회한다.")
    @Test
    void list() {
        // given
        List<OrderTable> orderTables = Arrays.asList(firstOrderTable, secondOrderTable);
        given(orderTableDao.findAll()).willReturn(orderTables);

        // when
        List<OrderTableResponse> findOrderTables = tableService.list();

        // then
        assertThat(findOrderTables)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(OrderTableResponse.of(orderTables.get(0)), OrderTableResponse.of(orderTables.get(1)));
        verify(orderTableDao, times(1)).findAll();
    }

    @DisplayName("테이블의 공석 유무를 변경한다.")
    @Test
    void changeEmpty() {
        // given
        Long selectedOrderTableId = savedOrderTable.getId();
        given(orderTableDao.findById(selectedOrderTableId)).willReturn(Optional.of(savedOrderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(selectedOrderTableId, COOKING_OR_MEAL_STATUS)).willReturn(false);

        OrderTableRequest changedEmptyRequest = 주문_테이블을_요청한다(0, false);
        OrderTable changedOrderTable = 주문_테이블을_저장한다(savedOrderTable, changedEmptyRequest);
        given(orderTableDao.save(any(OrderTable.class))).willReturn(changedOrderTable);

        // when
        OrderTableResponse result = tableService.changeEmpty(savedOrderTable.getId(), changedEmptyRequest);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(OrderTableResponse.of(changedOrderTable));

        verify(orderTableDao, times(1)).findById(1L);
        verify(orderDao, times(1)).existsByOrderTableIdAndOrderStatusIn(1L, COOKING_OR_MEAL_STATUS);
        verify(orderTableDao, times(1)).save(any(OrderTable.class));
    }

    @DisplayName("단체 손님인 경우 변경할 수 없다.")
    @Test
    void validTableGroupId() {
        // given
        OrderTable selectOrderTable = 주문_테이블을_저장한다(1L, 1L, orderTableRequest);
        given(orderTableDao.findById(1L)).willReturn(Optional.of(selectOrderTable));

        // when
        // then
        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTableRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(1)).findById(1L);
    }

    @DisplayName("조리/식사중인 경우 변경할 수 없다.")
    @Test
    void validOrderTableStatus() {
        // given
        OrderTable selectOrderTable = 주문_테이블을_저장한다(1L, null, orderTableRequest);
        given(orderTableDao.findById(1L)).willReturn(Optional.of(selectOrderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(1L, COOKING_OR_MEAL_STATUS)).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTableRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(1)).findById(1L);
        verify(orderDao, times(1)).existsByOrderTableIdAndOrderStatusIn(1L, COOKING_OR_MEAL_STATUS);
    }


    @DisplayName("테이블의 손님 인원을 변경한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        given(orderTableDao.findById(1L)).willReturn(Optional.of(savedOrderTable));

        OrderTableRequest changedNumberOfGuestRequest = 주문_테이블을_요청한다(7, orderTableRequest.isEmpty());
        OrderTable changedOrderTable = 주문_테이블을_저장한다(savedOrderTable, changedNumberOfGuestRequest);
        given(orderTableDao.save(any(OrderTable.class))).willReturn(changedOrderTable);

        // when
        OrderTableResponse result = tableService.changeNumberOfGuests(1L, changedNumberOfGuestRequest);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(OrderTableResponse.of(changedOrderTable));

        verify(orderTableDao, times(1)).findById(1L);
        verify(orderTableDao, times(1)).save(any(OrderTable.class));
    }

    @DisplayName("테이블의 손님 인원은 0이상이어야한다.")
    @Test
    void validNumberOfGuests() {
        // given
        OrderTableRequest invalidNumberOfGuestRequest = 주문_테이블을_요청한다(-1, orderTableRequest.isEmpty());

        // when
        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, invalidNumberOfGuestRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("변경할 테이블이 존재해야한다.")
    @Test
    void validOrderTable() {
        // given
        given(orderTableDao.findById(1L)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTableRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(1)).findById(1L);
    }

    @DisplayName("테이블이 공석인 경우 변경할 수 없다.")
    @Test
    void validOrderTableIsEmpty() {
        // given
        OrderTable emptyOrderTable = 주문_테이블을_저장한다(3L, null, 0, true);
        given(orderTableDao.findById(3L)).willReturn(Optional.of(emptyOrderTable));

        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(3L, orderTableRequest))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(1)).findById(3L);
    }
}