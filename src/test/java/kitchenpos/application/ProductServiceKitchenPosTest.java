package kitchenpos.application;

import kitchenpos.KitchenPosTestFixture;
import kitchenpos.product.application.ProductService;
import kitchenpos.product.dao.ProductDao;
import kitchenpos.product.domain.Product;
import kitchenpos.product.ui.dto.ProductRequest;
import kitchenpos.product.ui.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceKitchenPosTest extends KitchenPosTestFixture {

    @Mock
    private ProductDao productDao;
    @InjectMocks
    private ProductService productService;

    private final ProductRequest firstProductRequest = 상품을_요청한다("강정치킨", BigDecimal.valueOf(1700));
    private final ProductRequest secondProductRequest = 상품을_요청한다("튀김소보로", BigDecimal.valueOf(1200));

    @DisplayName("상품을 등록할 수 있다.")
    @Test
    void create() {
        // given
        Product savedFirstProduct = 상품을_저장한다(1L, firstProductRequest);
        given(productDao.save(any(Product.class))).willReturn(savedFirstProduct);

        // when
        ProductResponse savedProduct = productService.create(firstProductRequest);

        // then
        assertThat(savedProduct).usingRecursiveComparison().isEqualTo(ProductResponse.of(savedFirstProduct));
        verify(productDao, times(1)).save(any(Product.class));
    }

    @DisplayName("상품명이 null인 경우 예외가 발생한다.")
    @Test
    void nullNameProduct() {
        // given
        ProductRequest nullNameProduct = 상품을_요청한다(null, BigDecimal.valueOf(1700));

        // then
        assertThatThrownBy(() -> productService.create(nullNameProduct)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품명이 1미만인 경우 예외가 발생한다.")
    @Test
    void emptyNameProduct() {
        // given
        ProductRequest emptyNameProduct = 상품을_요청한다("", BigDecimal.valueOf(1700));

        // then
        assertThatThrownBy(() -> productService.create(emptyNameProduct)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 가격이 음수인 경우 예외가 발생한다.")
    @Test
    void negativeProductPrice() {
        // given
        ProductRequest negativePriceProduct = 상품을_요청한다("강정치킨", BigDecimal.valueOf(-1));

        // when
        assertThatThrownBy(() -> productService.create(negativePriceProduct)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품 가격이 null인 경우 예외가 발생한다.")
    @Test
    void validateProductPrice() {
        // given
        ProductRequest nullPriceProduct = 상품을_요청한다("강정치킨", null);

        // when
        assertThatThrownBy(() -> productService.create(nullPriceProduct)).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상품을 조회할 수 있다.")
    @Test
    void list() {
        // given
        Product savedFirstProduct = 상품을_저장한다(1L, firstProductRequest);
        Product savedSecondProduct = 상품을_저장한다(2L, secondProductRequest);
        List<Product> products = Arrays.asList(savedFirstProduct, savedSecondProduct);
        given(productDao.findAll()).willReturn(products);

        // when
        List<ProductResponse> result = productService.list();

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(ProductResponse.toList(products));
        verify(productDao, times(1)).findAll();
    }
}