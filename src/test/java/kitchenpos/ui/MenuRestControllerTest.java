package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.KitchenPosTestFixture;
import kitchenpos.menu.application.MenuService;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.ui.MenuRestController;
import kitchenpos.menu.ui.dto.MenuProductRequest;
import kitchenpos.menu.ui.dto.MenuRequest;
import kitchenpos.menu.ui.dto.MenuResponse;
import kitchenpos.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ContextConfiguration(classes = MenuRestController.class)
@WebMvcTest(MenuRestController.class)
class MenuRestControllerTest extends KitchenPosTestFixture {

    MenuProductRequest secondMenuProductRequest = 메뉴_상품을_요청한다(2L, 200L);
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MenuService menuService;
    private Product firstProduct = 상품을_저장한다(1L, "후라이드", BigDecimal.valueOf(3000));
    private Product secondProduct = 상품을_저장한다(1L, "후라이드", BigDecimal.valueOf(3000));
    private MenuProduct firstMenuProduct = 메뉴_상품을_저장한다(1L, 1L, firstProduct.getId(), 100L);
    private Menu firstMenu = 메뉴를_저장한다(1L, "닭강정", BigDecimal.valueOf(1000), 1L, Collections.singletonList(firstMenuProduct));
    MenuProductRequest firstMenuProductRequest = 메뉴_상품을_요청한다(firstMenuProduct.getProductId(), firstMenuProduct.getQuantity());
    MenuRequest firstMenuRequest = 메뉴를_요청한다(
            "닭강정",
            BigDecimal.valueOf(1000),
            1L,
            Collections.singletonList(firstMenuProductRequest)
    );
    private MenuProduct secondMenuProduct = 메뉴_상품을_저장한다(2L, 2L, secondProduct.getId(), 200L);
    private Menu secondMenu = 메뉴를_저장한다(2L, "menu2", BigDecimal.valueOf(3000), 1L, Collections.singletonList(secondMenuProduct));
    private MenuRequest secondMenuRequest = 메뉴를_요청한다(
            "떡볶이",
            BigDecimal.valueOf(3000),
            1L,
            Collections.singletonList(secondMenuProductRequest)
    );

    @Test
    void create() throws Exception {
        // given
        // when
        given(menuService.create(any(MenuRequest.class))).willReturn(MenuResponse.of(firstMenu));

        // then
        mvc.perform(post("/api/menus")
                        .content(objectMapper.writeValueAsString(firstMenuRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(firstMenu.getId().intValue())))
                .andExpect(jsonPath("$.name", is(firstMenu.getName())))
                .andExpect(jsonPath("$.price", is(firstMenu.getPrice().intValue())))
                .andExpect(jsonPath("$.menuGroupId", is(firstMenu.getMenuGroupId().intValue())))
                .andExpect(jsonPath("$.menuProducts", hasSize(1)));
    }

    @Test
    void list() throws Exception {
        // given
        List<Menu> menus = Arrays.asList(firstMenu, secondMenu);

        // when
        given(menuService.list()).willReturn(MenuResponse.toList(menus));

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/menus")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(firstMenu.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(firstMenu.getName())))
                .andExpect(jsonPath("$[0].price", is(firstMenu.getPrice().intValue())))
                .andExpect(jsonPath("$[0].menuGroupId", is(firstMenu.getMenuGroupId().intValue())))
                .andExpect(jsonPath("$[0].menuProducts", hasSize(firstMenu.getMenuProducts().size())))
                .andExpect(jsonPath("$[0].menuProducts[0].seq", is(firstMenu.getMenuProducts().get(0).getSeq().intValue())))
                .andExpect(jsonPath("$[1].id", is(secondMenu.getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(secondMenu.getName())))
                .andExpect(jsonPath("$[1].price", is(secondMenu.getPrice().intValue())))
                .andExpect(jsonPath("$[1].menuGroupId", is(secondMenu.getMenuGroupId().intValue())))
                .andExpect(jsonPath("$[1].menuProducts", hasSize(secondMenu.getMenuProducts().size())))
                .andExpect(jsonPath("$[1].menuProducts[0].seq", is(secondMenu.getMenuProducts().get(0).getSeq().intValue())));
    }
}