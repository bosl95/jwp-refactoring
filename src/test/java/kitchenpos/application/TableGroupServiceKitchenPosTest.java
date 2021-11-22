package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.order.dao.OrderDao;
import kitchenpos.order.dao.OrderTableDao;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.ui.dto.OrderTableRequest;
import kitchenpos.tablegroup.application.TableGroupService;
import kitchenpos.tablegroup.dao.TableGroupDao;
import kitchenpos.tablegroup.domain.TableGroup;
import kitchenpos.tablegroup.ui.dto.TableGroupRequest;
import kitchenpos.tablegroup.ui.dto.TableGroupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TableGroupServiceKitchenPosTest extends KitchenPosTestFixture {

    private OrderTableRequest firstOrderTableRequest = 주문_테이블을_요청한다(1L);
    private OrderTableRequest secondOrderTableRequest = 주문_테이블을_요청한다(2L);

    private TableGroupRequest tableGroupRequest = 테이블_그룹을_요청한다(
            LocalDateTime.now(),
            Arrays.asList(firstOrderTableRequest, secondOrderTableRequest)
    );

    private OrderTable firstOrderTable = 주문_테이블을_저장한다(1L, null, 3, true);
    private OrderTable secondOrderTable = 주문_테이블을_저장한다(2L, null, 2, true);

    private TableGroup savedTableGroup = 테이블_그룹을_저장한다(1L, LocalDateTime.now(), Arrays.asList(firstOrderTable, secondOrderTable));

    @Mock
    private OrderDao orderDao;
    @Mock
    private OrderTableDao orderTableDao;
    @Mock
    private TableGroupDao tableGroupDao;
    @InjectMocks
    private TableGroupService tableGroupService;

    @DisplayName("주문 테이블 그룹을 등록한다.")
    @Test
    void create() {
        // given
        List<Long> orderTableIds = Arrays.asList(firstOrderTableRequest.getId(), secondOrderTableRequest.getId());
        given(orderTableDao.findAllByIdIn(orderTableIds)).willReturn(Arrays.asList(firstOrderTable, secondOrderTable));
        given(tableGroupDao.save(any(TableGroup.class))).willReturn(savedTableGroup);

        // when
        TableGroupResponse createdTableGroup = tableGroupService.create(tableGroupRequest);

        // then
        assertThat(createdTableGroup).usingRecursiveComparison().isEqualTo(TableGroupResponse.of(savedTableGroup));

        verify(orderTableDao, times(1)).findAllByIdIn(orderTableIds);
        verify(tableGroupDao, times(1)).save(any(TableGroup.class));
        verify(orderTableDao, times(orderTableIds.size())).save(any(OrderTable.class));
    }

    @DisplayName("주문 테이블이 비어있는 경우 테이블 그룹을 등록할 수 없다.")
    @Test
    void validateTableGroupCreateWhenEmptyOrderTable() {
        // when
        TableGroupRequest invalidTableGroupRequest = 테이블_그룹을_요청한다(LocalDateTime.now(), Collections.emptyList());

        // then
        assertThatThrownBy(() -> tableGroupService.create(invalidTableGroupRequest)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블 크기가 2 미만 작은 경우 등록할 수 없다.")
    @Test
    void validateTableGroupCreateWhenOrderTableSizeLessThanTwo() {
        // when
        TableGroupRequest invalidTableGroupRequest = 테이블_그룹을_요청한다(LocalDateTime.now(), Collections.singletonList(주문_테이블을_요청한다(firstOrderTable.getId())));

        // then
        assertThatThrownBy(() -> tableGroupService.create(invalidTableGroupRequest)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문 테이블 번호를 통해 찾은 테이블 개수가 번호 개수와 같지 않은 경우 등록할 수 없다.")
    @Test
    void validateOrderTableSizeAndOrderTableIdsCount() {
        // given
        List<Long> orderTableIds = tableGroupRequest.getOrderTables()
                .stream()
                .map(OrderTableRequest::getId)
                .collect(Collectors.toList());
        given(orderTableDao.findAllByIdIn(orderTableIds)).willReturn(Collections.singletonList(firstOrderTable));

        // when
        // then
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest)).isInstanceOf(IllegalArgumentException.class);
        verify(orderTableDao, times(1)).findAllByIdIn(orderTableIds);
    }

    @DisplayName("저장된 주문 테이블이 비어있거나 주문 테이블 그룹이 이미 등록되어있다면 등록할 수 없다.")
    @Test
    void validateOrderTable() {
        // given
        List<Long> orderTableIds = tableGroupRequest.getOrderTables()
                .stream()
                .map(OrderTableRequest::getId)
                .collect(Collectors.toList());

        // when
        // then
        OrderTable findOrderTable = 주문_테이블을_저장한다(3L, null, 1, false);
        given(orderTableDao.findAllByIdIn(orderTableIds)).willReturn(Collections.singletonList(findOrderTable));
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest)).isInstanceOf(IllegalArgumentException.class);

        findOrderTable = 주문_테이블을_저장한다(3L, 3L, 1, true);
        given(orderTableDao.findAllByIdIn(orderTableIds)).willReturn(Collections.singletonList(findOrderTable));
        assertThatThrownBy(() -> tableGroupService.create(tableGroupRequest)).isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(orderTableIds.size())).findAllByIdIn(orderTableIds);
    }

    @DisplayName("주문받을 테이블을 제거한다.")
    @Test
    void ungroup() {
        // given
        List<OrderTable> orderTables = Arrays.asList(firstOrderTable, secondOrderTable);
        given(orderTableDao.findAllByTableGroupId(1L)).willReturn(orderTables);

        List<Long> orderTableIds = Arrays.asList(1L, 2L);
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, COOKING_OR_MEAL_STATUS)).willReturn(false);

        // when
        // then
        assertDoesNotThrow(() -> tableGroupService.ungroup(1L));

        verify(orderTableDao, times(1)).findAllByTableGroupId(1L);
        verify(orderDao, times(1)).existsByOrderTableIdInAndOrderStatusIn(orderTableIds, COOKING_OR_MEAL_STATUS);
        verify(orderTableDao, times(orderTables.size())).save(any(OrderTable.class));
    }

    @DisplayName("주문 테이블의 상태가 조리중/식사중인 경우 제거할 수 없다.")
    @Test
    void validateOrderStatus() {
        // given
        given(orderTableDao.findAllByTableGroupId(1L)).willReturn(Arrays.asList(firstOrderTable, secondOrderTable));
        List<Long> orderTableIds = Arrays.asList(1L, 2L);
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, COOKING_OR_MEAL_STATUS)).willReturn(true);

        // when
        // then
        assertThatThrownBy(() -> tableGroupService.ungroup(1L)).isInstanceOf(IllegalArgumentException.class);

        verify(orderTableDao, times(1)).findAllByTableGroupId(1L);
        verify(orderDao, times(1)).existsByOrderTableIdInAndOrderStatusIn(orderTableIds, COOKING_OR_MEAL_STATUS);
    }
}