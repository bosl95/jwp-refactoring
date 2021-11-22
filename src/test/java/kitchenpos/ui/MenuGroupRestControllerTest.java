package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.KitchenPosTestFixture;
import kitchenpos.application.MenuGroupService;
import kitchenpos.ui.dto.MenuGroupResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = MenuGroupRestController.class)
@WebMvcTest(MenuGroupRestController.class)
class MenuGroupRestControllerTest extends KitchenPosTestFixture {

    private final MenuGroupResponse firstMenuGroup = 메뉴_그룹을_응답한다(1L, "추천 메뉴");
    private final MenuGroupResponse secondMenuGroup = 메뉴_그룹을_응답한다(2L, "추천 메뉴2");
    private final MenuGroupResponse thirdMenuGroup = 메뉴_그룹을_응답한다(3L, "추천 메뉴3");
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MenuGroupService menuGroupService;

    @Test
    void create() throws Exception {
        // given
        // when
        given(menuGroupService.create(any())).willReturn(firstMenuGroup);

        // then
        mvc.perform(post("/api/menu-groups")
                        .content(objectMapper.writeValueAsString(메뉴_그룹을_요청한다("추천 메뉴")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(firstMenuGroup.getId().intValue()))
                .andExpect(jsonPath("$.name").value(firstMenuGroup.getName()));
    }

    @Test
    void list() throws Exception {
        // given
        List<MenuGroupResponse> menuGroups = Arrays.asList(firstMenuGroup, secondMenuGroup, thirdMenuGroup);

        // when
        given(menuGroupService.list()).willReturn(menuGroups);

        // then
        mvc.perform(MockMvcRequestBuilders.get("/api/menu-groups")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(firstMenuGroup.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(firstMenuGroup.getName())))
                .andExpect(jsonPath("$[1].id", is(secondMenuGroup.getId().intValue())))
                .andExpect(jsonPath("$[1].name", is(secondMenuGroup.getName())))
                .andExpect(jsonPath("$[2].id", is(thirdMenuGroup.getId().intValue())))
                .andExpect(jsonPath("$[2].name", is(thirdMenuGroup.getName())));
    }
}