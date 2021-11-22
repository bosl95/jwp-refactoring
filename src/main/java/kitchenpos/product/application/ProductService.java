package kitchenpos.product.application;

import kitchenpos.product.dao.ProductDao;
import kitchenpos.product.domain.Product;
import kitchenpos.product.ui.dto.ProductRequest;
import kitchenpos.product.ui.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductService {
    private final ProductDao productDao;

    public ProductService(final ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public ProductResponse create(final ProductRequest productRequest) {
        validateName(productRequest);
        validatePrice(productRequest);

        Product savedProduct = productDao.save(productRequest.toEntity());
        return ProductResponse.of(savedProduct);
    }

    private void validateName(ProductRequest productRequest) {
        final String name = productRequest.getName();

        if (Objects.isNull(name) || name.length() < 1) {
            throw new IllegalArgumentException();
        }
    }

    private void validatePrice(ProductRequest productRequest) {
        final BigDecimal price = productRequest.getPrice();

        if (Objects.isNull(price) || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException();
        }
    }

    public List<ProductResponse> list() {
        return ProductResponse.toList(productDao.findAll());
    }
}
