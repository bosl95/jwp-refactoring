package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.ui.dto.MenuProductRequest;
import kitchenpos.ui.dto.MenuRequest;
import kitchenpos.ui.dto.MenuResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuServiceKitchenPosTest extends KitchenPosTestFixture {

    private final Product product = 상품을_저장한다(
            1L,
            "강정치킨",
            BigDecimal.valueOf(17000)
    );
    private final MenuProduct menuProduct = 메뉴_상품을_저장한다(
            1L,
            1L,
            product.getId(),
            2L
    );
    private final Menu menu = 메뉴를_저장한다(
            1L,
            "후라이드+후라이드",
            BigDecimal.valueOf(19000),
            1L,
            Collections.singletonList(menuProduct)
    );
    private final MenuProductRequest menuProductRequest = 메뉴_상품을_요청한다(
            product.getId(),
            menuProduct.getQuantity()
    );
    private final MenuRequest menuRequest = 메뉴를_요청한다(
            "강정치킨",
            menu.getPrice(),
            1L,
            Collections.singletonList(menuProductRequest)
    );
    @Mock
    private MenuDao menuDao;
    @Mock
    private MenuGroupDao menuGroupDao;
    @Mock
    private MenuProductDao menuProductDao;
    @Mock
    private ProductDao productDao;
    @InjectMocks
    private MenuService menuService;

    @DisplayName("메뉴를 등록할 수 있다.")
    @Test
    void create() {
        // given
        given(menuGroupDao.existsById(menuRequest.getMenuGroupId())).willReturn(true);
        given(productDao.findById(any(Long.class))).willReturn(Optional.of(product));
        given(menuDao.save(any(Menu.class))).willReturn(menu);
        given(menuProductDao.save(any(MenuProduct.class))).willReturn(menuProduct);

        // when
        MenuResponse savedMenu = menuService.create(menuRequest);

        // then
        assertThat(savedMenu).usingRecursiveComparison().isEqualTo(MenuResponse.of(menu));
        verify(menuGroupDao, times(1)).existsById(menu.getMenuGroupId());
        verify(productDao, times(1)).findById(menuProduct.getProductId());
        verify(menuDao, times(1)).save(any(Menu.class));
    }

    @DisplayName("메뉴를 조회할 수 있다.")
    @Test
    void list() {
        // given
        Menu savedMenu = 메뉴를_저장한다(1L, menu);

        given(menuDao.findAll()).willReturn(Collections.singletonList(savedMenu));
        given(menuProductDao.findAllByMenuId(savedMenu.getId())).willReturn(Collections.singletonList(menuProduct));

        // when
        List<MenuResponse> findMenus = menuService.list();

        // then
        assertThat(findMenus).usingRecursiveComparison().isEqualTo(Collections.singletonList(MenuResponse.of(savedMenu)));
        verify(menuDao, times(1)).findAll();
        verify(menuProductDao, times(1)).findAllByMenuId(savedMenu.getId());
    }

    @DisplayName("메뉴 가격은 0원 이상이어야한다.")
    @Test
    void validateMenuPriceLength() {
        // when
        MenuRequest invalidMenuPriceRequest = new MenuRequest(
                menuRequest.getName(),
                BigDecimal.valueOf(-1),
                menuRequest.getMenuGroupId(),
                menuRequest.getMenuProducts()
        );

        // then
        assertThatThrownBy(() -> menuService.create(invalidMenuPriceRequest)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴 제품 양은 0 이상이어야한다.")
    @Test
    void validateMenuProduct() {
        // when
        MenuProductRequest menuProductRequest = 메뉴_상품을_요청한다(product.getId(), -1L);
        MenuRequest invalidMenuPriceRequest = new MenuRequest(
                menuRequest.getName(),
                menuRequest.getPrice(),
                menuRequest.getMenuGroupId(),
                Collections.singletonList(menuProductRequest)
        );
        menu.getMenuProducts().get(0).setQuantity(-1);

        // then
        assertThatThrownBy(() -> menuService.create(invalidMenuPriceRequest)).isInstanceOf(IllegalArgumentException.class);
    }
}