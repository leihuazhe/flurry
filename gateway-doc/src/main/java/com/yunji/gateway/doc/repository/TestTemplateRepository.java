package com.yunji.gateway.doc.repository;

import com.yunji.gateway.doc.dto.TestTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author with struy.
 * Create by 2018/3/29 19:39
 * email :yq1724555319@gmail.com
 */

@Repository
public interface TestTemplateRepository extends CrudRepository<TestTemplate, String> {
    /**
     * 按照服务名+版本号+方法查找
     *
     * @param serviceName
     * @param version
     * @param method
     * @return
     */
    List<TestTemplate> findByServiceNameAndVersionAndMethod(String serviceName, String version, String method);
}