package com.kb.cosmetic_wms.architecture;

final class DependencyRules {

    static final String ROOT = "com.kb.cosmetic_wms";
    static final String GLOBAL = "global";

    private DependencyRules() {
    }

    /**
     * 루트 패키지 바로 아래의 업무 도메인 이름. 루트 직속 클래스나 루트 밖 클래스는 null.
     */
    static String businessDomainOf(String packageName) {
        if (!packageName.startsWith(ROOT + ".")) {
            return null;
        }
        String rest = packageName.substring(ROOT.length() + 1);
        int dot = rest.indexOf('.');
        String first = dot < 0 ? rest : rest.substring(0, dot);
        return first.equals(GLOBAL) ? null : first;
    }

    static boolean isUnderRoot(String packageName) {
        return packageName.equals(ROOT) || packageName.startsWith(ROOT + ".");
    }

    /**
     * 서로 다른 업무 도메인 사이의 의존이 허용되는 경로인지 판단한다.
     * - adapter.in.event: 상대 도메인의 domain.event
     * - adapter.out: 상대 도메인의 application.port.in (UseCase)
     */
    static boolean isAllowedCrossDomainDependency(String originPackage, String targetPackage) {
        String origin = businessDomainOf(originPackage);
        String target = businessDomainOf(targetPackage);
        if (origin == null || target == null || origin.equals(target)) {
            return true;
        }
        if (originPackage.contains(".adapter.in.event")) {
            return targetPackage.contains(".domain.event");
        }
        if (originPackage.contains(".adapter.out")) {
            return targetPackage.contains(".application.port.in");
        }
        return false;
    }
}
