package kitchenpos.tablegroup.ui.dto;

import kitchenpos.table.ui.dto.OrderTableRequest;
import kitchenpos.tablegroup.domain.TableGroup;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class TableGroupRequest {

    private LocalDateTime createdDate;
    private List<OrderTableRequest> orderTables;

    private TableGroupRequest() {
    }

    public TableGroupRequest(List<OrderTableRequest> orderTables) {
        this.orderTables = Collections.unmodifiableList(orderTables);
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public List<OrderTableRequest> getOrderTables() {
        return orderTables;
    }

    public TableGroup toEntity() {
        TableGroup tableGroup = new TableGroup();
        tableGroup.setCreatedDate(createdDate);
        tableGroup.setOrderTables(OrderTableRequest.toList(orderTables));
        return tableGroup;
    }
}
