package org.uu.wallet.config;


import com.fasterxml.classmate.TypeResolver;
import org.uu.wallet.vo.BannerListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.Map;


@Configuration
@EnableSwagger2
public class Swagger2 {


    @Autowired
    private TypeResolver typeResolver;


    /**
     * 通过 createRestApi函数来构建一个DocketBean
     * 函数名,可以随意命名,喜欢什么命名就什么命名
     */
    @Bean
    public Docket createRestApi() {

        // 自定义的排除逻辑
        Predicate<String> notExcludedPaths = input ->
                !input.equals("/api/v1/banner/getBannerList") &&
                !input.equals("/api/v1/announcement/getAnnouncementList") &&
                !input.equals("/api/v1/announcement/detail");

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())//调用apiInfo方法,创建一个ApiInfo实例,里面是展示在文档页面信息内容
                .alternateTypeRules(//解决返回对象为Map<String, List<VersionPushStatisticResp>>时，Swagger页面报错
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(Map.class, String.class, typeResolver.resolve(List.class, BannerListVo.class)),
                                typeResolver.resolve(Map.class, String.class, WildcardType.class), Ordered.HIGHEST_PRECEDENCE))
                .select()
                //控制暴露出去的路径下的实例
                //如果某个接口不想暴露,可以使用以下注解
                //@ApiIgnore 这样,该接口就不会暴露在 swagger2 的页面下
                .apis(RequestHandlerSelectors.basePackage("org.uu.wallet"))
                .paths(PathSelectors.any())
                // 使用自定义的Predicate来排除特定路径
                .paths(notExcludedPaths)
                .build();
    }

    //构建 api文档的详细信息函数
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("uu-Wallet API")
                //条款地址
//                .termsOfServiceUrl("http://despairyoke.github.io/")
                .contact("zwd")
                .version("1.0")
                //描述
//                .description("API 描述")
                .build();
    }
}
