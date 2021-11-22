package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.menugroup.application.MenuGroupService;
import kitchenpos.menugroup.dao.MenuGroupDao;
import kitchenpos.menugroup.domain.MenuGroup;
import kitchenpos.menugroup.ui.dto.MenuGroupRequest;
import kitchenpos.menugroup.ui.dto.MenuGroupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceKitchenPosTest extends KitchenPosTestFixture {

    @Mock
    private MenuGroupDao menuGroupDao;
    @InjectMocks
    private MenuGroupService menuGroupService;

    private final MenuGroup menuGroup = 메뉴_그룹을_저장한다(null, "추천 메뉴");
    private final MenuGroupRequest menuGroupRequest = 메뉴_그룹을_요청한다("추천 메뉴");

    @DisplayName("메뉴 분류를 위한 그룹을 등록할 수 있다.")
    @Test
    void create() {
        // given
        MenuGroup savedMenuGroup = 메뉴_그룹을_저장한다(1L, menuGroup.getName());
        given(menuGroupDao.save(any(MenuGroup.class))).willReturn(savedMenuGroup);

        // when
        MenuGroupResponse result = menuGroupService.create(menuGroupRequest);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(MenuGroupResponse.of(savedMenuGroup));
        verify(menuGroupDao, times(1)).save(any(MenuGroup.class));
    }

    @DisplayName("메뉴 분류를 위한 그룹을 조회할 수 있다.")
    @Test
    void list() {
        // given
        MenuGroup savedMenuGroup1 = 메뉴_그룹을_저장한다(1L, menuGroup.getName());
        MenuGroup savedMenuGroup2 = 메뉴_그룹을_저장한다(2L, menuGroup.getName());
        List<MenuGroup> savedGroups = Arrays.asList(savedMenuGroup1, savedMenuGroup2);

        // when
        given(menuGroupDao.findAll()).willReturn(savedGroups);
        List<MenuGroupResponse> result = menuGroupService.list();

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(MenuGroupResponse.toList(savedGroups));
        verify(menuGroupDao, times(1)).findAll();
    }
}