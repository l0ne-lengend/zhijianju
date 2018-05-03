package cn.edu.iip.nju.dao;

import cn.edu.iip.nju.dao.SQL.InjureCaseDaoSQL;
import cn.edu.iip.nju.model.InjureCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by xu on 2017/10/24.
 */
@Repository
public interface InjureCaseDao extends JpaRepository<InjureCase,Integer>,InjureCaseDaoSQL {
    Page<InjureCase> findAllByInjureTypeNotNullAndAndInjureTypeNot(Pageable pageable,String not);
    Page<InjureCase> findAllByProductName(Pageable pageable,String productName);

    long countAllByProvinceLike(String prov);

    long countAllByProvinceLikeAndInjureDegreeEquals(String prov,String injureDegree);

    InjureCase findFirstByProductNameOrderByInjureTimeAsc(String productName);

    InjureCase findFirstByProductNameOrderByInjureTimeDesc(String productName);

    long countAllByProductNameAndInjureTypeNot(String productName,String not);


}