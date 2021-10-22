package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderTableDao orderTableDao;

    @InjectMocks
    private TableService tableService;

    private OrderTable orderTable;

    @BeforeEach
    void setUp() {
        orderTable = new OrderTable();
        orderTable.setNumberOfGuests(0);
        orderTable.setEmpty(true);
    }

    @DisplayName("테이블을 등록할 수 있다.")
    @Test
    void create() {
        // given
        OrderTable savedOrderTable = new OrderTable();
        savedOrderTable.setNumberOfGuests(0);
        savedOrderTable.setEmpty(true);
        given(orderTableDao.save(orderTable)).willReturn(savedOrderTable);

        // when
        OrderTable result = tableService.create(orderTable);

        // then
        assertThat(result).isEqualTo(savedOrderTable);
    }

    @DisplayName("전체 테이블을 조회한다.")
    @Test
    void list() {
        // given
        orderTable.setId(1L);
        given(orderTableDao.findAll()).willReturn(Collections.singletonList(orderTable));

        // when
        List<OrderTable> orderTables = tableService.list();

        // then
        assertThat(orderTables).containsExactly(orderTable);
    }

    @DisplayName("테이블의 공석 유무를 변경한다.")
    @Test
    void changeEmpty() {
        // given
        orderTable.setId(1L);
        given(orderTableDao.findById(1L)).willReturn(java.util.Optional.ofNullable(orderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(1L, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
                .willReturn(false);


        OrderTable changedOrderTable = 주문_테이블을_저장한다(null, 0, false);
        given(orderTableDao.save(any(OrderTable.class))).willReturn(changedOrderTable);

        // when
        OrderTable result = tableService.changeEmpty(1L, this.orderTable);

        // then
        assertThat(result).isEqualTo(changedOrderTable);
    }

    @DisplayName("단체 손님인 경우 변경할 수 없다.")
    @Test
    void validTableGroupId() {
        // given
        orderTable.setId(1L);
        orderTable.setTableGroupId(1L);
        given(orderTableDao.findById(1L)).willReturn(java.util.Optional.ofNullable(orderTable));

        // when
        // then
        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTable)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("조리/식사중인 경우 변경할 수 없다.")
    @Test
    void validOrderTableStatus() {
        // given
        orderTable.setId(1L);
        given(orderTableDao.findById(1L)).willReturn(java.util.Optional.ofNullable(orderTable));
        given(orderDao.existsByOrderTableIdAndOrderStatusIn(
                1L, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> tableService.changeEmpty(1L, orderTable)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블의 손님 인원을 변경한다.")
    @Test
    void changeNumberOfGuests() {
        // given
        orderTable = 주문_테이블을_저장한다(1L, orderTable.getNumberOfGuests(), false);
        given(orderTableDao.findById(1L)).willReturn(java.util.Optional.ofNullable(orderTable));

        OrderTable changedOrderTable = new OrderTable();
        changedOrderTable.setNumberOfGuests(10);
        changedOrderTable.setEmpty(false);

        OrderTable expected = 주문_테이블을_저장한다(orderTable.getId(), orderTable.getNumberOfGuests(), false);
        given(orderTableDao.save(any(OrderTable.class))).willReturn(expected);

        // when
        OrderTable result = tableService.changeNumberOfGuests(1L, changedOrderTable);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("테이블의 손님 인원은 0이상이어야한다.")
    @Test
    void validNumberOfGuests() {
        // given
        OrderTable changedOrderTable = 주문_테이블을_저장한다(orderTable.getId(), -1, orderTable.isEmpty());

        // when
        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, changedOrderTable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("변경할 테이블이 존재해야한다.")
    @Test
    void validOrderTable() {
        // given
        given(orderTableDao.findById(1L)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블이 공석인 경우 변경할 수 없다.")
    @Test
    void validOrderTableIsEmpty() {
        // given
        given(orderTableDao.findById(1L)).willReturn(Optional.of(orderTable));

        // then
        assertThatThrownBy(() -> tableService.changeNumberOfGuests(1L, orderTable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private OrderTable 주문_테이블을_저장한다(Long id, int numberOfGuests, boolean empty) {
        OrderTable orderTable = new OrderTable();
        orderTable.setId(this.orderTable.getId());
        orderTable.setNumberOfGuests(this.orderTable.getNumberOfGuests());
        orderTable.setEmpty(empty);
        return orderTable;
    }
}