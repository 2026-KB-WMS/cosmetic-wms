package com.kb.cosmetic_wms.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = DependencyRules.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule 도메인_계층은_프레임워크와_바깥_계층에_의존하지_않는다 =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..", "jakarta.persistence..", "..application..", "..adapter..");

    @ArchTest
    static final ArchRule 애플리케이션_계층은_어댑터에_의존하지_않는다 =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule 인바운드_어댑터는_아웃바운드_어댑터에_의존하지_않는다 =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..");

    @ArchTest
    static final ArchRule 도메인_간_의존은_허용된_경로로만_한다 =
            classes().that().resideInAPackage(DependencyRules.ROOT + "..")
                    .should(onlyUseAllowedCrossDomainPaths());

    @ArchTest
    static final ArchRule 전역_모듈은_업무_도메인에_의존하지_않는다 =
            classes().that().resideInAPackage(DependencyRules.ROOT + "." + DependencyRules.GLOBAL + "..")
                    .should(notDependOnBusinessDomains());

    private static ArchCondition<JavaClass> onlyUseAllowedCrossDomainPaths() {
        return new ArchCondition<>("서로 다른 업무 도메인에는 adapter.out→application.port, adapter.in.event→domain.event 경로로만 의존한다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (!DependencyRules.isUnderRoot(target.getPackageName())) {
                        continue;
                    }
                    if (!DependencyRules.isAllowedCrossDomainDependency(origin.getPackageName(), target.getPackageName())) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> notDependOnBusinessDomains() {
        return new ArchCondition<>("업무 도메인 패키지에 의존하지 않는다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    if (DependencyRules.businessDomainOf(dependency.getTargetClass().getPackageName()) != null) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }
}
