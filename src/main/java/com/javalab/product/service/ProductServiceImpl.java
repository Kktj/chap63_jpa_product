package com.javalab.product.service;


import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.function.Function;

import com.javalab.product.dto.ProductDTO;
import com.javalab.product.dto.PageRequestDTO;
import com.javalab.product.dto.PageResultDTO;
import com.javalab.product.dto.ProductDTO;
import com.javalab.product.entity.Category;
import com.javalab.product.entity.Product;
import com.javalab.product.entity.Product;
import com.javalab.product.repository.CategoryRepository;
import com.javalab.product.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	// 의존성주입 - ProductRepository
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    public ProductServiceImpl(ProductRepository productRepository,
    							CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // 상품 목록 조회
    @Override
    public PageResultDTO<ProductDTO, Product> getList(PageRequestDTO requestDTO) {

        Pageable pageable = requestDTO.getPageable(Sort.by("productId").descending());

        Page<Product> result = productRepository.findAll(pageable);
        
        Function<Product, ProductDTO> fn = (entity -> entityToDto(entity)); // java.util.Function
        return new PageResultDTO<>(result, fn );
    }    
    

    
    // 상품 한개 조회
    @Override
    public ProductDTO read(Integer productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() ? entityToDto(product.get()): null;
    }

    /*
     * 상품 저장
     *  - 화면에서 전달받은 정보를 저장하고 있는 ProductDTO를 Category Entity로
     *    변환해서 영속 영역에 저장한다.
     */
    @Override
    public Product register(ProductDTO dto) {
    	Product entity = dtoToEntity(dto);
        return productRepository.save(entity);
    }

//    @Override
//    public void modify(ProductDTO productDTO) {
//    	// 1. 수정할 상품 엔티티 조회
//    	// Optional<Category> 감싸는 이유 : 결과가 nul 일수도 있기 때문
//        Optional<Product> product = productRepository.findById(productDTO.getProductId());
//        
//        // 2. 엔티티가 존재할 경우 수정작업
//        if (product.isPresent()) {
//        	Product existingCategory = product.get();
//            existingCategory.setProductName(productDTO.getProductName()); 	// 화면의 입력값으로 변경
//            existingCategory.setPrice(productDTO.getPrice());
//            existingCategory.setCategory(productDTO.getCategoryId());            
//            existingCategory.setDescription(productDTO.getDescription()); 		// 화면의 입력값으로 변경
//            productRepository.save(existingCategory);	// 영속화
//        } 
//    }

    @Override
    public void modify(ProductDTO productDTO) {
        Optional<Product> product = productRepository.findById(productDTO.getProductId());
        if (product.isPresent()) {
            Product existingProduct = product.get();
            existingProduct.setProductName(productDTO.getProductName());
            existingProduct.setPrice(productDTO.getPrice());
            
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리가 존재하지 않습니다."));
            
            existingProduct.setCategory(category); 
            existingProduct.setDescription(productDTO.getDescription());
            productRepository.save(existingProduct);
        }
    }

    
    
    @Override
    public boolean remove(Integer productId) {
    	// 1. 삭제할 상품 엔티티 조회
        Optional<Product> user = productRepository.findById(productId);
        // 2. 존재할 경우 삭제 처리
        if (user.isPresent()) {
            productRepository.deleteById(productId);
            return true;
        } else {
            return false;
        }
    }
    
 
    /*
     * 복잡한 검색시 사용시 필요
    private BooleanBuilder getSearch(PageRequestDTO requestDTO){

        String type = requestDTO.getType();
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QGuestbook qGuestbook = QGuestbook.guestbook;
        String keyword = requestDTO.getKeyword();
        BooleanExpression expression = qGuestbook.categoryId.gt(0L); // gno > 0 조건만 생성
        booleanBuilder.and(expression);
        if(type == null || type.trim().length() == 0){ //검색 조건이 없는 경우
            return booleanBuilder;
        }

        //검색 조건을 작성하기
        BooleanBuilder conditionBuilder = new BooleanBuilder();
        if(type.contains("t")){
            conditionBuilder.or(qGuestbook.title.contains(keyword));
        }
        if(type.contains("c")){
            conditionBuilder.or(qGuestbook.content.contains(keyword));
        }
        if(type.contains("w")){
            conditionBuilder.or(qGuestbook.writer.contains(keyword));
        }

        //모든 조건 통합
        booleanBuilder.and(conditionBuilder);
        return booleanBuilder;
    }    
    */
    
}
