package kitchenpos.menugroup.application;

import kitchenpos.menugroup.dao.MenuGroupDao;
import kitchenpos.menugroup.domain.MenuGroup;
import kitchenpos.menugroup.ui.dto.MenuGroupRequest;
import kitchenpos.menugroup.ui.dto.MenuGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MenuGroupService {
    private final MenuGroupDao menuGroupDao;

    public MenuGroupService(final MenuGroupDao menuGroupDao) {
        this.menuGroupDao = menuGroupDao;
    }

    @Transactional
    public MenuGroupResponse create(final MenuGroupRequest menuGroupRequest) {
        MenuGroup saveMenuGroup = menuGroupDao.save(menuGroupRequest.toEntity());
        return MenuGroupResponse.of(saveMenuGroup);
    }

    public List<MenuGroupResponse> list() {
        return MenuGroupResponse.toList(menuGroupDao.findAll());
    }
}
